package com.fluxboard.app.domain.usecases

import com.fluxboard.app.domain.repository.IAuthRepository
import javax.inject.Inject

/**
 * Caso de uso: garantiza una sesión anónima de Supabase Auth.
 *
 * Se invoca en el primer arranque (onboarding) para que exista un `uid` antes
 * de llegar al muro de pago y al [com.fluxboard.app.data.worker.SyncWorker].
 * Es idempotente: si ya hay sesión, devuelve el identificador existente sin
 * crear una nueva identidad.
 */
class SignInAnonymouslyUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(): Result<String> =
        authRepository.signInAnonymously()
}
