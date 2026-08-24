package com.example.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.data.SettingsRepository
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object AdsManager {
    private const val TAG = "AdsManager"

    // Production AdMob Ad Unit IDs provided by user
    const val BANNER_AD_UNIT_ID = "ca-app-pub-4465133942819698/3657869476"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-4465133942819698/3769550118"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-4465133942819698/8718624462"

    // Test fallback Ad Unit IDs (AdMob standard test IDs) for emulator/testing
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    private val _isRewardedReady = MutableStateFlow(false)
    val isRewardedReady: StateFlow<Boolean> = _isRewardedReady.asStateFlow()

    private var calculationActionCount = 0
    private var lastInterstitialShownTime = 0L
    private const val ACTIONS_THRESHOLD_FOR_INTERSTITIAL = 6
    private const val MIN_INTERSTITIAL_INTERVAL_MS = 90_000L // 90 seconds cooldown

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true

        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.Main).launch {
            try {
                MobileAds.initialize(appContext) { status ->
                    Log.d(TAG, "MobileAds initialized: $status")
                    preloadAds(appContext)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize MobileAds safely", e)
            }
        }
    }

    fun requestConsentAndInit(activity: Activity) {
        val appContext = activity.applicationContext
        try {
            val params = ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build()

            val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
            consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                        if (formError != null) {
                            Log.w(TAG, "Consent form error: ${formError.message}")
                        }
                        initialize(appContext)
                    }
                },
                { requestConsentError ->
                    Log.w(TAG, "Consent request error: ${requestConsentError.message}")
                    initialize(appContext)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Consent flow error", e)
            initialize(appContext)
        }
    }

    fun isOnline(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = connectivityManager?.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            false
        }
    }

    fun preloadAds(context: Context) {
        if (!isOnline(context)) return
        loadInterstitial(context)
        loadRewarded(context)
    }

    private fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        val adRequest = AdRequest.Builder().build()
        // Try production first, if test environment use test
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(TAG, "Interstitial ad failed to load: ${error.message}. Retrying with test ID.")
                    // Fallback to test ad ID if production ID is unverified during development
                    InterstitialAd.load(
                        context,
                        TEST_INTERSTITIAL_AD_UNIT_ID,
                        adRequest,
                        object : InterstitialAdLoadCallback() {
                            override fun onAdLoaded(ad: InterstitialAd) {
                                interstitialAd = ad
                            }
                            override fun onAdFailedToLoad(err: LoadAdError) {
                                interstitialAd = null
                            }
                        }
                    )
                }
            }
        )
    }

    fun onUserCalculationCompleted(activity: Activity, settingsRepo: SettingsRepository) {
        if (settingsRepo.isAdFreeActive()) return

        calculationActionCount++
        if (calculationActionCount >= ACTIONS_THRESHOLD_FOR_INTERSTITIAL) {
            val now = System.currentTimeMillis()
            if (now - lastInterstitialShownTime > MIN_INTERSTITIAL_INTERVAL_MS) {
                showInterstitialIfAvailable(activity) {
                    calculationActionCount = 0
                    lastInterstitialShownTime = now
                }
            }
        }
    }

    fun showInterstitialIfAvailable(activity: Activity, onDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial(activity)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    loadInterstitial(activity)
                    onDismissed()
                }
            }
            ad.show(activity)
        } else {
            loadInterstitial(activity)
            onDismissed()
        }
    }

    fun loadRewarded(context: Context) {
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    _isRewardedReady.value = true
                    Log.d(TAG, "Rewarded ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    _isRewardedReady.value = false
                    Log.w(TAG, "Rewarded ad failed to load: ${error.message}. Retrying with test ID.")
                    // Fallback to test ad ID
                    RewardedAd.load(
                        context,
                        TEST_REWARDED_AD_UNIT_ID,
                        adRequest,
                        object : RewardedAdLoadCallback() {
                            override fun onAdLoaded(ad: RewardedAd) {
                                rewardedAd = ad
                                _isRewardedReady.value = true
                            }
                            override fun onAdFailedToLoad(err: LoadAdError) {
                                rewardedAd = null
                                _isRewardedReady.value = false
                            }
                        }
                    )
                }
            }
        )
    }

    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: () -> Unit,
        onAdClosed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    _isRewardedReady.value = false
                    loadRewarded(activity)
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    rewardedAd = null
                    _isRewardedReady.value = false
                    loadRewarded(activity)
                    onAdClosed()
                }
            }
            ad.show(activity) {
                onUserEarnedReward()
            }
        } else {
            loadRewarded(activity)
            onAdClosed()
        }
    }
}
