package com.utilityapps.adshelperlib.networks.yandex;

import static com.utilityapps.adshelperlib.AdsHelper.LOG_TAG;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.utilityapps.adshelperlib.AdsHelper;
import com.utilityapps.adshelperlib.networks.INetwork;
import com.utilityapps.adshelperlib.networks.admob.AdMob;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.common.MobileAds;
import com.yandex.mobile.ads.rewarded.Reward;
import com.yandex.mobile.ads.rewarded.RewardedAd;
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener;

import org.jetbrains.annotations.Nullable;

public class RewardedInterstitialAdManager {

    private static RewardedAd mRewarded;
    private static INetwork.IOnReward mOnReward;

    public static boolean isRewardedAvailable() {
        return mRewarded != null;
    }

    static void loadRewarded(@NonNull Context context) {

        if (Yandex.AD_UNIT_REWARDED == null)
            return;

        Log.d(LOG_TAG, "Loading rewarded...");

        mRewarded = new RewardedAd(context);
        mRewarded.setAdUnitId(Yandex.AD_UNIT_REWARDED);
        mRewarded.setRewardedAdEventListener(new RewardedAdEventListener() {
            @Override public void onAdLoaded() {}
            @Override
            public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
                mRewarded = null;
            }

            @Override public void onAdShown() {
                mRewarded = null;
            }

            @Override
            public void onAdDismissed() {
                mRewarded = null;
            }

            @Override
            public void onRewarded(@NonNull Reward reward) {
                if (mOnReward != null)
                    mOnReward.onReward();
            }

            @Override public void onAdClicked() {}
            @Override public void onLeftApplication() {}
            @Override public void onReturnedToApplication() {}
            @Override public void onImpression(@androidx.annotation.Nullable ImpressionData impressionData) {}
        });

        mRewarded.loadAd(Yandex.createAdRequest());

    }

    static void showRewarded(INetwork.IOnReward onReward) {

        if (Yandex.AD_UNIT_REWARDED == null)
            return;

        if (mRewarded == null) {
            Log.d(LOG_TAG, "No rewarded to show!");
            return;
        }

        mOnReward = onReward;
        mRewarded.show();
    }

}
