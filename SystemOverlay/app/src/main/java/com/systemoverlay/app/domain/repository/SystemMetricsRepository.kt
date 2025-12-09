package com.systemoverlay.app.domain.repository

import com.systemoverlay.app.domain.model.CpuMetrics
import com.systemoverlay.app.domain.model.GpuMetrics
import com.systemoverlay.app.domain.model.RamMetrics
import com.systemoverlay.app.domain.model.SystemMetrics
import com.systemoverlay.app.domain.model.TopProcesses
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for collecting system metrics
 */
interface SystemMetricsRepository {
    
    /**
     * Get current CPU metrics
     */
    suspend fun getCpuMetrics(): CpuMetrics
    
    /**
     * Get current GPU metrics
     */
    suspend fun getGpuMetrics(): GpuMetrics
    
    /**
     * Get current RAM metrics
     */
    suspend fun getRamMetrics(): RamMetrics
    
    /**
     * Get all system metrics
     */
    suspend fun getSystemMetrics(): SystemMetrics
    
    /**
     * Observe system metrics as a flow with specified interval
     */
    fun observeSystemMetrics(intervalMs: Long): Flow<SystemMetrics>
    
    /**
     * Check if GPU monitoring is available on this device
     */
    fun isGpuMonitoringAvailable(): Boolean
    
    /**
     * Get top N processes by memory usage
     */
    suspend fun getTopProcesses(): TopProcesses
}
