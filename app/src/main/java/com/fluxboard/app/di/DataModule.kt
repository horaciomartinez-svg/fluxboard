package com.fluxboard.app.di

import android.content.Context
import androidx.room.Room
import com.fluxboard.app.data.local.ClipDao
import com.fluxboard.app.data.local.FluxDatabase
import com.fluxboard.app.data.repository.AuthRepositoryImpl
import com.fluxboard.app.data.repository.ClipRepositoryImpl
import com.fluxboard.app.data.repository.RevenueCatRepositoryImpl
import com.fluxboard.app.domain.repository.IAuthRepository
import com.fluxboard.app.domain.repository.IClipRepository
import com.fluxboard.app.domain.repository.ISubscriptionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de inyección de dependencias para la capa de datos local.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindClipRepository(impl: ClipRepositoryImpl): IClipRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(impl: RevenueCatRepositoryImpl): ISubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): IAuthRepository

    companion object {

        @Provides
        @Singleton
        fun provideFluxDatabase(
            @ApplicationContext context: Context
        ): FluxDatabase = Room.databaseBuilder(
            context,
            FluxDatabase::class.java,
            "flux_board.db"
        ).build()

        @Provides
        fun provideClipDao(database: FluxDatabase): ClipDao = database.clipDao()
    }
}
