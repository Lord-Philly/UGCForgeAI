package com.ugcforge.data

import android.content.Context
import com.ugcforge.core.AssetType
import com.ugcforge.core.QualityPreset
import com.ugcforge.core.UgcProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Arquivo-backed storage for UGC projects.
 * Layout: <filesDir>/ugcforge/projects/<projectId>/{project.json, refs/, exports/}
 */
class ProjectRepository(private val context: Context) {

    private val root: File by lazy {
        File(context.filesDir, "ugcforge/projects").apply { mkdirs() }
    }

    fun projectDir(id: String): File = File(root, id).apply { mkdirs() }

    fun refsDir(id: String): File = File(projectDir(id), "refs").apply { mkdirs() }

    fun exportsDir(id: String): File = File(projectDir(id), "exports").apply { mkdirs() }

    private fun projectFile(id: String): File = File(projectDir(id), "project.json")

    suspend fun create(
        name: String,
        assetType: AssetType,
        description: String = "",
        style: String = "Roblox",
        primaryColor: String = "",
        secondaryColor: String = "",
        geometryStyle: String = "stylized",
        quality: QualityPreset = QualityPreset.STANDARD,
        refImages: List<String> = emptyList(),
        rightsConfirmed: Boolean = true,
    ): UgcProject = withContext(Dispatchers.IO) {
        val p = UgcProject(
            id = UgcProject.newId(),
            name = name,
            assetType = assetType,
            description = description,
            style = style,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            geometryStyle = geometryStyle,
            qualityPreset = quality,
            referenceImages = refImages,
            rightsConfirmed = rightsConfirmed,
        )
        projectFile(p.id).writeText(p.toJson())
        p
    }

    suspend fun list(): List<UgcProject> = withContext(Dispatchers.IO) {
        root.listFiles()?.mapNotNull { dir ->
            val f = File(dir, "project.json")
            if (f.exists()) runCatching { UgcProject.fromJson(f.readText()) }.getOrNull()
            else null
        }?.sortedByDescending { it.updatedAt } ?: emptyList()
    }

    suspend fun get(id: String): UgcProject? = withContext(Dispatchers.IO) {
        val f = projectFile(id)
        if (f.exists()) runCatching { UgcProject.fromJson(f.readText()) }.getOrNull() else null
    }

    suspend fun save(project: UgcProject): UgcProject = withContext(Dispatchers.IO) {
        val updated = project.copy(updatedAt = System.currentTimeMillis())
        projectFile(updated.id).writeText(updated.toJson())
        updated
    }

    suspend fun delete(id: String): Boolean = withContext(Dispatchers.IO) {
        projectDir(id).deleteRecursively()
    }

    suspend fun duplicate(id: String): UgcProject? = withContext(Dispatchers.IO) {
        val src = get(id) ?: return@withContext null
        val copy = src.copy(
            id = UgcProject.newId(),
            name = "${src.name} (copy)",
            status = com.ugcforge.core.ProjectStatus.QUEUED,
            exportedMeshPath = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        // copy reference images
        val srcRefs = refsDir(src.id)
        val dstRefs = refsDir(copy.id)
        srcRefs.listFiles()?.filter { it.isFile }?.forEach { old ->
            val new = File(dstRefs, old.name)
            old.copyTo(new)
        }
        projectFile(copy.id).writeText(copy.toJson())
        copy
    }
}