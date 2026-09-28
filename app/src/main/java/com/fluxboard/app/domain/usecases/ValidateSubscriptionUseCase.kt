package com.fluxboard.app.domain.usecases

import com.fluxboard.app.domain.models.SubscriptionTier
import com.fluxboard.app.domain.repository.IAuthRepository
import javax.inject.Inject

/**
 * Caso de uso: determina el nivel de suscripción efectivo del usuario.
 *
 * Se apoya en RevenueCat (vía [IAuthRepository]) para habilitar las funciones
 * Pro (historial ilimitado, sincronización en la nube y organización avanzada).
 */
class ValidateSubscriptionUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(): SubscriptionTier =
        authRepository.getSubscriptionTier()

    fun isPro(tier: SubscriptionTier): Boolean = tier != SubscriptionTier.FREE
}
