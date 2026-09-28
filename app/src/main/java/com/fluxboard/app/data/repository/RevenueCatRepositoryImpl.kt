package com.fluxboard.app.data.repository

import com.fluxboard.app.core.utils.ActivityProvider
import com.fluxboard.app.domain.models.SubscriptionTier
import com.fluxboard.app.domain.repository.ISubscriptionRepository
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.LogInCallback
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.models.StoreTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Implementación de monetización sobre la SDK de RevenueCat.
 *
 * Traduce el estado de RevenueCat a [SubscriptionTier] del dominio y encapsula
 * los callbacks de la SDK en funciones suspend que devuelven [Result].
 */
@Singleton
class RevenueCatRepositoryImpl @Inject constructor(
    private val activityProvider: ActivityProvider
) : ISubscriptionRepository {

    private val _tierFlow = MutableStateFlow(SubscriptionTier.FREE)
    override val tierFlow: StateFlow<SubscriptionTier> = _tierFlow.asStateFlow()

    @Volatile
    private var cachedAnnualPackage: Package? = null

    private val purchases: Purchases?
        get() = if (Purchases.isConfigured) Purchases.sharedInstance else null

    override suspend fun loadOfferings(): Result<Unit> = withContext(Dispatchers.Main) {
        val purchases = purchases ?: return@withContext Result.failure(notConfigured())
        suspendCancellableCoroutine { continuation ->
            purchases.getOfferings(object : ReceiveOfferingsCallback {
                override fun onReceived(offerings: Offerings) {
                    cachedAnnualPackage = offerings.current?.annual
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }

                override fun onError(error: PurchasesError) {
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(IllegalStateException(error.message)))
                    }
                }
            })
        }
    }

    override fun annualPriceFormatted(): String? =
        cachedAnnualPackage?.product?.price?.formatted

    override suspend fun purchaseAnnual(): Result<Unit> = withContext(Dispatchers.Main) {
        val purchases = purchases ?: return@withContext Result.failure(notConfigured())
        val activity = activityProvider.get()
            ?: return@withContext Result.failure(IllegalStateException("No hay Activity en primer plano"))
        val annualPackage = cachedAnnualPackage
            ?: return@withContext Result.failure(IllegalStateException("Plan anual no disponible"))

        suspendCancellableCoroutine { continuation ->
            purchases.purchase(
                PurchaseParams.Builder(activity, annualPackage).build(),
                object : PurchaseCallback {
                    override fun onCompleted(
                        storeTransaction: StoreTransaction,
                        customerInfo: CustomerInfo
                    ) {
                        _tierFlow.value = customerInfo.toTier()
                        if (continuation.isActive) continuation.resume(Result.success(Unit))
                    }

                    override fun onError(error: PurchasesError, userCancelled: Boolean) {
                        val message = if (userCancelled) "Compra cancelada" else error.message
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(IllegalStateException(message)))
                        }
                    }
                }
            )
        }
    }

    override suspend fun restorePurchases(): Result<Unit> = withContext(Dispatchers.Main) {
        val purchases = purchases ?: return@withContext Result.failure(notConfigured())
        suspendCancellableCoroutine { continuation ->
            purchases.restorePurchases(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    _tierFlow.value = customerInfo.toTier()
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }

                override fun onError(error: PurchasesError) {
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(IllegalStateException(error.message)))
                    }
                }
            })
        }
    }

    override suspend fun identifyUser(userId: String): Result<Unit> =
        withContext(Dispatchers.Main) {
            val purchases = purchases ?: return@withContext Result.failure(notConfigured())
            if (purchases.appUserID == userId) return@withContext Result.success(Unit)
            suspendCancellableCoroutine { continuation ->
                purchases.logIn(userId, object : LogInCallback {
                    override fun onReceived(customerInfo: CustomerInfo, created: Boolean) {
                        _tierFlow.value = customerInfo.toTier()
                        if (continuation.isActive) continuation.resume(Result.success(Unit))
                    }

                    override fun onError(error: PurchasesError) {
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(IllegalStateException(error.message)))
                        }
                    }
                })
            }
        }

    override suspend fun refreshCustomerInfo(): Result<SubscriptionTier> =
        withContext(Dispatchers.Main) {
            val purchases = purchases ?: return@withContext Result.failure(notConfigured())
            suspendCancellableCoroutine { continuation ->
                purchases.getCustomerInfo(object : ReceiveCustomerInfoCallback {
                    override fun onReceived(customerInfo: CustomerInfo) {
                        val tier = customerInfo.toTier()
                        _tierFlow.value = tier
                        if (continuation.isActive) continuation.resume(Result.success(tier))
                    }

                    override fun onError(error: PurchasesError) {
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(IllegalStateException(error.message)))
                        }
                    }
                })
            }
        }

    private fun CustomerInfo.toTier(): SubscriptionTier =
        if (entitlements.active.isNotEmpty()) SubscriptionTier.PRO else SubscriptionTier.FREE

    private fun notConfigured(): Throwable =
        IllegalStateException("RevenueCat no está configurado (API key ausente)")
}
