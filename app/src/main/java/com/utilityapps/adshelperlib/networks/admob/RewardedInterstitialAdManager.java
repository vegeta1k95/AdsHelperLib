package com.utilityapps.adshelperlib.networks.admob;

import static com.utilityapps.adshelperlib.AdsHelper.LOG_TAG;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.utilityapps.adshelperlib.AdsHelper;
import com.utilityapps.adshelperlib.networks.INetwork;

public class RewardedInterstitialAdManager {

    private static RewardedInterstitialAd mRewarded;

    public static boolean isRewardedAvailable() {
        return mRewarded != null;
    }

    public static void loadRewarded(@Nullable Context context) {

        if (context == null || AdMob.AD_UNIT_REWARDED == null)
            return;

        MobileAds.initialize(context, initializationStatus -> {
            Log.d(LOG_TAG, "Loading rewarded...");

            RewardedInterstitialAd.load(context, AdMob.AD_UNIT_REWARDED,
                    AdMob.createAdRequest(), new RewardedInterstitialAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.d(LOG_TAG, "Rewarded loading error: " + loadAdError);
                            mRewarded = null;
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedInterstitialAd rewardedAd) {
                            mRewarded = rewardedAd;
                            Log.d(LOG_TAG, "Rewarded was loaded.");
                        }
                    });
        });
    }

    public static void showRewarded(@Nullable Activity activity, boolean autoLoading,
                                    @Nullable INetwork.IOnReward onReward) {

        if (AdMob.AD_UNIT_REWARDED == null || activity == null)
            return;

        if (mRewarded == null) {
            Log.d(LOG_TAG, "No rewarded to show!");
            return;
        }

        mRewarded.setFullScreenContentCallback(new FullScreenContentCallback() {

            @Override
            public void onAdDismissedFullScreenContent() {
                mRewarded = null;
                Log.d(LOG_TAG, "Rewarded dismissed!");

                if (autoLoading)
                    loadRewarded(activity);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError error) {
                mRewarded = null;
                Log.d(LOG_TAG, "Rewarded showing error: " + error);

                if (autoLoading)
                    loadRewarded(activity);
            }

            @Override
            public void onAdShowedFullScreenContent() {
                mRewarded = null;
                Log.d(LOG_TAG, "Rewarded was shown!");

                if (autoLoading)
                    loadRewarded(activity);

            }
        });
        mRewarded.show(activity, rewardItem -> {
            Log.d(LOG_TAG, "User obtained reward!");
            if (onReward != null)
                onReward.onReward();
        });
    }
}
