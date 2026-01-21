package com.admobads.ads


import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.admobads.loading.skeleton.layout.SkeletonConstraintLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

class AdmobBannerAd(
    private val context: Activity,
    private val bannerAdContainer: FrameLayout
) {
    private var TAG = "AdmobBannerAd"
    private var adType: BannerAdType? = null
    private lateinit var adView: AdView
    private var initialLayoutComplete = false
    private lateinit var loadingLayout: ConstraintLayout
    private lateinit var adsLayout: LinearLayout
    private lateinit var adsParentLayout: LinearLayout
    private var skeletonConstraintLayout: SkeletonConstraintLayout? = null
    private var skeltoncolor: Int = 0
    private var backgroundcolor: Int = 0


    fun setSkeletonColor(color: Int): AdmobBannerAd {
        this.skeltoncolor = color
        return this
    }

    fun loadBannerAd(
        adUnitId: String,
        adType: Int,
        position: BannerPosition = BannerPosition.BOTTOM
    ) {
        loadBannerAd(adUnitId, BannerAdType.fromInt(adType), position)
    }


    fun loadBannerAd(
        adUnitId: String,
        adType: BannerAdType,
        position: BannerPosition = BannerPosition.BOTTOM
    ) {
        this.adType = adType
        setupViewHierarchy()
        showLoadingState()

//        if (!isNetworkAvailable(context)) {
//            hideAllAdViews()
//            return
//        }

        when (position) {
            BannerPosition.BOTTOM -> loadBannerAd(adUnitId, adType)
            BannerPosition.TOP -> loadTopBannerAd(adUnitId, adType)
        }
    }

    private fun setupViewHierarchy() {
        adsParentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        loadingLayout = returnLoadingLayout(adType)


        adsLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            visibility = View.GONE
        }

        adsParentLayout.addView(loadingLayout)
        adsParentLayout.addView(adsLayout)
        bannerAdContainer.addView(adsParentLayout)
    }

    private fun returnLoadingLayout(adType: BannerAdType? = null): ConstraintLayout {
        val layoutRes = when (adType) {
            BannerAdType.STANDARD -> R.layout.tlib_banner_standard_loading
            BannerAdType.LARGE_BANNER -> R.layout.tlib_banner_full_loading
            BannerAdType.MEDIUM_RECTANGLE -> R.layout.tlib_banner_rectangle_loading
            BannerAdType.COLLAPSIBLE -> R.layout.tlib_banner_standard_loading
            null -> R.layout.tlib_banner_standard_loading
        }

        loadingLayout = LayoutInflater.from(context).inflate(
            layoutRes,
            null,
            false
        ) as ConstraintLayout



        loadingLayout.apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            visibility = View.GONE
        }

        skeletonConstraintLayout = loadingLayout.findViewById(R.id.skeletonLayout)

        if (backgroundcolor != 0)
            skeletonConstraintLayout?.setBackgroundColor(
                context.resources.getColor(backgroundcolor)
            )

        if (skeltoncolor != 0)
            skeletonConstraintLayout?.setSkeletonColor(skeltoncolor)


        return loadingLayout
    }

    private fun loadBannerAd(adUnitId: String, adType: BannerAdType) {
        val adSize = getAdSizeForType(adType)
        initializeAdView(adUnitId, adSize)

        bannerAdContainer.viewTreeObserver.addOnGlobalLayoutListener {
            if (!initialLayoutComplete) {
                initialLayoutComplete = true
                when (adType) {
                    BannerAdType.STANDARD,
                    BannerAdType.LARGE_BANNER,
                    BannerAdType.MEDIUM_RECTANGLE -> loadStandardBanner()

                    BannerAdType.COLLAPSIBLE -> loadCollapsibleBanner(position = "bottom")
                }
            }
        }
    }

    private fun loadTopBannerAd(adUnitId: String, adType: BannerAdType) {
        val adSize = getAdSizeForType(adType)
        initializeAdView(adUnitId, adSize)

        bannerAdContainer.viewTreeObserver.addOnGlobalLayoutListener {
            if (!initialLayoutComplete) {
                initialLayoutComplete = true
                when (adType) {
                    BannerAdType.STANDARD,
                    BannerAdType.LARGE_BANNER,
                    BannerAdType.MEDIUM_RECTANGLE -> loadStandardBanner()
                    BannerAdType.COLLAPSIBLE -> loadCollapsibleBanner(position = "top")
                }
            }
        }
    }

    private fun initializeAdView(adUnitId: String, adSize: AdSize) {
        adsLayout.removeAllViews()

        adView = AdView(context).apply {
            this.adUnitId = adUnitId
            setAdSize(adSize)
        }
        adsLayout.addView(adView)
    }

    private fun loadStandardBanner() {
        val adRequest = AdRequest.Builder().build()
        skeletonConstraintLayout?.startLoading()
        loadAdWithListener(adRequest)
    }

    private fun loadCollapsibleBanner(position: String) {
        val extras = Bundle().apply {
            putString("collapsible", position)
        }
        skeletonConstraintLayout?.startLoading()
        val adRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()

        loadAdWithListener(adRequest)
    }

    private fun loadAdWithListener(adRequest: AdRequest) {
        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.d(TAG, "Failed to load ad: $error")
//                hideAllAdViews()
            }

            override fun onAdLoaded() {
                Log.d(TAG, "Ad is loaded")
                showAdViews()
            }
        }
    }

    private fun getAdSizeForType(adType: BannerAdType): AdSize {
        return when (adType) {
            BannerAdType.STANDARD -> adSize
            BannerAdType.LARGE_BANNER -> large_banner_adSize
            BannerAdType.MEDIUM_RECTANGLE -> AdSize.MEDIUM_RECTANGLE
            BannerAdType.COLLAPSIBLE -> {
                val display = context.windowManager.defaultDisplay
                val outMetrics = DisplayMetrics()
                display.getMetrics(outMetrics)
                val density = outMetrics.density
                var adWidthPixels = bannerAdContainer.width.toFloat()
                if (adWidthPixels == 0f) {
                    adWidthPixels = outMetrics.widthPixels.toFloat()
                }
                val adWidth = (adWidthPixels / density).toInt()
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
            }
        }
    }

    private fun showLoadingState() {
        adsLayout.visibility = View.GONE
        loadingLayout.visibility = View.VISIBLE
        adsParentLayout.visibility = View.VISIBLE
    }

    private fun showAdViews() {
        adsParentLayout.visibility = View.VISIBLE
        adsLayout.visibility = View.VISIBLE
        loadingLayout.visibility = View.GONE
    }

    private fun hideAllAdViews() {
        adsParentLayout.visibility = View.GONE
        adsLayout.visibility = View.GONE
        loadingLayout.visibility = View.GONE
    }

    fun destroy() {
        bannerAdContainer.removeAllViews()
        adView.destroy()
    }


    private val adSize: AdSize
        get() {
            val outMetrics = context.resources.displayMetrics
            val density = outMetrics.density

            // Always use screen width
            val adWidthPixels = outMetrics.widthPixels.toFloat()
            val adWidth = (adWidthPixels / density).toInt()

            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        }


    private val large_banner_adSize: AdSize
        get() {
            val displayMetrics = context.resources.displayMetrics
            val density = displayMetrics.density

            val screenWidthPixels = displayMetrics.widthPixels.toFloat()
            val adWidthInDp = (screenWidthPixels / density).toInt()

            val largeBannerHeightDp = AdSize.LARGE_BANNER.height

            return AdSize(adWidthInDp, largeBannerHeightDp)
        }

    private val full_banner_adSize: AdSize
        get() {
            val displayMetrics = context.resources.displayMetrics
            val density = displayMetrics.density

            val screenWidthPixels = displayMetrics.widthPixels.toFloat()
            val adWidthInDp = (screenWidthPixels / density).toInt()

            val largeBannerHeightDp = AdSize.FULL_BANNER.height

            return AdSize(adWidthInDp, largeBannerHeightDp)
        }



}

enum class BannerAdType {
    STANDARD,
    LARGE_BANNER,
    MEDIUM_RECTANGLE,
    COLLAPSIBLE;

    companion object {
        fun fromInt(value: Int): BannerAdType {
            return when (value) {
                4 -> LARGE_BANNER
                3 -> MEDIUM_RECTANGLE
                2 -> STANDARD
                1 -> COLLAPSIBLE
                else -> STANDARD
            }
        }
    }
}

enum class BannerPosition {
    TOP,
    BOTTOM
}