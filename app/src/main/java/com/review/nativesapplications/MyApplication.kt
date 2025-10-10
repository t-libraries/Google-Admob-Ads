package com.review.nativesapplications

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.admobads.ads.AdmobInterstitialAd

class MyApplication : Application() {

    companion object {
        var myApplication : MyApplication? = null
    }

    override fun onCreate() {
        super.onCreate()
        myApplication = this

        AdmobInterstitialAd.initValues(this, 3, 1, "ca-app-pub-3940256099942544/1033173712")

        val appLocale: LocaleListCompat =
            LocaleListCompat.forLanguageTags("en")
        AppCompatDelegate.setApplicationLocales(appLocale)

    }

}