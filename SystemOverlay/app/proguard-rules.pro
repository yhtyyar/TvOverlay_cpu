# System Overlay TV Optimized ProGuard Rules

# Keep debugging info for release builds
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Hilt DI Framework
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.Module class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }

# Coroutines - Essential for our performance optimizations
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keep class kotlinx.coroutines.** { *; }

# Compose - TV UI Components
-keep class androidx.compose.** { *; }
-keep class androidx.tv.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# DataStore for settings persistence
-keep class androidx.datastore.** { *; }
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences { *; }

# Our domain models - keep for reflection
-keep class com.systemoverlay.app.domain.model.** { *; }
-keepclassmembers class com.systemoverlay.app.domain.model.** { *; }

# Data sources - system file access patterns
-keep class com.systemoverlay.app.data.source.** { *; }
-keepclassmembers class com.systemoverlay.app.data.source.** {
    public <methods>;
}

# Core utilities for device optimization
-keep class com.systemoverlay.app.core.DeviceUtils { *; }
-keep class com.systemoverlay.app.core.Constants { *; }
-keep class com.systemoverlay.app.core.Logger { *; }

# Service components - critical for overlay functionality
-keep class com.systemoverlay.app.service.** { *; }
-keep class com.systemoverlay.app.receiver.** { *; }

# Remove Android Log calls in release for performance
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
}

# Optimize for TV - remove unnecessary mobile features
-dontwarn android.hardware.camera**
-dontwarn android.hardware.location**
-dontwarn android.hardware.microphone**

# TV specific optimizations
-keep class androidx.leanback.** { *; }
-keep class * extends androidx.fragment.app.Fragment { *; }
