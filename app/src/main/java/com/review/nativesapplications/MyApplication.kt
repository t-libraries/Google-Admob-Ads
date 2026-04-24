package com.review.nativesapplications

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.admobads.ads.AdmobInterstitialAd
import com.admobads.data.InterAdModel
import com.google.android.gms.ads.MobileAds

class MyApplication : Application() {

    companion object {
        var myApplication: MyApplication? = null
    }

    override fun onCreate() {
        super.onCreate()
        myApplication = this

        AdmobInterstitialAd.getInstance().initInterFromConfig(
            this@MyApplication,
            InterAdModel(
                inter_type = "timer",
                loading_type = "api",
                inter_counter_start = 1,
                inter_counter_gap = 1,
                inter_start_after_seconds = 15,
                inter_start_load_before_seconds = 5,
                inter_gap_after_seconds = 30,
                inter_gap_load_before_seconds = 25
            ),
            "ca-app-pub-3940256099942544/1033173712"
        )

        val appLocale: LocaleListCompat =
            LocaleListCompat.forLanguageTags("en")
        AppCompatDelegate.setApplicationLocales(appLocale)

    }

}