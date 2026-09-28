package com.fluxboard.app.domain.usecases

import com.fluxboard.app.domain.repository.IClipRepository
import javax.inject.Inject

/**
 * Caso de uso: persiste un nuevo recorte en la base de datos local de forma
 * síncrona (Offline-First). La subida a la nube la realiza WorkManager.
 */
class SaveClipUseCase @Inject constructor(
    private val clipRepository: IClipRepository
) {
    suspend operator fun invoke(content: String): Result<Unit> {
        if (content.isBlank()) {
            return Result.success(Unit)
        }
        return clipRepository.saveClip(content)
    }
}
