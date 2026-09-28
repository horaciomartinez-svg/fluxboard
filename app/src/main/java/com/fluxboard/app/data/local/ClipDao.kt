package com.fluxboard.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Data Access Object de los recortes.
 *
 * Todas las operaciones se ejecutan sobre la base de datos local (Room), lo que
 * garantiza la estrategia Offline-First: el teclado lee/escribe aquí de forma
 * inmediata y la sincronización ocurre en segundo plano vía WorkManager.
 */
@Dao
interface ClipDao {

    /** Inserta (o reemplaza) un recorte. Devuelve el rowId generado. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClip(clip: ClipEntity): Long

    /** Actualiza el estado de fijación de un recorte. */
    @Query("UPDATE clips SET is_pinned = :isPinned WHERE id = :clipId")
    suspend fun updatePinStatus(clipId: UUID, isPinned: Boolean)

    /** Actualiza el estado de sincronización de un recorte. */
    @Query("UPDATE clips SET is_synced = :isSynced WHERE id = :clipId")
    suspend fun updateSyncStatus(clipId: UUID, isSynced: Boolean)

    /** Flujo reactivo con los recortes más recientes, ordenados de forma descendente. */
    @Query("SELECT * FROM clips ORDER BY created_at DESC LIMIT :limit")
    fun getRecentClips(limit: Int): Flow<List<ClipEntity>>

    /** Obtiene un recorte puntual por su identificador. */
    @Query("SELECT * FROM clips WHERE id = :clipId LIMIT 1")
    suspend fun getClipById(clipId: UUID): ClipEntity?

    /** Recortes pendientes de sincronización (para SyncWorker). */
    @Query("SELECT * FROM clips WHERE is_synced = 0 ORDER BY created_at ASC")
    suspend fun getPendingSyncClips(): List<ClipEntity>

    /** Indica si ya existe un recorte con el mismo contenido (evita duplicados). */
    @Query("SELECT EXISTS(SELECT 1 FROM clips WHERE text_content = :content LIMIT 1)")
    suspend fun existsByContent(content: String): Boolean

    /** Elimina un recorte por su identificador. */
    @Query("DELETE FROM clips WHERE id = :clipId")
    suspend fun deleteClip(clipId: UUID)
}
