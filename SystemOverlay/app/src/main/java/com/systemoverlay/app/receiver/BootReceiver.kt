package com.systemoverlay.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.systemoverlay.app.domain.repository.SettingsRepository
import com.systemoverlay.app.service.OverlayService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receiver to start overlay service on device boot
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var settingsRepository: SettingsRepository
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        
        // Check if we have overlay permission
        if (!Settings.canDrawOverlays(context)) return
        
        CoroutineScope(Dispatchers.IO).launch {
            val settings = settingsRepository.getSettings()
            
            if (settings.startOnBoot) {
                val serviceIntent = Intent(context, OverlayService::class.java)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                
                // Update enabled state
                settingsRepository.setEnabled(true)
            }
        }
    }
}
