package com.systemoverlay.app.presentation.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemoverlay.app.presentation.ui.components.MetricsDisplayPanel
import com.systemoverlay.app.presentation.ui.components.SettingsPanel
import com.systemoverlay.app.presentation.ui.theme.Primary
import com.systemoverlay.app.presentation.ui.theme.SystemOverlayTheme
import com.systemoverlay.app.presentation.viewmodel.OverlayViewModel
import com.systemoverlay.app.service.OverlayService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val viewModel: OverlayViewModel by viewModels()
    
    private var permissionStateCallback: ((Boolean) -> Unit)? = null
    
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Check permission result and update UI
        val hasPermission = Settings.canDrawOverlays(this)
        permissionStateCallback?.invoke(hasPermission)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Auto-request overlay permission on first launch
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
        }
        
        setContent {
            SystemOverlayTheme {
                val settings by viewModel.settings.collectAsState()
                val metrics by viewModel.systemMetrics.collectAsState()
                var hasOverlayPermission by remember { 
                    mutableStateOf(Settings.canDrawOverlays(this)) 
                }
                
                // Update permission state callback
                permissionStateCallback = { hasPermission ->
                    hasOverlayPermission = hasPermission
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF121212))
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "System Overlay",
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
                        // Permission request card
                        Box(
                            modifier = Modifier
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
                                    text = "Overlay Permission Required",
                                    color = Color(0xFFFF5722),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "This app needs permission to draw over other apps to display system metrics.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        requestOverlayPermission()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF5722)
                                    )
                                ) {
                                    Text("Grant Permission")
                                }
                            }
                        }
                    } else {
                        // Start/Stop button
                        Button(
                            onClick = {
                                if (settings.isEnabled) {
                                    stopOverlayService()
                                    viewModel.toggleOverlay(false)
                                } else {
                                    startOverlayService()
                                    viewModel.toggleOverlay(true)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (settings.isEnabled) 
                                    Color(0xFFF44336) else Primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (settings.isEnabled) "Stop Overlay" else "Start Overlay",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Current metrics preview
                    Text(
                        text = "Current Metrics",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    MetricsDisplayPanel(
                        metrics = metrics,
                        showCpu = true,
                        showGpu = true,
                        showRam = true,
                        isGpuAvailable = viewModel.isGpuAvailable
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Settings
                    SettingsPanel(
                        settings = settings,
                        isGpuAvailable = viewModel.isGpuAvailable,
                        onShowClockChange = viewModel::setShowClock,
                        onShowCpuChange = viewModel::setShowCpu,
                        onShowGpuChange = viewModel::setShowGpu,
                        onShowRamChange = viewModel::setShowRam,
                        onPositionChange = viewModel::setPosition,
                        onOpacityChange = viewModel::setOpacity,
                        onStartOnBootChange = viewModel::setStartOnBoot,
                        onUpdateIntervalChange = viewModel::setUpdateInterval
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Recheck permission when returning from settings
        // ViewModel automatically handles monitoring
    }
    
    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }
    }
    
    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
    
    private fun stopOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        stopService(intent)
    }
}
