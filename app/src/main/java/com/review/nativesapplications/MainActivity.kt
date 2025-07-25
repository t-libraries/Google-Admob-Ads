package com.review.nativesapplications

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.admobads.AdmobAdManger
import com.admobads.DefaultAdPlacement
import com.admobads.ads.AdmobInterstitialAd
import com.admobads.ads.AdmobInterstitialAd.showInterAd
import com.admobads.data.RemoteModel
import com.google.android.gms.ads.MobileAds
import com.review.nativesapplications.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        MobileAds.initialize(this)

        AdmobInterstitialAd.setLoadingDialogBgColor("#FF0000".toColorInt())
        AdmobInterstitialAd.setLoadingDialogTextColor(Color.WHITE)

        AdmobInterstitialAd.initValues(0, 0, "ca-app-pub-3940256099942544/1033173712")

        binding.continueBtn.setOnClickListener {
            showInterAd {

            }
        }

        AdmobAdManger(
            this,
            binding.nativeAd
        )
            .setTextColor("#FF0000".toColorInt(), "#FF0000".toColorInt())
            .setMargintoNative(10 , 10)
            .loadAd(
                RemoteModel(
                    "ca-app-pub-3940256099942544/1044960115",
                    "native",
                    1,
                    false,
                    "#FFC0CB"

                ),
                DefaultAdPlacement.BANNER
            )
    }
}