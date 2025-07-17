package com.admobads.ads

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import androidx.core.graphics.drawable.toDrawable
import com.admobads.ads.databinding.TlibAdLoadingDialogBinding
import com.admobads.ads.utils.isNetworkAvailable
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import androidx.core.graphics.toColorInt

object AdmobInterstitialAd {
    private var splashInterstitialAd : InterstitialAd? = null

    private var mInterstitialAd: InterstitialAd? = null
    private var isPreviousAdLoading = false
    private var isPurchased = false
    private var inter_counter_start = 2
    private var inter_counter_gap = 3
    private var inside_inter_ad_id = ""

    private var dialogBackgroundColor = "#F8F8F8".toColorInt()
    private var dialogTextColor = Color.BLACK


    fun isPurchased(isPurchased: Boolean = false) {
        this.isPurchased = isPurchased
    }


    fun setLoadingDialogBgColor(loadingDialogBgColor: Int) {
        this.dialogBackgroundColor = loadingDialogBgColor
    }

    fun setLoadingDialogTextColor(loadingDialogTextColor: Int) {
        this.dialogTextColor = loadingDialogTextColor
    }

    fun initValues(
        inter_counter_start: Int = 2,
        inter_counter_gap: Int = 3,
        inside_inter_ad_id: String
    ) {
        this.inter_counter_start = inter_counter_start
        this.inter_counter_gap = inter_counter_gap
        this.inside_inter_ad_id = inside_inter_ad_id
    }

    fun loadSplashInter(ctx: Activity, id: String) {
        if (!isNetworkAvailable(ctx)) {
            return
        }
        if (splashInterstitialAd != null) {
            return
        }
        val callback = object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                isPreviousAdLoading = false
                splashInterstitialAd = interstitialAd
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                isPreviousAdLoading = false
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
                    callBack.invoke()
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    splashInterstitialAd = null
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    callBack.invoke()
                }
            }

            splashInterstitialAd?.show(this)
        }, 1500)
    }


    fun load(ctx: Activity, id: String) {
        if (!isNetworkAvailable(ctx)) {
            return
        }
        if (mInterstitialAd != null) {
            return
        }
        val callback = object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                isPreviousAdLoading = false
                mInterstitialAd = interstitialAd
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                isPreviousAdLoading = false
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
        if (mInterstitialAd == null) {
            inter_counter_start -= 1
            if (inter_counter_start.toInt() <= 1) {
                inter_counter_start = inter_counter_gap
                load(this, inside_inter_ad_id)
            }
            callBack.invoke()
            return
        }
        val dialog = dialogAdLoading()
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    callBack.invoke()
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    mInterstitialAd = null
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    callBack.invoke()
                }
            }

            mInterstitialAd?.show(this)
        }, 1500)
    }

    fun Activity.showInterAd(intent: Intent? = null, finish: Boolean = false) {
        if (isPurchased) {
            intent?.let { startActivity(intent) }
            if (finish) finish()
            return
        }
        if (mInterstitialAd == null) {
            inter_counter_start -= 1
            if (inter_counter_start.toInt() <= 1) {
                inter_counter_start = inter_counter_gap
                load(this, inside_inter_ad_id)
            }
            intent?.let { startActivity(intent) }
            if (finish) finish()
            return
        }
        val dialog = dialogAdLoading()
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            intent?.let { startActivity(intent) }
            if (finish) finish()
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()

                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    mInterstitialAd = null
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                }
            }

            mInterstitialAd?.show(this)
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
                return@setOnKeyListener true // Consume the event (prevent closing)
            }
            false
        }
        if (!dialog.isShowing)
            dialog.show()
        return dialog
    }
}