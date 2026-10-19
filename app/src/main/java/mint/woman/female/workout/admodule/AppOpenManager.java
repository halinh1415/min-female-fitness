package mint.woman.female.workout.admodule;

import static androidx.lifecycle.Lifecycle.Event.ON_START;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mint.woman.female.workout.custom.progress.KProgressHUD;
import mint.woman.female.workout.utils.RemoteConfigKeys;
import mint.woman.female.workout.utils.MyPreference;


public class AppOpenManager implements Application.ActivityLifecycleCallbacks, LifecycleObserver {
    private static final String LOG_TAG = "AppOpenManager";
    private static volatile AppOpenManager INSTANCE;

    private AppOpenAd appResumeAd = null;
    private AppOpenAd appResumeMediumAd = null;
    private AppOpenAd appResumeHighAd = null;
    private FullScreenContentCallback fullScreenContentCallback;
    private boolean enableScreenContentCallback;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback, loadCallbackHigh, loadCallbackMedium, loadCallbackNormal;
    private Activity currentActivity;
    public static boolean isShowingAd = false;
    KProgressHUD hud = null;
    private boolean disableAdResumeByClickAction = false;
    private boolean isAppResumeEnabled = true;
    private boolean isRemoveAd = false;
    private boolean isPremium = false;
    private final List<Class> disabledAppOpenList;
    private long loadTime = 0;
    private boolean isLoadingAd = false;
    private int pendingLoadCallbacks = 0;

    private Application myApplication;
    private String adResumeHighId = "", adResumeMediumId = "", adResumeId = "";

    public static synchronized AppOpenManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AppOpenManager();
        }

        return INSTANCE;
    }

    public void setRemoveAd(boolean removeAd) {
        isRemoveAd = removeAd;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    public String getAdResumeHighId() {
        return adResumeHighId;
    }

    public void setAdResumeHighId(String adResumeHighId) {
        this.adResumeHighId = adResumeHighId;
    }

    public String getAdResumeMediumId() {
        return adResumeMediumId;
    }

    public void setAdResumeMediumId(String adResumeMediumId) {
        this.adResumeMediumId = adResumeMediumId;
    }

    public String getAdResumeId() {
        return adResumeId;
    }

    public void setAdResumeId(String adResumeId) {
        this.adResumeId = adResumeId;
    }

    public void init(Application application, String adResumeId) {
        this.myApplication = application;
        this.adResumeId = adResumeId;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    /**
     * Constructor
     */
    public AppOpenManager() {
        this.disabledAppOpenList = new ArrayList<>();
    }



    /**
     * Co premium/go quang cao trong bo nho chi duoc gan o Splash/Paywall; app bi kill roi mo lai tu
     * recents/thong bao (khong qua Splash) thi ve false -> premium van thay quang cao resume.
     * Doc them MyPreference.isShowAds() (false khi premium/da go quang cao) moi lan.
     */
    private boolean adsBlocked() {
        if (isPremium || isRemoveAd) return true;
        return myApplication != null && !new MyPreference(myApplication).isShowAds();
    }

    public void fetchAd() {
        if (adsBlocked()) return;
        // Have unused ad, no need to fetch another.
        if (isAdAvailable() || isLoadingAd || myApplication == null) {
            return;
        }

        if (!FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.APP_OPEN_RESUME)) {
            return;
        }

        isLoadingAd = true;
        pendingLoadCallbacks = 3;

        loadCallback =
                new AppOpenAd.AppOpenAdLoadCallback() {

                    @Override
                    public void onAdLoaded(AppOpenAd ad) {
                        AppOpenManager.this.appResumeAd = ad;
                        onLoadFinished();
                    }


                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(LOG_TAG, "error in loading");
                        onLoadFinished();
                    }

                };
        loadCallbackHigh = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                super.onAdLoaded(appOpenAd);
                AppOpenManager.this.appResumeHighAd = appOpenAd;
                onLoadFinished();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                // Handle the error.
                Log.d(LOG_TAG, "error in loading appResumeHighAd");
                onLoadFinished();
            }
        };
        loadCallbackMedium = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                super.onAdLoaded(appOpenAd);
                AppOpenManager.this.appResumeMediumAd = appOpenAd;
                onLoadFinished();


            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                // Handle the error.
                Log.d(LOG_TAG, "error in loading appResumeMediumAd");
                onLoadFinished();
            }
        };
        AdRequest request = getAdRequest();
        loadTime = (new Date()).getTime();
        AppOpenAd.load(myApplication, adResumeId, request, loadCallback);
        AppOpenAd.load(myApplication, adResumeHighId, request, loadCallbackHigh);
        AppOpenAd.load(myApplication, adResumeMediumId, request, loadCallbackMedium);
    }

    private void onLoadFinished() {
        pendingLoadCallbacks--;
        if (pendingLoadCallbacks <= 0) {
            pendingLoadCallbacks = 0;
            isLoadingAd = false;
        }
    }

    public void setAdOpenId(String id) {
        this.adResumeId = id;
    }

    public void disableAdResumeByClickAction() {
        this.disableAdResumeByClickAction = true;
    }

    public void setDisableAdResumeByClickAction(boolean disableAdResumeByClickAction) {
        this.disableAdResumeByClickAction = disableAdResumeByClickAction;
    }

    public void disableAppResumeWithActivity(Class activityClass) {
        Log.d("AppOpenManager", "disableAppResumeWithActivity: " + activityClass.getName());
        this.disabledAppOpenList.add(activityClass);
    }

    public void enableAppResumeWithActivity(Class activityClass) {
        Log.d("AppOpenManager", "enableAppResumeWithActivity: " + activityClass.getName());
        this.disabledAppOpenList.remove(activityClass);
    }

    public boolean isAppResumeEnabled() {
        return isAppResumeEnabled;
    }

    public void enableAppResume() {
        isAppResumeEnabled = true;
    }

    public void disableAppResume() {
        isAppResumeEnabled = false;
    }


    void showDialog() {
        try {
            if (!(currentActivity).isFinishing()) {
                hud = KProgressHUD.create(currentActivity)
                        .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                        .setLabel("Welcome Back!")
                        .setDetailsLabel("Loading ad...")
                        .setCancellable(false)
                        .setAnimationSpeed(2)
                        .setDimAmount(0.5f)
                        .show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void hideDialog() {
        try {
            if (hud != null && hud.isShowing()) {
                hud.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showAdIfAvailable() {
        if (currentActivity == null) {
            Log.d(LOG_TAG, "Can not show ad without an Activity.");
            return;
        }
        if (!isShowingAd && isAdAvailable()) {
            Log.d(LOG_TAG, "Will show ad. on activity: "+currentActivity.getClass().getName());

            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {

                            Log.d(LOG_TAG, "onAdDismissedFullScreenContent");
                            // Set the reference to null so isAdAvailable() returns false.
                            AppOpenManager.this.appResumeAd = null;
                            AppOpenManager.this.appResumeHighAd = null;
                            AppOpenManager.this.appResumeMediumAd = null;
                            isShowingAd = false;
                            hideDialog();
                            fetchAd();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            AppOpenManager.this.appResumeAd = null;
                            AppOpenManager.this.appResumeHighAd = null;
                            AppOpenManager.this.appResumeMediumAd = null;
                            isShowingAd = false;
                            hideDialog();
                            fetchAd();
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            isShowingAd = true;
                            hideDialog();
                        }
                    };
            AppOpenAd adToShow = appResumeHighAd != null
                    ? appResumeHighAd
                    : (appResumeMediumAd != null ? appResumeMediumAd : appResumeAd);
            Activity activityToShowFrom = currentActivity;
            if (adToShow != null && activityToShowFrom != null) {
                // Reserve the show operation before the delay so lifecycle callbacks cannot
                // schedule the same ad twice while the loading HUD is visible.
                isShowingAd = true;
                adToShow.setFullScreenContentCallback(fullScreenContentCallback);
                showDialog();
                new Handler(activityToShowFrom.getMainLooper()).postDelayed(() -> {
                    hideDialog();
                    if (!activityToShowFrom.isFinishing() && !activityToShowFrom.isDestroyed()) {
                        try {
                            adToShow.show(activityToShowFrom);
                        } catch (RuntimeException error) {
                            isShowingAd = false;
                            Log.w(LOG_TAG, "Unable to show app-open ad", error);
                            fetchAd();
                        }
                    } else {
                        isShowingAd = false;
                    }
                }, 800);
            } else {
                Log.d(LOG_TAG, "Can not show ad.");
                fetchAd();
            }




        } else {
            Log.d(LOG_TAG, "Can not show ad.");
            fetchAd();
        }
    }

    public void showAdResumeIfAvailable() {
        if (!isShowingAd && isAdAvailable()) {
            Log.d(LOG_TAG, "Will show ad.");

            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {

                            Log.d(LOG_TAG, "onAdDismissedFullScreenContent");
                            // Set the reference to null so isAdAvailable() returns false.
                            AppOpenManager.this.appResumeAd = null;
                            isShowingAd = false;
                            hideDialog();
                            fetchAd();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            AppOpenManager.this.appResumeAd = null;
                            isShowingAd = false;
                            hideDialog();
                            fetchAd();
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            isShowingAd = true;
                        }
                    };

            appResumeAd.setFullScreenContentCallback(fullScreenContentCallback);
            appResumeAd.show(currentActivity);

        } else {
            Log.d(LOG_TAG, "Can not show ad.");
            fetchAd();
        }
    }


    /**
     * Creates and returns ad request.
     */
    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    public boolean isAdAvailable() {
        return (appResumeAd != null || appResumeHighAd != null || appResumeMediumAd != null)
                && wasLoadTimeLessThanNHoursAgo(4);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (!isShowingAd) {
            currentActivity = activity;
        }
    }


    @Override
    public void onActivityResumed(Activity activity) {
        if (isShowingAd) {
            return;
        }
        this.currentActivity = activity;
        Log.d("AppOpenManager", "onActivityResumed: " + this.currentActivity);
        if (!disabledAppOpenList.contains(activity.getClass())) {
            // Queue the SDK request after the Activity has had a chance to draw its first frame.
            activity.getWindow().getDecorView().post(this::fetchAd);
        }

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (currentActivity == activity) {
            currentActivity = null;
        }
    }

    /**
     * LifecycleObserver methods
     */
    @OnLifecycleEvent(ON_START)
    public void onStart() {
        if (currentActivity == null) {
            Log.d(LOG_TAG, "No resumed activity available for app-open ad");
        } else if (adsBlocked()) {
            Log.d(LOG_TAG, "Remove open ad in app!!");
        } else if (!isAppResumeEnabled) {
            Log.d(LOG_TAG, "onResume: app resume is disabled");
        } else if (disableAdResumeByClickAction) {
            disableAdResumeByClickAction = false;
            Log.d(LOG_TAG, "onResume: disableAdResumeByClickAction");
        } else if (disabledAppOpenList.contains(currentActivity.getClass())) {

            Log.d(LOG_TAG, "onResume: disableAdResumeByActivity");
        } else {
            showAdIfAvailable();

        }

        Log.d(LOG_TAG, "onStart");
    }
}
