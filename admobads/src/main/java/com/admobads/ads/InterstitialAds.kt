package com.admobads.ads

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.Window
import com.admobads.ads.utils.isNetworkAvailable
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialAds() {


    private var waiting_dialog: Dialog? = null
    var TAG = "interadd"

    companion object {
        var context: Context? = null
        var isFirstInterEnable = false
        var isCounterInterEnable = false
        var firstclickInterAdId = ""
        var counterclickInterAd = ""
        var firstInterCounter = 2L
        var counterInterCounter = 3L
        var isPurchased = false

        var firstclickInter: InterstitialAd? = null
        var counterInter: InterstitialAd? = null

        var isInterRequestPending = false

        var isfirstclickinterShowed = false

        var firstclick_inter_count = 1
        var counter_inter_count = 1

        private var mInstance: InterstitialAds? = null
        fun getInstance(): InterstitialAds? {
            if (mInstance == null) {
                mInstance = InterstitialAds()
            }
            return mInstance
        }
    }


    fun initValues(
        first_InterCounter : Long,
        counter_InterCounter: Long,
        isPurched : Boolean,
        context1: Context?,
        firstclickInterAdId1: String?,
        counterclickInterAd1: String?
    ) {
        isInterRequestPending = false
        isPurchased = isPurched
        context = context1
        if (firstclickInterAdId1 != null) {
            firstclickInterAdId = firstclickInterAdId1
        }
        if (counterclickInterAd1 != null) {
            counterclickInterAd = counterclickInterAd1
        }

        firstInterCounter =
            first_InterCounter
        counterInterCounter =
            counter_InterCounter

        if (firstInterCounter > 0){
            isFirstInterEnable = true
        }
        else {
            isFirstInterEnable = false
        }

        if (counterInterCounter > 0){
            isCounterInterEnable = true
        }
        else {
            isCounterInterEnable = false
        }

        if (isFirstInterEnable) {
            context?.let { firstclickInterAdId.let { it1 -> loadAdFirstInter(it, it1) } }
        } else {
            if (isCounterInterEnable) {
                isfirstclickinterShowed = true
                context?.let { counterclickInterAd.let { it1 -> loadAdCounterInter(it, it1) } }
            }
        }
    }


    fun loadAdFirstInter(context: Context, id: String) {
        if (firstclickInter == null && !isPurchased && isNetworkAvailable(context) && !isInterRequestPending) {
            isInterRequestPending = true
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                id,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        isInterRequestPending = false
                        Log.d(TAG, "loadAd: ==========${adError.message}")
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        firstclickInter = interstitialAd
                        isInterRequestPending = false
                        Log.d(TAG, "loadAd: ==========adloded")

                    }
                })
        }
    }

    fun loadAdCounterInter(context: Context, id: String) {
        if (counterInter == null && !isPurchased && isNetworkAvailable(context) && !isInterRequestPending) {
            isInterRequestPending = true
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                id,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        isInterRequestPending = false
                        Log.d(TAG, "loadAd: ==========${adError.message}")
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        counterInter = interstitialAd
                        isInterRequestPending = false
                        Log.d(TAG, "loadAd: ==========adloded")
                    }
                })
        }
    }


    private fun showAdIfLoaded(
        activity: Activity,
        callbackMethod: () -> Unit,
        interstitialAd: InterstitialAd
    ) {
        try {
            interstitialAd.show(activity)
            interstitialAd.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        Log.d(TAG, "showIfLoaded: onadddismiss")
                        if (isfirstclickinterShowed) {
                            firstclickInter = null
                            counterInter = null
                            counter_inter_count = 1
                            if (isCounterInterEnable) {
                                counterclickInterAd.let { loadAdCounterInter(activity, it) }
                            }
                        } else {
                            firstclickInter = null
                        }
                        callbackMethod()
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        if (isfirstclickinterShowed) {
                            firstclickInter = null
                            counterInter = null
                            counter_inter_count = 0
                            if (isCounterInterEnable) {
                                counterclickInterAd.let { loadAdCounterInter(activity, it) }
                            }
                        } else {
                            firstclickInter = null
                        }
                        callbackMethod()
                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                    }
                }
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }


    fun calculate_clicks(activity: Activity, callbackMethod: () -> Unit) {
        Log.d(TAG, "First Click = $firstclick_inter_count Counter Click = $counter_inter_count")
        if (!isfirstclickinterShowed) {
            if (firstclick_inter_count >= firstInterCounter) {
                if (firstclickInter != null) {
                    isfirstclickinterShowed = true
                    showWaitingDialog(activity)
                    Handler().postDelayed({
                        dismissWaitingDialog()
                        firstclickInter?.let {
                            showAdIfLoaded(activity, callbackMethod, it)
                        } ?: callbackMethod()
                    }, 1500)
                } else {
                    if (firstInterCounter > 0)
                        loadAdFirstInter(activity, firstclickInterAdId)
                    callbackMethod()
                }
            } else {
                firstclick_inter_count++
                callbackMethod()
            }
        } else {
            if (counter_inter_count >= counterInterCounter) {
                if (counterInter != null) {
                    showWaitingDialog(activity)
                    Handler().postDelayed({
                        dismissWaitingDialog()
                        counterInter?.let {
                            showAdIfLoaded(activity, callbackMethod, it)
                        } ?: callbackMethod()
                    }, 1500)
                } else {
                    if (counterInterCounter > 0)
                        loadAdCounterInter(activity, counterclickInterAd)
                    callbackMethod()
                }
            } else {
                counter_inter_count++
                callbackMethod()
            }
        }
    }


    private fun showWaitingDialog(context: Context) {
        try {
            waiting_dialog = Dialog(context)
            waiting_dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            waiting_dialog?.setCancelable(false)
            waiting_dialog?.setContentView(R.layout.ad_loading_dialog)
            waiting_dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            waiting_dialog?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun dismissWaitingDialog() {
        try {
            waiting_dialog?.dismiss()
        }
        catch (e : IllegalArgumentException){
            e.printStackTrace()
        }
    }

}