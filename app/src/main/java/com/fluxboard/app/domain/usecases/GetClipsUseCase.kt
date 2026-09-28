package com.fluxboard.app.domain.usecases

import com.fluxboard.app.domain.models.ClipItem
import com.fluxboard.app.domain.repository.IClipRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso: expone de forma reactiva los recortes más recientes almacenados
 * en Room, para que el teclado y la app se actualicen en tiempo real.
 */
class GetClipsUseCase @Inject constructor(
    private val clipRepository: IClipRepository
) {
    operator fun invoke(limit: Int = DEFAULT_LIMIT): Flow<List<ClipItem>> =
        clipRepository.getRecentClips(limit)

    companion object {
        const val DEFAULT_LIMIT = 200
    }
}
