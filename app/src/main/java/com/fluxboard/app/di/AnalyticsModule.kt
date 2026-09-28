package com.fluxboard.app.di

import com.fluxboard.app.core.analytics.AnalyticsTracker
import com.fluxboard.app.data.analytics.PostHogAnalyticsTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de inyección para la infraestructura de analíticas.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsTracker(impl: PostHogAnalyticsTracker): AnalyticsTracker
}
