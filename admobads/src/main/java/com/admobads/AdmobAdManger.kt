package com.admobads

import android.app.Activity
import android.widget.FrameLayout
import androidx.core.graphics.toColorInt
import com.admobads.ads.AdmobBannerAd
import com.admobads.ads.AdmobNativeAd
import com.admobads.ads.BannerPosition

class AdmobAdManger(
    private val context: Activity,
    private val bannerAdContainer: FrameLayout
) {

    private var skeletonColor: Int = "#E6E6E6".toColorInt()
    private var ctaPosition: String = "bottom"
    private var bodytextColor: Int = 0
    private var headingtextColor: Int = 0
    private var position: BannerPosition = BannerPosition.BOTTOM

    fun loadAd(
        modelItem: RemoteModel
    ) {
        if (modelItem.ad_format == "banner") {
            AdmobBannerAd(context, bannerAdContainer)
                .setSkeletonColor(skeletonColor)
                .loadBannerAd(modelItem.id, modelItem.ad_type, position)

        } else {
            AdmobNativeAd(
                context,
                bannerAdContainer,
                modelItem.id,
                modelItem.ad_type,
                modelItem.cta_color
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

}