package com.admobads.ads;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import com.admobads.loading.skeleton.layout.SkeletonConstraintLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.Objects;

public class AdmobNativeAd {

    private final Activity ctx;
    private final FrameLayout nativeAdContainer;
    private final String id;
    private final String type;
    private String buttonColor;
    private String cta_btn_position = "";
    private Integer bodytextColor = 0;
    private Integer headingtextColor = 0;
    private Integer skeltonColor = 0;
    private Integer backgroundcolor = 0;


    private String TAG = "AdNativeOnDemand";

    public AdmobNativeAd(Activity ctx, FrameLayout nativeAdContainer, String id, String type, String buttonColor) {
        this.ctx = ctx;
        this.nativeAdContainer = nativeAdContainer;
        this.id = id;
        this.type = type;
        this.buttonColor = buttonColor;
    }

    public AdmobNativeAd setTextColor(Integer bodytextColor, Integer headingtextColor) {
        this.bodytextColor = bodytextColor;
        this.headingtextColor = headingtextColor;
        return this;
    }

    public AdmobNativeAd setSkeltonColor(Integer skeltonColor) {
        this.skeltonColor = skeltonColor;
        return this;
    }


//    public AdmobNativeAd setBackgroundColor(Integer bgcolor) {
//        this.backgroundcolor = bgcolor;
//        return this;
//    }

    public AdmobNativeAd setCtaButtonPosition(String cta_btn_position) {
        this.cta_btn_position = cta_btn_position;
        return this;
    }

    public void load() {
        setupNativeContainer(type); // Adjusted to use `type` passed in constructor
        AdLoader adLoader = new AdLoader.Builder(ctx, id)
                .forNativeAd(this::displayNativeAd)
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        Log.d(TAG, "Ad Loaded");
                        // No-op
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.d(TAG, "Failed :: " + loadAdError.getMessage());
                        nativeAdContainer.removeAllViews();
                        nativeAdContainer.setVisibility(View.GONE);
                    }
                }).build();

        adLoader.loadAd(new AdManagerAdRequest.Builder().build());
    }

    private void setupNativeContainer(String adType) {
        // Remove any existing views in the container
        nativeAdContainer.removeAllViews();

        // Determine the height for the loading view based on ad type
        AdType type = AdType.fromString(adType);
        int adHeight = type.getHeightDp();

        // Convert height from DP to pixels
        float density = ctx.getResources().getDisplayMetrics().density;
        int heightPx = (int) (adHeight * density);

        // Inflate the loading view
        View loadingView = getloadingtype(adType);

        if (skeltonColor != 0) {
            SkeletonConstraintLayout skeletonLayout = loadingView.findViewById(R.id.skeletonLayout);
            skeletonLayout.setSkeletonColor(skeltonColor);
        }

        if (backgroundcolor != 0)
            loadingView.findViewById(R.id.skeletonLayout).setBackgroundColor(ctx.getResources().getColor(backgroundcolor));

        // Set the height of the loading view
        ViewGroup.LayoutParams layoutParams = loadingView.getLayoutParams();
        layoutParams.height = heightPx;
        loadingView.setLayoutParams(layoutParams);

        // Add the loading view to the container
        nativeAdContainer.addView(loadingView);

        // Set background color
        nativeAdContainer.setBackgroundColor(Color.parseColor("#00000000"));
    }

    private void displayNativeAd(NativeAd nativeAd) {
        NativeAdView nativeView = createNativeView();
        bindNativeAd(nativeAd, nativeView);
        nativeAdContainer.removeAllViews();
        nativeAdContainer.addView(nativeView);
    }

    private NativeAdView createNativeView() {
        AdType adType = AdType.fromString(type);
        int layoutId;
        switch (adType) {
            case BANNER: {
                if (Objects.equals(cta_btn_position, "top")) {
                    layoutId = R.layout.gnt_small_template_view_side_button_a;
                } else {
                    layoutId = R.layout.gnt_small_template_view_side_button_a;
                }
            }
            break;
            case SMALL:
            case ADAPTIVE:
                if (Objects.equals(cta_btn_position, "top")) {
                    layoutId = R.layout.large_variant_six_top;
                } else
                    layoutId = R.layout.large_variant_six_bottom;
                break;
            case LARGE:
                if (Objects.equals(cta_btn_position, "top")) {
                    layoutId = R.layout.large_variant_four_top;
                } else
                    layoutId = R.layout.large_variant_four_bottom;
                break;
            case LARGE_1:
                layoutId = R.layout.large_variant_three_bottom;
                break;
            default:
                if (Objects.equals(cta_btn_position, "top")) {
                    layoutId = R.layout.large_variant_five_top;
                } else
                    layoutId = R.layout.large_variant_five_bottom;
                break;
        }

        NativeAdView nativeAdView = new NativeAdView(ctx);
        LayoutInflater.from(ctx).inflate(layoutId, nativeAdView, true);

        return nativeAdView;
    }

    private void bindNativeAd(NativeAd nativeAd, NativeAdView adView) {
        adView.setHeadlineView(adView.findViewById(R.id.primary));
        adView.setBodyView(adView.findViewById(R.id.body));
        adView.setCallToActionView(adView.findViewById(R.id.cta));
        adView.setIconView(adView.findViewById(R.id.icon));
        adView.setStarRatingView(adView.findViewById(R.id.rating_bar));
        adView.setMediaView(adView.findViewById(R.id.media_view));

        // Set rating
        AdType adType = AdType.fromString(type);

        if (adType != AdType.BANNER) {
            if (adType != AdType.LARGE_1) {
                if (nativeAd.getStarRating() == null) {
                    if (adView.getStarRatingView() != null)
                        adView.getStarRatingView().setVisibility(View.GONE);
                } else if (adView.getStarRatingView() instanceof RatingBar) {
                    adView.getStarRatingView().setVisibility(View.VISIBLE);
                    ((RatingBar) adView.getStarRatingView()).setRating(nativeAd.getStarRating().floatValue());
                }
            }
        }

        // Set icon
        if (nativeAd.getIcon() != null && adView.getIconView() instanceof ImageView) {
            adView.getIconView().setVisibility(View.VISIBLE);
            ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
        } else if (adView.getIconView() != null) {
            adView.getIconView().setVisibility(View.GONE);
        }

        // Set media content
        if (nativeAd.getMediaContent() != null && adView.getMediaView() != null) {
            adView.getMediaView().setVisibility(View.VISIBLE);
            adView.getMediaView().setMediaContent(nativeAd.getMediaContent());
        }

        // Headline
        if (adView.getHeadlineView() instanceof TextView) {
            ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
            adView.getHeadlineView().setSelected(true);
        }

        // Body
        if (nativeAd.getBody() != null && adView.getBodyView() instanceof TextView) {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        } else if (adView.getBodyView() != null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        }


        //TextViews
        if (bodytextColor != 0) {
            TextView bodyText = (TextView) adView.getBodyView();
            assert bodyText != null;
            bodyText.setTextColor(ctx.getResources().getColor(bodytextColor));
        }

        if (headingtextColor != 0) {
            TextView headlineView = (TextView) adView.getHeadlineView();
            assert headlineView != null;
            headlineView.setTextColor(ctx.getResources().getColor(headingtextColor));
        }


        // Call to action
        if (nativeAd.getCallToAction() != null && adView.getCallToActionView() instanceof AppCompatButton) {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            AppCompatButton button = (AppCompatButton) adView.getCallToActionView();
            button.setText(nativeAd.getCallToAction());
            int backgroundColor;
            if (buttonColor != null) {
                try {
                    backgroundColor = Color.parseColor(buttonColor.trim());
                } catch (Exception e) {
                    buttonColor = "#" + buttonColor;
                    backgroundColor = Color.parseColor(buttonColor.trim());
                }

            } else {
                backgroundColor = Color.parseColor("#008000");
            }

            Drawable drawable = button.getBackground();
            drawable.setColorFilter(backgroundColor, android.graphics.PorterDuff.Mode.SRC);
            button.setBackground(drawable);
        } else if (adView.getCallToActionView() != null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        }
        adView.setNativeAd(nativeAd);
    }

    public View getloadingtype(String type) {
        if (type == null)
            return LayoutInflater.from(ctx).inflate(R.layout.gnt_loading_large_progress, nativeAdContainer, false);
        switch (type.toLowerCase()) {
            case "banner":
                return LayoutInflater.from(ctx).inflate(R.layout.gnt_loading_banner_progress, nativeAdContainer, false);
            case "small":
            case "adaptive":
                return LayoutInflater.from(ctx).inflate(R.layout.gnt_loading_adaptive_progress, nativeAdContainer, false);
            case "medium":
                return LayoutInflater.from(ctx).inflate(R.layout.gnt_loading_medium_progress, nativeAdContainer, false);
            case "large":
                return LayoutInflater.from(ctx).inflate(R.layout.gnt_loading_large_progress, nativeAdContainer, false);
            case "large_1":
                return LayoutInflater.from(ctx).inflate(R.layout.gnt_loading_large_progress, nativeAdContainer, false);
        }
        return null;
    }
}


enum AdType {
    BANNER(72),
    SMALL(175),
    MEDIUM(210),
    LARGE(288),
    LARGE_1(288),
    ADAPTIVE(175);

    private final int heightDp;

    AdType(int heightDp) {
        this.heightDp = heightDp;
    }

    public int getHeightDp() {
        return heightDp;
    }

    public static AdType fromString(String type) {
        if (type == null) return LARGE; // default
        switch (type.toLowerCase()) {
            case "banner":
                return BANNER;
            case "small":
                return SMALL;
            case "medium":
                return MEDIUM;
            case "large":
                return LARGE;
            case "large_1":
                return LARGE_1;
            case "adaptive":
                return ADAPTIVE;
            default:
                return LARGE; // fallback
        }
    }
}