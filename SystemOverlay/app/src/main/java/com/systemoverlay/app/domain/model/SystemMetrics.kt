package com.systemoverlay.app.domain.model

/**
 * Represents CPU metrics including overall usage and per-core usage
 */
data class CpuMetrics(
    val overallUsage: Float = 0f,
    val coreUsages: List<Float> = emptyList(),
    val frequency: Long = 0L,
    val temperature: Float? = null
)

/**
 * Represents GPU metrics
 */
data class GpuMetrics(
    val usage: Float = 0f,
    val memoryUsed: Long = 0L,
    val memoryTotal: Long = 0L,
    val temperature: Float? = null,
    val isAvailable: Boolean = false
)

/**
 * Represents RAM/Memory metrics
 */
data class RamMetrics(
    val usedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val availableBytes: Long = 0L
) {
    val usagePercent: Float
        get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes) * 100f else 0f
    
    val usedMB: Long
        get() = usedBytes / (1024 * 1024)
    
    val totalMB: Long
        get() = totalBytes / (1024 * 1024)
    
    val availableMB: Long
        get() = availableBytes / (1024 * 1024)
}

/**
 * Combined system metrics
 */
data class SystemMetrics(
    val cpu: CpuMetrics = CpuMetrics(),
    val gpu: GpuMetrics = GpuMetrics(),
    val ram: RamMetrics = RamMetrics(),
    val topProcesses: TopProcesses = TopProcesses.empty(),
    val timestamp: Long = System.currentTimeMillis()
)
