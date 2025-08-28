# Admob Ads

🔥 Headline Options:
Plug-and-Play Ad Integration for Android – Native, Banner, Interstitial & More!

Ad Integration Made Easy: One Kotlin Library for All Ad Formats

Boost Your App Revenue – Seamless Ads with Customizable Layouts

## All-in-One Ad Library for Android with Templates & Easy Controls

## Simplify Monetization – Native, Banner, Interstitial, and App Open Ads in Minutes

## This Kotlin library empowers Android developers to:

Load Native, Banner, Interstitial & App Open Ads

Use built-in loading layouts and templates

Customize UI components directly

Reduce setup time and boost ad revenue instantly

Users are time-sensitive and may skip an app due to long loading times and missing visual feedback.

This library implements the Skeleton View pattern and provides an easy way for other developers to
enable it in their apps.

### Getting Started

##### Gradle

```gradle
buildscript {
    repositories {
         maven { url 'https://jitpack.io' }
    }
}
```

```gradle
dependencies {
    implementation 'com.github.t-libraries:Google-Admob-Ads:1.9-alpha'
```

##### XML

```xml

<com.google.android.material.card.MaterialCardView android:id="@+id/adContainer"
    android:layout_height="wrap_content" android:layout_marginTop="@dimen/_12sdp"
    android:layout_width="wrap_content" app:cardBackgroundColor="#f1f0f8"
    app:cardCornerRadius="@dimen/_8sdp" app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toBottomOf="@+id/constraintLayout6" app:strokeWidth="0dp">

    <FrameLayout android:id="@+id/adLayout" android:layout_height="wrap_content"
        android:layout_width="wrap_content" app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent" app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />


</com.google.android.material.card.MaterialCardView>

```

**Native & Banner Ads**

##### Kotlin

```kotlin

class MainActivity : AppCompatActivity() {

    private var item: RemoteModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        item = RemoteModel(
            id = "ca-app-pub-3940256099942544/1044960115",
            ad_format = "native",
            ad_type = 1,
            hide = false,
            cta_color = "#F42727"
        )

        binding?.apply {
            AdmobAdManger(requireActivity(), adContainer, adLayout)
                .setBannerCollapsiblePosition(BannerPosition.TOP) // optional by default (BannerPosition.BOTTOM)
                .setMargintoNative(14, 14) //optional by default (0 , 0)
                .setSkeltonColor("#C8C8C8".toColorInt()) // optional by default ("#C8C8C8)
                .setTextColor(
                    "#ffffff".toColorInt(),
                    "#ffffff".toColorInt()
                ) // optional by default ("#000000".toColorInt() , "#000000".toColorInt())
                .loadAd(
                    item,
                    DefaultAdPlacement.BANNER
                )
        }
    }
}
```

##### Java

```java
public class MainActivity extends AppCompatActivity {

    private MaterialCardView adContainer;
    private FrameLayout adLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adContainer = findViewbyid(R.id.adContainer);
        adLayout = findViewbyid(R.id.adLayout);


        AdmobAdManger(requireActivity(), adContainer, adLayout)
                .setBannerCollapsiblePosition(BannerPosition.TOP) // optional
                .setMargintoNative(14, 14) //optional
                .setSkeltonColor("#C8C8C8".toColorInt()) // optional
                .setTextColor("#ffffff".toColorInt(), "#ffffff".toColorInt()) // optional
                .loadAd(
                        item,
                        DefaultAdPlacement.BANNER);
    }


}
```

## Say goodbye to complex ad integrations!

This Kotlin-powered Android library lets you load all major ad types—Native, Banner, Interstitial,
App Open—with beautiful templates, smooth loading layouts, and user-controlled UI. Monetization has
never been this simple.

### Configuration

| ad_format        | ad_type | hide                                                                                |
|------------------|---------|-------------------------------------------------------------------------------------|
| nativesize       | size    | Must for Native size ("banner" , "small/adaptive" , "medium" , "large" , "large_1") |
| background color | color   | Color of the Native Ad container (defaults to ##008000)                             |

## Native Ad Template Design

## Banner Native

<img src="./images/banner.png" alt="Banner Native Ad Displaying">

## Small Native / Adaptive Native

<img src="./images/small.png" alt="Adaptive or Small Native Ad Displaying">

## Medium Native

<img src="./images/medium.png" alt="Medium Native Ad Displaying">

## Large Native

<img src="./images/large.png" alt="Large Native Ad Displaying">

## Large Native 1

<img src="./images/large_1.png" alt="Large Native 1 Ad Displaying">




**Splash Intenstitial**

To pre-load the interstital ad e.g for splash

    **To Load the Interstital Ad:**

        AdmobInterstitialAd.loadSplashInter(
            this, getString(R.string.splash_interstitial),
            onAdFailedToLoad = {
                                perform task if ad is failed to load
                                },
            onAdLoaded = {
                        perform task if ad is successfully loaded
                        }
                    )



    **To show ad**

        In the activity

            showSplashInterAd {

                perform task when the ad is dismissed

            }

        In the fragments

            requireActivity().showSplashInterAd {

                perform task when the ad is dismissed
    
            }



**Inside Intenstitial**

    **To Load the Inside Interstital Ad:**

        AdmobInterstitialAd.initValues(
            context = this,
            inter_counter_start = firebaseRemoteConfig.getLong("inter_start_counter").toInt(),
            inter_counter_gap = firebaseRemoteConfig.getLong("inter_gap_counter").toInt(),
            inside_inter_ad_id = getString(R.string.inside_interstitial)
        )

    **To show ad**

        In the activity

        showInterAd {

            perform task when the ad is dismissed

        }

    In the fragments

        requireActivity().showInterAd {

            perform task when the ad is dismissed

        }



**AppOpen**

    **To intializer the Inside AppOpen Ads:**

    MyApplication.myApplication?.let {
        AdmobAppOpenAd(
        applicationContext = it, //Application Conttext
        ad_Id = "ca-app-pub-3940256099942544/9257395921", //ad unit id
        exceptionalActivities = listOf("MainActivity") // list of actvities name (Exact) to exlude where appopen will no show
        )
        }


**Important Paramater to handle**

    AdmobAdManger.isPurchased(ispremium) // Call this inside the code where checking if the user is premium or not
    AdmobInterstitialAd.setLoadingDialogTextColor(Color.YELLOW) //To change the background color of the loading dialog of both Interstital and AppOpen
    AdmobInterstitialAd.setLoadingDialogBgColor("#000000".toColorInt()) //To change the text color of the loading dialog of both Interstital and AppOpen



### Third-party licenses

This software uses following technologies with great appreciation:

* [AndroidX](https://developer.android.com/jetpack/androidx)
* [ColorSlider](https://github.com/naz013/ColorSlider)
* [gradle-maven-publish-plugin](https://github.com/vanniktech/gradle-maven-publish-plugin)
* [Material Components for Android](https://material.io/components)

### License

Copyright 2025 Philipp Fahlteich

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

<img src="./images/android.png" alt=""> 
