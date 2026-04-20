package com.admobads.ads

import android.app.Activity
import android.app.Application
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
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
import com.admobads.ads.utils.isNetworkAvailable
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.material.card.MaterialCardView

class AdmobAppOpenAd(
    private val applicationContext: Application,
    private val ad_Id: String,
    private val exceptionalActivities: List<String> = emptyList()
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {
    private var appOpenAd: AppOpenAd? = null
    private var loadCallback: AppOpenAd.AppOpenAdLoadCallback? = null
    private var currentActivity: Activity? = null
    private var TAG = "+openapp"
    private var isLoadingAd = false
    private var isColdStart = true
    private var blockedActivity: Activity? = null
    var loadingView: View? = null
    private var currentComposeLoadingView: View? = null
    private var backCallback: androidx.activity.OnBackPressedCallback? = null
    private var adMessage = ""


    override fun onStart(owner: LifecycleOwner) {
        try {

            Log.d(TAG, "onStart: Coming Inside $isColdStart")

            if (isColdStart) {
                isColdStart = false
                return
            }

            if (shouldShowAppOpenAd()) {
                currentActivity?.let {
                    if (isNetworkAvailable(it)) {
                        loadAd()
                    }
                }

            }

        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }


    fun loadAd() {

        if (isLoadingAd || isAppOpenAdAvailable() || isShowingAd) {
            return
        }

        if (isPurchased) {
            return
        }


        currentActivity?.let {
            blockTouches(it)
        }
        loadingView = currentActivity?.showAdLoadingView()

        isLoadingAd = true

        loadCallback =
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(appOpenAd: AppOpenAd) {
                    this@AdmobAppOpenAd.appOpenAd = appOpenAd
                    Log.d(LOG_TAG, "App Open Ad Loaded")
                    isLoadingAd = false
                    adMessage = "App Open Loaded"
                    currentActivity?.hideAdLoadingView(loadingView)
                    showAdIfAvailable()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("error", loadAdError.message)
                    isLoadingAd = false
                    adMessage = "App Open Loading Failed Error : ${loadAdError.message}"
                    Log.d(LOG_TAG, "App Open Ad Failed")
                    currentActivity?.hideAdLoadingView(loadingView)
                }
            }

        try {
            val request = adRequest
            ad_Id.let {
                loadCallback?.let { it1 ->
                    AppOpenAd.load(
                        applicationContext,
                        ad_Id,
                        request,
                        it1
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }


    private fun isAppOpenAdAvailable(): Boolean {
        return appOpenAd != null
    }

    fun showAdIfAvailable(
    ) {

        Log.d(LOG_TAG, "Load $isPurchased , $isShowingAd")

        if (isPurchased) {
            return
        }

        if (isShowingAd) {
            return
        }

        Log.d(LOG_TAG, "Will show ad.")
        appOpenAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                unblockTouches()

            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false
                unblockTouches()

            }

            override fun onAdShowedFullScreenContent() {
                Log.d(LOG_TAG, "onAdShowedFullScreenContent.")
            }
        }
        isShowingAd = true
        currentActivity?.let {

            appOpenAd!!.show(it)
        }
    }


    /**
     * Creates and returns ad request.
     */
    private val adRequest: AdRequest
        get() {
            return AdRequest.Builder().build()
        }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {

        try {
            currentActivity?.hideAdLoadingView(loadingView)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        currentActivity = null

    }

    companion object {
        private const val LOG_TAG = "AppOpenManager"
        private var isShowingAd = false
        private var isPurchased = false
        private var isComposed = false
        private var shouldshowAppOpen = true

        private var dialogTextColor = Color.BLACK
        private var dialogBackgroundColor = "#F8F8F8".toColorInt()
        fun setPurchased(isPurchase: Boolean = false) {
            isPurchased = isPurchase
        }

        fun setComposed(isCompose: Boolean = false) {
            isComposed = isCompose
        }

        fun shouldshowAppOpen(isInterstitialShowing: Boolean = true) {
            shouldshowAppOpen = isInterstitialShowing
        }

        fun setDialogTextColor(textcolor: Int) {
            this.dialogTextColor = textcolor
        }

        fun setDialogBGColor(bgcolor: Int) {
            this.dialogBackgroundColor = bgcolor
        }
    }

    /**
     * Constructor
     */
    init {
        applicationContext.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }


    fun Activity.showAdLoadingView(): View {
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

            loadingView.findViewById<TextView>(R.id.textView21).text =
                getString(R.string.tlib_loading)
            loadingView.findViewById<MaterialCardView>(R.id.dialogBg)
                .setCardBackgroundColor(dialogBackgroundColor)

            rootView.addView(loadingView)
            return loadingView
        }
    }

    fun Activity.hideAdLoadingView(loadingView: View?) {
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


    private fun shouldShowAppOpenAd(): Boolean {
        val activityName = currentActivity?.javaClass?.simpleName ?: return false

        return shouldshowAppOpen &&
                !activityName.contains("splash", ignoreCase = true) &&
                !activityName.contains("iap", ignoreCase = true) &&
                !activityName.contains("premium", ignoreCase = true) &&
                !activityName.contains("subscription", ignoreCase = true) &&
                !exceptionalActivities.contains(activityName)

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