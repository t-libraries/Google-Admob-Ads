package com.review.nativesapplications

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.admobads.ads.AdmobInterstitialAd
import com.review.nativesapplications.databinding.ActivityBannerAdBinding

class BannerActivity : AppCompatActivity() {


    private val binding: ActivityBannerAdBinding by lazy {
        ActivityBannerAdBinding.inflate(layoutInflater)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            AdmobInterstitialAd.getInstance().showInterAd(
                this@BannerActivity,
                message = {
                    Toast.makeText(this@BannerActivity, it, Toast.LENGTH_SHORT).show()
                },
                callBack = {
                    finish()
                }

            )
        }

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

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

}