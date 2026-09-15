package com.ugcforge.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ugcforge.ai.GenerationPhase
import com.ugcforge.ai.ProviderRegistry
import com.ugcforge.core.AssetType
import com.ugcforge.core.ProjectStatus
import com.ugcforge.core.QualityPreset
import com.ugcforge.core.UgcProject
import com.ugcforge.data.ProjectRepository
import com.ugcforge.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class UgcForgeViewModel(app: Application) : AndroidViewModel(app) {

    val projectsRepo = ProjectRepository(app)
    val settings = SettingsRepository(app)

    private val _projects = MutableStateFlow<List<UgcProject>>(emptyList())
    val projects: StateFlow<List<UgcProject>> = _projects.asStateFlow()

    private val _snackbar = MutableStateFlow<String?>(null)
    val snackbar: StateFlow<String?> = _snackbar.asStateFlow()

    fun consumeSnackbar() { _snackbar.value = null }

    init { refreshProjects() }

    fun refreshProjects() = viewModelScope.launch { _projects.value = projectsRepo.list() }

    fun saveProjectImage(projectId: String, uri: Uri, fileName: String): String? =
        runCatching {
            val dir = projectsRepo.refsDir(projectId)
            val out = File(dir, fileName)
            contentResolver().openInputStream(uri)?.use { input ->
                FileOutputStream(out).use { fos -> input.copyTo(fos) }
            }
            out.absolutePath
        }.getOrNull()

    private val stagedImages = mutableListOf<File>()
    private val stageDir: File by lazy { File(getApplication<Application>().cacheDir, "ugcforge-stage").apply { mkdirs() } }

    /** Copy a picked Uri into a staging area until the project exists. Returns staged abs path. */
    fun stageImage(uri: Uri, displayName: String): String? = runCatching {
        val ext = displayName.substringAfterLast('.', "jpg").lowercase().take(6)
        val out = File(stageDir, "${System.currentTimeMillis()}_${stagedImages.size}.$ext")
        contentResolver().openInputStream(uri)?.use { input -> FileOutputStream(out).use { fos -> input.copyTo(fos) } }
        out.absolutePath.also { stagedImages.add(File(it)) }
    }.getOrNull()

    fun clearStaged() { stagedImages.clear() }

    private fun contentResolver() = getApplication<Application>().contentResolver

    fun createProject(
        name: String,
        type: AssetType,
        description: String,
        style: String,
        primary: String,
        secondary: String,
        geometry: String,
        quality: QualityPreset,
        stagedImages: List<String>,
        rightsConfirmed: Boolean,
        onDone: (UgcProject) -> Unit,
    ) = viewModelScope.launch {
        val p = projectsRepo.create(
            name = name, assetType = type, description = description, style = style,
            primaryColor = primary, secondaryColor = secondary,
            geometryStyle = geometry, quality = quality,
            refImages = emptyList(), rightsConfirmed = rightsConfirmed,
        )
        // promote staged images into the project refs dir
        val promoted = stagedImages.mapNotNull { stagedPath ->
            val staged = File(stagedPath)
            if (staged.exists()) {
                val dest = File(projectsRepo.refsDir(p.id), staged.name)
                if (!dest.exists()) staged.copyTo(dest)
                dest.absolutePath
            } else null
        }
        if (promoted.isNotEmpty()) {
            val withRefs = p.copy(referenceImages = promoted)
            projectsRepo.save(withRefs)
            refreshProjects()
            onDone(withRefs)
        } else {
            refreshProjects()
            onDone(p)
        }
    }

    fun requestGeneration(project: UgcProject, onPhase: (GenerationPhase) -> Unit) = viewModelScope.launch {
        val result = runGeneration(project, onPhase)
        _snackbar.value = result.message
    }

    suspend fun runGeneration(project: UgcProject, onPhase: (GenerationPhase) -> Unit): com.ugcforge.ai.GenerationResult {
        val provider = ProviderRegistry.resolve(settings)
        val result = provider.generate(getApplication(), project.copy(status = ProjectStatus.PROCESSING), onPhase)
        val next = projectRepoResult(project, result.phase)
        projectsRepo.save(next)
        refreshProjects()
        return result
    }

    private fun projectRepoResult(project: UgcProject, phase: GenerationPhase): UgcProject =
        project.copy(
            status = when (phase) {
                GenerationPhase.NEEDS_BACKEND -> ProjectStatus.NEEDS_BACKEND
                GenerationPhase.FAILED -> ProjectStatus.FAILED
                GenerationPhase.AVAILABLE -> ProjectStatus.COMPLETED
                else -> ProjectStatus.PROCESSING
            },
            engine = if (phase == GenerationPhase.AVAILABLE) "local" else project.engine,
            updatedAt = System.currentTimeMillis(),
        )

    fun renameProject(id: String, name: String) = viewModelScope.launch {
        projectsRepo.get(id)?.let { projectsRepo.save(it.copy(name = name)) }
        refreshProjects()
    }

    fun duplicateProject(id: String) = viewModelScope.launch {
        projectsRepo.duplicate(id)
        refreshProjects()
    }

    fun deleteProject(id: String) = viewModelScope.launch {
        projectsRepo.delete(id)
        refreshProjects()
    }

    fun saveExportedMesh(project: UgcProject, meshFile: File) = viewModelScope.launch {
        val copy = File(projectsRepo.exportsDir(project.id), meshFile.name)
        if (!copy.exists()) meshFile.copyTo(copy)
        projectsRepo.save(project.copy(
            status = ProjectStatus.COMPLETED,
            engine = "exported",
            exportedMeshPath = copy.absolutePath,
            updatedAt = System.currentTimeMillis(),
        ))
        refreshProjects()
    }
}