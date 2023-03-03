package com.utilityapps.adshelperlib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue;
import com.utilityapps.adshelperlib.networks.INetwork;
import com.utilityapps.adshelperlib.networks.admob.AdMob;
import com.utilityapps.adshelperlib.networks.admob.AppOpenAdManager;
import com.utilityapps.adshelperlib.networks.admob.InterstitialAdManager;
import com.utilityapps.adshelperlib.networks.admob.RewardedInterstitialAdManager;
import com.utilityapps.adshelperlib.networks.yandex.Yandex;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdsHelper {

    public interface IOnInit {
        void onInit();
    }

    public static Context appContext;

    public static final String LOG_TAG = "MYTAG (AdHelper)";

    private static final String KEY_ADS_NETWORK = "ads_network";

    public static final String NETWORK_ADMOB = "ADMOB";
    public static final String NETWORK_YANDEX = "YANDEX";

    private static INetwork network;

    public static void setAdUnitInter(String network, String adUnit) {
        if (network.equals(NETWORK_ADMOB))
            AdMob.setAdUnitInter(adUnit);
        else if (network.equals(NETWORK_YANDEX))
            Yandex.setAdUnitInter(adUnit);
    }
    public static void setAdUnitBanner(String network, String adUnit) {
        if (network.equals(NETWORK_ADMOB))
            AdMob.setAdUnitBanner(adUnit);
        else if (network.equals(NETWORK_YANDEX))
            Yandex.setAdUnitBanner(adUnit);
    }
    public static void setAdUnitNative(String network, String adUnit) {
        if (network.equals(NETWORK_ADMOB))
            AdMob.setAdUnitNative(adUnit);
        else if (network.equals(NETWORK_YANDEX))
            Yandex.setAdUnitNative(adUnit);
    }
    public static void setAdUnitAppOpen(String network, String adUnit) {
        if (network.equals(NETWORK_ADMOB))
            AdMob.setAdUnitAppOpen(adUnit);
    }
    public static void setAdUnitRewardedInter(String network, String adUnit) {
        if (network.equals(NETWORK_ADMOB))
            AdMob.setAdUnitRewardedInter(adUnit);
        else if (network.equals(NETWORK_YANDEX))
            Yandex.setAdUnitRewardedInter(adUnit);
    }

    public static void initialize(Application application, String defaultNetwork) {
        initialize(application, defaultNetwork, null);
    }

    public static void initialize(Application application, String defaultNetwork, @Nullable IOnInit onComplete) {

        appContext = application.getApplicationContext();

        FirebaseApp.initializeApp(application);
        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();

        Map<String, Object> defaults = new HashMap<>();
        defaults.put(KEY_ADS_NETWORK, defaultNetwork);

        config.setDefaultsAsync(mergeDefaults(config, defaults)).addOnCompleteListener(t ->
                config.fetchAndActivate().addOnCompleteListener(task ->
                        initNetwork(application, config.getString(KEY_ADS_NETWORK), onComplete)));
    }

    public static boolean isMainProcess(Application application) {

        if (Build.VERSION.SDK_INT >= 28) {
            return TextUtils.equals(Application.getProcessName(), application.getPackageName());
        }

        // Try using ActivityThread to determine the current process name.
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThread = Class.forName(
                    "android.app.ActivityThread",
                    false,
                    Application.class.getClassLoader());
            final Object packageName;
            @SuppressLint("DiscouragedPrivateApi")
            Method currentProcessName = activityThread.getDeclaredMethod("currentProcessName");
            currentProcessName.setAccessible(true);
            packageName = currentProcessName.invoke(null);

            if (packageName instanceof String) {
                return TextUtils.equals((String) packageName, application.getPackageName());
            }

        } catch (Throwable exception) { /* ... */ }

        // Fallback to the most expensive way
        int pid = android.os.Process.myPid();
        ActivityManager am = (ActivityManager) application.getSystemService(Context.ACTIVITY_SERVICE);

        if (am != null) {
            List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
            if (processes != null && !processes.isEmpty()) {
                for (ActivityManager.RunningAppProcessInfo process : processes) {
                    if (process.pid == pid) {
                        return TextUtils.equals(process.processName, application.getPackageName());
                    }
                }
            }
        }

        return false;
    }

    private static Map<String, Object> mergeDefaults(FirebaseRemoteConfig config, Map<String, Object> newDefaults) {
        Map<String, FirebaseRemoteConfigValue> oldValues = config.getAll();
        Map<String, Object> oldDefaults = new HashMap<>();
        for (Map.Entry<String, FirebaseRemoteConfigValue> entry : oldValues.entrySet()) {
            if (entry.getValue().getSource() == FirebaseRemoteConfig.VALUE_SOURCE_DEFAULT)
                oldDefaults.put(entry.getKey(), entry.getValue().asString());
        }
        oldDefaults.putAll(newDefaults);
        return oldDefaults;
    }

    private static void initNetwork(Application application, String networkType, @Nullable IOnInit onComplete) {
        if (networkType.equals(NETWORK_YANDEX))
            network = new Yandex();
        else
            network = new AdMob();
        network.init(application, onComplete);
    }

    public static void setInterEnabled(boolean enabled) {
        if (network != null)
            network.setInterEnabled(enabled);
    }

    public static void setInterDelay(long millis) {
        if (network != null)
            network.setInterDelay(millis);
    }

    public static void loadInter(@Nullable Context context) {

        if (context == null)
            return;

        if (network != null && network.isInitialized())
            network.loadInter(context);
    }

    public static void showInter(@Nullable Activity activity) {

        if (activity == null)
            return;

        if (network != null && network.isInitialized())
            network.showInter(activity);
    }

    public static boolean hasWatchedRewarded() {
        if (network != null && network.isInitialized()) {
            return network.hasWatchedRewarded();
        }
        return true;
    }

    public static void loadRewardedInter(@Nullable Context context) {

        if (context == null)
            return;

        if (network != null && network.isInitialized())
            network.loadRewardedInter(context);
    }

    public static void showRewardedInter(@Nullable Activity activity, boolean autoLoading,
                                    @Nullable INetwork.IOnReward onReward) {

        if (activity == null)
            return;

        if (network != null && network.isInitialized())
            network.showRewardedInter(activity, autoLoading, onReward);
    }

    public static boolean isRewardedAvailable() {
        if (network != null && network.isInitialized())
            return network.isRewardedAvailable();
        return false;
    }

    public static void loadAndShowBanner(@Nullable Activity activity, @NonNull ViewGroup container) {

        if (activity == null)
            return;

        if (network != null && network.isInitialized())
            network.loadAndShowBanner(activity, container);
    }


    public static void loadAndShowNative(@Nullable Context context,
                                         @NonNull LayoutInflater inflater,
                                         int layoutResId,
                                         @NonNull ViewGroup container) {

        if (context == null)
            return;

        if (network != null && network.isInitialized())
            network.loadAndShowNative(context, inflater, layoutResId, container);
    }
}

