package com.fluxboard.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Fila de la tabla `clip_metadata` en Supabase.
 *
 * IMPORTANTE: este registro contiene EXCLUSIVAMENTE metadata. El texto real del
 * recorte nunca se persiste en Supabase; vive cifrado en Cloudflare KV y en
 * claro, solo, en la base de datos local del dispositivo.
 */
@Serializable
data class ClipMetadataDto(
    @SerialName("cloudflare_key") val cloudflareKey: String,
    @SerialName("is_pinned") val isPinned: Boolean,
    @SerialName("content_hash") val contentHash: String,
    @SerialName("size_bytes") val sizeBytes: Int
)
