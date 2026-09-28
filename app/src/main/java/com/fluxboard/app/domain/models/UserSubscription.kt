package com.fluxboard.app.domain.models

import java.util.Date

/**
 * Estado consolidado de la suscripción del usuario.
 *
 * Modelo puro de Kotlin: no contiene identificadores de RevenueCat ni de Supabase.
 */
data class UserSubscription(
    val tier: SubscriptionTier = SubscriptionTier.FREE,
    val isActive: Boolean = false,
    val expiresAt: Date? = null
) {
    val isPro: Boolean
        get() = tier == SubscriptionTier.PRO && isActive
}
