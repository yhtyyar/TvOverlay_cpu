package com.systemoverlay.app.data.repository

import com.systemoverlay.app.data.source.CpuDataSource
import com.systemoverlay.app.data.source.GpuDataSource
import com.systemoverlay.app.data.source.ProcessMonitorDataSource
import com.systemoverlay.app.data.source.RamDataSource
import com.systemoverlay.app.domain.model.CpuMetrics
import com.systemoverlay.app.domain.model.GpuMetrics
import com.systemoverlay.app.domain.model.RamMetrics
import com.systemoverlay.app.domain.model.SystemMetrics
import com.systemoverlay.app.domain.model.TopProcesses
import com.systemoverlay.app.domain.repository.SystemMetricsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SystemMetricsRepository
 */
@Singleton
class SystemMetricsRepositoryImpl @Inject constructor(
    private val cpuDataSource: CpuDataSource,
    private val gpuDataSource: GpuDataSource,
    private val ramDataSource: RamDataSource,
    private val processMonitorDataSource: ProcessMonitorDataSource
) : SystemMetricsRepository {
    
    override suspend fun getCpuMetrics(): CpuMetrics {
        return cpuDataSource.getCpuMetrics()
    }
    
    override suspend fun getGpuMetrics(): GpuMetrics {
        return gpuDataSource.getGpuMetrics()
    }
    
    override suspend fun getRamMetrics(): RamMetrics {
        return ramDataSource.getRamMetrics()
    }
    
    override suspend fun getSystemMetrics(): SystemMetrics {
        return SystemMetrics(
            cpu = getCpuMetrics(),
            gpu = getGpuMetrics(),
            ram = getRamMetrics(),
            topProcesses = getTopProcesses(),
            timestamp = System.currentTimeMillis()
        )
    }
    
    override suspend fun getTopProcesses(): TopProcesses {
        return processMonitorDataSource.getTopProcessesByMemory(topN = 5)
    }
    
    override fun observeSystemMetrics(intervalMs: Long): Flow<SystemMetrics> = flow {
        while (true) {
            emit(getSystemMetrics())
            delay(intervalMs)
        }
    }
    
    override fun isGpuMonitoringAvailable(): Boolean {
        return gpuDataSource.isGpuMonitoringAvailable()
    }
}
