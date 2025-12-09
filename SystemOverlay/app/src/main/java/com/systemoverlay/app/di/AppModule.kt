package com.systemoverlay.app.di

import com.systemoverlay.app.data.repository.SettingsRepositoryImpl
import com.systemoverlay.app.data.repository.SystemMetricsRepositoryImpl
import com.systemoverlay.app.domain.repository.SettingsRepository
import com.systemoverlay.app.domain.repository.SystemMetricsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    
    @Binds
    @Singleton
    abstract fun bindSystemMetricsRepository(
        impl: SystemMetricsRepositoryImpl
    ): SystemMetricsRepository
    
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}
