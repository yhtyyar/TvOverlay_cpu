package com.systemoverlay.app.core

import android.util.Log
import com.systemoverlay.app.BuildConfig

/**
 * Application logger with tag management and debug-only logging
 */
object Logger {
    
    private const val TAG = "SystemOverlay"
    
    private val isDebug: Boolean
        get() = BuildConfig.DEBUG
    
    fun d(message: String, tag: String = TAG) {
        if (isDebug) {
            Log.d(tag, message)
        }
    }
    
    fun i(message: String, tag: String = TAG) {
        Log.i(tag, message)
    }
    
    fun w(message: String, throwable: Throwable? = null, tag: String = TAG) {
        if (throwable != null) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, message)
        }
    }
    
    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
    
    fun v(message: String, tag: String = TAG) {
        if (isDebug) {
            Log.v(tag, message)
        }
    }
    
    /**
     * Log with custom tag prefix
     */
    fun withTag(customTag: String): TaggedLogger = TaggedLogger("$TAG:$customTag")
    
    class TaggedLogger(private val tag: String) {
        fun d(message: String) = Logger.d(message, tag)
        fun i(message: String) = Logger.i(message, tag)
        fun w(message: String, throwable: Throwable? = null) = Logger.w(message, throwable, tag)
        fun e(message: String, throwable: Throwable? = null) = Logger.e(message, throwable, tag)
        fun v(message: String) = Logger.v(message, tag)
    }
}

/**
 * Inline logging extensions
 */
inline fun <T> T.logDebug(message: (T) -> String): T {
    Logger.d(message(this))
    return this
}

inline fun <T> T.logInfo(message: (T) -> String): T {
    Logger.i(message(this))
    return this
}
