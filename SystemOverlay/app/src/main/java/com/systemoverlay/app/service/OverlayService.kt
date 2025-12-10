package com.systemoverlay.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.systemoverlay.app.R
import com.systemoverlay.app.core.Constants
import com.systemoverlay.app.core.DeviceUtils
import com.systemoverlay.app.core.Logger
import com.systemoverlay.app.domain.model.OverlayPosition
import com.systemoverlay.app.domain.model.OverlaySettings
import com.systemoverlay.app.domain.model.SystemMetrics
import com.systemoverlay.app.domain.usecase.GetSystemMetricsUseCase
import com.systemoverlay.app.domain.usecase.ManageOverlaySettingsUseCase
import com.systemoverlay.app.presentation.ui.MainActivity
import com.systemoverlay.app.presentation.ui.components.OverlayWidget
import com.systemoverlay.app.presentation.ui.theme.SystemOverlayTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that displays system metrics overlay.
 * Implements LifecycleOwner for Compose integration.
 */
@AndroidEntryPoint
class OverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {
    
    @Inject
    lateinit var getSystemMetricsUseCase: GetSystemMetricsUseCase
    
    @Inject
    lateinit var manageOverlaySettingsUseCase: ManageOverlaySettingsUseCase
    
    @Inject
    lateinit var deviceUtils: DeviceUtils
    
    private val logger = Logger.withTag("OverlayService")
    
    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.e("Coroutine exception", throwable)
    }
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + exceptionHandler)
    
    private val _settings = MutableStateFlow(OverlaySettings())
    private val settings: StateFlow<OverlaySettings> = _settings.asStateFlow()
    
    private val _metrics = MutableStateFlow(SystemMetrics())
    private val metrics: StateFlow<SystemMetrics> = _metrics.asStateFlow()
    
    private var metricsJob: Job? = null
    private var settingsJob: Job? = null
    private var adaptiveJob: Job? = null
    
    // Adaptive performance tracking
    @Volatile
    private var currentUpdateInterval: Long = Constants.DEFAULT_UPDATE_INTERVAL_MS
    @Volatile
    private var lastAdaptiveCheck: Long = 0L
    
    // Lifecycle management for Compose
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
    
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
    
    companion object {
        private const val ACTION_STOP = "com.systemoverlay.app.STOP_SERVICE"
    }
    
    override fun onCreate() {
        super.onCreate()
        logger.i("Service onCreate")
        
        // Setup exception handler for Compose crashes (e.g., ACTION_HOVER_EXIT on TV)
        setupExceptionHandler()
        
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        
        observeSettings()
    }
    
    private fun setupExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Log all exceptions
            logger.e("Uncaught exception in thread ${thread.name}", throwable)
            
            // Handle specific Compose hover event crash on Android TV
            if (throwable is IllegalStateException && 
                throwable.message?.contains("ACTION_HOVER_EXIT") == true) {
                logger.w("Caught ACTION_HOVER_EXIT crash (TV hover event issue), recovering gracefully")
                // Don't crash - this is a known Compose TV issue
                // Service will continue running
                return@setDefaultUncaughtExceptionHandler
            }
            
            // For other exceptions, use default handler
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
    
    private fun observeSettings() {
        settingsJob?.cancel()
        settingsJob = serviceScope.launch {
            manageOverlaySettingsUseCase.observeSettings().collect { newSettings ->
                val oldSettings = _settings.value
                _settings.value = newSettings
                
                // Update overlay position if changed
                if (oldSettings.position != newSettings.position) {
                    updateOverlayPosition(newSettings.position)
                }
                
                // Restart metrics collection if interval changed
                if (oldSettings.updateIntervalMs != newSettings.updateIntervalMs) {
                    startMetricsCollection(newSettings.updateIntervalMs)
                }
            }
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger.i("Service onStartCommand, action: ${intent?.action}")
        
        // Handle stop action
        if (intent?.action == ACTION_STOP) {
            logger.i("Stop action received")
            stopSelf()
            return START_NOT_STICKY
        }
        
        // Check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            logger.e("No overlay permission, stopping service")
            stopSelf()
            return START_NOT_STICKY
        }
        
        startForeground(Constants.NOTIFICATION_ID, createNotification())
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        
        createOverlayView()
        startMetricsCollection(_settings.value.updateIntervalMs)
        
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        logger.i("Service started successfully")
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        logger.i("Service onDestroy")
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        removeOverlayView()
        metricsJob?.cancel()
        settingsJob?.cancel()
        adaptiveJob?.cancel()
        serviceScope.cancel()
        
        // Update settings to reflect service stopped
        serviceScope.launch {
            try {
                manageOverlaySettingsUseCase.toggleOverlay(false)
            } catch (e: Exception) {
                // Ignore, scope is being cancelled
            }
        }
        
        super.onDestroy()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.overlay_service_text)
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val stopIntent = Intent(this, OverlayService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.overlay_service_title))
            .setContentText(getString(R.string.overlay_service_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentPendingIntent)
            .addAction(
                R.drawable.ic_notification,
                getString(R.string.stop_service),
                stopPendingIntent
            )
            .setOngoing(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    private fun createOverlayView() {
        if (overlayView != null) {
            logger.d("Overlay view already exists")
            return
        }
        
        logger.d("Creating overlay view")
        
        val params = createWindowLayoutParams(_settings.value.position)
        
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)
            
            // Add drag & drop for mobile devices
            if (!deviceUtils.isTvDevice()) {
                setOnTouchListener(DragTouchListener(params, windowManager, this@OverlayService.logger))
            }
            
            setContent {
                val currentSettings by settings.collectAsState()
                val currentMetrics by metrics.collectAsState()
                
                SystemOverlayTheme {
                    OverlayWidget(
                        metrics = currentMetrics,
                        settings = currentSettings,
                        isGpuAvailable = getSystemMetricsUseCase.isGpuAvailable()
                    )
                }
            }
        }
        
        try {
            windowManager.addView(composeView, params)
            overlayView = composeView
            logger.i("Overlay view created successfully (draggable: ${!deviceUtils.isTvDevice()})")
        } catch (e: SecurityException) {
            logger.e("Security exception creating overlay - no permission", e)
            stopSelf()
        } catch (e: WindowManager.BadTokenException) {
            logger.e("Bad token exception - invalid window type", e)
            stopSelf()
        } catch (e: Exception) {
            logger.e("Error creating overlay view", e)
        }
    }
    
    private fun createWindowLayoutParams(position: OverlayPosition): WindowManager.LayoutParams {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        
        val gravity = when (position) {
            OverlayPosition.TOP_LEFT -> Gravity.TOP or Gravity.START
            OverlayPosition.TOP_RIGHT -> Gravity.TOP or Gravity.END
            OverlayPosition.BOTTOM_LEFT -> Gravity.BOTTOM or Gravity.START
            OverlayPosition.BOTTOM_RIGHT -> Gravity.BOTTOM or Gravity.END
        }
        
        val marginDp = deviceUtils.getOverlayMargin()
        val margin = (marginDp * resources.displayMetrics.density).toInt()
        
        // Fix for ACTION_HOVER_EXIT crash on Android TV
        val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                // Disable touch for TV to prevent hover events crash
                if (deviceUtils.isTvDevice()) {
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                } else {
                    0
                }
        
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = gravity
            x = margin
            y = margin
        }
    }
    
    private fun updateOverlayPosition(position: OverlayPosition) {
        overlayView?.let { view ->
            val params = createWindowLayoutParams(position)
            try {
                windowManager.updateViewLayout(view, params)
                logger.d("Overlay position updated to $position")
            } catch (e: IllegalArgumentException) {
                logger.w("View not attached, cannot update position")
            } catch (e: Exception) {
                logger.e("Error updating overlay position", e)
            }
        }
    }
    
    private fun removeOverlayView() {
        overlayView?.let { view ->
            try {
                windowManager.removeView(view)
                logger.d("Overlay view removed")
            } catch (e: IllegalArgumentException) {
                logger.w("View not attached, already removed")
            } catch (e: Exception) {
                logger.e("Error removing overlay view", e)
            }
            overlayView = null
        }
    }
    
    private fun startMetricsCollection(intervalMs: Long) {
        metricsJob?.cancel()
        adaptiveJob?.cancel()
        
        currentUpdateInterval = deviceUtils.getOptimalUpdateInterval()
        logger.d("Starting metrics collection with initial interval ${currentUpdateInterval}ms (requested: ${intervalMs}ms)")
        
        metricsJob = serviceScope.launch {
            getSystemMetricsUseCase.observe(currentUpdateInterval).collect { newMetrics ->
                _metrics.value = newMetrics
            }
        }
        
        // Start adaptive performance monitoring for TV devices
        if (deviceUtils.isTvDevice() || deviceUtils.shouldUseAdaptivePerformance()) {
            startAdaptiveMonitoring()
        }
    }
    
    /**
     * Adaptive monitoring adjusts update interval based on system load
     */
    private fun startAdaptiveMonitoring() {
        adaptiveJob = serviceScope.launch {
            kotlinx.coroutines.delay(30000) // Wait 30 seconds before starting adaptive behavior
            
            while (true) {
                try {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastAdaptiveCheck > 10000) { // Check every 10 seconds
                        adjustUpdateIntervalBasedOnLoad()
                        lastAdaptiveCheck = currentTime
                    }
                    
                    kotlinx.coroutines.delay(10000)
                } catch (e: Exception) {
                    logger.w("Error in adaptive monitoring: ${e.message}")
                    kotlinx.coroutines.delay(30000) // Back off on error
                }
            }
        }
    }
    
    /**
     * Adjust update interval based on current system load
     */
    private suspend fun adjustUpdateIntervalBasedOnLoad() {
        try {
            val metrics = getSystemMetricsUseCase.invoke()
            val optimalInterval = calculateOptimalInterval(metrics)
            
            if (optimalInterval != currentUpdateInterval) {
                logger.d("Adjusting update interval from ${currentUpdateInterval}ms to ${optimalInterval}ms")
                currentUpdateInterval = optimalInterval
                
                // Restart metrics collection with new interval
                metricsJob?.cancel()
                metricsJob = serviceScope.launch {
                    getSystemMetricsUseCase.observe(currentUpdateInterval).collect { newMetrics ->
                        _metrics.value = newMetrics
                    }
                }
            }
        } catch (e: Exception) {
            logger.w("Error adjusting update interval: ${e.message}")
        }
    }
    
    /**
     * Calculate optimal update interval based on system metrics
     */
    private fun calculateOptimalInterval(metrics: com.systemoverlay.app.domain.model.SystemMetrics): Long {
        return when {
            // High CPU usage - slow down updates
            metrics.cpu.overallUsage > Constants.HIGH_CPU_USAGE_THRESHOLD -> {
                Constants.TV_SLOW_UPDATE_INTERVAL_MS
            }
            // Low memory - slow down updates
            metrics.ram.availableBytes < Constants.MEMORY_PRESSURE_THRESHOLD_MB * 1024 * 1024 -> {
                Constants.TV_SLOW_UPDATE_INTERVAL_MS
            }
            // Power saving mode
            deviceUtils.shouldUsePowerSavingMode() -> {
                Constants.TV_SLOW_UPDATE_INTERVAL_MS
            }
            // TV device in normal mode
            deviceUtils.isTvDevice() -> {
                Constants.DEFAULT_UPDATE_INTERVAL_MS
            }
            // Mobile device
            else -> {
                Constants.TV_FAST_UPDATE_INTERVAL_MS
            }
        }
    }
    
    /**
     * Touch listener for dragging overlay on mobile devices
     * Clean implementation for smooth drag & drop
     */
    private class DragTouchListener(
        private val params: WindowManager.LayoutParams,
        private val windowManager: WindowManager,
        private val logger: Logger.TaggedLogger
    ) : android.view.View.OnTouchListener {
        
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f
        
        override fun onTouch(view: android.view.View, event: android.view.MotionEvent): Boolean {
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    // Save initial position
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    return true
                }
                
                android.view.MotionEvent.ACTION_MOVE -> {
                    // Calculate new position
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    
                    params.x = initialX + deltaX
                    params.y = initialY + deltaY
                    
                    // Update overlay position
                    try {
                        windowManager.updateViewLayout(view, params)
                    } catch (e: Exception) {
                        logger.e("Error updating view during drag", e)
                    }
                    return true
                }
                
                android.view.MotionEvent.ACTION_UP -> {
                    logger.d("Overlay dragged to position (${params.x}, ${params.y})")
                    return true
                }
            }
            return false
        }
    }
}
