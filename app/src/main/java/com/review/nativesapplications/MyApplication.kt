package com.review.nativesapplications

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class MyApplication : Application() {

    companion object {
        var myApplication : MyApplication? = null
    }

    override fun onCreate() {
        super.onCreate()
        myApplication = this

        val appLocale: LocaleListCompat =
            LocaleListCompat.forLanguageTags("es")
        AppCompatDelegate.setApplicationLocales(appLocale)

    }

}