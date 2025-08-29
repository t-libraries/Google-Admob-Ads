package com.admobads.ads

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import com.admobads.ads.databinding.TlibAdLoadingDialogBinding
import com.admobads.ads.utils.isNetworkAvailable
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdmobInterstitialAd {
    private var splashInterstitialAd: InterstitialAd? = null

    private var TAG = "AdmobInterstitialAd_"

    private var mInterstitialAd: InterstitialAd? = null
    private var isPreviousAdLoading = false
    private var isPurchased = false
    private var inter_counter_start = 2
    private var inter_counter_start_inside = 2
    private var inter_counter_gap = 3
    private var inter_counter_gap_inside = 3
    private var inside_inter_ad_id = ""

    private var shouldLoadAd = false

    private var dialogBackgroundColor = "#F8F8F8".toColorInt()
    private var dialogTextColor = Color.BLACK


    fun setPurchased(isPurchased: Boolean = false) {
        this.isPurchased = isPurchased
    }


    fun setLoadingDialogBgColor(loadingDialogBgColor: Int) {
        this.dialogBackgroundColor = loadingDialogBgColor
        AdmobAppOpenAd.setDialogBGColor(loadingDialogBgColor)
    }

    fun setLoadingDialogTextColor(loadingDialogTextColor: Int) {
        this.dialogTextColor = loadingDialogTextColor
        AdmobAppOpenAd.setDialogTextColor(loadingDialogTextColor)
    }

    fun initValues(
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
            Log.d(TAG , "Both are 0")
            shouldLoadAd = false
        } else if (inter_counter_start != 0 && inter_counter_start <= 2) {
            Log.d(TAG , "inter_counter_start = ${inter_counter_start}")
            shouldLoadAd = true
            load(context, inside_inter_ad_id)
        } else {
            Log.d(TAG , "inter_counter_gap = ${inter_counter_gap}")
            shouldLoadAd = true
            if (inter_counter_gap != 0 && inter_counter_gap <= 2) {
                load(context, inside_inter_ad_id)
            }
        }

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
                isPreviousAdLoading = false
                splashInterstitialAd = interstitialAd
                onAdLoaded.invoke()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
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

    fun Activity.showSplashInterAd(callBack: () -> Unit) {
        if (isPurchased) {
            callBack.invoke()
            return
        }

        if (splashInterstitialAd == null) {
            callBack.invoke()
            return
        }

        val dialog = dialogAdLoading()
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            splashInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    splashInterstitialAd = null
                    AdmobAppOpenAd.shouldshowAppOpen()
                    callBack.invoke()
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    splashInterstitialAd = null
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    callBack.invoke()
                    AdmobAppOpenAd.shouldshowAppOpen()
                }
            }

            splashInterstitialAd?.show(this)
            AdmobAppOpenAd.shouldshowAppOpen(false)
        }, 1500)
    }


    fun load(ctx: Context, id: String) {

        if (isPurchased) {
            return
        }

        if (!shouldLoadAd) {
            return
        }

        if (!isNetworkAvailable(ctx)) {
            return
        }

        if (mInterstitialAd != null) {
            return
        }

        if (splashInterstitialAd != null) {
            mInterstitialAd = splashInterstitialAd
            splashInterstitialAd = null
            return
        }


        val callback = object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                isPreviousAdLoading = false
                mInterstitialAd = interstitialAd
                Log.d(TAG, "AdmobInterstitialAd: onAdLoaded")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                isPreviousAdLoading = false
                Log.d(TAG, "AdmobInterstitialAd: onAdFailedToLoad")
            }
        }
        if (!isPreviousAdLoading) {
            isPreviousAdLoading = true
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(ctx, id, adRequest, callback)
        }
    }

    fun Activity.showInterAd(callBack: () -> Unit) {
        if (isPurchased) {
            callBack.invoke()
            return
        }

        if (inter_counter_start != 0 && mInterstitialAd == null) {
            callBack.invoke()
            inter_counter_start -= 1
            if (inter_counter_start <= 2) {
                load(this, inside_inter_ad_id)
            }
            return
        } else if (inter_counter_start == 0) {
            callBack.invoke()
            return
        } else {
            inter_counter_start -= 1
            if (inter_counter_start > 0) {
                callBack.invoke()
                return
            }
        }


        val dialog = dialogAdLoading()
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    callBack.invoke()
                    AdmobAppOpenAd.shouldshowAppOpen()
                    if (inter_counter_start != 0 && inter_counter_start <= 2) {
                        load(this@showInterAd, inside_inter_ad_id)
                    }
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    mInterstitialAd = null
                    inter_counter_start = inter_counter_gap
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    callBack.invoke()
                    AdmobAppOpenAd.shouldshowAppOpen()

                }
            }

            mInterstitialAd?.show(this)
            AdmobAppOpenAd.shouldshowAppOpen(false)
        }, 1500)
    }

    fun Activity.showInterAd(intent: Intent? = null, finish: Boolean = false) {
        if (isPurchased) {
            intent?.let { startActivity(intent) }
            if (finish) finish()
            return
        }

        Log.d("intercounterstart", "$inter_counter_start")

        if (inter_counter_start != 0 && mInterstitialAd == null) {
            inter_counter_start -= 1
            if (inter_counter_start <= 2) {
                load(this, inside_inter_ad_id)
            }
            intent?.let { startActivity(intent) }
            if (finish) finish()
            return
        } else if (inter_counter_start == 0) {
            intent?.let { startActivity(intent) }
            if (finish) finish()
            return
        } else {
            inter_counter_start -= 1
            if (inter_counter_start > 0) {
                intent?.let { startActivity(intent) }
                if (finish) finish()
                return
            }
        }


        val dialog = dialogAdLoading()
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    intent?.let { startActivity(intent) }
                    if (finish) finish()
                    AdmobAppOpenAd.shouldshowAppOpen()
                    if (inter_counter_start != 0 && inter_counter_start <= 2) {
                        load(this@showInterAd, inside_inter_ad_id)
                    }

                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    mInterstitialAd = null
                    inter_counter_start = inter_counter_gap
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    intent?.let { startActivity(intent) }
                    if (finish) finish()
                    AdmobAppOpenAd.shouldshowAppOpen()
                }
            }

            mInterstitialAd?.show(this)
            AdmobAppOpenAd.shouldshowAppOpen(false)
        }, 1500)
    }

    fun Activity.dialogAdLoading(): Dialog {
        val dialog = Dialog(this)
        val dialogBinding = TlibAdLoadingDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        dialogBinding.textView21.setTextColor(dialogTextColor)
        dialogBinding.progressBar.indeterminateTintList = ColorStateList.valueOf(dialogTextColor)
        dialogBinding.mainLayout.setBackgroundColor(dialogBackgroundColor)

        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                return@setOnKeyListener true
            }
            false
        }
        if (!dialog.isShowing)
            dialog.show()
        return dialog
    }
}