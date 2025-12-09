package com.systemoverlay.app.domain.repository

import com.systemoverlay.app.domain.model.OverlayPosition
import com.systemoverlay.app.domain.model.OverlaySettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing overlay settings
 */
interface SettingsRepository {
    
    /**
     * Get current overlay settings
     */
    suspend fun getSettings(): OverlaySettings
    
    /**
     * Observe settings changes
     */
    fun observeSettings(): Flow<OverlaySettings>
    
    /**
     * Update overlay enabled state
     */
    suspend fun setEnabled(enabled: Boolean)
    
    /**
     * Update overlay position
     */
    suspend fun setPosition(position: OverlayPosition)
    
    /**
     * Update overlay opacity
     */
    suspend fun setOpacity(opacity: Float)
    
    /**
     * Update clock visibility
     */
    suspend fun setShowClock(show: Boolean)
    
    /**
     * Update CPU visibility
     */
    suspend fun setShowCpu(show: Boolean)
    
    /**
     * Update GPU visibility
     */
    suspend fun setShowGpu(show: Boolean)
    
    /**
     * Update RAM visibility
     */
    suspend fun setShowRam(show: Boolean)
    
    /**
     * Update update interval
     */
    suspend fun setUpdateInterval(intervalMs: Long)
    
    /**
     * Update start on boot setting
     */
    suspend fun setStartOnBoot(enabled: Boolean)
    
    /**
     * Update all settings at once
     */
    suspend fun updateSettings(settings: OverlaySettings)
}
