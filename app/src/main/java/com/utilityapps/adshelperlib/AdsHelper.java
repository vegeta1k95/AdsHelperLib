package com.utilityapps.adshelperlib;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAdView;

public class AdsHelper {

    private static final String LOG_TAG = "MYTAG (AdHelper)";

    private static String AD_UNIT_INTER = "ca-app-pub-8327815374327931/2179415243";
    private static String AD_UNIT_BANNER = "ca-app-pub-8327815374327931/7431741924";
    private static String AD_UNIT_NATIVE = "ca-app-pub-8327815374327931/3300925224";

    private static final String PREFERENCES = "ads";
    private static final String KEY_LAST_INTER = "last_time";

    private static final long MILLIS_BETWEEN_INTER = 60000; // 1 minute

    private static InterstitialAd mInter;

    private static AdRequest createAdRequest() {
        Bundle extras = new Bundle();
        extras.putString("npa", "1");
        return new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();
    }

    private static void setAdUnitInter(String adUnit) { AD_UNIT_INTER = adUnit; }
    private static void setAdUnitBanner(String adUnit) { AD_UNIT_BANNER = adUnit; }
    private static void setAdUnitNative(String adUnit) { AD_UNIT_NATIVE = adUnit; }

    public static void loadInter(@Nullable Context context) {

        if (context == null)
            return;

        MobileAds.initialize(context, initializationStatus -> {
            SharedPreferences prefs = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

            long lastTime = prefs.getLong(KEY_LAST_INTER, 0);
            long now = System.currentTimeMillis();

            if (now - lastTime < MILLIS_BETWEEN_INTER)
                return;

            InterstitialAd.load(context, AD_UNIT_INTER, createAdRequest(),
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            mInter = interstitialAd;
                            mInter.setFullScreenContentCallback(new FullScreenContentCallback() {

                                @Override
                                public void onAdFailedToShowFullScreenContent(@NonNull AdError error) {
                                    mInter = null;
                                    Log.d(LOG_TAG, "Inter showing error: " + error.toString());
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    mInter = null;
                                    SharedPreferences prefs = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putLong(KEY_LAST_INTER, System.currentTimeMillis());
                                    editor.apply();
                                    loadInter(context);
                                }
                            });
                            Log.d(LOG_TAG, "Inter loaded!");
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            mInter = null;
                            Log.d(LOG_TAG, "Inter loading error: " + loadAdError.toString());
                        }
                    });
        });

    }

    public static void showInter(@NonNull Activity activity) {
        if (mInter != null)
            mInter.show(activity);
    }

    public static void loadAndShowBanner(@Nullable Context context, @NonNull ViewGroup container) {

        if (context == null)
            return;

        MobileAds.initialize(context, initializationStatus -> {
            AdView adView = new AdView(context);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(AD_UNIT_BANNER);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    container.removeAllViews();
                    container.addView(adView);
                }
            });
            adView.loadAd(createAdRequest());
        });
    }


    public static void loadAndShowNative(@Nullable Context context,
                                         @NonNull LayoutInflater inflater,
                                         int layoutResId,
                                         @NonNull ViewGroup container) {

        if (context == null)
            return;

        MobileAds.initialize(context, initializationStatus -> {
            AdLoader adLoader = new AdLoader.Builder(context, AD_UNIT_NATIVE)
                    .forNativeAd(ad -> {
                        NativeAdView adView = (NativeAdView) inflater.inflate(layoutResId, container, false);
                        container.removeAllViews();
                        container.addView(adView);

                        // Set the media view
                        adView.setMediaView(adView.findViewById(R.id.img_warning_ad_img));
                        adView.getMediaView().setImageScaleType(ImageView.ScaleType.FIT_XY);

                        if (ad.getMediaContent() != null && !ad.getMediaContent().hasVideoContent()) {
                            adView.getMediaView().setMediaContent(ad.getMediaContent());
                        }

                        // Set other ad assets.
                        adView.setHeadlineView(adView.findViewById(R.id.txt_warning_ad_title));
                        adView.setBodyView(adView.findViewById(R.id.txt_warning_ad_text));
                        adView.setCallToActionView(adView.findViewById(R.id.btn_warning_button));
                        adView.setIconView(adView.findViewById(R.id.img_warning_icon));

                        // The headline, body are guaranteed to be in every UnifiedNativeAd.
                        ((TextView) adView.getHeadlineView()).setText(ad.getHeadline());
                        ((TextView) adView.getBodyView()).setText(ad.getBody());

                        if (ad.getIcon() != null) {
                            ((ImageView) adView.getIconView()).setImageDrawable(ad.getIcon().getDrawable());
                        }

                        if (ad.getCallToAction() != null) {
                            ((Button) adView.getCallToActionView()).setText(ad.getCallToAction());
                        }
                        adView.setNativeAd(ad);

                    }).build();
            adLoader.loadAd(createAdRequest());
        });

    }

}

