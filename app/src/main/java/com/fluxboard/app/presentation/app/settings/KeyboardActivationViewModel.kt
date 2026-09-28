package com.fluxboard.app.presentation.app.settings

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModel
import com.fluxboard.app.core.analytics.AnalyticsEvent
import com.fluxboard.app.core.analytics.AnalyticsTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class KeyboardActivationUiState(
    val isKeyboardEnabled: Boolean = false
)

/**
 * ViewModel del modal de activación. Consulta al [InputMethodManager] si
 * FluxBoard ya figura entre los métodos de entrada habilitados.
 */
@HiltViewModel
class KeyboardActivationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(KeyboardActivationUiState())
    val uiState: StateFlow<KeyboardActivationUiState> = _uiState.asStateFlow()

    init {
        refreshKeyboardState()
    }

    fun refreshKeyboardState() {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

        val isEnabled = inputMethodManager?.enabledInputMethodList
            ?.any { it.packageName == context.packageName } == true

        val wasEnabled = _uiState.value.isKeyboardEnabled
        _uiState.update { it.copy(isKeyboardEnabled = isEnabled) }

        if (isEnabled && !wasEnabled) {
            analyticsTracker.track(AnalyticsEvent.KeyboardActivated)
        }
    }
}
