package com.systemoverlay.app.domain.model

/**
 * Position of the overlay on screen
 */
enum class OverlayPosition {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}

/**
 * Settings for the overlay display
 */
data class OverlaySettings(
    val isEnabled: Boolean = false,
    val position: OverlayPosition = OverlayPosition.TOP_RIGHT,
    val opacity: Float = 0.8f,
    val showClock: Boolean = true,
    val showCpu: Boolean = true,
    val showGpu: Boolean = true,
    val showRam: Boolean = true,
    val updateIntervalMs: Long = 1000L,
    val startOnBoot: Boolean = false,
    val textSize: Float = 14f,
    val backgroundColor: Long = 0x80000000,
    val textColor: Long = 0xFFFFFFFF,
    val cpuColor: Long = 0xFF4CAF50,
    val gpuColor: Long = 0xFFFF9800,
    val ramColor: Long = 0xFF9C27B0
)
