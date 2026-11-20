package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.BillingManager
import com.example.model.UserProfile
import com.example.ui.theme.AuroraBackground
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GoldGlowButton
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.ToolTopBar

@Composable
fun SubscriptionScreen(
    userProfile: UserProfile?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val billingManager = remember(context) { BillingManager(context) }
    val productDetails by billingManager.productDetails.collectAsState()
    val activePurchases by billingManager.activePurchaseIds.collectAsState()
    val statusMessage by billingManager.statusMessage.collectAsState()

    DisposableEffect(billingManager) {
        billingManager.startConnection()
        onDispose { billingManager.endConnection() }
    }

    AuroraBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                ToolTopBar(
                    title = "Premium Membership",
                    subtitle = "Google Play Billing",
                    credits = userProfile?.credits,
                    onBack = onBack
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 720.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = PremiumGold
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Current Plan: ${(userProfile?.membership ?: "free").uppercase()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = PremiumGold
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            listOf(
                                "Up to 25,000 AI Credits",
                                "100% Ad-Free Experience",
                                "Priority Gemini Processing",
                                "All 15 Pro AI Tools Unlocked"
                            ).forEach { benefit ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessEmerald,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(benefit, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }

                    // Free Plan Card
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Free Plan", style = MaterialTheme.typography.titleLarge)
                            Text("₹0 • 20 Daily Free Credits • Supported by Ads", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    // Paid Plans via Google Play Billing
                    items(BillingManager.PLANS, key = { it.productId }) { plan ->
                        val livePrice = productDetails[plan.productId]
                            ?.subscriptionOfferDetails
                            ?.firstOrNull()
                            ?.pricingPhases
                            ?.pricingPhaseList
                            ?.firstOrNull()
                            ?.formattedPrice
                            ?: plan.priceFallback
                        val isPurchased = activePurchases.contains(plan.productId)

                        GlassCard(
                            highlightGold = plan.productId == "ai_india_monthly",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(plan.title, style = MaterialTheme.typography.titleLarge)
                                Text(
                                    text = plan.badge,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PremiumGold
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(livePrice, style = MaterialTheme.typography.headlineMedium, color = PremiumGold)
                            Text(
                                text = plan.creditsLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            GoldGlowButton(
                                text = if (isPurchased) "Active Subscription" else "Subscribe via Google Play",
                                enabled = !isPurchased,
                                onClick = {
                                    val activity = context as? Activity ?: return@GoldGlowButton
                                    billingManager.launchPurchase(activity, plan.productId)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("subscribe_${plan.productId}")
                            )
                        }
                    }

                    if (!statusMessage.isNullOrBlank()) {
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = statusMessage!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PremiumGold
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { billingManager.restorePurchases() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("restore_purchases_button")
                            ) {
                                Text("Restore Purchases")
                            }
                            OutlinedButton(
                                onClick = { billingManager.openManageSubscriptions(context) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("manage_subscriptions_button")
                            ) {
                                Text("Manage in Play")
                            }
                        }
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}
