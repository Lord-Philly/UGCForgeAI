package com.ugcforge.core

import kotlinx.serialization.Serializable

@Serializable
enum class QualityPreset(val label: String, val maxTriangles: Int) {
    MOBILE("Mobile", 1500),
    OPTIMIZED("Optimized", 2500),
    STANDARD("Standard", 4000),
    HIGH("High", 8000),
    PREMIUM("Premium", 12000);
}