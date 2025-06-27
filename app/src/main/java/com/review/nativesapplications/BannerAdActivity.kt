package com.review.nativesapplications

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.admobads.ads.AdmobBannerAd
import com.review.nativesapplications.databinding.ActivityBannerAdBinding

class BannerAdActivity : AppCompatActivity() {


    private val binding: ActivityBannerAdBinding by lazy {
        ActivityBannerAdBinding.inflate(layoutInflater)
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

        AdmobBannerAd(this, binding.bannercontainer)
            .setSkeletonColor(Color.parseColor("#E6E6E6"))
            .loadBannerAd("ca-app-pub-3940256099942544/9214589741", "MEDIUM_RECTANGLE")
    }
}