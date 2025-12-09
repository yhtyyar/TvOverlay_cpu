package com.systemoverlay.app.presentation.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemoverlay.app.R
import com.systemoverlay.app.core.PermissionHelper
import com.systemoverlay.app.domain.model.OverlaySettings
import com.systemoverlay.app.domain.model.SystemMetrics
import com.systemoverlay.app.presentation.ui.components.CpuCoresView
import com.systemoverlay.app.presentation.ui.components.DetailedMetricsView
import com.systemoverlay.app.presentation.ui.components.SettingsPanel
import com.systemoverlay.app.presentation.ui.theme.Primary
import com.systemoverlay.app.service.OverlayService
import kotlinx.coroutines.flow.StateFlow

/**
 * Main screen content composable
 */
@Composable
fun MainScreenContent(
    settings: StateFlow<OverlaySettings>,
    metrics: StateFlow<SystemMetrics>,
    isGpuAvailable: Boolean,
    onToggleOverlay: (Boolean) -> Unit,
    onShowClockChange: (Boolean) -> Unit,
    onShowCpuChange: (Boolean) -> Unit,
    onShowGpuChange: (Boolean) -> Unit,
    onShowRamChange: (Boolean) -> Unit,
    onPositionChange: (com.systemoverlay.app.domain.model.OverlayPosition) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onStartOnBootChange: (Boolean) -> Unit,
    onUpdateIntervalChange: (Long) -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsState by settings.collectAsState()
    val metricsState by metrics.collectAsState()
    
    var hasOverlayPermission by remember {
        mutableStateOf(PermissionHelper.canDrawOverlays(context))
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = stringResource(R.string.app_name),
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Monitor CPU, GPU & RAM",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (!hasOverlayPermission) {
            PermissionRequestCard(
                onRequestPermission = {
                    onRequestPermission()
                    // Re-check after returning
                    hasOverlayPermission = PermissionHelper.canDrawOverlays(context)
                },
                packageName = context.packageName
            )
        } else {
            // Start/Stop button
            OverlayControlButton(
                isEnabled = settingsState.isEnabled,
                onClick = {
                    if (settingsState.isEnabled) {
                        stopOverlayService(context)
                        onToggleOverlay(false)
                    } else {
                        startOverlayService(context)
                        onToggleOverlay(true)
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Detailed metrics
        DetailedMetricsView(
            metrics = metricsState,
            isGpuAvailable = isGpuAvailable
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // CPU Cores (if available)
        if (metricsState.cpu.coreUsages.isNotEmpty()) {
            CpuCoresView(coreUsages = metricsState.cpu.coreUsages)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Settings
        SettingsPanel(
            settings = settingsState,
            isGpuAvailable = isGpuAvailable,
            onShowClockChange = onShowClockChange,
            onShowCpuChange = onShowCpuChange,
            onShowGpuChange = onShowGpuChange,
            onShowRamChange = onShowRamChange,
            onPositionChange = onPositionChange,
            onOpacityChange = onOpacityChange,
            onStartOnBootChange = onStartOnBootChange,
            onUpdateIntervalChange = onUpdateIntervalChange
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PermissionRequestCard(
    onRequestPermission: () -> Unit,
    packageName: String,
    modifier: Modifier = Modifier
) {
    val isTv = PermissionHelper.isAndroidTv(LocalContext.current)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Color(0xFFFF5722).copy(alpha = 0.2f),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.permissions_required),
                color = Color(0xFFFF5722),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.overlay_permission_desc),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            
            if (isTv) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "ADB Command:",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                
                Text(
                    text = PermissionHelper.getOverlayAdbCommand(packageName),
                    color = Primary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5722)
                )
            ) {
                Text(stringResource(R.string.grant_permission))
            }
        }
    }
}

@Composable
private fun OverlayControlButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEnabled) Color(0xFFF44336) else Primary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = if (isEnabled) 
                stringResource(R.string.stop_overlay) 
            else 
                stringResource(R.string.start_overlay),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun startOverlayService(context: Context) {
    val intent = Intent(context, OverlayService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}

private fun stopOverlayService(context: Context) {
    val intent = Intent(context, OverlayService::class.java)
    context.stopService(intent)
}
