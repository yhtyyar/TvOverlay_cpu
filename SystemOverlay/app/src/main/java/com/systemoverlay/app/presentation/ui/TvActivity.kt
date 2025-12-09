package com.systemoverlay.app.presentation.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.systemoverlay.app.presentation.ui.components.MetricsDisplayPanel
import com.systemoverlay.app.presentation.ui.components.SettingsPanel
import com.systemoverlay.app.presentation.ui.theme.Primary
import com.systemoverlay.app.presentation.ui.theme.SystemOverlayTheme
import com.systemoverlay.app.presentation.viewmodel.OverlayViewModel
import com.systemoverlay.app.service.OverlayService
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity optimized for Android TV with D-pad navigation
 */
@AndroidEntryPoint
class TvActivity : ComponentActivity() {
    
    private val viewModel: OverlayViewModel by viewModels()
    
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            SystemOverlayTheme {
                val settings by viewModel.settings.collectAsState()
                val metrics by viewModel.systemMetrics.collectAsState()
                var hasOverlayPermission by remember { 
                    mutableStateOf(Settings.canDrawOverlays(this)) 
                }
                val focusRequester = remember { FocusRequester() }
                
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF121212))
                        .padding(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Left panel - Main controls and metrics
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "System Overlay",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Monitor CPU, GPU & RAM on your TV",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        if (!hasOverlayPermission) {
                            // Permission request card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color(0xFFFF5722).copy(alpha = 0.2f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(24.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Overlay Permission Required",
                                        color = Color(0xFFFF5722),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Use ADB command to grant permission:\nadb shell appops set ${packageName} SYSTEM_ALERT_WINDOW allow",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        onClick = {
                                            hasOverlayPermission = Settings.canDrawOverlays(this@TvActivity)
                                        },
                                        colors = ButtonDefaults.colors(
                                            containerColor = Color(0xFFFF5722)
                                        ),
                                        modifier = Modifier.focusRequester(focusRequester)
                                    ) {
                                        Text("Check Permission", fontSize = 18.sp)
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
                                    .height(72.dp)
                                    .focusRequester(focusRequester),
                                colors = ButtonDefaults.colors(
                                    containerColor = if (settings.isEnabled) 
                                        Color(0xFFF44336) else Primary,
                                    focusedContainerColor = if (settings.isEnabled)
                                        Color(0xFFD32F2F) else Color(0xFF1976D2)
                                ),
                                shape = ButtonDefaults.shape(
                                    shape = RoundedCornerShape(16.dp)
                                )
                            ) {
                                Text(
                                    text = if (settings.isEnabled) "Stop Overlay" else "Start Overlay",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Current metrics preview
                        Text(
                            text = "Current Metrics",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        MetricsDisplayPanel(
                            metrics = metrics,
                            showCpu = true,
                            showGpu = true,
                            showRam = true,
                            isGpuAvailable = viewModel.isGpuAvailable
                        )
                    }
                    
                    // Right panel - Settings
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
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
                    }
                }
            }
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
