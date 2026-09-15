package com.ugcforge.core

import kotlinx.serialization.Serializable

@Serializable
enum class AssetType(
    val label: String,
    val monogram: String,
    val color: Long,
    val robloxProfile: String,
) {
    HAIR("Hair", "H", 0xFF7C4DFF, "Layered/Classic hair"),
    ACCESSORY("Accessory", "A", 0xFF00BFA5, "Rigid accessory"),
    CLOTHING("Clothing", "C", 0xFF2979FF, "Layered clothing"),
    FACE("Face", "F", 0xFFFF7043, "Face accessory"),
    BODY("Body", "B", 0xFFFFC400, "Body / bundle"),
    OTHER("Other", "O", 0xFF90A4AE, "Custom"),
}