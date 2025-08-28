package com.admobads.ads

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColorInt
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.admobads.ads.utils.isNetworkAvailable
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

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
    private var waiting_dialog: Dialog? = null
    private var isColdStart = true


    override fun onStart(owner: LifecycleOwner) {
        try {
            Log.d(TAG, "onStart: Coming Inside $isColdStart")

            if (isColdStart) {
                isColdStart = false // Only skip on first launch
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
            dismissWaitingDialog()
            return
        }

        if (isPurchased) {
            dismissWaitingDialog()
            return
        }

        currentActivity?.let {
            showWaitingDialog(it)
        }

        isLoadingAd = true

        loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(appOpenAd: AppOpenAd) {
                this@AdmobAppOpenAd.appOpenAd = appOpenAd
                Log.d(LOG_TAG, "App Open Ad Loaded")
                isLoadingAd = false
                dismissWaitingDialog()
                showAdIfAvailable()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e("error", loadAdError.message)
                isLoadingAd = false
                Log.d(LOG_TAG, "App Open Ad Failed")
                dismissWaitingDialog()
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

        if (isPurchased) {
            return
        }

        if (isShowingAd) {
            return
        }


        if (!isAppOpenAdAvailable()) {
            dismissWaitingDialog()
            return
        }

        Log.d(LOG_TAG, "Will show ad.")
        appOpenAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
            /** Called when full screen content is dismissed. */
            override fun onAdDismissedFullScreenContent() {
                // Set the reference to null so isAdAvailable() returns false.
                appOpenAd = null
                isShowingAd = false

            }

            /** Called when fullscreen content failed to show. */
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false

            }

            /** Called when fullscreen content is shown. */
            override fun onAdShowedFullScreenContent() {
                Log.d(LOG_TAG, "onAdShowedFullScreenContent.")
            }

        }
        isShowingAd = true
        currentActivity?.let { appOpenAd!!.show(it) }
    }


    /**
     * Creates and returns ad request.
     */
    private val adRequest: AdRequest
        get() {
            return AdRequest.Builder().build()
        }

    /*   && wasLoadTimeLessThanNHoursAgo(4)*/

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
        currentActivity = null
    }

    companion object {
        private const val LOG_TAG = "AppOpenManager"
        private var isShowingAd = false
        private var isPurchased = false
        private var shouldshowAppOpen = true

        private var dialogTextColor = Color.BLACK
        private var dialogBackgroundColor = "#F8F8F8".toColorInt()
        fun setPurchased(isPurchase: Boolean = false) {
            isPurchased = isPurchase
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

    private fun showWaitingDialog(context: Context) {
        try {
            waiting_dialog = Dialog(context)
            waiting_dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            waiting_dialog?.setCancelable(false)
            waiting_dialog?.setContentView(R.layout.tlib_ad_loading_dialog)
            waiting_dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            waiting_dialog?.findViewById<TextView>(R.id.textView21)?.text =
                context.getString(R.string.tlib_loading)

            waiting_dialog?.findViewById<TextView>(R.id.textView21)?.setTextColor(dialogTextColor)
            waiting_dialog?.findViewById<ProgressBar>(R.id.progressBar)?.indeterminateTintList =
                ColorStateList.valueOf(dialogTextColor)
            waiting_dialog?.findViewById<ConstraintLayout>(R.id.mainLayout)
                ?.setBackgroundColor(dialogBackgroundColor)

            waiting_dialog?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun dismissWaitingDialog() {
        waiting_dialog?.dismiss()
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

}