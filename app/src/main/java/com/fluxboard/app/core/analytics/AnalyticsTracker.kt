package com.fluxboard.app.core.analytics

/**
 * Contrato de analíticas de producto de FluxBoard.
 *
 * REGLA DE CERO TEXTO (inquebrantable): ninguna implementación ni caso de uso
 * puede enviar el `textContent` de un recorte —ni el contenido del portapapeles—
 * a un servidor de rastreo, ni escribirlo en un log. Esta interfaz deliberadamente
 * solo acepta identificadores de evento y, como mucho, el `userId` opaco.
 */
interface AnalyticsTracker {

    /** Registra un evento de embudo anónimo. */
    fun track(event: AnalyticsEvent)

    /**
     * Asocia los eventos siguientes con un identificador de usuario opaco
     * (el uid de Supabase Auth). Nunca se pasa contenido de recortes aquí.
     */
    fun identify(userId: String)
}

/**
 * Catálogo cerrado de eventos del embudo. Nombres exactos de producto.
 */
enum class AnalyticsEvent(val eventName: String) {
    OnboardingStarted("Onboarding_Started"),
    PaywallViewed("Paywall_Viewed"),
    KeyboardActivated("Keyboard_Activated"),
    CopyInterceptedSuccess("Copy_Intercepted_Success")
}
