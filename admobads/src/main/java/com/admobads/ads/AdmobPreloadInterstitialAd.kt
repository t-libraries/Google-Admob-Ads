package com.admobads.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.core.graphics.toColorInt
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.admobads.ads.utils.AdLoadingComposable
import com.admobads.ads.utils.GlobalState
import com.admobads.data.InterAdModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.ResponseInfo
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdPreloader
import com.google.android.gms.ads.preload.PreloadCallbackV2
import com.google.android.gms.ads.preload.PreloadConfiguration
import com.google.android.material.card.MaterialCardView

@SuppressLint("StaticFieldLeak")
object AdmobPreloadInterstitialAd {
    private var AD_UNIT_ID = ""
    private var preloadedAdsCount = 0
    private var appStartTime = 0L
    private var TAG = "AdmobInterstitialAd_"
    private var interCounterStart = 2
    private var interCounterGap = 3
    private var currentCounter = interCounterStart
    private var shouldLoadAd = false
    private var isFirstTimeInterShown = false
    private var lastInterShownTime = 0L
    private var inter_counter_start_time = 0L
    private var inter_counter_gap_time = 0L
    private var inter_type = "click"
    private var isPurchased = false
    private var isComposed = false
    private var backCallback: androidx.activity.OnBackPressedCallback? = null
    private var dialogBackgroundColor = "#F8F8F8".toColorInt()
    private var dialogTextColor = Color.BLACK
    private var currentComposeLoadingView: View? = null
    private var blockedActivity: Activity? = null
    private val handler = Handler(Looper.getMainLooper())
    private var adRunnable: Runnable? = null
    private var interCallback: FullScreenContentCallback? = null

    private var shouldshowAd = false

    private var isAppInForeground = true

    private var pendingActivity: Activity? = null
    private var pendingLoadingView: View? = null
    private var adMessage: String = "Ad Loading"

    private var mInterstitialAd: InterstitialAd? = null


    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                isAppInForeground = true
                resumeAdIfNeeded()
            }

            override fun onStop(owner: LifecycleOwner) {
                isAppInForeground = false
                pauseAd()
            }
        })
    }

    private fun pauseAd() {
        adRunnable?.let {
            handler.removeCallbacks(it)
        }
    }

    private fun resumeAdIfNeeded() {
        if (pendingActivity != null && mInterstitialAd != null && shouldshowAd) {
            pendingActivity?.let { activity ->

                val loadingView = pendingLoadingView

                adRunnable = Runnable {
                    activity.hideAdLoadingView(loadingView)
                    mInterstitialAd?.show(activity)
                    mInterstitialAd?.fullScreenContentCallback =
                        interCallback

                }
                adRunnable?.let { handler.postDelayed(it, 1000) }
            }

            pendingActivity = null
            pendingLoadingView = null
        }
    }

    fun setPurchased(isPurchased: Boolean = false) {
        this.isPurchased = isPurchased
    }

    fun setComposed(isPurchased: Boolean = false) {
        this.isComposed = isPurchased
    }

    fun setLoadingDialogBgColor(loadingDialogBgColor: Int) {
        this.dialogBackgroundColor = loadingDialogBgColor
        AdmobAppOpenAd.setDialogBGColor(loadingDialogBgColor)
    }

    fun setLoadingDialogTextColor(loadingDialogTextColor: Int) {
        this.dialogTextColor = loadingDialogTextColor
        AdmobAppOpenAd.setDialogTextColor(loadingDialogTextColor)
    }

    fun start(interAdModel: InterAdModel, adunitID: String) {

        inter_type = interAdModel.inter_type

        if (interAdModel.inter_type == "timer") {
            appStartTime = System.currentTimeMillis()
            isFirstTimeInterShown = false
            lastInterShownTime = 0L
            shouldLoadAd =
                !(interAdModel.inter_start_after_seconds == 0L && interAdModel.inter_gap_after_seconds == 0L)
            if (!shouldLoadAd) {
                Log.d(TAG, "Ad loading disabled: both counters are 0")
                return
            }

            inter_counter_start_time =
                interAdModel.inter_start_after_seconds * 1000

            inter_counter_gap_time =
                interAdModel.inter_gap_after_seconds * 1000

        } else {
            shouldLoadAd = !(interCounterStart == 0 && interCounterGap == 0)
            if (!shouldLoadAd) {
                Log.d(TAG, "Ad loading disabled: both counters are 0")
                return
            }

            this.interCounterStart = interAdModel.inter_counter_start
            this.interCounterGap = interAdModel.inter_counter_gap

            if (interCounterStart != 0) {
                currentCounter = interCounterStart
            } else {
                if (interCounterGap != 0) {
                    currentCounter = interCounterGap
                } else {
                    currentCounter = 0
                }
            }
        }


        AD_UNIT_ID = adunitID

        if (isPurchased) {
            Log.d(TAG, "Inside Purchased")
            return
        }

        Log.d(TAG, "Inside  ${interAdModel}")

        val config = PreloadConfiguration.Builder(AD_UNIT_ID)
            .setBufferSize(2)
            .build()

        InterstitialAdPreloader.start(
            AD_UNIT_ID,
            config,
            object : PreloadCallbackV2() {
                override fun onAdPreloaded(preloadId: String, responseInfo: ResponseInfo?) {
                    preloadedAdsCount++
                    adMessage = "Inside Ad Loaded"
                    Log.d(TAG, "Ad Ready | Total preloaded: $preloadedAdsCount")
                }

                override fun onAdsExhausted(preloadId: String) {
                    preloadedAdsCount = 0
                    Log.d(TAG, "All ads exhausted | Reloading")
                }

                override fun onAdFailedToPreload(preloadId: String, adError: AdError) {
                    Log.e(TAG, "Preload failed: ${adError.message}")
                    adMessage = "Inside Ad Loading Failed Error : ${adError.message}"
                }
            }
        )
    }

    fun Activity.showPreloadInter(
        message: (String) -> Unit = {},
        callBack: () -> Unit,
    ) {

        if (isPurchased) {
            message.invoke("Premium User")
            callBack.invoke()
            return
        }

        message.invoke(adMessage)

        if (!isReady()) {
            callBack.invoke()
            return
        }

        if (!shouldLoadAd) {
            callBack.invoke()
            return
        }

        if (inter_type == "timer") {
            showPreloadTimeInter(
                message = {
                    message.invoke(adMessage)
                },

                callBack = {
                    callBack.invoke()
                }

            )
            return
        }



        Log.d("TAG", "Counter is: $currentCounter")

        if (currentCounter > 1 || currentCounter == 0) {
            if (currentCounter != 0)
                currentCounter--
            Log.d(TAG, "Skipping ad | Counter: $currentCounter")
            callBack.invoke()
            return
        }

        if (!isReady()) {
            callBack.invoke()
            return
        }

        lastInterShownTime = System.currentTimeMillis()
        isFirstTimeInterShown = true

        val ad = InterstitialAdPreloader.pollAd(AD_UNIT_ID)
        ad ?: run {
            callBack.invoke()
            return
        }

        mInterstitialAd = ad
        currentCounter = interCounterGap

        preloadedAdsCount--
        Log.d(TAG, "Ad shown | Remaining preloaded: $preloadedAdsCount")

        val callback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad Showed")
                mInterstitialAd = null
            }

            override fun onAdDismissedFullScreenContent() {
                unblockTouches()
                GlobalState.isInterShowing = false
                Log.d(TAG, "Ad Dismissed")
                mInterstitialAd = null
                shouldshowAd = false
                AdmobAppOpenAd.shouldshowAppOpen()
                callBack.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                unblockTouches()
                GlobalState.isInterShowing = false
                Log.e(TAG, "Show Failed: ${adError.message}")
                AdmobAppOpenAd.shouldshowAppOpen()
                mInterstitialAd = null
                shouldshowAd = false
                callBack.invoke()
            }

            override fun onAdImpression() {
                Log.d(TAG, "Ad Impression")
            }

            override fun onAdClicked() {
                Log.d(TAG, "Ad Clicked")
            }
        }

        interCallback = callback
        shouldshowAd = true
        GlobalState.isInterShowing = true
        AdmobAppOpenAd.shouldshowAppOpen(false)
        blockTouches(this)
        val loadingView = showAdLoadingView()
        pendingActivity = this
        pendingLoadingView = loadingView
        adRunnable = Runnable {
            hideAdLoadingView(loadingView)
            ad.fullScreenContentCallback = callback
            ad.show(this)
        }
        adRunnable?.let { handler.postDelayed(it, 1500) }
    }

    @SuppressLint("StaticFieldLeak")
    fun Activity.showPreloadTimeInter(
        message: (String) -> Unit = {},
        callBack: () -> Unit
    ) {

        if (isPurchased) {
            callBack.invoke()
            return
        }

        if (!shouldLoadAd) {
            callBack.invoke()
            return
        }

        message.invoke(adMessage)
        Log.d("TAGinginthetimer", "Time is: ${isTimeReadyToShow()}")

        if (!isTimeReadyToShow()) {
            callBack.invoke()
            return
        }


        val ad = InterstitialAdPreloader.pollAd(AD_UNIT_ID)
        ad ?: run {
            callBack.invoke()
            return
        }

        mInterstitialAd = ad

        val callback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad Showed")
                mInterstitialAd = null
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad Dismissed")
                unblockTouches()
                GlobalState.isInterShowing = false
                shouldshowAd = false
                mInterstitialAd = null
                lastInterShownTime = System.currentTimeMillis()
                AdmobAppOpenAd.shouldshowAppOpen()
                isFirstTimeInterShown = true
                callBack.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Show Failed: ${adError.message}")
                unblockTouches()
                GlobalState.isInterShowing = false
                mInterstitialAd = null
                lastInterShownTime = System.currentTimeMillis()
                AdmobAppOpenAd.shouldshowAppOpen()
                isFirstTimeInterShown = true
                shouldshowAd = false
                callBack.invoke()
            }

            override fun onAdImpression() {
                Log.d(TAG, "Ad Impression")
            }

            override fun onAdClicked() {
                Log.d(TAG, "Ad Clicked")
            }
        }

        interCallback = callback
        shouldshowAd = true
        GlobalState.isInterShowing = true
        AdmobAppOpenAd.shouldshowAppOpen(false)
        blockTouches(this)
        val loadingView = showAdLoadingView()
        pendingActivity = this
        pendingLoadingView = loadingView
        adRunnable = Runnable {
            hideAdLoadingView(loadingView)
            lastInterShownTime = System.currentTimeMillis()
            isFirstTimeInterShown = true
            ad.fullScreenContentCallback = callback
            ad.show(this)
        }
        adRunnable?.let { handler.postDelayed(it, 1500) }

    }

    private fun isTimeReadyToShow(): Boolean {

        if (!shouldLoadAd) return false

        val now = System.currentTimeMillis()

        if (!isFirstTimeInterShown) {

            val requiredTime =
                if (inter_counter_start_time > 0)
                    inter_counter_start_time
                else
                    inter_counter_gap_time

            if (requiredTime <= 0) return false

            val elapsed = now - appStartTime

            Log.d(TAG, "First Ad elapsed=$elapsed required=$requiredTime")

            return elapsed >= requiredTime
        }

        if (inter_counter_gap_time <= 0) return false

        val elapsed = now - lastInterShownTime

        Log.d(TAG, "Gap elapsed=$elapsed required=$inter_counter_gap_time")

        return elapsed >= inter_counter_gap_time
    }

    fun isReady(): Boolean {
        return InterstitialAdPreloader.isAdAvailable(AD_UNIT_ID)
    }

    private fun Activity.showAdLoadingView(): View {
        if (isComposed) {
            val composeView = ComposeView(this).apply {
                setContent {
                    AdLoadingComposable(
                        backgroundColor = androidx.compose.ui.graphics.Color(dialogBackgroundColor),
                        textColor = androidx.compose.ui.graphics.Color(dialogTextColor),
                        progressColor = androidx.compose.ui.graphics.Color(dialogTextColor)
                    )
                }
            }

            val rootView = findViewById<ViewGroup>(android.R.id.content)
            rootView.addView(composeView)

            currentComposeLoadingView = composeView
            return composeView
        } else {
            val rootView = findViewById<ViewGroup>(android.R.id.content)
            val loadingView =
                layoutInflater.inflate(R.layout.tlib_ad_loading_dialog, rootView, false)

            loadingView.findViewById<TextView>(R.id.textView21).setTextColor(dialogTextColor)
            loadingView.findViewById<ProgressBar>(R.id.progressBar).indeterminateTintList =
                ColorStateList.valueOf(dialogTextColor)
            loadingView.findViewById<MaterialCardView>(R.id.dialogBg)
                .setCardBackgroundColor(dialogBackgroundColor)

            rootView.addView(loadingView)
            return loadingView
        }
    }

    private fun Activity.hideAdLoadingView(loadingView: View?) {
        try {
            if (!isFinishing && !isDestroyed) {
                val rootView = findViewById<ViewGroup>(android.R.id.content)
                loadingView?.let { view ->
                    if (view.parent != null) {
                        rootView.removeView(view)
                    }

                    if (view is ComposeView) {
                        view.disposeComposition()
                    }
                }

                if (isComposed) {
                    currentComposeLoadingView = null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding loading view: ${e.message}")
        }
    }


    fun clearPreloadedAds() {
        while (InterstitialAdPreloader.isAdAvailable(AD_UNIT_ID)) {
            val ad =
                InterstitialAdPreloader.pollAd(AD_UNIT_ID)
            ad?.fullScreenContentCallback = null
        }
        preloadedAdsCount = 0
        Log.d(TAG, "All preloaded ads cleared")
    }

    private fun blockTouches(activity: Activity) {
        try {
            blockedActivity = activity

            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )

            if (activity is androidx.activity.ComponentActivity) {
                backCallback = object : androidx.activity.OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        Log.d(TAG, "backpressed")
                    }
                }
                activity.onBackPressedDispatcher.addCallback(activity, backCallback!!)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun unblockTouches() {
        try {
            blockedActivity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            backCallback?.remove()
            backCallback = null

            blockedActivity = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}