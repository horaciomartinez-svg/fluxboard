package com.fluxboard.app.data.remote

import com.fluxboard.app.data.remote.dto.ClipMetadataDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Capa de datos remota para Supabase (PostgREST).
 *
 * Solo persiste METADATA de los recortes. El texto real NUNCA se escribe aquí:
 * su lugar es Cloudflare KV (cifrado) y la base local del dispositivo.
 */
@Singleton
class SupabaseSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {

    /** Identificador único del usuario autenticado (Supabase Auth), o null. */
    fun currentUserId(): String? = supabaseClient.auth.currentUserOrNull()?.id

    /** Inserta una fila de metadata en `clip_metadata`. */
    suspend fun insertClipMetadata(metadata: ClipMetadataDto): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                supabaseClient.postgrest
                    .from(CLIP_METADATA_TABLE)
                    .insert(metadata)
            }.map { Unit }
        }

    private companion object {
        const val CLIP_METADATA_TABLE = "clip_metadata"
    }
}
