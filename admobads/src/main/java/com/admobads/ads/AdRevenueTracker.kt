package com.admobads.ads

import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.OnPaidEventListener

fun interface AdRevenueListener {
    fun onAdPaid(eventName: String, params: Bundle)
}

object AdRevenueTracker {

    const val EVENT_AD_IMPRESSION = "ad_impression_adj"
    const val FORMAT_APP_OPEN = "app_open"
    const val FORMAT_BANNER = "banner"
    const val FORMAT_INTERSTITIAL = "interstitial"
    const val FORMAT_NATIVE = "native"

    private var revenueMultiplier = 1.20

    @Volatile
    private var listener: AdRevenueListener? = null

    @JvmStatic
    fun setListener(revenueMultiplier: Double, listener: AdRevenueListener?) {
        this.revenueMultiplier = revenueMultiplier
        this.listener = listener
    }

    @JvmStatic
    @JvmOverloads
    fun notifyAdPaid(eventName: String = EVENT_AD_IMPRESSION, params: Bundle) {
        listener?.onAdPaid(eventName, params)
    }

    @JvmStatic
    @JvmOverloads
    fun paidEventListener(
        adUnitId: String,
        adFormat: String,
        logTag: String
    ): OnPaidEventListener {
        return OnPaidEventListener { adValue ->
            val (revenue, params) = buildImpressionParams(
                adValue = adValue,
                adUnitId = adUnitId,
                adFormat = adFormat
            )
            val adjustedRevenue = params.getDouble("value")
            Log.d(
                logTag,
                "Ad impression revenue: original=$revenue " +
                        "adjusted=$adjustedRevenue " +
                        "currency=${adValue.currencyCode} " +
                        "revnueMultiplier=${revenueMultiplier} " +
                        "and params = $params"
            )
            notifyAdPaid(params = params)
        }
    }

    fun buildImpressionParams(
        adValue: AdValue,
        adUnitId: String,
        adFormat: String
    ): Pair<Double, Bundle> {
        var revenue = adValue.valueMicros / 1_000_000.0
        try {
            revenue = (adValue.valueMicros / 1_000_000.0) * (revenueMultiplier / 1.5)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val params = Bundle().apply {
            putDouble("value", revenue)
            putString("currency", adValue.currencyCode)
            putString("ad_platform", "Custom")
            putString("ad_source", "Custom")
            putString("ad_unit_name", adUnitId)
            putString("ad_format", adFormat)
            putString("PriceAccuracy", "BID")
            putString("revenue_precision", adValue.precisionType.toString())
        }

        return revenue to params
    }
}
