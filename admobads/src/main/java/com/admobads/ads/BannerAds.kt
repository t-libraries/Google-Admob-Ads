package com.admobads.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Suppress("DEPRECATION")
class BannerAds(
    var context: Activity,
    var bannerGoogleAdContainer: FrameLayout,
    var llLoading: LinearLayout,
    var llAds: LinearLayout,
    var lladslayout : LinearLayout
) {

    private lateinit var googleBannerAdView: AdView
    private var initialLayoutComplete = false
    private val TAG: String = "BannerAds"

    fun callBothBannerAds(adId: String , type: String) {
        llAds.visibility = View.GONE
        llLoading.visibility = View.VISIBLE
        if (isNetworkAvailable(context)) {
            loadGoogleBannerAds(adId , type)
        } else {
            lladslayout?.visibility = View.GONE
            llAds.visibility = View.GONE
            llLoading.visibility = View.GONE
            bannerGoogleAdContainer.visibility = View.GONE
        }
    }


    fun callBothBannerAdsTop(adId: String , type: String) {
        llAds.visibility = View.GONE
        llLoading.visibility = View.VISIBLE
        if (isNetworkAvailable(context)) {
            loadGoogleBannerAdsTop(adId , type)
        } else {
            lladslayout?.visibility = View.GONE
            llAds.visibility = View.GONE
            llLoading.visibility = View.GONE
            bannerGoogleAdContainer.visibility = View.GONE
        }
    }

    private fun loadGoogleBannerAds(adId: String , type : String) {
        Log.e(TAG, "Admob Banner Called")
        googleBannerAdView = AdView(context)
        googleBannerAdView.adUnitId = adId
        bannerGoogleAdContainer.addView(googleBannerAdView)
        bannerGoogleAdContainer.viewTreeObserver.addOnGlobalLayoutListener {
            if (!initialLayoutComplete) {
                initialLayoutComplete = true
                if (type == "simple"){
                    loadGoogleBannerFunction()
                }
                else{
                    loadCollapasableGoogleBannerFunction()
                }
            }
        }
    }

    private fun loadGoogleBannerAdsTop(adId: String , type : String) {
        Log.e(TAG, "Admob Banner Called")
        googleBannerAdView = AdView(context)
        googleBannerAdView.adUnitId = adId
        bannerGoogleAdContainer.addView(googleBannerAdView)
        bannerGoogleAdContainer.viewTreeObserver.addOnGlobalLayoutListener {
            if (!initialLayoutComplete) {
                initialLayoutComplete = true
                if (type == "simple"){
                    loadGoogleBannerFunction()
                }
                else{
                    loadCollapasableGoogleBannerFunctionTop()
                }
            }
        }
    }

    private fun loadGoogleBannerFunction() {
        val adSize = adSize
        googleBannerAdView.setAdSize(adSize)
        val extras = Bundle()
        extras.putString("collapsible", "bottom")
        val adRequest: AdRequest = AdRequest.Builder().build()
//            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
//            .build()

        googleBannerAdView.loadAd(adRequest)
        googleBannerAdView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                lladslayout?.visibility = View.GONE
                llLoading.visibility = View.GONE
                llAds.visibility = View.GONE
                bannerGoogleAdContainer.visibility = View.GONE
                Log.e(TAG, "Load Banner Error ${p0.message}")
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                lladslayout.visibility = View.VISIBLE
                llAds.visibility = View.VISIBLE
                llLoading.visibility = View.GONE
                bannerGoogleAdContainer.visibility = View.VISIBLE
                Log.e(TAG, "Banner Ad Loaded ")
            }
        }
    }


    private fun loadCollapasableGoogleBannerFunction() {
        val adSize = adSize
        googleBannerAdView.setAdSize(adSize)
        val extras = Bundle()
        extras.putString("collapsible", "bottom")
        val adRequest: AdRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()

        googleBannerAdView.loadAd(adRequest)
        googleBannerAdView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                lladslayout.visibility = View.GONE
                llLoading.visibility = View.GONE
                llAds.visibility = View.GONE
                bannerGoogleAdContainer.visibility = View.GONE
                Log.e(TAG, "Load Banner Error ${p0.message}")
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                lladslayout.visibility = View.VISIBLE
                llAds.visibility = View.VISIBLE
                llLoading.visibility = View.GONE
                bannerGoogleAdContainer.visibility = View.VISIBLE
                Log.e(TAG, "Banner Ad Loaded ")
            }
        }
    }

    private fun loadCollapasableGoogleBannerFunctionTop() {
        val adSize = adSize
        googleBannerAdView.setAdSize(adSize)
        val extras = Bundle()
        extras.putString("collapsible", "top")
        val adRequest: AdRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()

        googleBannerAdView.loadAd(adRequest)
        googleBannerAdView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                lladslayout.visibility = View.GONE
                llLoading.visibility = View.GONE
                llAds.visibility = View.GONE
                bannerGoogleAdContainer.visibility = View.GONE
                Log.e(TAG, "Load Banner Error ${p0.message}")
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                lladslayout.visibility = View.VISIBLE
                llAds.visibility = View.VISIBLE
                llLoading.visibility = View.GONE
                bannerGoogleAdContainer.visibility = View.VISIBLE
                Log.e(TAG, "Banner Ad Loaded ")
            }
        }
    }

    private val adSize: AdSize
        get() {
            val display = context.windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = bannerGoogleAdContainer.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        }


    private fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }
}