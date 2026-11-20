package com.example.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SubscriptionPlanSpec(
    val productId: String,
    val title: String,
    val priceFallback: String,
    val creditsLabel: String,
    val badge: String
)

class BillingManager(private val context: Context) : PurchasesUpdatedListener {
    companion object {
        private const val TAG = "BillingManager"
        val PLANS = listOf(
            SubscriptionPlanSpec(
                productId = "ai_india_weekly",
                title = "Weekly Premium",
                priceFallback = "₹99 / week",
                creditsLabel = "500 AI Credits + Ad-Free",
                badge = "Starter"
            ),
            SubscriptionPlanSpec(
                productId = "ai_india_monthly",
                title = "Monthly Premium",
                priceFallback = "₹299 / month",
                creditsLabel = "2,000 AI Credits + Ad-Free + Priority",
                badge = "Most Popular"
            ),
            SubscriptionPlanSpec(
                productId = "ai_india_yearly",
                title = "Yearly Premium",
                priceFallback = "₹2,499 / year",
                creditsLabel = "25,000 AI Credits + All Pro Tools",
                badge = "Best Value"
            )
        )
    }

    private val _productDetails = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetails: StateFlow<Map<String, ProductDetails>> = _productDetails.asStateFlow()

    private val _activePurchaseIds = MutableStateFlow<Set<String>>(emptySet())
    val activePurchaseIds: StateFlow<Set<String>> = _activePurchaseIds.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    fun startConnection() {
        if (billingClient.isReady) {
            querySubscriptions()
            restorePurchases()
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    querySubscriptions()
                    restorePurchases()
                } else {
                    Log.w(TAG, "Billing setup result: ${result.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
            }
        })
    }

    private fun querySubscriptions() {
        val products = PLANS.map { plan ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(plan.productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(products).build()
        billingClient.queryProductDetailsAsync(params) { result, detailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _productDetails.value = detailsList.associateBy { it.productId }
            }
        }
    }

    fun launchPurchase(activity: Activity, productId: String) {
        if (!billingClient.isReady) {
            _statusMessage.value = "Google Play Billing is unavailable on this device or Play Store account."
            return
        }
        val details = _productDetails.value[productId]
        val offerToken = details?.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (details == null || offerToken == null) {
            _statusMessage.value = "Subscription SKU '$productId' is not configured in Google Play Console yet."
            return
        }
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _statusMessage.value = "Play Billing error: ${result.debugMessage}"
        }
    }

    fun restorePurchases() {
        if (!billingClient.isReady) {
            _statusMessage.value = "Connect to Google Play Store to restore active subscriptions."
            return
        }
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
                _statusMessage.value = if (purchases.isEmpty()) {
                    "No active Google Play subscriptions found for this account."
                } else {
                    "Subscriptions synced with Google Play."
                }
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) handlePurchases(purchases)
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _statusMessage.value = "Purchase cancelled."
            }
            else -> {
                _statusMessage.value = "Billing status: ${result.debugMessage}"
            }
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        val activeIds = mutableSetOf<String>()
        purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }.forEach { purchase ->
            activeIds.addAll(purchase.products)
            if (!purchase.isAcknowledged) {
                val ackParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(ackParams) { ackResult ->
                    Log.d(TAG, "Acknowledge result: ${ackResult.responseCode}")
                }
            }
        }
        _activePurchaseIds.value = activeIds
    }

    fun openManageSubscriptions(context: Context) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/account/subscriptions")
        )
        context.startActivity(intent)
    }

    fun clearStatus() {
        _statusMessage.value = null
    }

    fun endConnection() {
        if (billingClient.isReady) billingClient.endConnection()
    }
}
