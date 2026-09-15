package com.ugcforge.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class UgcProject(
    val id: String,
    val name: String,
    val assetType: AssetType,
    val description: String = "",
    val style: String = "Roblox",
    val primaryColor: String = "",
    val secondaryColor: String = "",
    val geometryStyle: String = "stylized",
    val qualityPreset: QualityPreset = QualityPreset.STANDARD,
    val referenceImages: List<String> = emptyList(),
    val rightsConfirmed: Boolean = true,
    val status: ProjectStatus = ProjectStatus.QUEUED,
    val engine: String = "none",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val exportedMeshPath: String? = null,
) {
    fun with(mutator: UgcProject.() -> UgcProject): UgcProject = mutator(this)

    fun toJson(json: Json = Json { prettyPrint = true }): String = json.encodeToString(this)

    companion object {
        val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }

        fun fromJson(raw: String): UgcProject =
            json.decodeFromString(raw)

        fun newId(): String =
            "prj_${java.util.UUID.randomUUID().toString().take(13)}"
    }
}

@Serializable
data class ProjectMeta(
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
) {
    fun toJsonJson(): String = json.encodeToString(this)
    companion object { val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true } }
}