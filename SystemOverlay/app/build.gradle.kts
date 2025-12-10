plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.systemoverlay.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.systemoverlay.app"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.5.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // TV and performance optimizations
        vectorDrawables.useSupportLibrary = true
        
        // Native C++ configuration
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17 -O3 -ffast-math -Wall"
                arguments += "-DANDROID_STL=c++_shared"
            }
        }
        
        ndk {
            // Include only necessary architectures for size optimization
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }
    
    // CMake configuration for native C++ code
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            // Use default CMake version from SDK
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Performance optimizations
            signingConfig = signingConfigs.getByName("debug") // Use debug signing for simplicity
            
            // TV specific optimizations
            buildConfigField("boolean", "IS_TV_OPTIMIZED", "true")
        }
        debug {
            buildConfigField("boolean", "IS_TV_OPTIMIZED", "false")
            applicationIdSuffix = ".debug"
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    
    // Performance and size optimizations
    bundle {
        language {
            enableSplit = false
        }
        density {
            enableSplit = false
        }
        abi {
            enableSplit = true
        }
    }
    
    // Compiler optimizations for TV performance
    compileOptions {
        isCoreLibraryDesugaringEnabled = false // Disable if not needed for better performance
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)
    debugImplementation(libs.androidx.ui.tooling)
    
    // TV
    implementation(libs.androidx.leanback)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
}
