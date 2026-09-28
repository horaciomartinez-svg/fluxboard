package com.fluxboard.app.data.repository

import com.fluxboard.app.domain.models.SubscriptionTier
import com.fluxboard.app.domain.models.UserSubscription
import com.fluxboard.app.domain.repository.IAuthRepository
import com.fluxboard.app.domain.repository.ISubscriptionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de autenticación sobre Supabase Auth (auth-kt).
 *
 * FluxBoard es Offline-First: el primer arranque inicia una sesión ANÓNIMA
 * silenciosa para que el SyncWorker y el muro de pago dispongan de un `uid`
 * estable antes de cualquier operación remota. No se solicita ningún dato
 * personal en este flujo.
 *
 * El estado de suscripción se delega por completo a [ISubscriptionRepository]
 * (RevenueCat), manteniendo Supabase como proveedor de identidad.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val subscriptionRepository: ISubscriptionRepository
) : IAuthRepository {

    override fun getCurrentUserId(): String? =
        supabaseClient.auth.currentUserOrNull()?.id

    override fun observeSubscription(): Flow<UserSubscription> =
        subscriptionRepository.tierFlow.map { tier ->
            UserSubscription(
                tier = tier,
                isActive = tier == SubscriptionTier.PRO
            )
        }

    override suspend fun getSubscriptionTier(): SubscriptionTier =
        subscriptionRepository.tierFlow.value

    override suspend fun signInAnonymously(): Result<String> = runCatching {
        getCurrentUserId()?.let { return@runCatching it }

        supabaseClient.auth.signInAnonymously()

        getCurrentUserId()
            ?: error("Supabase no devolvió un usuario tras el inicio de sesión anónimo")
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        supabaseClient.auth.signOut()
    }
}
