package com.fluxboard.app.presentation.keyboard.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.AbstractComposeView
import com.fluxboard.app.domain.models.ClipItem
import kotlinx.coroutines.flow.StateFlow

/**
 * Puente entre el [android.inputmethodservice.InputMethodService] y Jetpack
 * Compose. Envuelve la UI del teclado en un [AbstractComposeView] que se acopla
 * a la ventana del servicio.
 *
 * La lista de recortes llega como [StateFlow] para que el teclado se actualice
 * en tiempo real en cuanto Room emite un cambio.
 */
class FluxKeyboardComposeView(
    context: Context,
    private val clips: StateFlow<List<ClipItem>>,
    private val onClipSelected: (ClipItem) -> Unit
) : AbstractComposeView(context) {

    @Composable
    override fun Content() {
        val items by clips.collectAsState()
        FluxKeyboardScreen(
            clips = items,
            onClipSelected = onClipSelected
        )
    }
}
