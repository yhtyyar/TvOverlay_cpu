package com.systemoverlay.app.domain.model

/**
 * Process information model
 * Represents a running app/process with its resource usage
 */
data class ProcessInfo(
    val packageName: String,
    val appName: String,
    val memoryUsageMB: Long,
    val cpuUsagePercent: Float = 0f,
    val pid: Int = 0
) {
    companion object {
        fun empty() = ProcessInfo(
            packageName = "",
            appName = "Unknown",
            memoryUsageMB = 0,
            cpuUsagePercent = 0f,
            pid = 0
        )
    }
}

/**
 * Top processes information
 */
data class TopProcesses(
    val processes: List<ProcessInfo> = emptyList(),
    val ownAppProcess: ProcessInfo? = null, // Our app for benchmark
    val totalProcesses: Int = 0
) {
    companion object {
        fun empty() = TopProcesses(emptyList(), null, 0)
    }
}
