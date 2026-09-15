package com.ugcforge.core

import kotlinx.serialization.Serializable

@Serializable
enum class ProjectStatus(val label: String) {
    QUEUED("Queued"),
    PROCESSING("Processing"),
    OPTIMIZING("Optimizing"),
    VALIDATING("Validating"),
    COMPLETED("Completed"),
    NEEDS_BACKEND("Needs backend"),
    FAILED("Failed"),
}