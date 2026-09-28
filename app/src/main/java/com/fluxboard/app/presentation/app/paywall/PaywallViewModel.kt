package com.fluxboard.app.presentation.app.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fluxboard.app.core.analytics.AnalyticsEvent
import com.fluxboard.app.core.analytics.AnalyticsTracker
import com.fluxboard.app.domain.models.SubscriptionTier
import com.fluxboard.app.domain.repository.ISubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaywallUiState(
    val isLoading: Boolean = false,
    val isPro: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel del muro de pago. Orquesta la carga de ofertas y la compra del
 * plan anual a través de [ISubscriptionRepository] (RevenueCat).
 */
@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val subscriptionRepository: ISubscriptionRepository,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaywallUiState())
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    init {
        analyticsTracker.track(AnalyticsEvent.PaywallViewed)
        observeTier()
        loadOfferings()
    }

    private fun observeTier() {
        viewModelScope.launch {
            subscriptionRepository.tierFlow.collect { tier ->
                _uiState.update { it.copy(isPro = tier == SubscriptionTier.PRO) }
            }
        }
    }

    private fun loadOfferings() {
        viewModelScope.launch {
            subscriptionRepository.loadOfferings()
        }
    }

    fun onPurchaseClicked(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            subscriptionRepository.purchaseAnnual()
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isPro = true) }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                }
        }
    }

    fun onRestoreClicked(onRestored: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            subscriptionRepository.restorePurchases()
                .onSuccess {
                    val tier = subscriptionRepository.refreshCustomerInfo().getOrNull()
                    _uiState.update {
                        it.copy(isLoading = false, isPro = tier == SubscriptionTier.PRO)
                    }
                    if (tier == SubscriptionTier.PRO) onRestored()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                }
        }
    }
}
