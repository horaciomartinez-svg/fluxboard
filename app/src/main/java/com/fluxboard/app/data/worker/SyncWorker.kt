package com.fluxboard.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fluxboard.app.core.analytics.AnalyticsTracker
import com.fluxboard.app.core.security.ClipCipher
import com.fluxboard.app.data.local.ClipDao
import com.fluxboard.app.data.local.ClipEntity
import com.fluxboard.app.data.remote.CloudflareSource
import com.fluxboard.app.data.remote.SupabaseSource
import com.fluxboard.app.data.remote.dto.ClipMetadataDto
import com.fluxboard.app.domain.repository.ISubscriptionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Ejecuta la sincronización remota de los recortes pendientes.
 *
 * Orden por recorte:
 *  1. Cifrar el texto (AES-256/GCM, AndroidKeyStore).
 *  2. Subir el texto cifrado a Cloudflare KV con la clave `clip:{user_id}:{id}`.
 *  3. Insertar la metadata (sin texto) en Supabase `clip_metadata`.
 *  4. Marcar el recorte como sincronizado en Room.
 *
 * Cualquier fallo de red devuelve [Result.retry] para que WorkManager reintente
 * con backoff exponencial. Un fallo irrecuperable (por ejemplo, cifrado) marca el
 * trabajo como fallido para no reintentar indefinidamente.
 *
 * Privacidad: este Worker jamás registra ni transmite el texto en claro.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val clipDao: ClipDao,
    private val cloudflareSource: CloudflareSource,
    private val supabaseSource: SupabaseSource,
    private val clipCipher: ClipCipher,
    private val analyticsTracker: AnalyticsTracker,
    private val subscriptionRepository: ISubscriptionRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val pendingClips = clipDao.getPendingSyncClips()
        if (pendingClips.isEmpty()) return Result.success()

        val userId = supabaseSource.currentUserId() ?: return retryOrGiveUp()

        linkUserIdentity(userId)

        for (clip in pendingClips) {
            when (syncClip(clip, userId)) {
                SyncOutcome.Synced -> Unit
                SyncOutcome.Retry -> return retryOrGiveUp()
                SyncOutcome.Fatal -> return Result.failure()
            }
        }

        return Result.success()
    }

    /**
     * Enlaza el uid de Supabase Auth con RevenueCat y PostHog. Es el equivalente
     * en runtime de `setAppUserID`, necesario para atribuir correctamente el LTV.
     */
    private suspend fun linkUserIdentity(userId: String) {
        runCatching { analyticsTracker.identify(userId) }
        runCatching { subscriptionRepository.identifyUser(userId) }
    }

    private suspend fun syncClip(clip: ClipEntity, userId: String): SyncOutcome {
        val encryptedText = runCatching { clipCipher.encrypt(clip.textContent) }
            .getOrElse { return SyncOutcome.Fatal }
        val key = buildKvKey(userId, clip.id.toString())

        val upload = cloudflareSource.uploadClip(key = key, encryptedText = encryptedText)
        if (upload.isFailure) return SyncOutcome.Retry

        val metadata = ClipMetadataDto(
            cloudflareKey = key,
            isPinned = clip.isPinned,
            contentHash = clipCipher.sha256(clip.textContent),
            sizeBytes = clipCipher.sizeBytes(clip.textContent)
        )
        val insert = supabaseSource.insertClipMetadata(metadata)
        if (insert.isFailure) return SyncOutcome.Retry

        clipDao.updateSyncStatus(clipId = clip.id, isSynced = true)
        return SyncOutcome.Synced
    }

    private fun retryOrGiveUp(): Result =
        if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()

    private fun buildKvKey(userId: String, clipId: String): String = "clip:$userId:$clipId"

    private enum class SyncOutcome { Synced, Retry, Fatal }

    private companion object {
        const val MAX_ATTEMPTS = 5
    }
}
