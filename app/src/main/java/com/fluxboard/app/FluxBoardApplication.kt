package com.fluxboard.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.fluxboard.app.core.analytics.AnalyticsTracker
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Punto de entrada de la aplicación.
 *
 * - Habilita la inyección de dependencias de Hilt.
 * - Configura RevenueCat para el flujo de monetización.
 * - Provee la [Configuration] de WorkManager con [HiltWorkerFactory] para que
 *   [SyncWorker][com.fluxboard.app.data.worker.SyncWorker] reciba sus
 *   dependencias.
 * - Inicializa las analíticas (PostHog) al arrancar.
 */
@HiltAndroidApp
class FluxBoardApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var analyticsTracker: AnalyticsTracker

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        configureRevenueCat()
    }

    private fun configureRevenueCat() {
        val apiKey = BuildConfig.REVENUECAT_API_KEY
        if (apiKey.isBlank()) {
            return
        }
        Purchases.configure(
            PurchasesConfiguration.Builder(this, apiKey).build()
        )
    }
}
