package com.fluxboard.app.domain.repository

import com.fluxboard.app.domain.models.ClipItem
import kotlinx.coroutines.flow.Flow

/**
 * Contrato que debe implementar la capa de datos para la gestión de recortes.
 *
 * La UI del teclado y la app principal consumen exclusivamente esta abstracción,
 * de modo que el dominio desconoce por completo Room, Supabase y Cloudflare.
 */
interface IClipRepository {

    /** Retorna un flujo reactivo para actualizar el teclado en tiempo real. */
    fun getRecentClips(limit: Int): Flow<List<ClipItem>>

    /** Guarda localmente y encola para sincronización. */
    suspend fun saveClip(content: String): Result<Unit>

    /** Cambia el estado de un recorte a fijado/no fijado. */
    suspend fun togglePinStatus(clipId: String): Result<Unit>

    /** Fuerza sincronización manual o programada. */
    suspend fun syncPendingClips(): Result<Unit>
}
