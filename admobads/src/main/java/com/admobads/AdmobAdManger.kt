package com.admobads

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import androidx.core.graphics.toColorInt
import com.admobads.ads.AdmobBannerAd
import com.admobads.ads.AdmobInterstitialAd
import com.admobads.ads.AdmobNativeAd
import com.admobads.ads.BannerPosition
import com.admobads.data.RemoteModel

class AdmobAdManger(
    private val context: Activity,
    private val bannerAdContainer: FrameLayout
) {

    companion object {
        private var isPurchased = false

        fun isPurchased(value: Boolean = false) {
            isPurchased = value
            AdmobInterstitialAd.isPurchased(value)
        }
    }

    private var skeletonColor: Int = "#E6E6E6".toColorInt()
    private var ctaPosition: String = "bottom"
    private var bodytextColor: Int = 0
    private var headingtextColor: Int = 0
    private var position: BannerPosition = BannerPosition.BOTTOM

    private var nativeAdMarginStart = 0
    private var nativeAdMarginEnd = 0

    fun loadAd(
        modelItem: RemoteModel?, default_ad_format: DefaultAdPlacement = DefaultAdPlacement.NATIVE
    ) {

        if (isPurchased)
            return

        if (modelItem?.hide == true) {
            bannerAdContainer.visibility = View.GONE
            return
        }

        modelItem?.apply {
            if (id != "") {
                if (ad_format == "banner") {
                    AdmobBannerAd(context, bannerAdContainer)
                        .setSkeletonColor(skeletonColor)
                        .loadBannerAd(id, ad_type, position)

                } else {
                    AdmobNativeAd(
                        context,
                        bannerAdContainer,
                        id,
                        ad_type,
                        cta_color
                    )
                        .setMargintoNative(nativeAdMarginStart , nativeAdMarginEnd)
                        .setSkeltonColor(skeletonColor)
                        .setCtaButtonPosition(ctaPosition)
                        .setTextColor(bodytextColor, headingtextColor)
                        .load()
                }
            } else {
                setupDefaultLayout(default_ad_format)
            }

        } ?: run {
            setupDefaultLayout(default_ad_format)
        }

    }

    private fun setupDefaultLayout(default_ad_format: DefaultAdPlacement) {
        if (default_ad_format == DefaultAdPlacement.BANNER) {
            AdmobBannerAd(context, bannerAdContainer)
                .setSkeletonColor(skeletonColor)
                .loadBannerAd("", 2, position)

        } else {
            AdmobNativeAd(
                context,
                bannerAdContainer,
                "",
                3,
                "#00ff00"
            )
                .setMargintoNative(nativeAdMarginStart , nativeAdMarginEnd)
                .setSkeltonColor(skeletonColor)
                .setCtaButtonPosition(ctaPosition)
                .setTextColor(bodytextColor, headingtextColor)
                .load()
        }
    }

    fun setCtaPostion(ctaPosition: String): AdmobAdManger {
        this.ctaPosition = ctaPosition
        return this
    }

    fun setTextColor(bodytextColor: Int, headingtextColor: Int): AdmobAdManger {
        this.bodytextColor = bodytextColor
        this.headingtextColor = headingtextColor
        return this
    }

    fun setBannerCollapsiblePosition(position: BannerPosition): AdmobAdManger {
        this.position = position
        return this
    }

    fun setSkeltonColor(color: Int): AdmobAdManger {
        this.skeletonColor = color
        return this
    }

    fun setMargintoNative(start: Int, end: Int): AdmobAdManger {
        this.nativeAdMarginStart = start
        this.nativeAdMarginEnd = end
        return this

    }

}

enum class DefaultAdPlacement {
    NATIVE,
    BANNER
}