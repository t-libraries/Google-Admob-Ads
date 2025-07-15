package com.review.nativesapplications

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.admobads.AdmobAdManger
import com.admobads.RemoteModel
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

        AdmobAdManger(
            this,
            binding.nativeAd
        )
            .loadAd(
                RemoteModel(
                    id = "ca-app-pub-3940256099942544/1044960115",
                    ad_format = "native",
                    ad_type = 1,
                    cta_color = "#F42727",
                    hide = false
                )
            )

    }
}