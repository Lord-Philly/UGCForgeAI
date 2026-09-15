package com.ugcforge.ai

import android.content.Context
import com.ugcforge.core.UgcProject
import com.ugcforge.data.SettingsRepository
import kotlinx.coroutines.delay

enum class GenerationPhase(val label: String) {
    ANALYZING("Analyzing reference"),
    PLANNING("Planning prompt"),
    GENERATING("Generating mesh"),
    AVAILABLE("Mesh available"),
    NEEDS_BACKEND("Generation requires AI backend"),
    FAILED("Failed"),
}

data class GenerationResult(
    val phase: GenerationPhase,
    val message: String,
    val meshPath: String? = null,
    val meshFormat: String? = null,
)

/**
 * Abstraction over generation engines. A heavy engine (TripoSR, Hunyuan3D, TRELLIS)
 * must run on a remote backend; the app stays as an offline-capable client.
 */
interface AIProvider {
    val id: String
    suspend fun generate(context: Context, project: UgcProject, onProgress: (GenerationPhase) -> Unit): GenerationResult
}

class LocalProvider(private val settings: SettingsRepository) : AIProvider {
    override val id: String = "local"

    override suspend fun generate(context: Context, project: UgcProject, onProgress: (GenerationPhase) -> Unit): GenerationResult {
        // Honest offline-first behaviour: without a backend we do NOT fake a mesh.
        onProgress(GenerationPhase.ANALYZING)
        delay(150)
        onProgress(GenerationPhase.PLANNING)
        delay(150)
        val privacy = settings.privacyMode
        val modeNote = when (privacy) {
            com.ugcforge.data.PrivacyMode.LOCAL -> "Privacy: LOCAL — reference is processed on device."
            com.ugcforge.data.PrivacyMode.REMOTE -> "Privacy: REMOTE — reference may leave the device."
            com.ugcforge.data.PrivacyMode.ASK -> "Privacy: ASK — you will be asked on send."
        }
        onProgress(GenerationPhase.NEEDS_BACKEND)
        return GenerationResult(
            phase = GenerationPhase.NEEDS_BACKEND,
            message = "No AI backend connected. Configure a backend in Settings to generate meshes. $modeNote",
        )
    }
}

object ProviderRegistry {
    fun resolve(settings: SettingsRepository): AIProvider = LocalProvider(settings)
}