package com.systemoverlay.app.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemoverlay.app.domain.model.OverlayPosition
import com.systemoverlay.app.domain.model.OverlaySettings
import com.systemoverlay.app.presentation.ui.theme.Primary

@Composable
fun SettingsPanel(
    settings: OverlaySettings,
    isGpuAvailable: Boolean,
    onShowClockChange: (Boolean) -> Unit,
    onShowCpuChange: (Boolean) -> Unit,
    onShowGpuChange: (Boolean) -> Unit,
    onShowRamChange: (Boolean) -> Unit,
    onPositionChange: (OverlayPosition) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onStartOnBootChange: (Boolean) -> Unit,
    onUpdateIntervalChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1E1E))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Display Settings",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        SettingSwitch(
            title = "Show Clock",
            checked = settings.showClock,
            onCheckedChange = onShowClockChange
        )
        
        SettingSwitch(
            title = "Show CPU Usage",
            checked = settings.showCpu,
            onCheckedChange = onShowCpuChange
        )
        
        if (isGpuAvailable) {
            SettingSwitch(
                title = "Show GPU Usage",
                checked = settings.showGpu,
                onCheckedChange = onShowGpuChange
            )
        }
        
        SettingSwitch(
            title = "Show RAM Usage",
            checked = settings.showRam,
            onCheckedChange = onShowRamChange
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Position",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        PositionSelector(
            selectedPosition = settings.position,
            onPositionChange = onPositionChange
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Appearance",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        OpacitySlider(
            opacity = settings.opacity,
            onOpacityChange = onOpacityChange
        )
        
        UpdateIntervalSlider(
            intervalMs = settings.updateIntervalMs,
            onIntervalChange = onUpdateIntervalChange
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Behavior",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        SettingSwitch(
            title = "Start on Boot",
            checked = settings.startOnBoot,
            onCheckedChange = onStartOnBootChange
        )
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableFloatStateOf(0f) }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isFocused > 0f) Color(0xFF3A3A3A) else Color.Transparent)
            .border(
                width = if (isFocused > 0f) 2.dp else 0.dp,
                color = if (isFocused > 0f) Primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .focusable()
            .onFocusChanged { isFocused = if (it.isFocused) 1f else 0f }
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 16.sp
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Primary,
                checkedTrackColor = Primary.copy(alpha = 0.5f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun PositionSelector(
    selectedPosition: OverlayPosition,
    onPositionChange: (OverlayPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PositionButton(
                text = "Top Left",
                isSelected = selectedPosition == OverlayPosition.TOP_LEFT,
                onClick = { onPositionChange(OverlayPosition.TOP_LEFT) },
                modifier = Modifier.weight(1f)
            )
            PositionButton(
                text = "Top Right",
                isSelected = selectedPosition == OverlayPosition.TOP_RIGHT,
                onClick = { onPositionChange(OverlayPosition.TOP_RIGHT) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PositionButton(
                text = "Bottom Left",
                isSelected = selectedPosition == OverlayPosition.BOTTOM_LEFT,
                onClick = { onPositionChange(OverlayPosition.BOTTOM_LEFT) },
                modifier = Modifier.weight(1f)
            )
            PositionButton(
                text = "Bottom Right",
                isSelected = selectedPosition == OverlayPosition.BOTTOM_RIGHT,
                onClick = { onPositionChange(OverlayPosition.BOTTOM_RIGHT) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PositionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableFloatStateOf(0f) }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Primary else Color(0xFF2A2A2A))
            .border(
                width = if (isFocused > 0f) 2.dp else 0.dp,
                color = if (isFocused > 0f) Primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .focusable()
            .onFocusChanged { isFocused = if (it.isFocused) 1f else 0f }
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun OpacitySlider(
    opacity: Float,
    onOpacityChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Opacity",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp
            )
            Text(
                text = "${(opacity * 100).toInt()}%",
                color = Primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = opacity,
            onValueChange = onOpacityChange,
            valueRange = 0.2f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Primary,
                activeTrackColor = Primary,
                inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun UpdateIntervalSlider(
    intervalMs: Long,
    onIntervalChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Update Interval",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp
            )
            Text(
                text = "${intervalMs / 1000.0}s",
                color = Primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = intervalMs.toFloat(),
            onValueChange = { onIntervalChange(it.toLong()) },
            valueRange = 500f..5000f,
            steps = 8,
            colors = SliderDefaults.colors(
                thumbColor = Primary,
                activeTrackColor = Primary,
                inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
            )
        )
    }
}
