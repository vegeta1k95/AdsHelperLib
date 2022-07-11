package com.utilityapps.adshelperlib;

import static com.utilityapps.adshelperlib.AdsHelper.AD_UNIT_REWARDED;
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
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class RewardedAdManager {

    private static RewardedAd mRewarded;

    public interface OnRewardEarned {
        void onRewarded();
    }

    public static boolean isRewardedAvailable() {
        return mRewarded != null;
    }

    public static void loadRewarded(@Nullable Context context) {

        if (context == null || !AdsHelper.ADS_ENABLED || AD_UNIT_REWARDED == null)
            return;

        MobileAds.initialize(context, initializationStatus -> {
            Log.d(LOG_TAG, "Loading rewarded...");

            RewardedAd.load(context, AdsHelper.AD_UNIT_REWARDED,
                    AdsHelper.createAdRequest(), new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.d(LOG_TAG, "Rewarded loading error: " + loadAdError);
                            mRewarded = null;
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            mRewarded = rewardedAd;
                            Log.d(LOG_TAG, "Rewarded was loaded.");
                        }
                    });
        });
    }

    public static void showRewarded(@Nullable Activity activity, boolean autoLoading,
                                    @Nullable OnRewardEarned onReward) {

        if (AD_UNIT_REWARDED == null || activity == null)
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
                onReward.onRewarded();
        });
    }
}
