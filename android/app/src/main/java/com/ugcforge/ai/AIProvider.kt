package com.ugcforge.ai

import android.content.Context
import com.ugcforge.core.UgcProject
import com.ugcforge.data.SettingsRepository
import com.ugcforge.exporter.ObjExporter
import com.ugcforge.mesh.MeshData
import kotlinx.coroutines.delay
import java.io.File

enum class GenerationPhase(val label: String) {
    ANALYZING("Analisando"),
    PLANNING("Planejando"),
    GENERATING("Gerando"),
    AVAILABLE("Disponível"),
    NEEDS_BACKEND("Requer backend"),
    FAILED("Falhou"),
}

data class GenerationResult(
    val phase: GenerationPhase,
    val message: String,
    val meshPath: String? = null,
    val meshFormat: String? = null,
)

interface AIProvider {
    val id: String
    suspend fun generate(context: Context, project: UgcProject, onProgress: (GenerationPhase) -> Unit): GenerationResult
}

class LocalProvider(private val settings: SettingsRepository) : AIProvider {
    override val id: String = "local"

    override suspend fun generate(context: Context, project: UgcProject, onProgress: (GenerationPhase) -> Unit): GenerationResult {
        onProgress(GenerationPhase.ANALYZING)
        delay(250)
        onProgress(GenerationPhase.PLANNING)
        delay(250)
        onProgress(GenerationPhase.GENERATING)
        delay(400)
        val mesh: MeshData = LocalMeshGenerator.generate(
            assetTypeKey = project.assetType.key,
        geometryStyle = project.geometryStyle,
        qualitySegments = 16,
        seed = project.id.hashCode(),
        )
        val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
        val result = ObjExporter().export(project, mesh, exportDir)
        return GenerationResult(
            phase = GenerationPhase.AVAILABLE,
            message = "Gerado na hora no dispositivo (procedural local — sem IA externa).",
            meshPath = result.file.absolutePath,
            meshFormat = result.format,
        )
    }
}

object ProviderRegistry {
    fun resolve(settings: SettingsRepository): AIProvider = LocalProvider(settings)
}
