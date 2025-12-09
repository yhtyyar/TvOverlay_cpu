package com.systemoverlay.app.domain.usecase

import com.systemoverlay.app.domain.model.OverlayPosition
import com.systemoverlay.app.domain.model.OverlaySettings
import com.systemoverlay.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for managing overlay settings
 */
class ManageOverlaySettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    /**
     * Get current settings
     */
    suspend fun getSettings(): OverlaySettings {
        return repository.getSettings()
    }
    
    /**
     * Observe settings changes
     */
    fun observeSettings(): Flow<OverlaySettings> {
        return repository.observeSettings()
    }
    
    /**
     * Toggle overlay on/off
     */
    suspend fun toggleOverlay(enabled: Boolean) {
        repository.setEnabled(enabled)
    }
    
    /**
     * Update overlay position
     */
    suspend fun setPosition(position: OverlayPosition) {
        repository.setPosition(position)
    }
    
    /**
     * Update overlay opacity
     */
    suspend fun setOpacity(opacity: Float) {
        repository.setOpacity(opacity.coerceIn(0f, 1f))
    }
    
    /**
     * Toggle clock visibility
     */
    suspend fun setShowClock(show: Boolean) {
        repository.setShowClock(show)
    }
    
    /**
     * Toggle CPU visibility
     */
    suspend fun setShowCpu(show: Boolean) {
        repository.setShowCpu(show)
    }
    
    /**
     * Toggle GPU visibility
     */
    suspend fun setShowGpu(show: Boolean) {
        repository.setShowGpu(show)
    }
    
    /**
     * Toggle RAM visibility
     */
    suspend fun setShowRam(show: Boolean) {
        repository.setShowRam(show)
    }
    
    /**
     * Set update interval
     */
    suspend fun setUpdateInterval(intervalMs: Long) {
        repository.setUpdateInterval(intervalMs.coerceIn(500L, 5000L))
    }
    
    /**
     * Toggle start on boot
     */
    suspend fun setStartOnBoot(enabled: Boolean) {
        repository.setStartOnBoot(enabled)
    }
}
