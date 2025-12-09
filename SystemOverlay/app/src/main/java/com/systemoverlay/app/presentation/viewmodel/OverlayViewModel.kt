package com.systemoverlay.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemoverlay.app.domain.model.OverlayPosition
import com.systemoverlay.app.domain.model.OverlaySettings
import com.systemoverlay.app.domain.model.SystemMetrics
import com.systemoverlay.app.domain.usecase.GetSystemMetricsUseCase
import com.systemoverlay.app.domain.usecase.ManageOverlaySettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for overlay management and system metrics
 */
@HiltViewModel
class OverlayViewModel @Inject constructor(
    private val getSystemMetricsUseCase: GetSystemMetricsUseCase,
    private val manageOverlaySettingsUseCase: ManageOverlaySettingsUseCase
) : ViewModel() {
    
    private val _settings = MutableStateFlow(OverlaySettings())
    val settings: StateFlow<OverlaySettings> = _settings.asStateFlow()
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val systemMetrics: StateFlow<SystemMetrics> = _settings
        .flatMapLatest { settings ->
            getSystemMetricsUseCase.observe(settings.updateIntervalMs)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SystemMetrics()
        )
    
    val isGpuAvailable: Boolean
        get() = getSystemMetricsUseCase.isGpuAvailable()
    
    init {
        viewModelScope.launch {
            manageOverlaySettingsUseCase.observeSettings().collect { settings ->
                _settings.value = settings
            }
        }
    }
    
    fun toggleOverlay(enabled: Boolean) {
        viewModelScope.launch {
            manageOverlaySettingsUseCase.toggleOverlay(enabled)
        }
    }
    
    fun setPosition(position: OverlayPosition) {
        viewModelScope.launch {
            manageOverlaySettingsUseCase.setPosition(position)
        }
    }
    
    fun setOpacity(opacity: Float) {
        viewModelScope.launch {
            manageOverlaySettingsUseCase.setOpacity(opacity)
        }
    }
    
    fun setShowClock(show: Boolean) {
        viewModelScope.launch {
            manageOverlaySettingsUseCase.setShowClock(show)
        }
    }
    
    fun setShowCpu(show: Boolean) {
        viewModelScope.launch {
            manageOverlaySettingsUseCase.setShowCpu(show)
        }
    }
    
    fun setShowGpu(show: Boolean) {
        viewModelScope.launch {
            manageOverlaySettingsUseCase.setShowGpu(show)
        }
    }
    
    fun setShowRam(show: Boolean) {
        viewModelScope.launch {
            manageOverlaySettingsUseCase.setShowRam(show)
        }
    }
    
    fun setUpdateInterval(intervalMs: Long) {
        viewModelScope.launch {
            manageOverlaySettingsUseCase.setUpdateInterval(intervalMs)
        }
    }
    
    fun setStartOnBoot(enabled: Boolean) {
        viewModelScope.launch {
            manageOverlaySettingsUseCase.setStartOnBoot(enabled)
        }
    }
}
