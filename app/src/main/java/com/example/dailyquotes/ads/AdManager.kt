package com.example.dailyquotes.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem

class AdManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "AdManager"
        
        // Test ad unit IDs for development and testing
        private const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111" // Test banner ID
        private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917" // Test rewarded ID
        private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712" // Test interstitial ID
        
        // Keep the original test IDs for reference (commented out)
        // private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3459996192536925/8231179933"
        // private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3459996192536925/2978853253"
        // private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3459996192536925/9866635746"
        
        @Volatile
        private var instance: AdManager? = null
        
        fun getInstance(context: Context): AdManager {
            return instance ?: synchronized(this) {
                instance ?: AdManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var isRewardedAdLoading = false
    private var isInterstitialAdLoading = false
    
    // Callback interface for rewarded ads
    interface RewardAdListener {
        fun onRewardAdLoaded()
        fun onRewardAdFailedToLoad(errorMessage: String)
        fun onRewardAdShown()
        fun onRewardAdDismissed()
        fun onRewardEarned(amount: Int, type: String)
    }
    
    // Callback interface for interstitial ads
    interface InterstitialAdListener {
        fun onInterstitialAdLoaded()
        fun onInterstitialAdFailedToLoad(errorMessage: String)
        fun onInterstitialAdShown()
        fun onInterstitialAdDismissed()
    }
    
    private var rewardAdListener: RewardAdListener? = null
    private var interstitialAdListener: InterstitialAdListener? = null
    
    init {
        // Initialize the Mobile Ads SDK
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "MobileAds initialization complete")
        }
    }
    
    fun setRewardAdListener(listener: RewardAdListener) {
        this.rewardAdListener = listener
    }
    
    fun setInterstitialAdListener(listener: InterstitialAdListener) {
        this.interstitialAdListener = listener
    }
    
    fun loadBannerAd(adContainer: ViewGroup) {
        try {
            // Clear the container first
            adContainer.removeAllViews()
            
            // Create a new AdView instance
            val adView = AdView(context)
            // Set properties using separate statements
            adView.adUnitId = BANNER_AD_UNIT_ID // Use test ad ID for development
            adView.setAdSize(AdSize.BANNER)
            
            // Add the AdView to the container
            adContainer.addView(adView)
            
            // Load an ad
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
            
            Log.d(TAG, "Banner ad loading with test ID: $BANNER_AD_UNIT_ID")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading banner ad: ${e.message}")
        }
    }
    
    fun loadInterstitialAd() {
        if (isInterstitialAdLoading || interstitialAd != null) {
            Log.d(TAG, "Interstitial ad is already loading or loaded")
            return
        }
        
        isInterstitialAdLoading = true
        Log.d(TAG, "Starting to load interstitial ad")
        
        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(context, INTERSTITIAL_AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Failed to load interstitial ad: ${loadAdError.message}")
                    interstitialAd = null
                    isInterstitialAdLoading = false
                    interstitialAdListener?.onInterstitialAdFailedToLoad(loadAdError.message)
                }
                
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    interstitialAd = ad
                    isInterstitialAdLoading = false
                    setupInterstitialAdCallbacks()
                    interstitialAdListener?.onInterstitialAdLoaded()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error loading interstitial ad: ${e.message}")
            isInterstitialAdLoading = false
        }
    }
    
    private fun setupInterstitialAdCallbacks() {
        val currentAd = interstitialAd ?: return
        
        currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial ad dismissed")
                interstitialAd = null
                interstitialAdListener?.onInterstitialAdDismissed()
                
                // Load a new ad
                loadInterstitialAd()
            }
            
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Interstitial ad failed to show: ${adError.message}")
                interstitialAd = null
                
                // Try to load a new ad
                loadInterstitialAd()
            }
            
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial ad showed fullscreen content")
                interstitialAdListener?.onInterstitialAdShown()
            }
        }
    }
    
    fun isInterstitialAdReady(): Boolean {
        return interstitialAd != null
    }
    
    fun showInterstitialAd(activity: Activity) {
        val currentAd = interstitialAd
        if (currentAd == null) {
            Log.d(TAG, "Interstitial ad not ready yet")
            loadInterstitialAd()
            return
        }
        
        Log.d(TAG, "Showing interstitial ad")
        try {
            currentAd.show(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing interstitial ad: ${e.message}")
            interstitialAd = null
            loadInterstitialAd()
        }
    }
    
    fun loadRewardedAd() {
        if (isRewardedAdLoading || rewardedAd != null) {
            Log.d(TAG, "Ad is already loading or loaded")
            return
        }
        
        isRewardedAdLoading = true
        Log.d(TAG, "Starting to load rewarded ad")
        
        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(context, REWARDED_AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Failed to load rewarded ad: ${loadAdError.message}")
                    rewardedAd = null
                    isRewardedAdLoading = false
                    rewardAdListener?.onRewardAdFailedToLoad(loadAdError.message)
                }
                
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully")
                    rewardedAd = ad
                    isRewardedAdLoading = false
                    setupRewardedAdCallbacks()
                    rewardAdListener?.onRewardAdLoaded()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error loading rewarded ad: ${e.message}")
            isRewardedAdLoading = false
        }
    }
    
    private fun setupRewardedAdCallbacks() {
        val currentAd = rewardedAd ?: return
        
        currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed")
                rewardedAd = null
                rewardAdListener?.onRewardAdDismissed()
                
                // Load a new ad
                loadRewardedAd()
            }
            
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Ad failed to show: ${adError.message}")
                rewardedAd = null
                
                // Try to load a new ad
                loadRewardedAd()
            }
            
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content")
                rewardAdListener?.onRewardAdShown()
            }
        }
    }
    
    fun isRewardedAdReady(): Boolean {
        return rewardedAd != null
    }
    
    fun showRewardedAd(activity: Activity) {
        val currentAd = rewardedAd
        if (currentAd == null) {
            Log.d(TAG, "Rewarded ad not ready yet")
            Toast.makeText(context, "Ad not ready yet, please try again later", Toast.LENGTH_SHORT).show()
            loadRewardedAd()
            return
        }
        
        Log.d(TAG, "Showing rewarded ad")
        try {
            currentAd.show(activity, OnUserEarnedRewardListener { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                rewardAdListener?.onRewardEarned(rewardItem.amount, rewardItem.type)
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error showing rewarded ad: ${e.message}")
            rewardedAd = null
            loadRewardedAd()
        }
    }
}
