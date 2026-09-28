package com.fluxboard.app.di

import com.fluxboard.app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * Módulo de red para Ktor (Cloudflare) y el cliente de Supabase.
 *
 * Privacidad: el plugin de logging se instala con LogLevel.NONE para que ni los
 * cuerpos ni las cabeceras (que contienen la carga cifrada y tokens) lleguen a
 * los logs del sistema.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideHttpClient(json: Json): HttpClient = HttpClient(OkHttp) {
        expectSuccess = false
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.NONE
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL.ifBlank { PLACEHOLDER_SUPABASE_URL },
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY.ifBlank { PLACEHOLDER_SUPABASE_KEY }
    ) {
        httpEngine = OkHttp.create()
        defaultLogLevel = io.github.jan.supabase.logging.LogLevel.NONE
        install(Auth)
        install(Postgrest)
    }

    private const val PLACEHOLDER_SUPABASE_URL = "https://placeholder.supabase.co"
    private const val PLACEHOLDER_SUPABASE_KEY = "placeholder-anon-key"
}
