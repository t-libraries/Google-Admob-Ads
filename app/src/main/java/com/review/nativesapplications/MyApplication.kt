package com.review.nativesapplications

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.admobads.ads.AdmobInterstitialAd
import com.admobads.data.InterAdModel

class MyApplication : Application() {

    companion object {
        var myApplication: MyApplication? = null
    }

    override fun onCreate() {
        super.onCreate()
        myApplication = this

        AdmobInterstitialAd.initInterFromConfig(
            this@MyApplication,
            InterAdModel(
                inter_type = "timer",
                inter_counter_start = 3,
                inter_counter_gap = 4,
                inter_start_after_seconds = 10,
                inter_start_load_before_seconds = 10,
                inter_gap_after_seconds = 10,
                inter_gap_load_before_seconds = 10
        ),
        "ca-app-pub-3940256099942544/1033173712"
        )

        val appLocale: LocaleListCompat =
            LocaleListCompat.forLanguageTags("en")
        AppCompatDelegate.setApplicationLocales(appLocale)

    }

}