package com.utilityapps.adshelperlib.networks;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.utilityapps.adshelperlib.AdsHelper;

public interface INetwork {

    interface IOnReward {
        void onReward();
    }

    void init(Application application, @Nullable AdsHelper.IOnInit onComplete);

    boolean isInitialized();

    // Inter
    void loadInter(@NonNull Context context);
    void showInter(@NonNull Activity activity, boolean autoLoading);
    void setInterEnabled(boolean enabled);
    void setInterDelay(long millis);

    // Rewarded Inter
    boolean isRewardedAvailable();
    void loadRewardedInter(@NonNull Context context);
    void showRewardedInter(@NonNull Activity activity, boolean autoLoading,
                      @Nullable IOnReward onReward);

    // Banner
    void loadAndShowBanner(@NonNull Activity activity, @NonNull ViewGroup container);

    // Native
    void loadAndShowNative(@NonNull Context context, @NonNull LayoutInflater inflater,
                           int layoutResId, @NonNull ViewGroup container);


}
