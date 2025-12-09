package com.systemoverlay.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.systemoverlay.app.domain.model.OverlayPosition
import com.systemoverlay.app.domain.model.OverlaySettings
import com.systemoverlay.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "overlay_settings")

/**
 * Implementation of SettingsRepository using DataStore
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {
    
    private object Keys {
        val IS_ENABLED = booleanPreferencesKey("is_enabled")
        val POSITION = stringPreferencesKey("position")
        val OPACITY = floatPreferencesKey("opacity")
        val SHOW_CLOCK = booleanPreferencesKey("show_clock")
        val SHOW_CPU = booleanPreferencesKey("show_cpu")
        val SHOW_GPU = booleanPreferencesKey("show_gpu")
        val SHOW_RAM = booleanPreferencesKey("show_ram")
        val UPDATE_INTERVAL = longPreferencesKey("update_interval")
        val START_ON_BOOT = booleanPreferencesKey("start_on_boot")
        val TEXT_SIZE = floatPreferencesKey("text_size")
        val BACKGROUND_COLOR = longPreferencesKey("background_color")
        val TEXT_COLOR = longPreferencesKey("text_color")
        val CPU_COLOR = longPreferencesKey("cpu_color")
        val GPU_COLOR = longPreferencesKey("gpu_color")
        val RAM_COLOR = longPreferencesKey("ram_color")
    }
    
    override suspend fun getSettings(): OverlaySettings {
        return observeSettings().first()
    }
    
    override fun observeSettings(): Flow<OverlaySettings> {
        return context.dataStore.data.map { preferences ->
            OverlaySettings(
                isEnabled = preferences[Keys.IS_ENABLED] ?: false,
                position = preferences[Keys.POSITION]?.let { 
                    try { OverlayPosition.valueOf(it) } catch (e: Exception) { OverlayPosition.TOP_RIGHT }
                } ?: OverlayPosition.TOP_RIGHT,
                opacity = preferences[Keys.OPACITY] ?: 0.8f,
                showClock = preferences[Keys.SHOW_CLOCK] ?: true,
                showCpu = preferences[Keys.SHOW_CPU] ?: true,
                showGpu = preferences[Keys.SHOW_GPU] ?: true,
                showRam = preferences[Keys.SHOW_RAM] ?: true,
                updateIntervalMs = preferences[Keys.UPDATE_INTERVAL] ?: 1000L,
                startOnBoot = preferences[Keys.START_ON_BOOT] ?: false,
                textSize = preferences[Keys.TEXT_SIZE] ?: 14f,
                backgroundColor = preferences[Keys.BACKGROUND_COLOR] ?: 0x80000000,
                textColor = preferences[Keys.TEXT_COLOR] ?: 0xFFFFFFFF,
                cpuColor = preferences[Keys.CPU_COLOR] ?: 0xFF4CAF50,
                gpuColor = preferences[Keys.GPU_COLOR] ?: 0xFFFF9800,
                ramColor = preferences[Keys.RAM_COLOR] ?: 0xFF9C27B0
            )
        }
    }
    
    override suspend fun setEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.IS_ENABLED] = enabled
        }
    }
    
    override suspend fun setPosition(position: OverlayPosition) {
        context.dataStore.edit { preferences ->
            preferences[Keys.POSITION] = position.name
        }
    }
    
    override suspend fun setOpacity(opacity: Float) {
        context.dataStore.edit { preferences ->
            preferences[Keys.OPACITY] = opacity
        }
    }
    
    override suspend fun setShowClock(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SHOW_CLOCK] = show
        }
    }
    
    override suspend fun setShowCpu(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SHOW_CPU] = show
        }
    }
    
    override suspend fun setShowGpu(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SHOW_GPU] = show
        }
    }
    
    override suspend fun setShowRam(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SHOW_RAM] = show
        }
    }
    
    override suspend fun setUpdateInterval(intervalMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[Keys.UPDATE_INTERVAL] = intervalMs
        }
    }
    
    override suspend fun setStartOnBoot(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.START_ON_BOOT] = enabled
        }
    }
    
    override suspend fun updateSettings(settings: OverlaySettings) {
        context.dataStore.edit { preferences ->
            preferences[Keys.IS_ENABLED] = settings.isEnabled
            preferences[Keys.POSITION] = settings.position.name
            preferences[Keys.OPACITY] = settings.opacity
            preferences[Keys.SHOW_CLOCK] = settings.showClock
            preferences[Keys.SHOW_CPU] = settings.showCpu
            preferences[Keys.SHOW_GPU] = settings.showGpu
            preferences[Keys.SHOW_RAM] = settings.showRam
            preferences[Keys.UPDATE_INTERVAL] = settings.updateIntervalMs
            preferences[Keys.START_ON_BOOT] = settings.startOnBoot
            preferences[Keys.TEXT_SIZE] = settings.textSize
            preferences[Keys.BACKGROUND_COLOR] = settings.backgroundColor
            preferences[Keys.TEXT_COLOR] = settings.textColor
            preferences[Keys.CPU_COLOR] = settings.cpuColor
            preferences[Keys.GPU_COLOR] = settings.gpuColor
            preferences[Keys.RAM_COLOR] = settings.ramColor
        }
    }
}
