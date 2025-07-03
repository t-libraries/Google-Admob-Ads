package com.admobads

import android.app.Activity
import android.graphics.Color
import android.widget.FrameLayout
import com.admobads.ads.AdmobBannerAd
import com.admobads.ads.AdmobNativeAd
import com.admobads.ads.BannerPosition
import java.util.Locale

class AdmobAdManger(
    private val context: Activity,
    private val bannerAdContainer: FrameLayout
) {

    private var skeletonColor: Int = Color.parseColor("#E6E6E6")
    private var ctaPosition: String = "bottom"
    private var bodytextColor: Int = 0
    private var headingtextColor: Int = 0
    private var position: BannerPosition = BannerPosition.BOTTOM
    private var ctaColor: String = "#00ff00"

    fun loadAd(
        adUnitId: String,
        adFormat: String,
        adType: Int
    ) {
        if (adFormat.lowercase(Locale.ROOT) == "banner") {
            AdmobBannerAd(context, bannerAdContainer)
                .setSkeletonColor(skeletonColor)
                .loadBannerAd(adUnitId, adType, position)

        } else {
            AdmobNativeAd(
                context,
                bannerAdContainer,
                adUnitId,
                adType,
                ctaColor
            )
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

    fun setCtaColor(ctaColor: String): AdmobAdManger {
        this.ctaColor = ctaColor
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


}