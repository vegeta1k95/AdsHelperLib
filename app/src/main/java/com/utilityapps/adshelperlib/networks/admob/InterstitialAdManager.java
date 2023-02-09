package com.utilityapps.adshelperlib.networks.admob;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import static com.utilityapps.adshelperlib.AdsHelper.LOG_TAG;

public class InterstitialAdManager {

    private static long MILLIS_BETWEEN_INTER = 60000; // 1 minute

    private static final String PREFERENCES = "ads";
    private static final String KEY_LAST_INTER = "last_time";

    private static boolean mIsEnabled = true;
    private static boolean mLoading = false;
    private static boolean mShowing = false;

    private static InterstitialAd mInter;

    private static boolean isTimeToShow(Context context) {
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

        if (!mIsEnabled || AdMob.AD_UNIT_INTER == null)
            return;

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
        InterstitialAd.load(context, AdMob.AD_UNIT_INTER, AdMob.createAdRequest(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mLoading = false;
                        mInter = interstitialAd;
                        Log.d(LOG_TAG, "Inter loaded!");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mLoading = false;
                        mInter = null;
                        Log.d(LOG_TAG, "Inter loading error: " + loadAdError);
                    }
                });

    }

    public static void showInter(@NonNull Activity activity) {

        if (AdMob.AD_UNIT_INTER == null || !mIsEnabled)
            return;

        if (mInter == null) {
            Log.d(LOG_TAG, "No inter to show!");
            return;
        }

        if (!isTimeToShow(activity)) {
            Log.d(LOG_TAG, "Not time yet to show inter!");
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

        mInter.setFullScreenContentCallback(new FullScreenContentCallback() {

            @Override
            public void onAdDismissedFullScreenContent() {
                mInter = null;
                mShowing = false;
                Log.d(LOG_TAG, "Inter dismissed!");
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError error) {
                mInter = null;
                mShowing = false;
                loadInter(activity);
                Log.d(LOG_TAG, "Inter showing error: " + error);
            }

            @Override
            public void onAdShowedFullScreenContent() {
                mInter = null;
                mShowing = true;
                SharedPreferences prefs = activity.getSharedPreferences(
                        PREFERENCES, Context.MODE_PRIVATE);
                prefs.edit()
                        .putLong(KEY_LAST_INTER, System.currentTimeMillis())
                        .apply();
                loadInter(activity);
                Log.d(LOG_TAG, "Inter was shown!");

            }
        });
        mInter.show(activity);
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
