package com.fluxboard.app.presentation.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fluxboard.app.domain.models.SubscriptionTier
import com.fluxboard.app.domain.repository.IAuthRepository
import com.fluxboard.app.domain.repository.ISubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la pantalla de [SettingsScreen].
 *
 * [anonymizedUserId] es el `uid` de Supabase Auth ya truncado para mostrarse
 * (nunca se expone el identificador completo en la interfaz).
 */
data class SettingsUiState(
    val anonymizedUserId: String? = null,
    val tier: SubscriptionTier = SubscriptionTier.FREE,
    val isManagingSubscription: Boolean = false
) {
    val isPro: Boolean
        get() = tier == SubscriptionTier.PRO
}

/**
 * ViewModel de Ajustes.
 *
 * Expone la identidad anónima (Supabase) y el estado de suscripción (RevenueCat),
 * y delega la apertura del centro de gestión de suscripción al repositorio.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val subscriptionRepository: ISubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            anonymizedUserId = authRepository.getCurrentUserId()?.let(::truncateUid)
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSubscription()
    }

    private fun observeSubscription() {
        viewModelScope.launch {
            subscriptionRepository.tierFlow.collect { tier ->
                _uiState.update { it.copy(tier = tier) }
            }
        }
    }

    /** Abre el centro de gestión de la suscripción del usuario. */
    fun onManageSubscriptionClicked() {
        if (_uiState.value.isManagingSubscription) return
        viewModelScope.launch {
            _uiState.update { it.copy(isManagingSubscription = true) }
            subscriptionRepository.manageSubscription()
            _uiState.update { it.copy(isManagingSubscription = false) }
        }
    }

    private fun truncateUid(uid: String): String =
        if (uid.length <= UID_VISIBLE_CHARS) {
            uid
        } else {
            "${uid.take(UID_PREFIX)}…${uid.takeLast(UID_SUFFIX)}"
        }

    private companion object {
        const val UID_PREFIX = 8
        const val UID_SUFFIX = 4
        const val UID_VISIBLE_CHARS = UID_PREFIX + UID_SUFFIX
    }
}
