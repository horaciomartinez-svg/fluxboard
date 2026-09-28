package com.fluxboard.app.data.repository

import com.fluxboard.app.data.local.ClipDao
import com.fluxboard.app.data.local.ClipEntity
import com.fluxboard.app.data.local.toDomain
import com.fluxboard.app.data.worker.SyncScheduler
import com.fluxboard.app.domain.models.ClipItem
import com.fluxboard.app.domain.repository.IClipRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación local (Room) del repositorio de recortes.
 *
 * Garantiza la estrategia Offline-First: toda lectura/escritura es local e
 * inmediata. La sincronización remota con Supabase/Cloudflare se completa en la
 * infraestructura de WorkManager (fase de sincronización).
 */
@Singleton
class ClipRepositoryImpl @Inject constructor(
    private val clipDao: ClipDao,
    private val syncScheduler: SyncScheduler
) : IClipRepository {

    override fun getRecentClips(limit: Int): Flow<List<ClipItem>> =
        clipDao.getRecentClips(limit).map { entities ->
            entities.map(ClipEntity::toDomain)
        }

    override suspend fun saveClip(content: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (clipDao.existsByContent(content)) return@runCatching
            clipDao.insertClip(
                ClipEntity(
                    id = UUID.randomUUID(),
                    textContent = content,
                    isPinned = false,
                    isSynced = false
                )
            )
            syncScheduler.enqueueOneTimeSync()
        }
    }

    override suspend fun togglePinStatus(clipId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val uuid = UUID.fromString(clipId)
                val current = clipDao.getClipById(uuid)
                    ?: error("Clip not found: $clipId")
                clipDao.updatePinStatus(clipId = uuid, isPinned = !current.isPinned)
            }
        }

    override suspend fun syncPendingClips(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { syncScheduler.enqueueOneTimeSync() }
    }
}
