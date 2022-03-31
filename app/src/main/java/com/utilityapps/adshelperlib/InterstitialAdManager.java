package com.utilityapps.adshelperlib;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import static com.utilityapps.adshelperlib.AdsHelper.LOG_TAG;

public class InterstitialAdManager {

    private static final long MILLIS_BETWEEN_INTER = 60000; // 1 minute

    private static final String PREFERENCES = "ads";
    private static final String KEY_LAST_INTER = "last_time";

    private static boolean mLoading = false;
    private static boolean mShowing = false;

    private static InterstitialAd mInter;

    private static boolean isTimeToLoad(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        long lastTime = prefs.getLong(KEY_LAST_INTER, 0);
        long now = System.currentTimeMillis();
        return (now - lastTime > MILLIS_BETWEEN_INTER);
    }

    public static void loadInter(@Nullable Context context) {

        if (context == null
                || !AdsHelper.ADS_ENABLED
                || AdsHelper.AD_UNIT_INTER == null)
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

        MobileAds.initialize(context, initializationStatus -> {
            Log.d(LOG_TAG, "Loading inter...");
            mLoading = true;
            InterstitialAd.load(context, AdsHelper.AD_UNIT_INTER, AdsHelper.createAdRequest(),
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
        });

    }

    public static void showInter(Activity activity, boolean autoLoading) {

        if (AdsHelper.AD_UNIT_INTER == null || activity == null)
            return;

        if (mInter == null) {
            Log.d(LOG_TAG, "No inter to show!");
            return;
        }

        if (mShowing) {
            Log.d(LOG_TAG, "Inter is shown rigth now!");
            return;
        }

        mShowing = true;
        mInter.show(activity);
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
                Log.d(LOG_TAG, "Inter showing error: " + error);

                if (autoLoading)
                    loadInter(activity);
            }

            @Override
            public void onAdShowedFullScreenContent() {
                mInter = null;
                SharedPreferences prefs = activity.getSharedPreferences(
                        PREFERENCES, Context.MODE_PRIVATE);
                prefs.edit()
                        .putLong(KEY_LAST_INTER, System.currentTimeMillis())
                        .apply();
                Log.d(LOG_TAG, "Inter was shown!");

                if (autoLoading)
                    loadInter(activity);

            }
        });
    }

}
