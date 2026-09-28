package com.fluxboard.app.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Carga útil enviada al Worker de Cloudflare que escribe en KV.
 *
 * `value` es SIEMPRE texto cifrado (AES-256/GCM); jamás el contenido en claro.
 * La `key` sigue el patrón `clip:{user_id}:{clip_metadata_id}`.
 */
@Serializable
data class CloudflareClipPayload(
    @SerialName("key") val key: String,
    @SerialName("value") val value: String
)
