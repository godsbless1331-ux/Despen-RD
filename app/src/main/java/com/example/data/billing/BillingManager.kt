package com.example.data.billing

import android.app.Activity
import android.content.Context
import android.widget.Toast
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
import com.example.data.local.MonetizationConstants
import com.example.data.local.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class BillingManager(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val scope: CoroutineScope
) : PurchasesUpdatedListener {

    private var billingClient: BillingClient? = null
    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private val _billingError = MutableStateFlow<String?>(null)
    val billingError: StateFlow<String?> = _billingError.asStateFlow()

    init {
        setupBillingClient()
    }

    private fun setupBillingClient() {
        try {
            billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder()
                        .enableOneTimeProducts()
                        .build()
                )
                .build()

            // Do NOT start connection automatically on init to avoid internet error messages in sandbox
            // startConnection()
        } catch (e: Exception) {
            _billingError.value = e.message
        }
    }

    fun startConnection() {
        val client = billingClient ?: return
        if (client.isReady) {
            queryProductDetails()
            queryPurchases()
            return
        }

        _isConnecting.value = true
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                _isConnecting.value = false
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                    queryPurchases()
                } else {
                    _billingError.value = "Billing connection code: ${billingResult.responseCode}"
                }
            }

            override fun onBillingServiceDisconnected() {
                _isConnecting.value = false
            }
        })
    }

    private fun queryProductDetails() {
        val client = billingClient ?: return
        if (!client.isReady) return

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(MonetizationConstants.PRO_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        client.queryProductDetailsAsync(params) { billingResult, queryProductDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = queryProductDetailsList.firstOrNull {
                    it.productId == MonetizationConstants.PRO_PRODUCT_ID
                }
                _productDetails.value = details
            }
        }
    }

    fun queryPurchases() {
        val client = billingClient ?: return
        if (!client.isReady) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        client.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(context, "Compra cancelada", Toast.LENGTH_SHORT).show()
        } else {
            _billingError.value = billingResult.debugMessage
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Verify and activate Pro permanently
            preferencesManager.setProUser(
                isPro = true,
                purchaseToken = purchase.purchaseToken,
                purchaseTime = purchase.purchaseTime
            )

            // Acknowledge purchase if not already acknowledged
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { ackResult ->
                    if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        scope.launch(Dispatchers.Main) {
                            Toast.makeText(context, "¡Acceso Pro Activado de por vida!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                scope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "¡Acceso Pro Activado de por vida!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity): Boolean {
        val client = billingClient
        val details = _productDetails.value

        if (client != null && client.isReady && details != null) {
            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)
                    .build()
            )
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            val responseCode = client.launchBillingFlow(activity, billingFlowParams).responseCode
            return responseCode == BillingClient.BillingResponseCode.OK
        } else {
            // Silently fail and let the UI handle the VIP fallback
            startConnection()
            return false
        }
    }

    fun restorePurchases() {
        val client = billingClient
        if (client != null && client.isReady) {
            queryPurchases()
            Toast.makeText(context, "Verificando compras anteriores con Google Play...", Toast.LENGTH_SHORT).show()
        } else {
            val currentPro = preferencesManager.configFlow.value.isProUser
            if (currentPro) {
                Toast.makeText(context, "Tu estado Pro de por vida ya está activo.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "No se encontraron compras previas registradas.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
