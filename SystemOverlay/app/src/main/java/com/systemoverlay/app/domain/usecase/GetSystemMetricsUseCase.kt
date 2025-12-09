package com.systemoverlay.app.domain.usecase

import com.systemoverlay.app.domain.model.SystemMetrics
import com.systemoverlay.app.domain.repository.SystemMetricsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting system metrics
 */
class GetSystemMetricsUseCase @Inject constructor(
    private val repository: SystemMetricsRepository
) {
    /**
     * Get current system metrics once
     */
    suspend operator fun invoke(): SystemMetrics {
        return repository.getSystemMetrics()
    }
    
    /**
     * Observe system metrics as a flow
     */
    fun observe(intervalMs: Long): Flow<SystemMetrics> {
        return repository.observeSystemMetrics(intervalMs)
    }
    
    /**
     * Check if GPU monitoring is available
     */
    fun isGpuAvailable(): Boolean {
        return repository.isGpuMonitoringAvailable()
    }
}
