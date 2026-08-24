package com.example.ads

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    isAdFree: Boolean = false
) {
    if (isAdFree) return

    val context = LocalContext.current
    var isAdLoaded by remember { mutableStateOf(false) }
    var adFailed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    val adView = AdView(ctx)
                    val adSize = getAdaptiveAdSize(ctx)
                    adView.setAdSize(adSize)
                    adView.adUnitId = AdsManager.BANNER_AD_UNIT_ID

                    adView.adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            isAdLoaded = true
                            adFailed = false
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            super.onAdFailedToLoad(loadAdError)
                            // Retry with test ad unit id if production id is not yet provisioned in test sandbox
                            if (adView.adUnitId == AdsManager.BANNER_AD_UNIT_ID) {
                                adView.adUnitId = AdsManager.TEST_BANNER_AD_UNIT_ID
                                adView.loadAd(AdRequest.Builder().build())
                            } else {
                                adFailed = true
                                isAdLoaded = false
                            }
                        }
                    }

                    if (AdsManager.isOnline(ctx)) {
                        adView.loadAd(AdRequest.Builder().build())
                    } else {
                        adFailed = true
                    }

                    adView
                },
                update = { view ->
                    // View update if needed
                }
            )

            if (!isAdLoaded && !adFailed) {
                // Subtle minimal space preservation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ADVERTISEMENT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }
    }
}

private fun getAdaptiveAdSize(context: Context): AdSize {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    val displayMetrics = DisplayMetrics()
    if (windowManager != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            var adWidthPixels = bounds.width().toFloat()
            val density = context.resources.displayMetrics.density
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val density = displayMetrics.density
            val adWidth = (displayMetrics.widthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        }
    }
    return AdSize.BANNER
}
