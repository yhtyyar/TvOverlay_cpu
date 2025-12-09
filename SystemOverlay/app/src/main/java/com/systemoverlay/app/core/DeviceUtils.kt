package com.systemoverlay.app.core

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.BatteryManager
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for device-specific optimizations
 */
@Singleton
class DeviceUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val logger = Logger.withTag("DeviceUtils")
    
    /**
     * Check if the device is an Android TV
     */
    fun isTvDevice(): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        return uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }
    
    /**
     * Check if the device is running on battery
     */
    fun isRunningOnBattery(): Boolean {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return batteryManager?.isCharging != true
    }
    
    /**
     * Get battery level (0-100)
     */
    fun getBatteryLevel(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
    }
    
    /**
     * Get optimal update interval based on device type and state
     */
    fun getOptimalUpdateInterval(): Long {
        return when {
            isTvDevice() -> {
                // TV devices - prioritize power efficiency
                when {
                    isLowPowerMode() -> Constants.TV_SLOW_UPDATE_INTERVAL_MS
                    else -> Constants.DEFAULT_UPDATE_INTERVAL_MS
                }
            }
            isRunningOnBattery() && getBatteryLevel() < Constants.LOW_BATTERY_THRESHOLD -> {
                // Mobile device with low battery
                Constants.TV_SLOW_UPDATE_INTERVAL_MS
            }
            else -> {
                // Mobile device with good battery
                Constants.MOBILE_UPDATE_INTERVAL_MS
            }
        }
    }
    
    /**
     * Check if device is in low power mode
     */
    private fun isLowPowerMode(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) == BatteryManager.BATTERY_STATUS_UNKNOWN
        } else {
            false
        }
    }
    
    /**
     * Get appropriate overlay margin for device type
     */
    fun getOverlayMargin(): Int {
        return if (isTvDevice()) {
            Constants.TV_OVERLAY_MARGIN_DP
        } else {
            Constants.OVERLAY_MARGIN_DP
        }
    }
    
    /**
     * Check if adaptive performance should be enabled
     */
    fun shouldUseAdaptivePerformance(): Boolean {
        return isTvDevice() || (isRunningOnBattery() && getBatteryLevel() < 50)
    }
    
    /**
     * Get thermal throttling info
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getThermalStatus(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
                powerManager?.currentThermalStatus ?: 0
            } catch (e: Exception) {
                logger.v("Cannot read thermal status: ${e.message}")
                0
            }
        } else {
            0
        }
    }
    
    /**
     * Check if device should use power saving mode
     */
    fun shouldUsePowerSavingMode(): Boolean {
        return when {
            isTvDevice() -> true  // Always use power saving on TV
            !isRunningOnBattery() -> false  // Never on AC power
            getBatteryLevel() < Constants.LOW_BATTERY_THRESHOLD -> true
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && getThermalStatus() > 2 -> true
            else -> false
        }
    }
}
