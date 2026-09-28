package com.fluxboard.app.domain.repository

import com.fluxboard.app.domain.models.SubscriptionTier
import com.fluxboard.app.domain.models.UserSubscription
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de autenticación y estado de suscripción del usuario.
 *
 * Abstrae Supabase (identidad) y RevenueCat (estado Pro) de la capa de dominio.
 */
interface IAuthRepository {

    /** Identificador del usuario autenticado, o null si no hay sesión. */
    fun getCurrentUserId(): String?

    /** Flujo reactivo con el estado de suscripción del usuario. */
    fun observeSubscription(): Flow<UserSubscription>

    /** Retorna el nivel de suscripción actual de forma puntual. */
    suspend fun getSubscriptionTier(): SubscriptionTier

    /** Inicia sesión anónima (primer arranque offline-first). */
    suspend fun signInAnonymously(): Result<String>

    /** Cierra la sesión activa. */
    suspend fun signOut(): Result<Unit>
}
