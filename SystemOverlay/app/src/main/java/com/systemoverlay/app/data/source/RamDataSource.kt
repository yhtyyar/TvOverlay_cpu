package com.systemoverlay.app.data.source

import android.app.ActivityManager
import android.content.Context
import com.systemoverlay.app.core.Constants
import com.systemoverlay.app.core.DeviceUtils
import com.systemoverlay.app.core.Logger
import com.systemoverlay.app.core.MemoryPaths
import com.systemoverlay.app.domain.model.RamMetrics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for collecting RAM/Memory metrics.
 * Uses /proc/meminfo for detailed information with ActivityManager fallback.
 */
@Singleton
class RamDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceUtils: DeviceUtils
) {
    
    private val logger = Logger.withTag("RamDataSource")
    private val mutex = Mutex()
    
    private val activityManager: ActivityManager by lazy {
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }
    
    // Cache for total memory (doesn't change)
    @Volatile
    private var cachedTotalMemory: Long = 0L
    
    // Performance caching for RAM metrics
    @Volatile
    private var cachedMetrics: RamMetrics = RamMetrics()
    
    @Volatile
    private var lastMetricsCheck: Long = 0L
    
    /**
     * Get current RAM metrics with adaptive caching
     */
    suspend fun getRamMetrics(): RamMetrics = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        
        // Fast RAM updates for responsive UI (0.5-1 second)
        val cacheThreshold = when {
            deviceUtils.shouldUsePowerSavingMode() -> 2000L  // 2 seconds for power saving
            deviceUtils.isTvDevice() -> 1000L                // 1 second for TV
            else -> 500L                                     // 0.5 second for mobile (fast)
        }
        
        // Use cached result if not expired
        if (currentTime - lastMetricsCheck < cacheThreshold) {
            return@withContext cachedMetrics
        }
        
        mutex.withLock {
            try {
                // For Android TV, prefer ActivityManager (more reliable and always works)
                val result = if (deviceUtils.isTvDevice()) {
                    logger.v("Using ActivityManager for Android TV (reliable)")
                    getMemInfoFromActivityManager()
                } else {
                    // For mobile, try /proc/meminfo first
                    val memInfo = getMemInfoFromProc()
                    if (memInfo != null) {
                        memInfo
                    } else {
                        logger.v("Using ActivityManager fallback for memory info")
                        getMemInfoFromActivityManager()
                    }
                }
                
                // Cache the result
                cachedMetrics = result
                lastMetricsCheck = currentTime
                
                result
            } catch (e: Exception) {
                logger.e("Error reading RAM metrics", e)
                // Return cached result on error if available, otherwise default
                if (cachedMetrics.totalBytes > 0) cachedMetrics else RamMetrics()
            }
        }
    }
    
    private fun getMemInfoFromProc(): RamMetrics? {
        return try {
            val file = File(MemoryPaths.PROC_MEMINFO)
            if (!file.exists() || !file.canRead()) return null
            
            val lines = file.readLines()
            var memTotal = 0L
            var memFree = 0L
            var memAvailable = 0L
            var buffers = 0L
            var cached = 0L
            var sReclaimable = 0L
            var shmem = 0L
            
            for (line in lines) {
                when {
                    line.startsWith("MemTotal:") -> memTotal = parseMemValue(line)
                    line.startsWith("MemFree:") -> memFree = parseMemValue(line)
                    line.startsWith("MemAvailable:") -> memAvailable = parseMemValue(line)
                    line.startsWith("Buffers:") -> buffers = parseMemValue(line)
                    line.startsWith("Cached:") -> cached = parseMemValue(line)
                    line.startsWith("SReclaimable:") -> sReclaimable = parseMemValue(line)
                    line.startsWith("Shmem:") -> shmem = parseMemValue(line)
                }
            }
            
            // Cache total memory
            if (cachedTotalMemory == 0L && memTotal > 0) {
                cachedTotalMemory = memTotal * 1024
                logger.i("Total RAM: ${cachedTotalMemory / (1024 * 1024)} MB")
            }
            
            // If MemAvailable is not present (older kernels < 3.14), calculate it
            // Using a more accurate formula
            if (memAvailable == 0L) {
                memAvailable = memFree + buffers + cached + sReclaimable - shmem
            }
            
            // Convert from KB to bytes first
            val totalBytes = memTotal * 1024L
            val availableBytes = memAvailable * 1024L
            
            // Calculate used memory with safety checks
            val usedBytes = (totalBytes - availableBytes).coerceAtLeast(0L)
            
            // Validate values before returning
            if (totalBytes <= 0 || usedBytes < 0 || availableBytes < 0) {
                logger.w("Invalid memory values: total=$totalBytes, used=$usedBytes, available=$availableBytes")
                return null
            }
            
            logger.v("RAM: used=${usedBytes/(1024*1024)}MB, total=${totalBytes/(1024*1024)}MB, available=${availableBytes/(1024*1024)}MB")
            
            RamMetrics(
                usedBytes = usedBytes,
                totalBytes = totalBytes,
                availableBytes = availableBytes
            )
        } catch (e: Exception) {
            logger.v("Cannot read /proc/meminfo: ${e.message}")
            null
        }
    }
    
    private fun parseMemValue(line: String): Long {
        return try {
            val parts = line.split(Regex("\\s+"))
            if (parts.size >= 2) {
                parts[1].toLongOrNull() ?: 0L
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun getMemInfoFromActivityManager(): RamMetrics {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        val usedBytes = (memInfo.totalMem - memInfo.availMem).coerceAtLeast(0L)
        
        logger.v("ActivityManager RAM: used=${usedBytes/(1024*1024)}MB, total=${memInfo.totalMem/(1024*1024)}MB")
        
        return RamMetrics(
            usedBytes = usedBytes,
            totalBytes = memInfo.totalMem,
            availableBytes = memInfo.availMem
        )
    }
}
