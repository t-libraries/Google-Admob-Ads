package com.review.nativesapplications

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.admobads.AdmobAdManger
import com.admobads.DefaultAdPlacement
import com.admobads.ads.AdmobAppOpenAd
import com.admobads.ads.AdmobInterstitialAd
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

        AdmobInterstitialAd.getInstance().setLoadingDialogTextColor(Color.BLACK)
        AdmobInterstitialAd.getInstance().setLoadingDialogBgColor("#ffffff".toColorInt())

        MobileAds.initialize(this)

        AdmobAdManger.isPurchased(false)

        MyApplication.myApplication?.let {
            AdmobAppOpenAd(
                it,
                "ca-app-pub-3940256099942544/9257395921"
            )
        }

        AdmobInterstitialAd.getInstance()
            .loadSplashInter(this, "ca-app-pub-3940256099942544/1033173712", {
            }, {
            })


        binding.continueBtn.setOnClickListener {
            AdmobInterstitialAd.getInstance().showSplashInterAd(
                this,

                message = {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                },

                callBack = { isSuccess ->
                    if (isSuccess) {
                        startActivity(Intent(this, BannerActivity::class.java))
                    } else {
                        startActivity(Intent(this, BannerActivity::class.java))
                    }
                }

            )
        }

        AdmobAdManger(
            this,
            binding.nativeContainer,
            binding.nativeAd
        )
            .setTextColor("#000000".toColorInt(), "#4E4E4EFF".toColorInt())
            .setMargintoNative(10, 10)
            .loadAd(
                RemoteModel(
                    "ca-app-pub-3940256099942544/9214589741",
                    "banner",
                    2,
                    false,
                    "#FFC0CB"

                ),
                DefaultAdPlacement.BANNER
            )
    }
}