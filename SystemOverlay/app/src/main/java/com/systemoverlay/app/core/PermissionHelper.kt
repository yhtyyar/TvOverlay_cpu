package com.systemoverlay.app.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

/**
 * Helper object for permission-related operations
 */
object PermissionHelper {
    
    /**
     * Check if overlay permission is granted
     */
    fun canDrawOverlays(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }
    
    /**
     * Get intent to request overlay permission
     */
    fun getOverlayPermissionIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
    }
    
    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    
    /**
     * Get intent to open app notification settings
     */
    fun getNotificationSettingsIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        }
    }
    
    /**
     * Get ADB command for granting overlay permission
     */
    fun getOverlayAdbCommand(packageName: String): String {
        return "adb shell appops set $packageName SYSTEM_ALERT_WINDOW allow"
    }
    
    /**
     * Get ADB command for disabling battery optimization
     */
    fun getBatteryOptimizationAdbCommand(packageName: String): String {
        return "adb shell dumpsys deviceidle whitelist +$packageName"
    }
    
    /**
     * Check if running on Android TV
     */
    fun isAndroidTv(context: Context): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? android.app.UiModeManager
        return uiModeManager?.currentModeType == android.content.res.Configuration.UI_MODE_TYPE_TELEVISION
    }
    
    /**
     * Check if device has touchscreen
     */
    fun hasTouchscreen(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_TOUCHSCREEN)
    }
}
