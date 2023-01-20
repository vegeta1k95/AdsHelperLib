package com.utilityapps.adshelperlib.networks;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface INetwork {

    interface IOnReward {
        void onReward();
    }

    void init(Application application);

    // Inter
    void loadInter(@Nullable Context context);
    void showInter(@Nullable Activity activity, boolean autoLoading);
    void setInterEnabled(boolean enabled);
    void setInterDelay(long millis);

    // Rewarded Inter
    void loadRewardedInter(@Nullable Context context);
    void showRewardedInter(@Nullable Activity activity, boolean autoLoading,
                      @Nullable IOnReward onReward);
    boolean isRewardedAvailable();

    // Banner
    void loadAndShowBanner(@Nullable Activity activity, @NonNull ViewGroup container);

    // Native
    void loadAndShowNative(@Nullable Context context, @NonNull LayoutInflater inflater,
                           int layoutResId, @NonNull ViewGroup container);


}
