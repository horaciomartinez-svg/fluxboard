package com.fluxboard.app.presentation.keyboard

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import com.fluxboard.app.core.analytics.AnalyticsEvent
import com.fluxboard.app.core.analytics.AnalyticsTracker
import com.fluxboard.app.domain.models.ClipItem
import com.fluxboard.app.domain.usecases.GetClipsUseCase
import com.fluxboard.app.domain.usecases.SaveClipUseCase
import com.fluxboard.app.presentation.keyboard.components.FluxKeyboardComposeView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Servicio central de FluxBoard.
 *
 * A partir de Android 10 el acceso al portapapeles en segundo plano está
 * restringido, por lo que la recolección de datos ocurre bajo el control
 * directo del usuario al abrir el teclado ([onStartInputView]).
 *
 * Toda la persistencia es síncrona contra Room (Offline-First); la UI es
 * Jetpack Compose montada en la ventana del IME.
 */
@AndroidEntryPoint
class FluxBoardKeyboard : InputMethodService() {

    @Inject
    lateinit var saveClipUseCase: SaveClipUseCase

    @Inject
    lateinit var getClipsUseCase: GetClipsUseCase

    @Inject
    lateinit var analyticsTracker: AnalyticsTracker

    private val keyboardScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val clipsState = MutableStateFlow<List<ClipItem>>(emptyList())

    private var lastProcessedClip: String? = null

    override fun onCreate() {
        super.onCreate()
        observeClips()
    }

    override fun onCreateInputView(): View {
        return FluxKeyboardComposeView(
            context = this,
            clips = clipsState,
            onClipSelected = ::commitClip
        )
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        captureClipboard()
    }

    /**
     * Lee el portapapeles primario y persiste el texto en Room de forma
     * síncrona. Se ejecuta únicamente cuando el teclado está visible.
     */
    private fun captureClipboard() {
        val clipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return

        val clipData = runCatching { clipboardManager.primaryClip }.getOrNull() ?: return

        if (clipData.itemCount == 0) return
        if (isSensitive(clipData.description)) return

        val text = runCatching { clipData.getItemAt(0).coerceToText(this)?.toString() }
            .getOrNull()

        if (text.isNullOrBlank()) return
        if (text == lastProcessedClip) return

        lastProcessedClip = text
        keyboardScope.launch {
            saveClipUseCase(text).onSuccess {
                analyticsTracker.track(AnalyticsEvent.CopyInterceptedSuccess)
            }
        }
    }

    /** Descarta contenido marcado como sensible (p. ej. contraseñas). */
    private fun isSensitive(description: ClipDescription?): Boolean {
        val extras = description?.extras ?: return false
        return extras.getBoolean(EXTRA_IS_SENSITIVE, false)
    }

    /** Observa Room y refleja los cambios en la UI del teclado en tiempo real. */
    private fun observeClips() {
        keyboardScope.launch {
            getClipsUseCase().collect { clips ->
                clipsState.value = clips
            }
        }
    }

    /** Inyecta el texto seleccionado en el campo activo de la app destino. */
    private fun commitClip(clip: ClipItem) {
        currentInputConnection?.commitText(clip.textContent, 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardScope.cancel()
    }

    private companion object {
        const val EXTRA_IS_SENSITIVE = "android.content.extra.IS_SENSITIVE"
    }
}
