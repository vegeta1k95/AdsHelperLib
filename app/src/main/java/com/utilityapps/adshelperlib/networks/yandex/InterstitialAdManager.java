package com.utilityapps.adshelperlib.networks.yandex;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.utilityapps.adshelperlib.AdsHelper.LOG_TAG;

import com.utilityapps.adshelperlib.AdsHelper;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.common.MobileAds;
import com.yandex.mobile.ads.interstitial.InterstitialAd;
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener;

public class InterstitialAdManager {

    private static long MILLIS_BETWEEN_INTER = 60000; // 1 minute

    private static final String PREFERENCES = "ads";
    private static final String KEY_LAST_INTER = "last_time";

    private static boolean mIsEnabled = true;
    private static boolean mLoading = false;
    private static boolean mShowing = false;

    private static InterstitialAd mInter;

    private static boolean isTimeToLoad(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        long lastTime = prefs.getLong(KEY_LAST_INTER, 0);
        long now = System.currentTimeMillis();
        return (now - lastTime > MILLIS_BETWEEN_INTER);
    }

    public static void setEnabled(boolean enabled) {
        mIsEnabled = enabled;
    }

    public static void setMillisBetweenInter(long millis) {
        MILLIS_BETWEEN_INTER = millis;
    }

    public static void loadInter(@NonNull Context context) {

        if (!mIsEnabled || Yandex.AD_UNIT_INTER == null)
            return;

        if (!isTimeToLoad(context)) {
            Log.d(LOG_TAG, "Not time yet to load inter!");
            return;
        }

        if (mLoading) {
            Log.d(LOG_TAG, "Loading is already in progress!");
            return;
        }

        if (mInter != null) {
            Log.d(LOG_TAG, "Cannot load new inter while old is still there!");
            return;
        }

        mLoading = true;

        Log.d(LOG_TAG, "Loading inter...");

        mInter = new InterstitialAd(context);
        mInter.setAdUnitId(Yandex.AD_UNIT_INTER);
        mInter.setInterstitialAdEventListener(new InterstitialAdEventListener() {
            @Override
            public void onAdLoaded() {
                mLoading = false;
                Log.d(LOG_TAG, "Inter loaded!");
            }

            @Override
            public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
                mLoading = false;
                mInter = null;
                Log.d(LOG_TAG, "Inter loading error: " + adRequestError.getDescription());
            }

            @Override
            public void onAdShown() {
                mInter = null;
                mShowing = true;
                SharedPreferences prefs = context.getSharedPreferences(
                        PREFERENCES, Context.MODE_PRIVATE);
                prefs.edit()
                        .putLong(KEY_LAST_INTER, System.currentTimeMillis())
                        .apply();
                Log.d(LOG_TAG, "Inter was shown!");
            }

            @Override
            public void onAdDismissed() {
                mInter = null;
                mShowing = false;
                Log.d(LOG_TAG, "Inter dismissed!");
            }

            @Override public void onAdClicked() {}
            @Override public void onLeftApplication() {}
            @Override public void onReturnedToApplication() {}
            @Override public void onImpression(@Nullable ImpressionData impressionData) {}
        });

        mInter.loadAd(Yandex.createAdRequest());

    }

    public static void showInter(@NonNull Activity activity) {

        if (Yandex.AD_UNIT_INTER == null || !mIsEnabled)
            return;

        if (mInter == null || !mInter.isLoaded()) {
            Log.d(LOG_TAG, "No inter to show!");
            return;
        }

        if (mShowing) {
            Log.d(LOG_TAG, "Inter is shown right now!");
            return;
        }

        if (isLaunchedFromPush(activity)) {
            Log.d(LOG_TAG, "Not showing when launched from push!");
            return;
        }

        mInter.show();
    }

    public static boolean isLaunchedFromPush(Activity activity) {
        Intent intent = activity.getIntent();
        Bundle extras = intent.getExtras();
        if (extras == null)
            return false;
        return extras.containsKey("billing_push_text")
                || extras.containsKey("billing_push_offer");
    }

}

