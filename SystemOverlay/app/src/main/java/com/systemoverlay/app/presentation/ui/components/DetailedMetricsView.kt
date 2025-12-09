package com.systemoverlay.app.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemoverlay.app.domain.model.SystemMetrics
import com.systemoverlay.app.presentation.ui.theme.CpuColor
import com.systemoverlay.app.presentation.ui.theme.GpuColor
import com.systemoverlay.app.presentation.ui.theme.RamColor

/**
 * Detailed metrics view with circular gauges for the main screen
 */
@Composable
fun DetailedMetricsView(
    metrics: SystemMetrics,
    isGpuAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // CPU Gauge
        CircularGauge(
            label = "CPU",
            value = metrics.cpu.overallUsage,
            color = CpuColor,
            subtitle = metrics.cpu.temperature?.let { "${it.toInt()}°C" }
        )
        
        // GPU Gauge (if available)
        if (isGpuAvailable) {
            CircularGauge(
                label = "GPU",
                value = metrics.gpu.usage,
                color = GpuColor,
                subtitle = metrics.gpu.temperature?.let { "${it.toInt()}°C" }
            )
        }
        
        // RAM Gauge
        CircularGauge(
            label = "RAM",
            value = metrics.ram.usagePercent,
            color = RamColor,
            subtitle = "${metrics.ram.usedMB}/${metrics.ram.totalMB} MB"
        )
    }
}

@Composable
private fun CircularGauge(
    label: String,
    value: Float,
    color: Color,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 500),
        label = "gaugeValue"
    )
    
    val displayColor = when {
        value > 90 -> Color(0xFFF44336)
        value > 75 -> Color(0xFFFF9800)
        else -> color
    }
    
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(100.dp)) {
                val strokeWidth = 10.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)
                
                // Background arc
                drawArc(
                    color = displayColor.copy(alpha = 0.15f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                
                // Progress arc
                drawArc(
                    color = displayColor,
                    startAngle = 135f,
                    sweepAngle = 270f * (animatedValue / 100f).coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${animatedValue.toInt()}%",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            color = displayColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        
        subtitle?.let {
            Text(
                text = it,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/**
 * CPU cores grid view
 */
@Composable
fun CpuCoresView(
    coreUsages: List<Float>,
    modifier: Modifier = Modifier
) {
    if (coreUsages.isEmpty()) return
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2A2A2A))
            .padding(16.dp)
    ) {
        Text(
            text = "CPU Cores",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Display cores in a grid (4 per row)
        val chunkedCores = coreUsages.chunked(4)
        chunkedCores.forEach { rowCores ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowCores.forEachIndexed { index, usage ->
                    CoreIndicator(
                        coreNumber = chunkedCores.indexOf(rowCores) * 4 + index,
                        usage = usage
                    )
                }
                // Fill remaining space if row is not complete
                repeat(4 - rowCores.size) {
                    Spacer(modifier = Modifier.width(50.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CoreIndicator(
    coreNumber: Int,
    usage: Float,
    modifier: Modifier = Modifier
) {
    val animatedUsage by animateFloatAsState(
        targetValue = usage,
        animationSpec = tween(durationMillis = 300),
        label = "coreUsage"
    )
    
    val color = when {
        usage > 90 -> Color(0xFFF44336)
        usage > 75 -> Color(0xFFFF9800)
        usage > 50 -> Color(0xFFFFEB3B)
        else -> CpuColor
    }
    
    Column(
        modifier = modifier.width(50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${animatedUsage.toInt()}",
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "C$coreNumber",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp
        )
    }
}
