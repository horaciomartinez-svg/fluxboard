package com.fluxboard.app.core.utils

import android.app.Activity
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provee la Activity en primer plano sin acoplar la capa de dominio a Android.
 *
 * La app principal actualiza la referencia en [set] y la limpia en [clear];
 * las implementaciones de pagos (RevenueCat) la consultan para iniciar el flujo
 * de compra.
 */
@Singleton
class ActivityProvider @Inject constructor() {

    private var activityReference: WeakReference<Activity>? = null

    fun set(activity: Activity) {
        activityReference = WeakReference(activity)
    }

    fun get(): Activity? = activityReference?.get()

    fun clear() {
        activityReference = null
    }
}
