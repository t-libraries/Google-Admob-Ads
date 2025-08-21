package com.admobads.ads;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
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
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.Objects;

public class AdmobNativeAd {

    private final Activity ctx;
    private final FrameLayout nativeAdContainer;
    private final String id;
    private final Integer type;
    private String buttonColor;
    private String cta_btn_position = "";
    private Integer bodytextColor = 0;
    private Integer headingtextColor = 0;
    private Integer skeltonColor = 0;
    private Integer backgroundcolor = 0;
    private Integer marginstart = 0;
    private Integer marginend = 0;


    private String TAG = "AdNativeOnDemand";

    public AdmobNativeAd(Activity ctx, FrameLayout nativeAdContainer, String id, Integer type, String buttonColor) {
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

    public AdmobNativeAd setMargintoNative(Integer marginstart, Integer marginend) {
        this.marginstart = marginstart;
        this.marginend = marginend;
        return this;
    }

    public AdmobNativeAd setSkeltonColor(Integer skeltonColor) {
        this.skeltonColor = skeltonColor;
        return this;
    }

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
//                        nativeAdContainer.removeAllViews();
//                        nativeAdContainer.setVisibility(View.GONE);
                    }
                })
                .withNativeAdOptions(
                        new NativeAdOptions.Builder()
                                .setRequestCustomMuteThisAd(true)
                                .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                                .build()
                )
                .build();

        adLoader.loadAd(new AdManagerAdRequest.Builder().build());
    }

    private void setupNativeContainer(Integer adType) {
        nativeAdContainer.removeAllViews();
        AdType type = AdType.fromInt(adType);
        View loadingView = getloadingtype(adType);

        if (skeltonColor != 0) {
            SkeletonConstraintLayout skeletonLayout = loadingView.findViewById(R.id.skeletonLayout);
            skeletonLayout.setSkeletonColor(skeltonColor);
        }

        if (backgroundcolor != 0) {
            loadingView.findViewById(R.id.skeletonLayout).setBackgroundColor(ctx.getResources().getColor(backgroundcolor));
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ctx.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        Resources resources = ctx.getResources();
        float density = resources.getDisplayMetrics().density;
        int startMarginPx = (int) (marginstart * density);
        int endMarginPx = (int) (marginend * density);

        int containerWidth = screenWidth - (startMarginPx + endMarginPx);

        FrameLayout.LayoutParams containerParams = (FrameLayout.LayoutParams) nativeAdContainer.getLayoutParams();
        if (containerParams == null) {
            containerParams = new FrameLayout.LayoutParams(
                    containerWidth,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
        } else {
            containerParams.width = containerWidth;
        }

        containerParams.gravity = Gravity.CENTER_HORIZONTAL;
        nativeAdContainer.setLayoutParams(containerParams);

        FrameLayout.LayoutParams loadingViewParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        nativeAdContainer.addView(loadingView, loadingViewParams);

        nativeAdContainer.setBackgroundColor(Color.parseColor("#00000000"));
    }

    private void displayNativeAd(NativeAd nativeAd) {
        NativeAdView nativeView = createNativeView();
        bindNativeAd(nativeAd, nativeView);
        nativeAdContainer.removeAllViews();
        nativeAdContainer.addView(nativeView);
    }

    private NativeAdView createNativeView() {
        AdType adType = AdType.fromInt(type);
        int layoutId;
        switch (adType) {
            case BANNER: {
                if (Objects.equals(cta_btn_position, "top")) {
                    layoutId = R.layout.tlib_banner_variant_one;
                } else {
                    layoutId = R.layout.tlib_banner_variant_one;
                }
            }
            break;
            case SMALL:
            case ADAPTIVE:
                if (Objects.equals(cta_btn_position, "top")) {
                    layoutId = R.layout.tlib_large_variant_one_top;
                } else
                    layoutId = R.layout.tlib_adaptive_variant_one_bottom;
                break;
            case LARGE:
                if (Objects.equals(cta_btn_position, "top")) {
                    layoutId = R.layout.tlib_large_variant_two_top;
                } else
                    layoutId = R.layout.tlib_large_variant_two_bottom;
                break;
            case LARGE_1:
                layoutId = R.layout.tlib_large_variant_one_bottom;
                break;
            default:
                if (Objects.equals(cta_btn_position, "top")) {
                    layoutId = R.layout.tlib_medium_variant_one_top;
                } else
                    layoutId = R.layout.tlib_medium_variant_one_bottom;
                break;
        }

        NativeAdView nativeAdView = new NativeAdView(ctx);
        LayoutInflater.from(ctx).inflate(layoutId, nativeAdView, true);

        return nativeAdView;
    }

    private void bindNativeAd(NativeAd nativeAd, NativeAdView adView) {
        try {
            adView.setHeadlineView(adView.findViewById(R.id.primary));
            adView.setBodyView(adView.findViewById(R.id.body));
            adView.setCallToActionView(adView.findViewById(R.id.cta));
            adView.setIconView(adView.findViewById(R.id.icon));
            adView.setStarRatingView(adView.findViewById(R.id.rating_bar));
            adView.setMediaView(adView.findViewById(R.id.media_view));

            // Set rating
            AdType adType = AdType.fromInt(type);

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
            if (nativeAd.getIcon() != null && adView.getIconView() != null && adView.getIconView() instanceof ImageView) {
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

                try {
                    bodyText.setTextColor(bodytextColor);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            if (headingtextColor != 0) {
                TextView headlineView = (TextView) adView.getHeadlineView();
                assert headlineView != null;

                try {
                    headlineView.setTextColor(headingtextColor);
                } catch (Exception e) {
                    e.printStackTrace();
                }

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
                    } catch (Exception e1) {
                        try {
                            buttonColor = "#" + buttonColor.trim();
                            backgroundColor = Color.parseColor(buttonColor);
                        } catch (Exception e2) {
                            backgroundColor = Color.parseColor("#008000"); // fallback color
                        }
                    }
                } else {
                    backgroundColor = Color.parseColor("#008000"); // default green
                }


                Drawable drawable = button.getBackground();
                drawable.setColorFilter(backgroundColor, android.graphics.PorterDuff.Mode.SRC);
                button.setBackground(drawable);
            } else if (adView.getCallToActionView() != null) {
                adView.getCallToActionView().setVisibility(View.INVISIBLE);
            }
            adView.setNativeAd(nativeAd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public View getloadingtype(Integer type) {
        if (type == null)
            return LayoutInflater.from(ctx).inflate(R.layout.tlib_loading_adaptive_progress, nativeAdContainer, false);
        switch (type) {
            case 3:
                return LayoutInflater.from(ctx).inflate(R.layout.tlib_loading_adaptive_progress, nativeAdContainer, false);
            case 4:
                return LayoutInflater.from(ctx).inflate(R.layout.tlib_loading_medium_progress, nativeAdContainer, false);
            case 2:
            case 1:
                return LayoutInflater.from(ctx).inflate(R.layout.tlib_loading_large_progress, nativeAdContainer, false);
        }
        return LayoutInflater.from(ctx).inflate(R.layout.tlib_loading_adaptive_progress, nativeAdContainer, false);
    }
}


enum AdType {
    BANNER,
    SMALL,
    MEDIUM,
    LARGE,
    LARGE_1,
    ADAPTIVE;


    public static AdType fromInt(Integer type) {
        if (type == null) return SMALL; // default
        switch (type) {
            case 3:
                return SMALL;
            case 4:
                return MEDIUM;
            case 2:
                return LARGE;
            case 1:
                return LARGE_1;
            default:
                return SMALL; // fallback
        }
    }
}