package com.fluxboard.app.data.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Programa la sincronización diferida de recortes.
 *
 * La red NUNCA se usa desde la UI ni desde el teclado: todo el tráfico remoto se
 * encola aquí y lo ejecuta [SyncWorker] con la restricción de conectividad.
 */
@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Encola una sincronización única. Se usa `KEEP` para no duplicar trabajo
     * cuando ya hay una sincronización pendiente, y backoff exponencial para los
     * reintentos. Las operaciones remotas son idempotentes, por lo que reintentar
     * es seguro.
     */
    fun enqueueOneTimeSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                MIN_BACKOFF_SECONDS,
                TimeUnit.SECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    companion object {
        const val SYNC_WORK_NAME = "fluxboard_sync_pending_clips"
        private const val MIN_BACKOFF_SECONDS = 30L
    }
}
