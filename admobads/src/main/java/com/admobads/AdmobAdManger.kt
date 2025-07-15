package com.admobads

import android.app.Activity
import android.view.View
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
        modelItem: RemoteModel?, default_ad_type: String
    ) {

        if (modelItem?.hide == true) {
            bannerAdContainer.visibility = View.GONE
            return
        }


        modelItem?.apply {

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
                    .setSkeltonColor(skeletonColor)
                    .setCtaButtonPosition(ctaPosition)
                    .setTextColor(bodytextColor, headingtextColor)
                    .load()
            }

        } ?: run {

            if (default_ad_type == "banner") {
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
                    .load()
            }
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