package com.fluxboard.app.presentation.app.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fluxboard.app.domain.usecases.SignInAnonymouslyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel del onboarding.
 *
 * Al abrir la app por primera vez dispara, en segundo plano, el inicio de
 * sesión anónimo con Supabase. De este modo, cuando el usuario alcance el muro
 * de pago y el SyncWorker, el `uid` ya estará disponible. El fallo es silencioso
 * (Offline-First): la app sigue funcionando y el Worker reintentará más tarde.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val signInAnonymously: SignInAnonymouslyUseCase
) : ViewModel() {

    /** Lanza la autenticación anónima silenciosa. Idempotente. */
    fun ensureAnonymousSession() {
        viewModelScope.launch {
            signInAnonymously()
        }
    }
}
