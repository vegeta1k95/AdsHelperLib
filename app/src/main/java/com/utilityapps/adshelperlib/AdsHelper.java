package com.utilityapps.adshelperlib;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleObserver;

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
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.HashMap;
import java.util.Map;

public class AdsHelper {

    private static boolean ADS_ENABLED = false;

    private static final String LOG_TAG = "MYTAG (AdHelper)";

    private static final String PREFERENCES = "ads";
    private static final String KEY_LAST_INTER = "last_time";
    private static final String KEY_ADS_ENABLED = "ads_enabled";

    private static final long MILLIS_BETWEEN_INTER = 60000; // 1 minute

    static String AD_UNIT_INTER = "ca-app-pub-3940256099942544/1033173712";
    static String AD_UNIT_BANNER = "ca-app-pub-3940256099942544/6300978111";
    static String AD_UNIT_NATIVE = "ca-app-pub-3940256099942544/2247696110";
    static String AD_UNIT_APP_OPEN = "ca-app-pub-3940256099942544/3419835294";

    private static InterstitialAd mInter;
    private static AppOpenAdManager mAppOpenAdManager;

    private static AdRequest createAdRequest() {
        return new AdRequest.Builder().build();
    }

    public static void setAdUnitInter(String adUnit) { AD_UNIT_INTER = adUnit; }
    public static void setAdUnitBanner(String adUnit) { AD_UNIT_BANNER = adUnit; }
    public static void setAdUnitNative(String adUnit) { AD_UNIT_NATIVE = adUnit; }
    public static void setAdUnitAppOpen(String adUnit) { AD_UNIT_APP_OPEN = adUnit; }

    public static void initialize(Application application) {
        FirebaseApp.initializeApp(application);
        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        Map<String, Object> defaults = new HashMap<>();
        defaults.put(KEY_ADS_ENABLED, false);
        config.setDefaultsAsync(defaults).addOnCompleteListener(t ->
                config.fetchAndActivate().addOnCompleteListener(task -> {
                    ADS_ENABLED = config.getBoolean(KEY_ADS_ENABLED);
                    if (ADS_ENABLED) {
                        Log.d(LOG_TAG, "Ads enabled!");
                        MobileAds.initialize(application, initializationStatus
                                -> mAppOpenAdManager = new AppOpenAdManager(application));
                    } else {
                        Log.d(LOG_TAG, "Ads disabled!");
                    }
                }));

    }

    public static void loadInter(@Nullable Context context) {

        if (context == null || !ADS_ENABLED)
            return;

        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

        long lastTime = prefs.getLong(KEY_LAST_INTER, 0);
        long now = System.currentTimeMillis();

        if (now - lastTime < MILLIS_BETWEEN_INTER)
            return;

        MobileAds.initialize(context, initializationStatus ->
                InterstitialAd.load(context, AD_UNIT_INTER, createAdRequest(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInter = interstitialAd;
                        mInter.setFullScreenContentCallback(new FullScreenContentCallback() {

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError error) {
                                mInter = null;
                                Log.d(LOG_TAG, "Inter showing error: " + error);
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
                        Log.d(LOG_TAG, "Inter loading error: " + loadAdError);
                    }
                }));

    }

    public static void showInter(Activity activity) {
        if (mInter != null && activity != null && ADS_ENABLED)
            mInter.show(activity);
    }

    public static void loadAndShowBanner(@Nullable Activity activity, @NonNull ViewGroup container) {

        if (activity == null || !ADS_ENABLED)
            return;

        MobileAds.initialize(activity, initializationStatus -> {
            AdView adView = new AdView(activity);
            adView.setAdSize(getAdSize(activity));
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

    private static AdSize getAdSize(Activity activity) {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }


    public static void loadAndShowNative(@Nullable Context context,
                                         @NonNull LayoutInflater inflater,
                                         int layoutResId,
                                         @NonNull ViewGroup container) {

        if (context == null || !ADS_ENABLED)
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

