package com.fluxboard.app.domain.repository

import com.fluxboard.app.domain.models.SubscriptionTier
import kotlinx.coroutines.flow.StateFlow

/**
 * Contrato de monetización (RevenueCat).
 *
 * Expone el estado de suscripción de forma reactiva y las operaciones de compra
 * y restauración. La capa de dominio desconoce por completo la SDK de RevenueCat.
 */
interface ISubscriptionRepository {

    /** Estado reactivo del nivel de suscripción del usuario. */
    val tierFlow: StateFlow<SubscriptionTier>

    /** Descarga y cachea las ofertas configuradas en RevenueCat. */
    suspend fun loadOfferings(): Result<Unit>

    /** Precio formateado del plan anual, o null si aún no se cargaron las ofertas. */
    fun annualPriceFormatted(): String?

    /** Inicia la compra del plan anual (prueba de 7 días). */
    suspend fun purchaseAnnual(): Result<Unit>

    /** Restaura compras previas del usuario. */
    suspend fun restorePurchases(): Result<Unit>

    /**
     * Enlaza el `appUserID` de RevenueCat con el identificador único del usuario
     * (uid de Supabase Auth). Es el equivalente en tiempo de ejecución de
     * `setAppUserID`: permite trazar correctamente el LTV y la conversión.
     */
    suspend fun identifyUser(userId: String): Result<Unit>

    /** Refresca la información del cliente y devuelve el nivel vigente. */
    suspend fun refreshCustomerInfo(): Result<SubscriptionTier>
}
