package com.utilityapps.adshelperlib.networks.admob;

import static com.utilityapps.adshelperlib.AdsHelper.LOG_TAG;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.utilityapps.adshelperlib.AdsHelper;
import com.utilityapps.adshelperlib.R;
import com.utilityapps.adshelperlib.networks.INetwork;

public class AdMob implements INetwork {

    static String AD_UNIT_INTER = null;
    static String AD_UNIT_BANNER = null;
    static String AD_UNIT_NATIVE = null;
    static String AD_UNIT_APP_OPEN = null;
    static String AD_UNIT_REWARDED = null;

    public static void setAdUnitInter(String adUnit) { AD_UNIT_INTER = adUnit; }
    public static void setAdUnitBanner(String adUnit) { AD_UNIT_BANNER = adUnit; }
    public static void setAdUnitNative(String adUnit) { AD_UNIT_NATIVE = adUnit; }
    public static void setAdUnitAppOpen(String adUnit) { AD_UNIT_APP_OPEN = adUnit; }
    public static void setAdUnitRewardedInter(String adUnit) { AD_UNIT_REWARDED = adUnit; }

    private static AppOpenAdManager mAppOpenAdManager;

    static AdRequest createAdRequest() {
        return new AdRequest.Builder().build();
    }

    private boolean mIsInitialized = false;

    @Override
    public void init(Application application) {
        MobileAds.initialize(application, initializationStatus -> {
            Log.d(LOG_TAG, "AdMob initialized.");
            mIsInitialized = true;
            if (AD_UNIT_APP_OPEN != null)
                mAppOpenAdManager = new AppOpenAdManager(application);
        });
    }

    @Override
    public boolean isInitialized() {
        return mIsInitialized;
    }

    @Override
    public void loadInter(@NonNull Context context) {
        InterstitialAdManager.loadInter(context);
    }

    @Override
    public void showInter(@NonNull Activity activity, boolean autoLoading) {
        InterstitialAdManager.showInter(activity, autoLoading);
    }

    @Override
    public void setInterEnabled(boolean enabled) {
        InterstitialAdManager.setEnabled(enabled);
    }

    @Override
    public void setInterDelay(long millis) {
        InterstitialAdManager.setMillisBetweenInter(millis);
    }

    @Override
    public void loadRewardedInter(@NonNull Context context) {
        RewardedInterstitialAdManager.loadRewarded(context);
    }

    @Override
    public void showRewardedInter(@NonNull Activity activity, boolean autoLoading, @Nullable IOnReward onReward) {
        RewardedInterstitialAdManager.showRewarded(activity, autoLoading, onReward);
    }

    @Override
    public boolean isRewardedAvailable() {
        return RewardedInterstitialAdManager.isRewardedAvailable();
    }

    @Override
    public void loadAndShowBanner(@NonNull Activity activity, @NonNull ViewGroup container) {

        if (AD_UNIT_BANNER == null)
            return;

        Log.d(LOG_TAG, "Loading banner...");
        AdView adView = new AdView(activity);
        adView.setAdSize(getAdSize(activity));
        adView.setAdUnitId(AD_UNIT_BANNER);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                container.removeAllViews();
                container.addView(adView);
            }
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                Log.d(LOG_TAG, "Banner failed to load: " + error);
            }
        });
        adView.loadAd(createAdRequest());

    }

    private static AdSize getAdSize(Activity activity) {

        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }

    @Override
    public void loadAndShowNative(@NonNull Context context, @NonNull LayoutInflater inflater, int layoutResId, @NonNull ViewGroup container) {

        if (AD_UNIT_NATIVE == null)
            return;

        Log.d(LOG_TAG, "Loading NativeAd...");
        AdLoader adLoader = new AdLoader.Builder(context, AD_UNIT_NATIVE)
                .forNativeAd(ad -> {

                    Log.d(LOG_TAG, "NativeAd loaded, inflating!");

                    NativeAdView adView = (NativeAdView) inflater.inflate(layoutResId, container, false);
                    container.removeAllViews();
                    container.addView(adView);

                    // Set the media view
                    MediaView mediaView = adView.findViewById(R.id.img_warning_ad_img);
                    if (mediaView != null) {
                        adView.setMediaView(mediaView);
                        adView.getMediaView().setImageScaleType(ImageView.ScaleType.FIT_XY);
                        if (ad.getMediaContent() != null && !ad.getMediaContent().hasVideoContent()) {
                            adView.getMediaView().setMediaContent(ad.getMediaContent());
                        }
                    }

                    TextView title = adView.findViewById(R.id.txt_warning_ad_title);
                    TextView text = adView.findViewById(R.id.txt_warning_ad_text);
                    Button btn = adView.findViewById(R.id.btn_warning_button);
                    ImageView icon = adView.findViewById(R.id.img_warning_icon);

                    // Set other ad assets.
                    if (title != null)
                        adView.setHeadlineView(title);

                    if (text != null)
                        adView.setBodyView(text);

                    if (btn != null)
                        adView.setCallToActionView(adView.findViewById(R.id.btn_warning_button));

                    if (icon != null)
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
    }
}
