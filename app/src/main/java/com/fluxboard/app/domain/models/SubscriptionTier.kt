package com.fluxboard.app.domain.models

/**
 * Nivel de suscripción del usuario dentro de FluxBoard.
 *
 * Modelo puro de Kotlin: agnóstico al framework y a RevenueCat/Supabase.
 */
enum class SubscriptionTier {
    FREE,
    PRO,
    TRIAL
}
