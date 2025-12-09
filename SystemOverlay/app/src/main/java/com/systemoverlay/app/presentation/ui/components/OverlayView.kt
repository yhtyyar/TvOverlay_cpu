package com.systemoverlay.app.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemoverlay.app.domain.model.OverlaySettings
import com.systemoverlay.app.domain.model.SystemMetrics
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Compact overlay widget for display over other apps.
 * Features smooth animations and efficient rendering.
 */
@Composable
fun OverlayWidget(
    metrics: SystemMetrics,
    settings: OverlaySettings,
    isGpuAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(settings.backgroundColor).copy(alpha = settings.opacity))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Metrics section - aligned to END (right)
        Column(
            horizontalAlignment = Alignment.End
        ) {
            if (settings.showClock) {
                ClockDisplay(textColor = Color(settings.textColor))
                if (settings.showCpu || settings.showGpu || settings.showRam) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            
            if (settings.showCpu) {
                AnimatedMetricRow(
                    icon = Icons.Default.Speed,
                    label = "CPU",
                    value = metrics.cpu.overallUsage,
                    color = Color(settings.cpuColor)
                )
            }
            
            if (settings.showGpu && isGpuAvailable) {
                AnimatedMetricRow(
                    icon = Icons.Default.Memory,
                    label = "GPU",
                    value = metrics.gpu.usage,
                    color = Color(settings.gpuColor)
                )
            }
            
            if (settings.showRam) {
                RamMetricRow(
                    metrics = metrics.ram,
                    baseColor = Color(settings.ramColor)
                )
            }
        }
        
        // Top Processes section - aligned to START (left)
        if (metrics.topProcesses.processes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                TopProcessesOverlay(
                    topProcesses = metrics.topProcesses,
                    backgroundColor = Color.Black.copy(alpha = 0.3f),
                    textColor = Color.White
                )
            }
        }
    }
}

@Composable
private fun ClockDisplay(
    textColor: Color,
    modifier: Modifier = Modifier
) {
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000)
        }
    }
    
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val currentTime = remember(currentTimeMillis) { 
        timeFormat.format(Date(currentTimeMillis)) 
    }
    
    Text(
        text = currentTime,
        color = textColor,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = modifier
    )
}

/**
 * Metric row with animated progress indicator
 */
@Composable
private fun AnimatedMetricRow(
    icon: ImageVector,
    label: String,
    value: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 300),
        label = "metricValue"
    )
    
    // Color transitions based on load level
    val displayColor by animateColorAsState(
        targetValue = when {
            value > 90 -> Color(0xFFF44336) // Red for critical
            value > 75 -> Color(0xFFFF9800) // Orange for high
            else -> color
        },
        animationSpec = tween(durationMillis = 500),
        label = "metricColor"
    )
    
    Row(
        modifier = modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        // Mini circular progress indicator
        MiniCircularProgress(
            progress = animatedValue / 100f,
            color = displayColor,
            size = 16.dp
        )
        
        Spacer(modifier = Modifier.width(6.dp))
        
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = "${animatedValue.toInt()}%",
            color = displayColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Mini circular progress indicator for compact display
 */
@Composable
private fun MiniCircularProgress(
    progress: Float,
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 2.dp.toPx()
        val radius = (size.toPx() - strokeWidth) / 2
        val center = Offset(size.toPx() / 2, size.toPx() / 2)
        
        // Background circle
        drawCircle(
            color = color.copy(alpha = 0.2f),
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )
        
        // Progress arc
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(size.toPx() - strokeWidth, size.toPx() - strokeWidth),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

/**
 * RAM metric row with MB display and color-coded indicators
 */
@Composable
private fun RamMetricRow(
    metrics: com.systemoverlay.app.domain.model.RamMetrics,
    baseColor: Color,
    modifier: Modifier = Modifier
) {
    val usedMB = metrics.usedMB
    val totalMB = metrics.totalMB
    val usagePercent = metrics.usagePercent
    
    // Animated values for smooth transitions
    val animatedUsed by animateFloatAsState(
        targetValue = usedMB.toFloat(),
        animationSpec = tween(durationMillis = 300),
        label = "ramUsed"
    )
    
    // Smart color coding based on memory pressure
    val displayColor by animateColorAsState(
        targetValue = when {
            usagePercent > 85 -> Color(0xFFF44336) // Red - Critical
            usagePercent > 70 -> Color(0xFFFF9800) // Orange - High
            usagePercent > 50 -> Color(0xFFFFC107) // Yellow - Medium
            else -> Color(0xFF4CAF50) // Green - Good
        },
        animationSpec = tween(durationMillis = 500),
        label = "ramColor"
    )
    
    Row(
        modifier = modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        // Mini circular progress indicator
        MiniCircularProgress(
            progress = (usagePercent / 100f).coerceIn(0f, 1f),
            color = displayColor,
            size = 16.dp
        )
        
        Spacer(modifier = Modifier.width(6.dp))
        
        Text(
            text = "RAM",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // Display used/total in MB
        Text(
            text = "${animatedUsed.toInt()}/${totalMB}MB",
            color = displayColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Simple metric row without animation (legacy support)
 */
@Composable
private fun MetricRow(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label:",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
