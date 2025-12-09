package com.systemoverlay.app.data.source

import com.systemoverlay.app.core.Constants
import com.systemoverlay.app.core.DeviceUtils
import com.systemoverlay.app.core.GpuPaths
import com.systemoverlay.app.core.Logger
import com.systemoverlay.app.core.coerceInPercent
import com.systemoverlay.app.core.readTextSafe
import com.systemoverlay.app.domain.model.GpuMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for collecting GPU metrics.
 * Supports multiple GPU vendors: Qualcomm Adreno, ARM Mali, Imagination PowerVR.
 * Thread-safe implementation.
 */
@Singleton
class GpuDataSource @Inject constructor(
    private val deviceUtils: DeviceUtils
) {
    
    private val logger = Logger.withTag("GpuDataSource")
    private val mutex = Mutex()
    
    @Volatile
    private var cachedGpuPath: String? = null
    
    @Volatile
    private var availabilityChecked = false
    
    @Volatile
    private var isAvailable = false
    
    @Volatile
    private var gpuVendor: GpuVendor = GpuVendor.UNKNOWN
    
    // Performance caching
    @Volatile
    private var lastAvailabilityCheck: Long = 0L
    
    @Volatile
    private var cachedMetrics: GpuMetrics = GpuMetrics(isAvailable = false)
    
    @Volatile
    private var lastMetricsCheck: Long = 0L
    
    /**
     * Supported GPU vendors
     */
    enum class GpuVendor {
        QUALCOMM_ADRENO,
        ARM_MALI,
        IMAGINATION_POWERVR,
        GENERIC,
        UNKNOWN
    }
    
    /**
     * Check if GPU monitoring is available on this device with caching
     */
    fun isGpuMonitoringAvailable(): Boolean {
        val currentTime = System.currentTimeMillis()
        
        // Use cached result if available and not expired
        if (availabilityChecked && 
            (currentTime - lastAvailabilityCheck) < Constants.GPU_AVAILABILITY_CHECK_CACHE_MS) {
            return isAvailable
        }
        
        val path = findGpuLoadPath()
        isAvailable = path != null
        availabilityChecked = true
        lastAvailabilityCheck = currentTime
        
        if (isAvailable) {
            logger.i("GPU monitoring available: vendor=$gpuVendor, path=$cachedGpuPath")
        } else {
            logger.i("GPU monitoring not available on this device")
        }
        
        return isAvailable
    }
    
    /**
     * Get current GPU metrics with adaptive caching for performance
     */
    suspend fun getGpuMetrics(): GpuMetrics = withContext(Dispatchers.IO) {
        if (!isGpuMonitoringAvailable()) {
            return@withContext GpuMetrics(isAvailable = false)
        }
        
        val currentTime = System.currentTimeMillis()
        
        // For power saving mode or TV devices, use cached results more aggressively
        val cacheThreshold = if (deviceUtils.shouldUsePowerSavingMode() || deviceUtils.isTvDevice()) {
            5000L // 5 seconds cache for power saving
        } else {
            2000L // 2 seconds cache for normal operation
        }
        
        if (currentTime - lastMetricsCheck < cacheThreshold) {
            return@withContext cachedMetrics
        }
        
        mutex.withLock {
            try {
                val usage = getGpuUsage()
                val (memUsed, memTotal) = getGpuMemory()
                val temperature = getGpuTemperature()
                
                val metrics = GpuMetrics(
                    usage = usage.coerceInPercent(),
                    memoryUsed = memUsed,
                    memoryTotal = memTotal,
                    temperature = temperature,
                    isAvailable = true
                )
                
                // Cache the result
                cachedMetrics = metrics
                lastMetricsCheck = currentTime
                
                metrics
            } catch (e: Exception) {
                logger.e("Error reading GPU metrics", e)
                // Return cached result on error if available
                if (cachedMetrics.isAvailable) cachedMetrics else GpuMetrics(isAvailable = true)
            }
        }
    }
    
    /**
     * Get detected GPU vendor
     */
    fun getGpuVendor(): GpuVendor {
        if (!availabilityChecked) {
            isGpuMonitoringAvailable()
        }
        return gpuVendor
    }
    
    private fun getGpuUsage(): Float {
        val loadPath = cachedGpuPath ?: findGpuLoadPath() ?: return 0f
        
        return try {
            val value = File(loadPath).readTextSafe() ?: return 0f
            parseGpuUsage(value)
        } catch (e: Exception) {
            logger.v("Cannot read GPU usage: ${e.message}")
            0f
        }
    }
    
    private fun parseGpuUsage(value: String): Float {
        // Handle different formats:
        // "75%" - percentage with symbol
        // "75" - plain number
        // "busy: 75" - labeled value
        // "75 25" - busy idle pair (Adreno gpubusy)
        
        val cleanValue = value.trim()
        
        // Check for "busy idle" format (Adreno)
        if (cleanValue.contains(" ") && !cleanValue.contains(":")) {
            val parts = cleanValue.split(Regex("\\s+"))
            if (parts.size >= 2) {
                val busy = parts[0].toLongOrNull() ?: 0L
                val idle = parts[1].toLongOrNull() ?: 0L
                val total = busy + idle
                return if (total > 0) (busy.toFloat() / total) * 100f else 0f
            }
        }
        
        // Extract numeric value
        val numericValue = cleanValue
            .replace("%", "")
            .replace(Regex("[^0-9.]"), "")
            .toFloatOrNull() ?: 0f
            
        return numericValue
    }
    
    private fun findGpuLoadPath(): String? {
        if (cachedGpuPath != null) return cachedGpuPath
        
        // Try Qualcomm Adreno first (most common)
        for (path in GpuPaths.ADRENO_PATHS) {
            if (isPathReadable(path)) {
                cachedGpuPath = path
                gpuVendor = GpuVendor.QUALCOMM_ADRENO
                return path
            }
        }
        
        // Try ARM Mali
        for (path in GpuPaths.MALI_PATHS) {
            if (isPathReadable(path)) {
                cachedGpuPath = path
                gpuVendor = GpuVendor.ARM_MALI
                return path
            }
        }
        
        // Try generic paths
        for (path in GpuPaths.GENERIC_PATHS) {
            if (isPathReadable(path)) {
                cachedGpuPath = path
                gpuVendor = GpuVendor.GENERIC
                return path
            }
        }
        
        gpuVendor = GpuVendor.UNKNOWN
        return null
    }
    
    private fun isPathReadable(path: String): Boolean {
        return try {
            val file = File(path)
            file.exists() && file.canRead()
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getGpuMemory(): Pair<Long, Long> {
        // GPU memory info is rarely available on Android
        // Most devices don't expose this information
        val memPaths = listOf(
            "/sys/class/kgsl/kgsl-3d0/gpu_memory_usage",
            "/sys/kernel/gpu/gpu_memory"
        )
        
        for (path in memPaths) {
            try {
                val content = File(path).readTextSafe() ?: continue
                val values = content.split(Regex("\\s+"))
                if (values.size >= 2) {
                    val used = values[0].toLongOrNull() ?: 0L
                    val total = values[1].toLongOrNull() ?: 0L
                    return Pair(used, total)
                }
            } catch (e: Exception) {
                continue
            }
        }
        return Pair(0L, 0L)
    }
    
    private fun getGpuTemperature(): Float? {
        for (path in GpuPaths.THERMAL_PATHS) {
            try {
                val temp = File(path).readTextSafe()?.toFloatOrNull()
                if (temp != null) {
                    // Temperature is usually in millidegrees Celsius
                    return if (temp > 1000) temp / 1000f else temp
                }
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }
}
