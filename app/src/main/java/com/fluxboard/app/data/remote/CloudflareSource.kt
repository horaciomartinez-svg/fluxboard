package com.fluxboard.app.data.remote

import com.fluxboard.app.BuildConfig
import com.fluxboard.app.data.remote.dto.CloudflareClipPayload
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Capa de datos remota para el almacén de objetos de Cloudflare (Workers + KV).
 *
 * Sube la carga útil CIFRADA del recorte. La clave KV sigue estrictamente el
 * patrón `clip:{user_id}:{clip_metadata_id}`.
 */
@Singleton
class CloudflareSource @Inject constructor(
    private val httpClient: HttpClient
) {

    /**
     * Sube [encryptedText] bajo [key]. Devuelve [Result.success] solo si el
     * Worker confirma con HTTP 200; cualquier otro estado o fallo de red se
     * traduce en [Result.failure] para que el Worker de sincronización reintente.
     */
    suspend fun uploadClip(key: String, encryptedText: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val baseUrl = BuildConfig.CLOUDFLARE_WORKER_URL
                require(baseUrl.isNotBlank()) { "CLOUDFLARE_WORKER_URL no configurada" }

                val response = httpClient.put("${baseUrl.trimEnd('/')}/clip") {
                    contentType(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer ${BuildConfig.CLOUDFLARE_API_TOKEN}")
                    setBody(CloudflareClipPayload(key = key, value = encryptedText))
                }

                check(response.status == HttpStatusCode.OK) {
                    "Cloudflare respondió con HTTP ${response.status.value}"
                }
            }.map { Unit }
        }
}
