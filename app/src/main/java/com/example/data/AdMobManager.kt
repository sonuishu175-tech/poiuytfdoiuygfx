package com.example.data

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.GlassCard
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.concurrent.atomic.AtomicBoolean

object AdMobIds {
    // Official Google AdMob Test Ad Unit IDs
    const val BANNER_TEST_ID = "ca-app-pub-3940256099942544/6300978111"
    const val NATIVE_TEST_ID = "ca-app-pub-3940256099942544/2247696110"
    const val REWARDED_TEST_ID = "ca-app-pub-3940256099942544/5224354917"
}

class RewardedAdController(private val context: Context) {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    fun preload(onStateChanged: (Boolean) -> Unit = {}) {
        if (rewardedAd != null || isLoading) {
            onStateChanged(rewardedAd != null)
            return
        }
        isLoading = true
        val request = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            AdMobIds.REWARDED_TEST_ID,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    onStateChanged(true)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w("AdMob", "Rewarded ad failed to load: ${error.message}")
                    rewardedAd = null
                    isLoading = false
                    onStateChanged(false)
                }
            }
        )
    }

    fun show(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onError: (String) -> Unit,
        onAvailabilityChanged: (Boolean) -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            preload(onAvailabilityChanged)
            onError("Rewarded ad is loading. Please tap again in a moment.")
            return
        }
        val rewardGranted = AtomicBoolean(false)
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                preload(onAvailabilityChanged)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                preload(onAvailabilityChanged)
                onError("Unable to display ad: ${adError.message}")
            }
        }
        ad.show(activity) { _ ->
            // Prevent duplicate reward claims for a single ad impression
            if (rewardGranted.compareAndSet(false, true)) {
                onRewardEarned()
            }
        }
    }
}

@Composable
fun AdMobBannerSection(
    showAds: Boolean,
    modifier: Modifier = Modifier
) {
    if (!showAds) return
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Sponsored",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdMobIds.BANNER_TEST_ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

@Composable
fun AdMobNativeCard(
    showAds: Boolean,
    modifier: Modifier = Modifier
) {
    if (!showAds) return
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(Unit) {
        val loader = AdLoader.Builder(context, AdMobIds.NATIVE_TEST_ID)
            .forNativeAd { ad ->
                nativeAd?.destroy()
                nativeAd = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w("AdMob", "Native ad failed: ${error.message}")
                }
            })
            .build()
        loader.loadAd(AdRequest.Builder().build())
        onDispose {
            nativeAd?.destroy()
            nativeAd = null
        }
    }

    val loadedAd = nativeAd
    if (loadedAd != null) {
        GlassCard(modifier = modifier.fillMaxWidth()) {
            Text(
                text = "Ad • Sponsored",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    val adView = NativeAdView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                    val container = android.widget.LinearLayout(ctx).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                    }
                    val headlineView = TextView(ctx).apply {
                        textSize = 15f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setTextColor(android.graphics.Color.WHITE)
                    }
                    val bodyView = TextView(ctx).apply {
                        textSize = 13f
                        setTextColor(android.graphics.Color.LTGRAY)
                        setPadding(0, 6, 0, 10)
                    }
                    val ctaButton = Button(ctx).apply {
                        textSize = 12f
                    }
                    container.addView(headlineView)
                    container.addView(bodyView)
                    container.addView(ctaButton)
                    adView.addView(container)
                    adView.headlineView = headlineView
                    adView.bodyView = bodyView
                    adView.callToActionView = ctaButton
                    adView
                },
                update = { adView ->
                    (adView.headlineView as? TextView)?.text = loadedAd.headline
                    (adView.bodyView as? TextView)?.text = loadedAd.body.orEmpty()
                    (adView.callToActionView as? Button)?.text = loadedAd.callToAction ?: "Learn More"
                    adView.setNativeAd(loadedAd)
                }
            )
        }
    }
}
