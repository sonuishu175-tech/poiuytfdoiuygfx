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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.RewardedAdController
import com.example.model.CreditTransaction
import com.example.model.UserProfile
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GoldGlowButton
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.SuccessEmerald
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CreditsScreen(
    userProfile: UserProfile?,
    transactions: List<CreditTransaction>,
    actionBanner: String?,
    onClaimDailyCredits: () -> Unit,
    onRewardEarned: () -> Unit,
    onOpenSubscriptions: () -> Unit,
    onClearBanner: () -> Unit
) {
    val context = LocalContext.current
    val rewardedController = remember(context) { RewardedAdController(context) }
    var adReady by remember { mutableStateOf(false) }
    var adStatusMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        rewardedController.preload { ready -> adReady = ready }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "AI Credits & Rewards",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item {
                GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Available Balance",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${userProfile?.credits ?: 0} Credits",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = JetBrainsMonoFamily,
                            color = PremiumGold
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Plan: ${(userProfile?.membership ?: "free").uppercase()} • Daily Free Limit: 20",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(16.dp))

                    GoldGlowButton(
                        text = if (adReady) "Watch Rewarded Ad (+5 Credits)" else "Load & Watch Ad (+5 Credits)",
                        onClick = {
                            adStatusMessage = null
                            onClearBanner()
                            val activity = context as? Activity ?: return@GoldGlowButton
                            rewardedController.show(
                                activity = activity,
                                onRewardEarned = onRewardEarned,
                                onError = { msg -> adStatusMessage = msg },
                                onAvailabilityChanged = { ready -> adReady = ready }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("watch_rewarded_ad_button")
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onClaimDailyCredits,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("claim_daily_credits_button")
                        ) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Daily 20")
                        }
                        Button(
                            onClick = onOpenSubscriptions,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("credits_upgrade_button")
                        ) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Go Pro")
                        }
                    }

                    val feedback = adStatusMessage ?: actionBanner
                    if (!feedback.isNullOrBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = feedback,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PremiumGold
                        )
                    }
                }
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Credit Usage Guide",
                        style = MaterialTheme.typography.titleMedium,
                        color = PremiumGold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "• AI Chat, Translator, Content, Email, Story, Business, Summarizer, Planner: 1 Credit\n" +
                            "• Vision Image Analyzer, Homework Helper, Coding Assistant, Resume Maker, PDF Assistant: 2 Credits\n" +
                            "• Rewarded Ad Bonus: +5 Credits per completed ad",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Text(
                    text = "Transaction History",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (transactions.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No credit transactions recorded yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(transactions, key = { it.transactionId }) { tx ->
                    val dateStr = tx.timestamp?.toDate()?.let {
                        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(it)
                    } ?: "Pending"
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tx.feature, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "${tx.type} • $dateStr",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = if (tx.amount >= 0) "+${tx.amount}" else "${tx.amount}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (tx.amount >= 0) SuccessEmerald else ErrorCoral
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}
