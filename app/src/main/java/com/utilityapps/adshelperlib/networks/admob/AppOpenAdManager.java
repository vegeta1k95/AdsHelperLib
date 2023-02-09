package com.utilityapps.adshelperlib.networks.admob;


import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.utilityapps.adshelperlib.AdsHelper;

import java.util.Arrays;
import java.util.List;

public class AppOpenAdManager implements DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private static final String LOG_TAG = "MYTAG (AppOpenAdManager)";

    private static boolean isShowing = false;

    private AppOpenAd mAd;
    private final Application mApplication;
    private Activity mActivity;

    private long mLoadTime = 0;

    public AppOpenAdManager(Application application) {
        mApplication = application;
        mApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    private void loadAd() {

        if (isAdAvailable())
            return;

        Log.d(LOG_TAG, "Loading AppOpen...");

        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(mApplication, AdMob.AD_UNIT_APP_OPEN, request,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                        Log.d(LOG_TAG, "AppOpen loaded!");
                        mAd = appOpenAd;
                        mLoadTime = System.currentTimeMillis();
                    }

                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        Log.d(LOG_TAG, "AppOpen failed to load: " + error);
                        mAd = null;
                    }
                });
    }

    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = System.currentTimeMillis() - this.mLoadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

    private boolean isAdAvailable() {
        return mAd != null && wasLoadTimeLessThanNHoursAgo(4);
    }

    private void showAdIfAvailable() {
        if (!isShowing && isAdAvailable()) {
            Log.d(LOG_TAG, "Showing AppOpen...");
            mAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    mAd = null;
                    isShowing = false;
                    loadAd();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    isShowing = true;
                }
            });
            mAd.show(mActivity);

        } else {
            loadAd();
        }
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        showAdIfAvailable();
    }

    @Override public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}
    @Override public void onActivityStarted(@NonNull Activity activity) { mActivity = activity; }
    @Override public void onActivityResumed(@NonNull Activity activity) { mActivity = activity; }
    @Override public void onActivityPaused(@NonNull Activity activity) {}
    @Override public void onActivityStopped(@NonNull Activity activity) {}
    @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
    @Override public void onActivityDestroyed(@NonNull Activity activity) { mActivity = null; }
}