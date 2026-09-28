package com.fluxboard.app.data.analytics

import android.content.Context
import com.fluxboard.app.BuildConfig
import com.fluxboard.app.core.analytics.AnalyticsEvent
import com.fluxboard.app.core.analytics.AnalyticsTracker
import com.posthog.PostHog
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de producto con PostHog.
 *
 * Solo se envían nombres de evento y el `distinctId` opaco. Cumple la regla de
 * cero texto: no existe ningún punto de entrada que acepte el contenido de un
 * recorte.
 */
@Singleton
class PostHogAnalyticsTracker @Inject constructor(
    @ApplicationContext private val context: Context
) : AnalyticsTracker {

    private val isEnabled: Boolean = BuildConfig.POSTHOG_API_KEY.isNotBlank()

    init {
        if (isEnabled) {
            runCatching {
                PostHogAndroid.setup(
                    context,
                    PostHogAndroidConfig(apiKey = BuildConfig.POSTHOG_API_KEY)
                )
            }
        }
    }

    override fun track(event: AnalyticsEvent) {
        if (!isEnabled) return
        runCatching { PostHog.capture(event.eventName) }
    }

    override fun identify(userId: String) {
        if (!isEnabled) return
        runCatching { PostHog.identify(userId) }
    }
}
