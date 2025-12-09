package com.systemoverlay.app.core

/**
 * Application-wide constants
 */
object Constants {
    
    // Service
    const val NOTIFICATION_CHANNEL_ID = "system_overlay_channel"
    const val NOTIFICATION_ID = 1001
    const val SERVICE_STOP_ACTION = "com.systemoverlay.app.STOP_SERVICE"
    
    // Metrics collection - FAST real-time updates
    const val DEFAULT_UPDATE_INTERVAL_MS = 800L    // 0.8 second for real-time feel
    const val TV_FAST_UPDATE_INTERVAL_MS = 800L    // Fast mode for TV
    const val TV_SLOW_UPDATE_INTERVAL_MS = 2000L   // Power saving mode
    const val MOBILE_UPDATE_INTERVAL_MS = 600L     // Mobile devices - very responsive
    const val MIN_UPDATE_INTERVAL_MS = 500L        // Minimum 0.5 second
    const val MAX_UPDATE_INTERVAL_MS = 10000L      // Maximum 10 seconds
    
    // RAM-specific for fast updates
    const val RAM_UPDATE_INTERVAL_MS = 800L        // RAM updates every 0.8 sec
    
    // Process monitoring - real-time
    const val PROCESS_UPDATE_INTERVAL_MS = 1000L   // Process CPU/RAM every second
    
    // Overlay - TV optimized with transparency
    const val DEFAULT_OPACITY = 0.65f             // More transparent by default
    const val MIN_OPACITY = 0.2f
    const val MAX_OPACITY = 1.0f
    const val OVERLAY_MARGIN_DP = 24              // Larger margin for TV safe area
    const val TV_OVERLAY_MARGIN_DP = 32           // TV safe zone margin
    
    // Performance thresholds
    const val HIGH_CPU_USAGE_THRESHOLD = 80f      // Switch to slower updates at high CPU
    const val LOW_BATTERY_THRESHOLD = 20           // Battery level for power saving
    const val MEMORY_PRESSURE_THRESHOLD_MB = 100  // Available memory threshold
    
    // Caching
    const val STATIC_INFO_CACHE_DURATION_MS = 30000L  // 30 seconds cache for static data
    const val GPU_AVAILABILITY_CHECK_CACHE_MS = 60000L // 1 minute cache for GPU availability
    
    // DataStore
    const val DATASTORE_NAME = "overlay_settings"
    
    // Permissions
    const val REQUEST_OVERLAY_PERMISSION = 1001
}

/**
 * CPU related paths
 */
object CpuPaths {
    const val PROC_STAT = "/proc/stat"
    const val CPU_FREQ_BASE = "/sys/devices/system/cpu/cpu"
    const val SCALING_CUR_FREQ = "/cpufreq/scaling_cur_freq"
    const val SCALING_MAX_FREQ = "/cpufreq/scaling_max_freq"
    
    val THERMAL_PATHS = listOf(
        "/sys/class/thermal/thermal_zone0/temp",
        "/sys/devices/virtual/thermal/thermal_zone0/temp",
        "/sys/class/hwmon/hwmon0/temp1_input",
        "/sys/devices/platform/coretemp.0/hwmon/hwmon0/temp1_input"
    )
}

/**
 * GPU related paths
 */
object GpuPaths {
    // Qualcomm Adreno
    val ADRENO_PATHS = listOf(
        "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage",
        "/sys/class/kgsl/kgsl-3d0/gpubusy",
        "/sys/kernel/gpu/gpu_busy"
    )
    
    // ARM Mali
    val MALI_PATHS = listOf(
        "/sys/class/misc/mali0/device/utilization",
        "/sys/devices/platform/mali.0/utilization",
        "/sys/module/mali/parameters/mali_gpu_utilization",
        "/sys/devices/platform/ffaf0000.gpu/utilization",
        "/sys/devices/platform/13000000.mali/utilization"
    )
    
    // Generic
    val GENERIC_PATHS = listOf(
        "/sys/kernel/gpu/gpu_busy",
        "/sys/devices/platform/gpu/utilization"
    )
    
    val THERMAL_PATHS = listOf(
        "/sys/class/thermal/thermal_zone1/temp",
        "/sys/class/thermal/thermal_zone2/temp",
        "/sys/class/kgsl/kgsl-3d0/temp"
    )
}

/**
 * Memory related paths
 */
object MemoryPaths {
    const val PROC_MEMINFO = "/proc/meminfo"
}
