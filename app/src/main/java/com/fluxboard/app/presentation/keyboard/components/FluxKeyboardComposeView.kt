package com.fluxboard.app.presentation.keyboard.components

import android.content.Context
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.AbstractComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.fluxboard.app.domain.models.ClipItem
import kotlinx.coroutines.flow.StateFlow

/**
 * Puente entre el [android.inputmethodservice.InputMethodService] y Jetpack
 * Compose. Envuelve la UI del teclado en un [AbstractComposeView] que se acopla
 * a la ventana del servicio.
 *
 * La lista de recortes llega como [StateFlow] para que el teclado se actualice
 * en tiempo real en cuanto Room emite un cambio.
 *
 * La ventana de un IME no es un árbol con [LifecycleOwner], por lo que Compose
 * no puede resolver su recomposer y lanza
 * `IllegalStateException: ViewTreeLifecycleOwner not found`. Esta vista actúa
 * como dueña de ciclo de vida y publica las tres dependencias del árbol de
 * vistas ([LifecycleOwner], [ViewModelStoreOwner] y [SavedStateRegistryOwner])
 * sobre la raíz de la ventana.
 */
class FluxKeyboardComposeView(
    context: Context,
    private val clips: StateFlow<List<ClipItem>>,
    private val onClipSelected: (ClipItem) -> Unit,
    private val onShowInputMethodPicker: () -> Unit
) : AbstractComposeView(context),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    init {
        savedStateController.performRestore(null)
        installViewTreeOwners(this)
    }

    override fun onAttachedToWindow() {
        installViewTreeOwners(rootView)
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    private fun installViewTreeOwners(view: View) {
        view.setViewTreeLifecycleOwner(this)
        view.setViewTreeViewModelStoreOwner(this)
        view.setViewTreeSavedStateRegistryOwner(this)
    }

    @Composable
    override fun Content() {
        val items by clips.collectAsState()
        FluxKeyboardScreen(
            clips = items,
            onClipSelected = onClipSelected,
            onShowInputMethodPicker = onShowInputMethodPicker
        )
    }
}
