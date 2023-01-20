package com.utilityapps.adshelperlib.networks.yandex;

import static com.utilityapps.adshelperlib.AdsHelper.LOG_TAG;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.utilityapps.adshelperlib.AdsHelper;
import com.utilityapps.adshelperlib.networks.INetwork;
import com.yandex.mobile.ads.banner.AdSize;
import com.yandex.mobile.ads.banner.BannerAdEventListener;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.common.MobileAds;

public class Yandex implements INetwork {

    static String AD_UNIT_INTER = null;
    static String AD_UNIT_BANNER = null;
    static String AD_UNIT_NATIVE = null;
    static String AD_UNIT_REWARDED = null;

    public static void setAdUnitInter(String adUnit) { AD_UNIT_INTER = adUnit; }
    public static void setAdUnitBanner(String adUnit) { AD_UNIT_BANNER = adUnit; }
    public static void setAdUnitNative(String adUnit) { AD_UNIT_NATIVE = adUnit; }
    public static void setAdUnitRewardedInter(String adUnit) { AD_UNIT_REWARDED = adUnit; }

    static AdRequest createAdRequest() {
        return new AdRequest.Builder().build();
    }

    @Override
    public void init(Application application) {
        MobileAds.initialize(application, () ->
                Log.d(LOG_TAG, "Yandex initialized."));
    }

    @Override
    public void loadInter(@Nullable Context context) {
        InterstitialAdManager.loadInter(context);
    }

    @Override
    public void showInter(@Nullable Activity activity, boolean autoLoading) {
        InterstitialAdManager.showInter(activity);
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
    public void loadRewardedInter(@Nullable Context context) {
        RewardedInterstitialAdManager.loadRewarded(context);
    }

    @Override
    public void showRewardedInter(@Nullable Activity activity, boolean autoLoading, @Nullable IOnReward onReward) {
        RewardedInterstitialAdManager.showRewarded(activity, onReward);
    }

    @Override
    public boolean isRewardedAvailable() {
        return RewardedInterstitialAdManager.isRewardedAvailable();
    }

    @Override
    public void loadAndShowBanner(@Nullable Activity activity, @NonNull ViewGroup container) {
        if (activity == null || !AdsHelper.ADS_ENABLED || AD_UNIT_BANNER == null)
            return;

        MobileAds.initialize(activity, () -> {
            Log.d(LOG_TAG, "Loading banner...");

            BannerAdView bannerAdView = new BannerAdView(activity);
            bannerAdView.setAdUnitId(AD_UNIT_BANNER);
            bannerAdView.setAdSize(getAdSize(activity));

            bannerAdView.setBannerAdEventListener(new BannerAdEventListener() {
                @Override
                public void onAdLoaded() {
                    container.removeAllViews();
                    container.addView(bannerAdView);
                }

                @Override
                public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
                    Log.d(LOG_TAG, "Banner failed to load: " + adRequestError);
                }

                @Override public void onAdClicked() {}
                @Override public void onLeftApplication() {}
                @Override public void onReturnedToApplication() {}
                @Override public void onImpression(@Nullable ImpressionData impressionData) {}
            });

            bannerAdView.loadAd(createAdRequest());
        });
    }

    private static AdSize getAdSize(Activity activity) {

        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);
        return AdSize.stickySize(adWidth);
    }

    @Override
    public void loadAndShowNative(@Nullable Context context, @NonNull LayoutInflater inflater, int layoutResId, @NonNull ViewGroup container) {

    }
}
