package com.systemoverlay.app.core

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import java.text.DecimalFormat
import kotlin.math.ln
import kotlin.math.pow

/**
 * Extension functions for the application
 */

/**
 * Format bytes to human readable string
 */
fun Long.formatBytes(): String {
    if (this <= 0) return "0 B"
    
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (ln(this.toDouble()) / ln(1024.0)).toInt()
    
    return DecimalFormat("#,##0.#")
        .format(this / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
}

/**
 * Format bytes to MB
 */
fun Long.toMB(): Long = this / (1024 * 1024)

/**
 * Format bytes to GB with decimals
 */
fun Long.toGB(): Float = this / (1024f * 1024f * 1024f)

/**
 * Format percentage value
 */
fun Float.formatPercent(decimals: Int = 0): String {
    return if (decimals > 0) {
        String.format("%.${decimals}f%%", this)
    } else {
        "${this.toInt()}%"
    }
}

/**
 * Format frequency from KHz to readable string
 */
fun Long.formatFrequency(): String {
    return when {
        this >= 1_000_000 -> String.format("%.2f GHz", this / 1_000_000.0)
        this >= 1_000 -> String.format("%.0f MHz", this / 1_000.0)
        else -> "$this KHz"
    }
}

/**
 * Format temperature
 */
fun Float.formatTemperature(): String {
    return String.format("%.1f°C", this)
}

/**
 * Check if overlay permission is granted
 */
fun Context.canDrawOverlays(): Boolean {
    return Settings.canDrawOverlays(this)
}

/**
 * Start foreground service with compatibility
 */
fun Context.startForegroundServiceCompat(intent: Intent) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}

/**
 * Safe read of a file, returns null on failure
 */
fun java.io.File.readTextSafe(): String? {
    return try {
        if (exists() && canRead()) readText().trim() else null
    } catch (e: Exception) {
        null
    }
}

/**
 * Coerce value within range
 */
fun Float.coerceInPercent(): Float = this.coerceIn(0f, 100f)
