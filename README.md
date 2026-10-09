# Google AdMob Ads — Exposed Methods & Implementation

Complete public API of the `admobads` library, with a working implementation for **every ad type**.

[![Release](https://jitpack.io/v/t-libraries/Google-Admob-Ads.svg)](https://jitpack.io/#t-libraries/Google-Admob-Ads)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![minSdk](https://img.shields.io/badge/minSdk-23-green.svg)](https://developer.android.com/google/play/requirements/target-sdk)

---

## Table of contents

1. [Installation](#1-installation)
2. [Public API index](#2-public-api-index)
3. [Data models](#3-data-models)
4. [Global controls — `AdmobAdManger`](#4-global-controls--admobadmanger)
5. [Native ads](#5-native-ads)
6. [Banner ads](#6-banner-ads)
7. [Interstitial ads](#7-interstitial-ads)
8. [Preload interstitial ads](#8-preload-interstitial-ads)
9. [App Open ads](#9-app-open-ads)
10. [Impression revenue](#10-impression-revenue)
11. [Optional utilities](#11-optional-utilities)
12. [End-to-end application sample](#12-end-to-end-application-sample)
13. [Test ad units](#13-test-ad-units)

---

## 1. Installation

```gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

```gradle
dependencies {
    implementation 'com.github.t-libraries:Google-Admob-Ads:Tag'
}
```

Replace `Tag` with the latest [JitPack tag](https://jitpack.io/#t-libraries/Google-Admob-Ads).

### Manifest

```xml
<uses-permission android:name="android.permission.INTERNET" />

<application android:name=".MyApplication" ...>
    <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="@string/appid" />
</application>
```

```xml
<string name="appid">ca-app-pub-3940256099942544~3347511713</string>
```

---

## 2. Public API index

| Class / object | Package | Role |
|---|---|---|
| `AdmobAdManger` | `com.admobads` | Native + banner loader, premium / Compose / revenue entry point |
| `DefaultAdPlacement` | `com.admobads` | Fallback layout when `RemoteModel` is null or `id` is empty |
| `RemoteModel` | `com.admobads.data` | Remote Config JSON for native / banner |
| `InterAdModel` | `com.admobads.data` | Interstitial click / timer / preload config |
| `AdmobNativeAd` | `com.admobads.ads` | Native ad loader (all templates) |
| `AdmobBannerAd` | `com.admobads.ads` | Banner ad loader (all sizes) |
| `BannerAdType` | `com.admobads.ads` | Banner size enum |
| `BannerPosition` | `com.admobads.ads` | Collapsible banner collapse edge |
| `AdmobInterstitialAd` | `com.admobads.ads` | Splash + in-app interstitial singleton |
| `AdmobPreloadInterstitialAd` | `com.admobads.ads` | AdMob Preloader path (`loading_type = "api"`) |
| `AdmobAppOpenAd` | `com.admobads.ads` | App Open on process foreground |
| `AdRevenueTracker` | `com.admobads.ads` | Paid-event bundle + host-app callback |
| `AdRevenueListener` | `com.admobads.ads` | `{ eventName, params -> }` |
| `AdLoadingComposable` | `com.admobads.ads.utils` | Compose loading overlay |
| `BlurUtils` | `com.admobads.utils` | Optional background blur |

Host apps normally use **`AdmobAdManger` + `AdmobInterstitialAd` + `AdmobAppOpenAd`**. Direct `AdmobNativeAd` / `AdmobBannerAd` / `AdmobPreloadInterstitialAd` calls are also supported.

---

## 3. Data models

### `RemoteModel`

```kotlin
data class RemoteModel(
    val id: String = "",
    val ad_format: String = "",
    val ad_type: Int = 3,
    val hide: Boolean = false,
    val cta_color: String = "#F42727"
)
```

| Field | Type | Default | Description |
|---|---|---|---|
| `id` | `String` | `""` | Ad unit ID. Empty uses `DefaultAdPlacement`. |
| `ad_format` | `String` | `""` | `"banner"` → banner. Any other value → native. |
| `ad_type` | `Int` | `3` | Native or banner size. See tables below. |
| `hide` | `Boolean` | `false` | `true` hides the container. |
| `cta_color` | `String` | `"#F42727"` | Native CTA hex color. |

Gson-annotated (`@SerializedName`), so it can be parsed from Firebase Remote Config JSON.

### `InterAdModel`

```kotlin
data class InterAdModel(
    val inter_type: String = "timer",
    var loading_type: String = "manual",
    val inter_counter_start: Int = 0,
    val inter_counter_gap: Int = 0,
    val inter_start_after_seconds: Long = 0,
    val inter_start_load_before_seconds: Long = 0,
    val inter_gap_after_seconds: Long = 0,
    val inter_gap_load_before_seconds: Long = 0
)
```

| Field | Values | Description |
|---|---|---|
| `inter_type` | `"click"` / `"timer"` | Show by click count or elapsed time. |
| `loading_type` | `"manual"` / `"api"` | On-demand load, or AdMob Preloader. |
| `inter_counter_start` | `Int` | Clicks before the first in-app interstitial. |
| `inter_counter_gap` | `Int` | Clicks between later interstitials. |
| `inter_start_after_seconds` | `Long` | Seconds after app start before first timer ad. |
| `inter_start_load_before_seconds` | `Long` | Load first timer ad this many seconds early. |
| `inter_gap_after_seconds` | `Long` | Seconds between later timer ads. |
| `inter_gap_load_before_seconds` | `Long` | Load next timer ad this many seconds early. |

If both start and gap counters **or** both start and gap times are `0`, in-app interstitials are disabled.

---

## 4. Global controls — `AdmobAdManger`

Constructor:

```kotlin
AdmobAdManger(
    context: Activity,
    adContainer: MaterialCardView,
    adLayout: FrameLayout
)
```

### Companion methods

| Method | Signature | Description |
|---|---|---|
| `isPurchased` | `fun isPurchased(value: Boolean = false)` | Premium flag. Hides native/banner and skips interstitial + app open. |
| `isComposed` | `fun isComposed(value: Boolean = false)` | `true` uses Compose loading overlay instead of XML. |
| `setAdRevenueListener` | `fun setAdRevenueListener(revenueMultiplier: Double, listener: AdRevenueListener?)` | Host-app paid-event callback. |

### Instance methods (fluent)

| Method | Signature | Default | Description |
|---|---|---|---|
| `setCtaPostion` | `fun setCtaPostion(ctaPosition: String): AdmobAdManger` | `"bottom"` | Native CTA `"top"` or `"bottom"`. |
| `setTextColor` | `fun setTextColor(headingtextColor: Int, bodytextColor: Int): AdmobAdManger` | `0` (theme default) | Native headline then body color. |
| `setBannerCollapsiblePosition` | `fun setBannerCollapsiblePosition(position: BannerPosition): AdmobAdManger` | `BOTTOM` | Collapse edge for collapsible banners. |
| `setSkeltonColor` | `fun setSkeltonColor(color: Int): AdmobAdManger` | `#E6E6E6` | Skeleton shimmer color. |
| `setMargintoNative` | `fun setMargintoNative(start: Int, end: Int): AdmobAdManger` | `0, 0` | Horizontal native margins in **dp**. |
| `loadAd` | `fun loadAd(modelItem: RemoteModel?, default_ad_format: DefaultAdPlacement = BANNER)` | — | Loads native or banner from `RemoteModel`. |

### `DefaultAdPlacement`

```kotlin
enum class DefaultAdPlacement { NATIVE, BANNER }
```

Used when `modelItem == null` or `id == ""`.

### Usage

```kotlin
AdmobAdManger.isPurchased(false)
AdmobAdManger.isComposed(false)
AdmobAdManger.setAdRevenueListener(1.30) { eventName, params ->
    FirebaseAnalytics.getInstance(this).logEvent(eventName, params)
}

AdmobAdManger(this, binding.adContainer, binding.adLayout)
    .setCtaPostion("bottom")
    .setTextColor("#000000".toColorInt(), "#4E4E4E".toColorInt())
    .setBannerCollapsiblePosition(BannerPosition.BOTTOM)
    .setSkeltonColor("#E6E6E6".toColorInt())
    .setMargintoNative(10, 10)
    .loadAd(remoteModel, DefaultAdPlacement.BANNER)
```

---

## 5. Native ads

XML container (required by `AdmobAdManger`):

```xml
<com.google.android.material.card.MaterialCardView
    android:id="@+id/adContainer"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:strokeWidth="0dp">

    <FrameLayout
        android:id="@+id/adLayout"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />
</com.google.android.material.card.MaterialCardView>
```

### Native `ad_type`

| `ad_type` | Template | CTA layouts |
|---|---|---|
| `1` | Large Native 1 | Bottom (`tlib_large_variant_one_bottom`) |
| `2` | Large Native | Top / bottom (`tlib_large_variant_two_*`) |
| `3` | Small / Adaptive (default) | Top / bottom (`tlib_large_variant_one_top` / `tlib_adaptive_variant_one_bottom`) |
| `4` | Medium Native | Top / bottom (`tlib_medium_variant_one_*`) |

Set CTA with `.setCtaPostion("top")` or `"bottom"`.

### Via `AdmobAdManger` (recommended)

```kotlin
fun loadNative(
    adType: Int,
    ctaPosition: String = "bottom",
    ctaColor: String = "#F42727"
) {
    AdmobAdManger(this, binding.adContainer, binding.adLayout)
        .setCtaPostion(ctaPosition)
        .setTextColor("#000000".toColorInt(), "#4E4E4E".toColorInt())
        .setSkeltonColor("#E6E6E6".toColorInt())
        .setMargintoNative(12, 12)
        .loadAd(
            RemoteModel(
                id = "ca-app-pub-3940256099942544/2247696110",
                ad_format = "native",
                ad_type = adType,
                hide = false,
                cta_color = ctaColor
            ),
            DefaultAdPlacement.NATIVE
        )
}

loadNative(adType = 1) // Large Native 1
loadNative(adType = 2, ctaPosition = "top") // Large
loadNative(adType = 3) // Small / Adaptive
loadNative(adType = 4) // Medium
```

### Direct `AdmobNativeAd` methods

```kotlin
AdmobNativeAd(
    ctx: Activity,
    nativeAdContainer: FrameLayout,
    id: String,
    type: Int,
    buttonColor: String
)
```

| Method | Signature | Description |
|---|---|---|
| `setTextColor` | `setTextColor(bodytextColor: Int, headingtextColor: Int): AdmobNativeAd` | **Body first, then heading** (opposite of `AdmobAdManger`). |
| `setMargintoNative` | `setMargintoNative(marginstart: Int, marginend: Int): AdmobNativeAd` | Horizontal margins in **dp**. |
| `setSkeltonColor` | `setSkeltonColor(skeltonColor: Int): AdmobNativeAd` | Skeleton color. |
| `setCtaButtonPosition` | `setCtaButtonPosition(cta_btn_position: String): AdmobNativeAd` | `"top"` or `"bottom"`. |
| `load` | `fun load()` | Builds `AdLoader` and displays the template. |

```kotlin
AdmobNativeAd(
    this,
    binding.adLayout,
    "ca-app-pub-3940256099942544/2247696110",
    3,
    "#F42727"
)
    .setTextColor("#4E4E4E".toColorInt(), "#000000".toColorInt())
    .setMargintoNative(12, 12)
    .setSkeltonColor("#E6E6E6".toColorInt())
    .setCtaButtonPosition("bottom")
    .load()
```

Paid events are attached when the native ad is bound (`ad_format = native`).

---

## 6. Banner ads

### Banner `ad_type`

| `ad_type` | `BannerAdType` | Size |
|---|---|---|
| `1` | `COLLAPSIBLE` | Anchored adaptive + collapsible extra |
| `2` | `STANDARD` | Anchored adaptive (default) |
| `3` | `MEDIUM_RECTANGLE` | `AdSize.MEDIUM_RECTANGLE` |
| `4` | `LARGE_BANNER` | Large banner height, full width |

### `BannerPosition`

```kotlin
enum class BannerPosition { TOP, BOTTOM }
```

Used by collapsible banners (`ad_type = 1`). Default is `BOTTOM`.

### Via `AdmobAdManger` (recommended)

```kotlin
fun loadBanner(adType: Int, position: BannerPosition = BannerPosition.BOTTOM) {
    AdmobAdManger(this, binding.adContainer, binding.adLayout)
        .setBannerCollapsiblePosition(position)
        .setSkeltonColor("#E6E6E6".toColorInt())
        .loadAd(
            RemoteModel(
                id = "ca-app-pub-3940256099942544/9214589741",
                ad_format = "banner",
                ad_type = adType,
                hide = false
            ),
            DefaultAdPlacement.BANNER
        )
}

loadBanner(1, BannerPosition.TOP)    // Collapsible top
loadBanner(1, BannerPosition.BOTTOM) // Collapsible bottom
loadBanner(2)                        // Standard adaptive
loadBanner(3)                        // Medium rectangle
loadBanner(4)                        // Large banner
```

### Direct `AdmobBannerAd` methods

```kotlin
AdmobBannerAd(context: Activity, bannerAdContainer: FrameLayout)
```

| Method | Signature | Description |
|---|---|---|
| `setSkeletonColor` | `fun setSkeletonColor(color: Int): AdmobBannerAd` | Skeleton color. |
| `loadBannerAd` | `fun loadBannerAd(adUnitId: String, adType: Int, position: BannerPosition = BOTTOM)` | Int overload. |
| `loadBannerAd` | `fun loadBannerAd(adUnitId: String, adType: BannerAdType, position: BannerPosition = BOTTOM)` | Enum overload. |
| `destroy` | `fun destroy()` | Clears paid listener, destroys `AdView`, removes views. |

```kotlin
val banner = AdmobBannerAd(this, binding.adLayout)
    .setSkeletonColor("#E6E6E6".toColorInt())

banner.loadBannerAd(
    adUnitId = "ca-app-pub-3940256099942544/9214589741",
    adType = BannerAdType.COLLAPSIBLE,
    position = BannerPosition.BOTTOM
)

// later, e.g. in onDestroy()
banner.destroy()
```

Paid events are attached when the `AdView` is created (`ad_format = banner`).

---

## 7. Interstitial ads

All splash and in-app interstitial APIs go through the singleton:

```kotlin
AdmobInterstitialAd.getInstance()
```

`initInterFromConfig()` also calls `MobileAds.initialize(context)`.

### Exposed methods

| Method | Signature | Description |
|---|---|---|
| `getInstance` | `fun getInstance(): AdmobInterstitialAd` | Singleton. |
| `initInterFromConfig` | `fun initInterFromConfig(context: Context, config: InterAdModel, inside_inter_ad_id: String)` | Call once from `Application.onCreate()`. |
| `loadSplashInter` | `fun loadSplashInter(ctx: Activity, id: String, onAdLoaded: () -> Unit, onAdFailedToLoad: () -> Unit)` | Preload splash interstitial. |
| `showSplashInterAd` | `fun showSplashInterAd(activity: Activity, message: (String) -> Unit = {}, callBack: (Boolean) -> Unit)` | Show splash ad. `callBack(true)` if shown/skipped as premium, `false` if missing/failed. |
| `showInterAd` | `fun showInterAd(activity: Activity, message: (String) -> Unit = {}, callBack: () -> Unit)` | In-app interstitial (click, timer, or preload). |
| `setPurchased` | `fun setPurchased(isPurchased: Boolean = false)` | Prefer `AdmobAdManger.isPurchased()`. |
| `isPurchased` | `fun isPurchased(): Boolean` | Current premium flag. |
| `setComposed` | `fun setComposed(isComposed: Boolean = false)` | Prefer `AdmobAdManger.isComposed()`. |
| `isComposed` | `fun isComposed(): Boolean` | Compose loading overlay flag. |
| `setLoadingDialogBgColor` | `fun setLoadingDialogBgColor(loadingDialogBgColor: Int)` | Also updates App Open + preload dialogs. |
| `setLoadingDialogTextColor` | `fun setLoadingDialogTextColor(loadingDialogTextColor: Int)` | Also updates App Open + preload dialogs. |
| `destroy` | `fun destroy()` | Cancels pending show, clears ads, resets init so config can run again. |

### 7.1 Initialize (Application)

```kotlin
AdmobInterstitialAd.getInstance().initInterFromConfig(
    this,
    InterAdModel(
        inter_type = "click",      // or "timer"
        loading_type = "manual",   // or "api"
        inter_counter_start = 2,
        inter_counter_gap = 3,
        inter_start_after_seconds = 15,
        inter_start_load_before_seconds = 5,
        inter_gap_after_seconds = 30,
        inter_gap_load_before_seconds = 25
    ),
    "ca-app-pub-3940256099942544/1033173712"
)
```

Routing inside `initInterFromConfig`:

| `loading_type` | `inter_type` | Implementation |
|---|---|---|
| `"manual"` | `"click"` | On-demand load, click counters |
| `"manual"` | `"timer"` | On-demand load, elapsed-time gates |
| `"api"` (anything except `"manual"`) | `"click"` or `"timer"` | `AdmobPreloadInterstitialAd.start()` |

### 7.2 Splash interstitial

```kotlin
AdmobInterstitialAd.getInstance().loadSplashInter(
    this,
    "ca-app-pub-3940256099942544/1033173712",
    onAdLoaded = { /* ready */ },
    onAdFailedToLoad = { /* continue */ }
)

AdmobInterstitialAd.getInstance().showSplashInterAd(
    this,
    message = { status -> },
    callBack = { shown ->
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
)
```

From a Fragment: pass `requireActivity()`.

If the splash ad is still in memory when the first in-app interstitial is requested, it is reused.

### 7.3 Click-based in-app interstitial

```kotlin
AdmobInterstitialAd.getInstance().initInterFromConfig(
    this,
    InterAdModel(
        inter_type = "click",
        loading_type = "manual",
        inter_counter_start = 2,
        inter_counter_gap = 3
    ),
    getString(R.string.inside_interstitial)
)
```

```kotlin
AdmobInterstitialAd.getInstance().showInterAd(
    this,
    message = { status -> },
    callBack = {
        startActivity(Intent(this, NextActivity::class.java))
    }
)
```

`callBack` runs whether the ad showed, failed, was skipped by the counter, or the user is premium. Put navigation there.

### 7.4 Timer-based in-app interstitial

```kotlin
AdmobInterstitialAd.getInstance().initInterFromConfig(
    this,
    InterAdModel(
        inter_type = "timer",
        loading_type = "manual",
        inter_start_after_seconds = 15,
        inter_start_load_before_seconds = 5,
        inter_gap_after_seconds = 30,
        inter_gap_load_before_seconds = 25
    ),
    getString(R.string.inside_interstitial)
)
```

Show with the same `showInterAd()` as click-based. If the timer has not elapsed or no ad is loaded, `callBack` runs immediately.

### Loading dialog colors

```kotlin
AdmobInterstitialAd.getInstance().setLoadingDialogTextColor(Color.BLACK)
AdmobInterstitialAd.getInstance().setLoadingDialogBgColor("#FFFFFF".toColorInt())
```

---

## 8. Preload interstitial ads

Used automatically when `loading_type` is not `"manual"`. Direct access:

```kotlin
AdmobPreloadInterstitialAd.getInstance()
```

| Method | Signature | Description |
|---|---|---|
| `getInstance` | `fun getInstance(): AdmobPreloadInterstitialAd` | Singleton. |
| `start` | `fun start(interAdModel: InterAdModel, adunitID: String)` | Starts `InterstitialAdPreloader` with buffer size `2`. |
| `showPreloadInter` | `fun showPreloadInter(activity: Activity, message: (String) -> Unit = {}, callBack: () -> Unit)` | Click or timer show. Called by `showInterAd()`. |
| `isReady` | `fun isReady(): Boolean` | `InterstitialAdPreloader.isAdAvailable`. |
| `clearPreloadedAds` | `fun clearPreloadedAds()` | Polls and discards buffered ads. |
| `setLoadingDialogBgColor` | `fun setLoadingDialogBgColor(loadingDialogBgColor: Int)` | Loading overlay. |
| `setLoadingDialogTextColor` | `fun setLoadingDialogTextColor(loadingDialogTextColor: Int)` | Loading overlay. |

```kotlin
AdmobInterstitialAd.getInstance().initInterFromConfig(
    this,
    InterAdModel(
        inter_type = "click", // or "timer"
        loading_type = "api",
        inter_counter_start = 1,
        inter_counter_gap = 1,
        inter_start_after_seconds = 15,
        inter_gap_after_seconds = 30
    ),
    "ca-app-pub-3940256099942544/1033173712"
)

AdmobInterstitialAd.getInstance().showInterAd(this) {
    // continue
}
```

Paid events are attached after `pollAd()` (`ad_format = interstitial`).

---

## 9. App Open ads

Constructor (create once):

```kotlin
AdmobAppOpenAd(
    applicationContext: Application,
    ad_Id: String,
    exceptionalActivities: List<String> = emptyList()
)
```

Observes `ProcessLifecycleOwner`. Shows on later foregrounds. **Cold start does not show** an app-open ad.

### Instance methods

| Method | Signature | Description |
|---|---|---|
| `loadAd` | `fun loadAd()` | Load + show. Called from process `onStart`. |
| `showAdIfAvailable` | `fun showAdIfAvailable()` | Shows the loaded ad if the activity has window focus. |

### Companion methods

| Method | Signature | Description |
|---|---|---|
| `isShowingAd` | `var isShowingAd: Boolean` | `true` while an app-open ad is on screen. `showInterAd` bails out if this is true. |
| `setPurchased` | `fun setPurchased(isPurchase: Boolean = false)` | Prefer `AdmobAdManger.isPurchased()`. |
| `setComposed` | `fun setComposed(isCompose: Boolean = false)` | Prefer `AdmobAdManger.isComposed()`. |
| `shouldshowAppOpen` | `fun shouldshowAppOpen(isInterstitialShowing: Boolean = true)` | Interstitial code sets this to `false` while an interstitial is showing. |
| `setDialogTextColor` | `fun setDialogTextColor(textcolor: Int)` | Loading overlay text. |
| `setDialogBGColor` | `fun setDialogBGColor(bgcolor: Int)` | Loading overlay background. |

### Implementation

```kotlin
MyApplication.myApplication?.let { application ->
    AdmobAppOpenAd(
        applicationContext = application,
        ad_Id = "ca-app-pub-3940256099942544/9257395921",
        exceptionalActivities = listOf("MainActivity", "SplashActivity")
    )
}
```

Skipped automatically when:

- user is premium
- an interstitial is showing (`GlobalState.isInterShowing` or `shouldshowAppOpen(false)`)
- activity simple name contains `splash`, `iap`, `AdActivity`, `premium`, or `subscription`
- activity simple name is in `exceptionalActivities`

Paid events are attached in `onAdLoaded` (`ad_format = app_open`).

---

## 10. Impression revenue

The library does **not** depend on Firebase. Every ad format attaches `OnPaidEventListener` and forwards a `Bundle` to the host app.

### `AdRevenueListener`

```kotlin
fun interface AdRevenueListener {
    fun onAdPaid(eventName: String, params: Bundle)
}
```

### `AdRevenueTracker`

| Member | Type | Description |
|---|---|---|
| `EVENT_AD_IMPRESSION` | `"ad_impression_adj"` | Event name sent to the listener. |
| `FORMAT_APP_OPEN` | `"app_open"` | |
| `FORMAT_BANNER` | `"banner"` | |
| `FORMAT_INTERSTITIAL` | `"interstitial"` | |
| `FORMAT_NATIVE` | `"native"` | |
| `setListener` | `fun setListener(revenueMultiplier: Double, listener: AdRevenueListener?)` | Prefer `AdmobAdManger.setAdRevenueListener`. |
| `notifyAdPaid` | `fun notifyAdPaid(eventName: String = EVENT_AD_IMPRESSION, params: Bundle)` | Invokes the host listener. |
| `paidEventListener` | `fun paidEventListener(adUnitId: String, adFormat: String, logTag: String): OnPaidEventListener` | Used internally by all ad classes. |
| `buildImpressionParams` | `fun buildImpressionParams(adValue: AdValue, adUnitId: String, adFormat: String): Pair<Double, Bundle>` | Builds the analytics bundle. |

### Host-app registration

```kotlin
AdmobAdManger.setAdRevenueListener(1.30) { eventName, params ->
    FirebaseAnalytics.getInstance(this).logEvent(eventName, params)
}
```

```gradle
implementation("com.google.firebase:firebase-analytics")
```

`params` keys:

| Key | Content |
|---|---|
| `value` | Adjusted revenue |
| `currency` | ISO code from AdMob |
| `ad_platform` | `"Custom"` |
| `ad_source` | `"Custom"` |
| `ad_unit_name` | Ad unit ID |
| `ad_format` | `app_open` / `banner` / `interstitial` / `native` |
| `PriceAccuracy` | `"BID"` |
| `revenue_precision` | AdMob precision type (`Int`) |

Attached on:

- App Open — `onAdLoaded`
- Banner — `AdView` creation
- Interstitial splash + inside — `onAdLoaded`
- Preload interstitial — after `pollAd()`
- Native — when the ad is displayed

---

## 11. Optional utilities

### Compose loading overlay

```kotlin
@Composable
fun AdLoadingComposable(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFF8F8F8),
    textColor: Color = Color.Black,
    progressColor: Color = Color.Black,
    onDismissRequest: () -> Unit = {}
)
```

Enabled globally with `AdmobAdManger.isComposed(true)`.

### `BlurUtils`

| Method | Signature |
|---|---|
| `applyBlurToBackground` | `fun applyBlurToBackground(activity: Activity, blurRadius: Float = 25f)` |
| `applyFastBlur` | `fun applyFastBlur(activity: Activity, blurRadius: Int = 25)` |
| `clearBlurEffect` | `fun clearBlurEffect(activity: Activity)` |

---

## 12. End-to-end application sample

```kotlin
class MyApplication : Application() {

    companion object {
        var myApplication: MyApplication? = null
    }

    override fun onCreate() {
        super.onCreate()
        myApplication = this

        AdmobInterstitialAd.getInstance().initInterFromConfig(
            this,
            InterAdModel(
                inter_type = "click",
                loading_type = "manual",
                inter_counter_start = 2,
                inter_counter_gap = 3
            ),
            "ca-app-pub-3940256099942544/1033173712"
        )

        AdmobAdManger.setAdRevenueListener(1.20) { eventName, params ->
            FirebaseAnalytics.getInstance(this).logEvent(eventName, params)
        }
    }
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        MobileAds.initialize(this)
        AdmobAdManger.isPurchased(false)
        AdmobAdManger.isComposed(false)

        AdmobInterstitialAd.getInstance().setLoadingDialogTextColor(Color.BLACK)
        AdmobInterstitialAd.getInstance().setLoadingDialogBgColor("#FFFFFF".toColorInt())

        MyApplication.myApplication?.let {
            AdmobAppOpenAd(
                it,
                "ca-app-pub-3940256099942544/9257395921",
                exceptionalActivities = listOf("SplashActivity")
            )
        }

        AdmobInterstitialAd.getInstance().loadSplashInter(
            this,
            "ca-app-pub-3940256099942544/1033173712",
            onAdLoaded = {},
            onAdFailedToLoad = {}
        )

        // Native medium
        AdmobAdManger(this, binding.adContainer, binding.adLayout)
            .setCtaPostion("bottom")
            .setTextColor("#000000".toColorInt(), "#4E4E4E".toColorInt())
            .setMargintoNative(10, 10)
            .loadAd(
                RemoteModel(
                    id = "ca-app-pub-3940256099942544/2247696110",
                    ad_format = "native",
                    ad_type = 4,
                    hide = false,
                    cta_color = "#F42727"
                ),
                DefaultAdPlacement.NATIVE
            )
    }

    fun openNextScreen() {
        AdmobInterstitialAd.getInstance().showSplashInterAd(this) { _ ->
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Standard banner
        AdmobAdManger(this, binding.adContainer, binding.adLayout)
            .loadAd(
                RemoteModel(
                    id = "ca-app-pub-3940256099942544/9214589741",
                    ad_format = "banner",
                    ad_type = 2
                ),
                DefaultAdPlacement.BANNER
            )
    }

    override fun onBackPressed() {
        AdmobInterstitialAd.getInstance().showInterAd(this) {
            finish()
        }
    }
}
```

---

## 13. Test ad units

| Format | Sample ID |
|---|---|
| App ID | `ca-app-pub-3940256099942544~3347511713` |
| App Open | `ca-app-pub-3940256099942544/9257395921` |
| Banner | `ca-app-pub-3940256099942544/9214589741` |
| Interstitial | `ca-app-pub-3940256099942544/1033173712` |
| Native | `ca-app-pub-3940256099942544/2247696110` |

Replace with your own units before release.

---

## License

Copyright 2025 [t-libraries](https://github.com/t-libraries)

Licensed under the Apache License, Version 2.0:

```
http://www.apache.org/licenses/LICENSE-2.0
```
