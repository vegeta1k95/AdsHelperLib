package com.utilityapps.adshelperlib.networks.yandex;

import static com.utilityapps.adshelperlib.AdsHelper.LOG_TAG;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.utilityapps.adshelperlib.AdsHelper;
import com.utilityapps.adshelperlib.R;
import com.utilityapps.adshelperlib.networks.INetwork;
import com.yandex.mobile.ads.banner.AdSize;
import com.yandex.mobile.ads.banner.BannerAdEventListener;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.common.MobileAds;
import com.yandex.mobile.ads.nativeads.NativeAd;
import com.yandex.mobile.ads.nativeads.NativeAdLoadListener;
import com.yandex.mobile.ads.nativeads.NativeAdLoader;
import com.yandex.mobile.ads.nativeads.NativeAdRequestConfiguration;
import com.yandex.mobile.ads.nativeads.template.NativeBannerView;

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

    private boolean mIsInitialized = false;

    @Override
    public void init(Application application, @Nullable AdsHelper.IOnInit onInit) {
        MobileAds.initialize(application, () -> {
            Log.d(LOG_TAG, "Yandex initialized.");
            mIsInitialized = true;

            if (onInit != null)
                new Handler(Looper.getMainLooper()).post(onInit::onInit);
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
    public void showInter(@NonNull Activity activity) { InterstitialAdManager.showInter(activity); }

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
        RewardedInterstitialAdManager.showRewarded(onReward);
    }

    @Override
    public boolean hasWatchedRewarded() {
        return RewardedInterstitialAdManager.hasWatchedRewarded();
    }

    @Override
    public boolean isRewardedAvailable() {
        return RewardedInterstitialAdManager.isRewardedAvailable();
    }

    @Override
    public void loadAndShowBanner(@NonNull Activity activity, @NonNull ViewGroup container) {

        if (AD_UNIT_BANNER == null)
            return;

        BannerAdView bannerAdView = new BannerAdView(activity);
        bannerAdView.setAdUnitId(AD_UNIT_BANNER);
        bannerAdView.setAdSize(getAdSize(activity));

        bannerAdView.setBannerAdEventListener(new BannerAdEventListener() {
            @Override
            public void onAdLoaded() {
                container.removeAllViews();
                ViewGroup.LayoutParams params = container.getLayoutParams();
                params.height = activity.getResources().getDimensionPixelSize(R.dimen._35sdp);
                container.setLayoutParams(params);
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

    private NativeAdLoader mNativeAdLoader;
    private NativeAd mNativeAd;

    @Override
    public void loadAndShowNative(@NonNull Context context, @NonNull LayoutInflater inflater,
                                  int layoutResId, @NonNull ViewGroup container) {

        mNativeAdLoader = new NativeAdLoader(context);
        mNativeAdLoader.setNativeAdLoadListener(new NativeAdLoadListener() {
            @Override
            public void onAdLoaded(@NonNull final NativeAd nativeAd) {
                Log.d(LOG_TAG, "Native ad loaded, inflating!");
                mNativeAd = nativeAd;
                final NativeBannerView nativeBannerView = new NativeBannerView(context);
                nativeBannerView.setAd(mNativeAd);
                container.removeAllViews();
                container.addView(nativeBannerView);
            }

            @Override
            public void onAdFailedToLoad(@NonNull final AdRequestError error) {
                Log.d(LOG_TAG, "Native ad failed to load: " + error.getDescription());
            }
        });

        final NativeAdRequestConfiguration nativeAdRequestConfiguration =
                new NativeAdRequestConfiguration.Builder(AD_UNIT_NATIVE).build();
        mNativeAdLoader.loadAd(nativeAdRequestConfiguration);

    }
}
