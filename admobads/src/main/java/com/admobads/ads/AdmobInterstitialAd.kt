package com.admobads.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
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
import androidx.annotation.Keep
import androidx.compose.ui.platform.ComposeView
import androidx.core.graphics.toColorInt
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.admobads.ads.AdmobPreloadInterstitialAd.showPreloadInter
import com.admobads.ads.utils.AdLoadingComposable
import com.admobads.ads.utils.GlobalState
import com.admobads.ads.utils.isNetworkAvailable
import com.admobads.data.InterAdModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
object AdmobInterstitialAd {

    private var splashInterstitialAd: InterstitialAd? = null
    private var TAG = "AdmobInterstitialAd_"
    private var mInterstitialAd: InterstitialAd? = null
    private var isPreviousAdLoading = false
    private var isPurchased = false
    private var isComposed = false
    private var currentComposeLoadingView: View? = null
    private var inter_counter_start = 2
    private var inter_counter_start_inside = 2
    private var inter_counter_gap = 3
    private var inter_counter_gap_inside = 3
    private var inside_inter_ad_id = ""
    private var inter_type = "click"
    private var inter_counter_start_time = 0L
    private var load_inter_counter_start_before = 0L
    private var inter_counter_gap_time = 0L
    private var load_inter_counter_gap_before = 0L
    private var isFirstTimeInterShown = false
    private var lastInterShownTime = 0L
    private var appStartTime = 0L
    private var isTimeAdLoaded = false
    private var shouldLoadAd = false
    private var dialogBackgroundColor = "#F8F8F8".toColorInt()
    private var dialogTextColor = Color.BLACK
    var blockedActivity: Activity? = null
    private var isInterIntialized = false
    private var loadingtype = "manual"
    private var backCallback: androidx.activity.OnBackPressedCallback? = null
    private var adMessage: String = "Ad Loading"
    private var isAppInForeground = true
    private var pendingActivity: Activity? = null
    private var pendingLoadingView: View? = null
    private var interCallback: FullScreenContentCallback? = null
    private var shouldshowAd = false


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
        if (pendingActivity != null && (mInterstitialAd != null || splashInterstitialAd != null) && shouldshowAd) {
            pendingActivity?.let { activity ->

                val loadingView = pendingLoadingView

                adRunnable = Runnable {
                    activity.hideAdLoadingView(loadingView)
                    if (splashInterstitialAd != null) {
                        splashInterstitialAd?.show(activity)
                        splashInterstitialAd?.fullScreenContentCallback = interCallback
                    } else {
                        mInterstitialAd?.show(activity)
                        mInterstitialAd?.fullScreenContentCallback = interCallback
                    }

                }
                adRunnable?.let { handler.postDelayed(it, 1000) }
            }

            pendingActivity = null
            pendingLoadingView = null
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var adRunnable: Runnable? = null

    fun setPurchased(isPurchased: Boolean = false) {
        this.isPurchased = isPurchased
    }

    fun isPurchased(): Boolean = isPurchased

    fun setComposed(isComposed: Boolean = false) {
        this.isComposed = isComposed
    }

    fun isComposed () : Boolean = isComposed

    fun setLoadingDialogBgColor(loadingDialogBgColor: Int) {
        this.dialogBackgroundColor = loadingDialogBgColor
        AdmobAppOpenAd.setDialogBGColor(loadingDialogBgColor)
        AdmobPreloadInterstitialAd.setLoadingDialogBgColor(loadingDialogBgColor)
    }

    fun setLoadingDialogTextColor(loadingDialogTextColor: Int) {
        this.dialogTextColor = loadingDialogTextColor
        AdmobAppOpenAd.setDialogTextColor(loadingDialogTextColor)
        AdmobPreloadInterstitialAd.setLoadingDialogTextColor(loadingDialogTextColor)
    }

    fun initInterFromConfig(
        context: Context,
        config: InterAdModel,
        inside_inter_ad_id: String
    ) {

        if (isInterIntialized) {
            return
        }

        isInterIntialized = true


        Log.d(TAG, "Inside  ${config?.loading_type}")

        loadingtype = if (config.loading_type == "manual") "manual" else ""

        if (config.loading_type == "manual") {

            if (config.inter_type == "timer") {
                initTimeBased(
                    context = context,
                    interStartAfterSeconds = config.inter_start_after_seconds,
                    loadFirstBeforeSeconds = config.inter_start_load_before_seconds,
                    gapAfterSeconds = config.inter_gap_after_seconds,
                    loadGapBeforeSeconds = config.inter_gap_load_before_seconds,
                    inside_inter_ad_id = inside_inter_ad_id
                )
            } else {
                initValues(
                    context = context,
                    inter_counter_start = config.inter_counter_start,
                    inter_counter_gap = config.inter_counter_gap,
                    inside_inter_ad_id = inside_inter_ad_id
                )
            }

        } else {
            AdmobPreloadInterstitialAd.start(
                config,
                inside_inter_ad_id
            )
        }

    }

    private fun initTimeBased(
        context: Context,
        interStartAfterSeconds: Long,
        loadFirstBeforeSeconds: Long,
        gapAfterSeconds: Long,
        loadGapBeforeSeconds: Long,
        inside_inter_ad_id: String
    ) {
        inter_type = "timer"
        this.inside_inter_ad_id = inside_inter_ad_id

        inter_counter_start_time = interStartAfterSeconds * 1000
        load_inter_counter_start_before = loadFirstBeforeSeconds * 1000

        inter_counter_gap_time = gapAfterSeconds * 1000
        load_inter_counter_gap_before = loadGapBeforeSeconds * 1000

        if (inter_counter_start_time == 0L && inter_counter_gap_time == 0L) {
            shouldLoadAd = false
            return
        }

        if (inter_counter_start_time == 0L && inter_counter_gap_time > 0L) {
            inter_counter_start_time = inter_counter_gap_time
            load_inter_counter_start_before = load_inter_counter_gap_before
        }

        shouldLoadAd = true
        appStartTime = System.currentTimeMillis()
        isFirstTimeInterShown = false

        scheduleTimeBasedLoad(context)
    }

    private fun initValues(
        context: Context,
        inter_counter_start: Int = 2,
        inter_counter_gap: Int = 3,
        inside_inter_ad_id: String

    ) {
        this.inter_counter_start = inter_counter_start
        this.inter_counter_start_inside = inter_counter_start
        this.inter_counter_gap = inter_counter_gap
        this.inter_counter_gap_inside = inter_counter_gap
        this.inside_inter_ad_id = inside_inter_ad_id

        if (inter_counter_start == 0) {
            this.inter_counter_start = inter_counter_gap
        }

        if (inter_counter_start == 0 && inter_counter_gap == 0) {
            Log.d(TAG, "Both are 0")
            shouldLoadAd = false
        } else if (inter_counter_start != 0 && inter_counter_start <= 2) {
            Log.d(TAG, "inter_counter_start = ${inter_counter_start}")
            shouldLoadAd = true
            load(context, inside_inter_ad_id)
        } else {
            Log.d(TAG, "inter_counter_gap = ${inter_counter_gap}")
            shouldLoadAd = true
            if (inter_counter_gap != 0 && inter_counter_gap <= 2) {
                load(context, inside_inter_ad_id)
            }
        }

    }

    private fun scheduleTimeBasedLoad(context: Context) {

        if (!isFirstTimeInterShown) {
            appStartTime
        } else {
            lastInterShownTime
        }

        val showAfter = if (!isFirstTimeInterShown) {
            inter_counter_start_time
        } else {
            inter_counter_gap_time
        }

        val loadBefore = if (!isFirstTimeInterShown) {
            load_inter_counter_start_before
        } else {
            load_inter_counter_gap_before
        }

        val loadDelay = showAfter - loadBefore

        if (loadDelay <= 0) {
            load(context, inside_inter_ad_id)
            return
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isPurchased && mInterstitialAd == null) {
                load(context, inside_inter_ad_id)
                isTimeAdLoaded = true
            }
        }, loadDelay)
    }

    private fun isTimeReadyToShow(): Boolean {

        if (isFirstTimeInterShown && inter_counter_gap_time <= 0) {
            return false
        }

        val baseTime = if (!isFirstTimeInterShown) {
            appStartTime
        } else {
            lastInterShownTime
        }

        val requiredTime = if (!isFirstTimeInterShown) {
            inter_counter_start_time
        } else {
            inter_counter_gap_time
        }

        val elapsed = System.currentTimeMillis() - baseTime
        return elapsed >= requiredTime

    }


    fun loadSplashInter(
        ctx: Activity, id: String,
        onAdLoaded: () -> Unit,
        onAdFailedToLoad: () -> Unit,
    ) {
        if (!isNetworkAvailable(ctx)) {
            onAdFailedToLoad.invoke()
            return
        }

        if (splashInterstitialAd != null) {
            onAdLoaded.invoke()
            return
        }

        val callback = object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                adMessage = "Splash Ad Loaded"
                isPreviousAdLoading = false
                splashInterstitialAd = interstitialAd
                onAdLoaded.invoke()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                adMessage = "Splash Ad Loading Failed Error: ${adError.message}"
                isPreviousAdLoading = false
                onAdFailedToLoad.invoke()
            }
        }
        if (!isPreviousAdLoading) {
            isPreviousAdLoading = true
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(ctx, id, adRequest, callback)
        }
    }

    fun Activity.showSplashInterAd(
        callBack: (Boolean) -> Unit
    ) {


        if (isPurchased) {
            callBack.invoke(true)
            return
        }


        if (splashInterstitialAd == null) {
            callBack.invoke(false)
            return
        }

        val callback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                splashInterstitialAd = null
                shouldshowAd = false
                GlobalState.isInterShowing = false
                AdmobAppOpenAd.shouldshowAppOpen()
                callBack.invoke(true)
                unblockTouches()
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                splashInterstitialAd = null
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)
                callBack.invoke(false)
                shouldshowAd = false
                GlobalState.isInterShowing = false
                AdmobAppOpenAd.shouldshowAppOpen()
                unblockTouches()
            }
        }

        interCallback = callback
        AdmobAppOpenAd.shouldshowAppOpen(false)
        GlobalState.isInterShowing = true
        shouldshowAd = true
        blockTouches(this)
        val loadingView = showAdLoadingView()
        pendingActivity = this
        pendingLoadingView = loadingView
        adRunnable = Runnable {
            if (
                !isAppInForeground ||
                isFinishing ||
                isDestroyed ||
                !hasWindowFocus()
            ) {
                Log.d(TAG, "Splash ad skipped: invalid state")
                AdmobAppOpenAd.shouldshowAppOpen(true)
                hideAdLoadingView(loadingView)
                shouldshowAd = false
                callBack.invoke(true)
                unblockTouches()
                GlobalState.isInterShowing = false
                return@Runnable
            }
            hideAdLoadingView(loadingView)
            splashInterstitialAd?.fullScreenContentCallback = callback
            splashInterstitialAd?.show(this)

        }
        adRunnable?.let { handler.postDelayed(it, 1500) }
    }

    private fun load(ctx: Context, id: String) {

        Log.d(TAG, "Loaded Requested $shouldLoadAd")

        if (isPurchased) {
            return
        }

        if (!shouldLoadAd) {
            return
        }

        if (!isNetworkAvailable(ctx)) {
            adMessage = "Internet not available"
            return
        }


        if (splashInterstitialAd != null) {
            mInterstitialAd = splashInterstitialAd
            splashInterstitialAd = null
            return
        }

        if (mInterstitialAd != null) {
            return
        }



        Log.d(TAG, "Loaded Requested")

        val callback = object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                isPreviousAdLoading = false
                mInterstitialAd = interstitialAd
                adMessage = "Inside Ad Loaded"
                Log.d(TAG, "AdmobInterstitialAd: onAdLoaded")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                isPreviousAdLoading = false
                adMessage = "Inside Ad Loading Failed Error : ${adError.message}"
                Log.d(TAG, "AdmobInterstitialAd: onAdFailedToLoad")
            }
        }
        if (!isPreviousAdLoading) {
            isPreviousAdLoading = true
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(ctx, id, adRequest, callback)
        }
    }

    fun Activity.showInterAd(
        callBack: () -> Unit
    ) {

        if (AdmobAppOpenAd.isShowingAd) {
            return
        }

        if (isPurchased) {
            callBack.invoke()
            return
        }



        if (loadingtype == "") {
            showPreloadInter {
                callBack.invoke()
            }
            return
        }

        if (inter_type == "timer") {
            showTimeBasedInter {
                callBack.invoke()
            }

            return
        }


        Log.d(TAG, "Counter == $inter_counter_start")


        if (inter_counter_start != 0 && mInterstitialAd == null) {
            callBack.invoke()
            inter_counter_start -= 1
            if (inter_counter_start <= 2) {
                load(this, inside_inter_ad_id)
            }
            return
        } else if (inter_counter_start == 0) {
            if (mInterstitialAd == null) {
                callBack.invoke()
                return
            }
        } else {
            inter_counter_start -= 1
            if (inter_counter_start > 0) {
                callBack.invoke()
                return
            }
        }

        val callback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                callBack.invoke()
                GlobalState.isInterShowing = false
                unblockTouches()
                shouldshowAd = false
                AdmobAppOpenAd.shouldshowAppOpen()
                if (inter_counter_start != 0 && inter_counter_start <= 2) {
                    load(this@showInterAd, inside_inter_ad_id)
                }

            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                mInterstitialAd = null
                runOnUiThread {
                    unblockTouches()
                }
                inter_counter_start = inter_counter_gap
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)
                callBack.invoke()
                shouldshowAd = false
                GlobalState.isInterShowing = false
                AdmobAppOpenAd.shouldshowAppOpen()
                unblockTouches()

            }
        }
        interCallback = callback
        shouldshowAd = true
        GlobalState.isInterShowing = true
        blockTouches(this)
        AdmobAppOpenAd.shouldshowAppOpen(false)
        val loadingView = showAdLoadingView()
        pendingActivity = this
        pendingLoadingView = loadingView
        adRunnable = Runnable {
            if (!isAppInForeground ||
                isFinishing ||
                isDestroyed ||
                !hasWindowFocus()
            ) {
                Log.d(TAG, "Ad skipped: app in background or activity invalid")
                hideAdLoadingView(loadingView)
                unblockTouches()
                callBack.invoke()
                shouldshowAd = false
                AdmobAppOpenAd.shouldshowAppOpen(true)
                GlobalState.isInterShowing = false
                return@Runnable
            }

            mInterstitialAd?.fullScreenContentCallback = callback
            hideAdLoadingView(loadingView)
            mInterstitialAd?.show(this)
        }

        adRunnable?.let { handler.postDelayed(it, 1500) }
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

    private fun Activity.showTimeBasedInter(
        callBack: () -> Unit,
    ) {

        if (isPurchased) {
            callBack.invoke()
            return
        }


        if (!isTimeReadyToShow() || mInterstitialAd == null) {
            callBack.invoke()
            return
        }

        val callback =
            object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                    lastInterShownTime = System.currentTimeMillis()
                    isFirstTimeInterShown = true
                    isTimeAdLoaded = false
                    GlobalState.isInterShowing = false
                    callBack.invoke()
                    shouldshowAd = false
                    AdmobAppOpenAd.shouldshowAppOpen()
                    scheduleTimeBasedLoad(this@showTimeBasedInter)
                    unblockTouches()
                }

                override fun onAdShowedFullScreenContent() {
                    mInterstitialAd = null
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    callBack.invoke()
                    shouldshowAd = false
                    GlobalState.isInterShowing = false
                    Log.d("isInterstitialAdShowing", "Failed to show")
                    AdmobAppOpenAd.shouldshowAppOpen()
                    unblockTouches()
                }
            }

        interCallback = callback

        shouldshowAd = true
        GlobalState.isInterShowing = true
        blockTouches(this)
        AdmobAppOpenAd.shouldshowAppOpen(false)
        val loadingView = showAdLoadingView()
        pendingActivity = this
        pendingLoadingView = loadingView

        adRunnable = Runnable {
            if (!isAppInForeground ||
                isFinishing ||
                isDestroyed ||
                !hasWindowFocus()
            ) {
                Log.d(TAG, "Ad skipped: app in background or activity invalid")
                hideAdLoadingView(loadingView)
                unblockTouches()
                callBack.invoke()
                shouldshowAd = false
                AdmobAppOpenAd.shouldshowAppOpen(true)
                GlobalState.isInterShowing = false
                return@Runnable
            }
            hideAdLoadingView(loadingView)
            mInterstitialAd?.fullScreenContentCallback = callback
            mInterstitialAd?.show(this)
            AdmobAppOpenAd.shouldshowAppOpen(false)

        }
        adRunnable?.let { handler.postDelayed(it, 1500) }
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
            CoroutineScope(Dispatchers.Main).launch {

                blockedActivity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                backCallback?.remove()
                backCallback = null

                blockedActivity = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}