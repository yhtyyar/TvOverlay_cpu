package com.systemoverlay.app.presentation.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemoverlay.app.domain.model.ProcessInfo
import com.systemoverlay.app.domain.model.TopProcesses

/**
 * Top Processes display component for overlay
 * Shows top 5 apps by RAM usage
 */
@Composable
fun TopProcessesOverlay(
    topProcesses: TopProcesses,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    if (topProcesses.processes.isEmpty() && topProcesses.ownAppProcess == null) return
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .animateContentSize(),
        horizontalAlignment = Alignment.Start
    ) {
        // Header - compact
        Text(
            text = "TOP 3 APPS",
            color = textColor.copy(alpha = 0.7f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Process list - show only top 3 (excluding our app)
        topProcesses.processes.take(3).forEachIndexed { index, process ->
            ProcessItemCompact(
                process = process,
                index = index + 1,
                textColor = textColor
            )
            if (index < topProcesses.processes.size - 1 && index < 2) {
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
        
        // Our app separately for benchmark
        topProcesses.ownAppProcess?.let { ownApp ->
            Spacer(modifier = Modifier.height(4.dp))
            
            // Separator line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(textColor.copy(alpha = 0.2f))
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Our app with label
            Text(
                text = "BENCHMARK",
                color = textColor.copy(alpha = 0.5f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            
            ProcessItemCompact(
                process = ownApp,
                index = null, // No index for our app
                textColor = textColor.copy(alpha = 0.8f),
                isBenchmark = true
            )
        }
    }
}

/**
 * Compact process item for overlay display
 */
@Composable
private fun ProcessItemCompact(
    process: ProcessInfo,
    index: Int?,
    textColor: Color,
    modifier: Modifier = Modifier,
    isBenchmark: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // App name with index
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (index != null) {
                Text(
                    text = "$index.",
                    color = textColor.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            
            Text(
                text = process.appName,
                color = if (isBenchmark) textColor.copy(alpha = 0.9f) else textColor,
                fontSize = 9.sp,
                fontWeight = if (isBenchmark) FontWeight.Bold else FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // CPU and RAM on second line - more compact
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 1.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CPU:",
                color = textColor.copy(alpha = 0.5f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "${process.cpuUsagePercent.toInt()}%",
                color = getColorForCpu(process.cpuUsagePercent),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            
            Spacer(modifier = Modifier.width(6.dp))
            
            Text(
                text = "RAM:",
                color = textColor.copy(alpha = 0.5f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "${process.memoryUsageMB}MB",
                color = getColorForMemory(process.memoryUsageMB),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/**
 * Get color based on CPU usage
 */
private fun getColorForCpu(cpuPercent: Float): Color {
    return when {
        cpuPercent > 70 -> Color(0xFFF44336) // Red for high usage (>70%)
        cpuPercent > 40 -> Color(0xFFFF9800) // Orange for medium (>40%)
        cpuPercent > 20 -> Color(0xFFFFC107) // Yellow for moderate (>20%)
        else -> Color(0xFF4CAF50) // Green for low
    }
}

/**
 * Get color based on memory usage
 */
private fun getColorForMemory(memoryMB: Long): Color {
    return when {
        memoryMB > 500 -> Color(0xFFF44336) // Red for high usage (>500MB)
        memoryMB > 200 -> Color(0xFFFF9800) // Orange for medium (>200MB)
        memoryMB > 100 -> Color(0xFFFFC107) // Yellow for moderate (>100MB)
        else -> Color(0xFF4CAF50) // Green for low
    }
}

/**
 * Top Processes display for settings panel (expanded view)
 */
@Composable
fun TopProcessesPanel(
    topProcesses: TopProcesses,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
    ) {
        Text(
            text = "Top RAM Usage (${topProcesses.totalProcesses} processes)",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (topProcesses.processes.isEmpty()) {
            Text(
                text = "No process data available",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        } else {
            topProcesses.processes.take(5).forEachIndexed { index, process ->
                ProcessItemExpanded(
                    process = process,
                    index = index + 1
                )
                if (index < topProcesses.processes.size - 1 && index < 4) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Expanded process item for settings panel
 */
@Composable
private fun ProcessItemExpanded(
    process: ProcessInfo,
    index: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$index.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
                
                Spacer(modifier = Modifier.width(6.dp))
                
                Text(
                    text = process.appName,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (process.packageName != process.appName) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = process.packageName,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = "${process.memoryUsageMB} MB",
            color = getColorForMemory(process.memoryUsageMB),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
