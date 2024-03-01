package mint.woman.female.workout.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.kaopiz.kprogresshud.KProgressHUD;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.language.LanguageActivity;
import mint.woman.female.workout.activity.ui.main.YogaActivity;
import mint.woman.female.workout.admodule.MaticAd;
import mint.woman.female.workout.admodule.MaticAdCallBack;
import mint.woman.female.workout.admodule.MaticAdKeys;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.NetworkUtil;
import mint.woman.female.workout.utils.RemoteConfigKeys;

import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int TIME_OUT = 6;
    MyPreference myPreference;
    ImageView mSplash;
    private Handler mHandler = new Handler();
    LottieAnimationView lottieAnimationView;
    ConsentInformation consentInformation;
    InterstitialAd mInterSplashHigh, mInterSplashMedium, mInterSplash;
    FullScreenContentCallback fullScreenContentCallback;
    boolean isShowingSplash = false;
    int timeChecker = 5, interChecker = 0;
    private final Handler handlerChecker = new Handler();
    private boolean isFirstRunApp = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        myPreference = new MyPreference(this);
        fullScreenContentCallback = new FullScreenContentCallback() {


            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                Log.d(TAG, " showAds Inter Splash show!");

                isShowingSplash = true;
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                Log.d(TAG, " showAds Inter Splash fail to show");

                // isShowingSplash = false;
//                mInterSplashHigh = null;
//                mInterSplashMedium = null;
//                mInterSplash = null;
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                Log.d(TAG, " showAds Inter Splash dismiss");
                //    isShowingSplash = false;
                mInterSplashHigh = null;
                mInterSplashMedium = null;
                mInterSplash = null;
                startMain();
            }
        };
        myPreference.setOK(true);
        myPreference.setShowRate(true);
        mSplash = findViewById(R.id.img_splash);
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        scaleUp.setFillAfter(true);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.centerCrop().transform(new RoundedCorners(1));
        Glide.with(getApplicationContext()).load(Uri.parse("file:///android_asset/images/img_splash.webp")).into(mSplash);
        lottieAnimationView = findViewById(R.id.lottieView);
        lottieAnimationView.setAnimation("load.json");
        lottieAnimationView.playAnimation();
        updateRemoteConfig();

//        Animation anim = new ScaleAnimation(
//                1f, 1.5f, // Start and end values for the X axis scaling
//                1f, 1.5f, // Start and end values for the Y axis scaling
//                Animation.RELATIVE_TO_PARENT, 0f, // Pivot point of X scaling
//                Animation.RELATIVE_TO_SELF, 1f); // Pivot point of Y scaling
//        anim.setFillAfter(true); // Needed to keep the result of the animation
//        anim.setDuration(2000);
//        mSplash.startAnimation(anim);

       startLoadAd();
    }

    void updateRemoteConfig() {

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        boolean updated = task.getResult();
//                            Log.d(TAG, "Config params updated: " + updated);
//                            Toast.makeText(SplashActivity.this, "Fetch and activate succeeded",
//                                    Toast.LENGTH_SHORT).show();

                    } else {
//                            Toast.makeText(SplashActivity.this, "Fetch failed",
//                                    Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstRunApp) {
            isFirstRunApp = false;
            return;
        }
        if (!isShowingSplash) {
            if (!isFinishing()) {
                checkShowInterSplash();
            }

        }
    }

    private void startLoadAd() {
        if (myPreference.isShowAds() && myPreference.getOK())
            MobileAds.initialize(this, initializationStatus -> {

            });

        if (myPreference.isShowAds() && myPreference.isFirstOpen()) {
            requestNativeLanguage();
            requestNativeOnBoard();
            requestNativeSetup();
            loadInterStart();
        }
        if (myPreference.isShowAds() && !myPreference.isFirstOpen() && FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.INTER_SPLASH)) {
            loadInterSplash3SameTime();
            checkShowInterSplash();

        } else {
            mHandler.postDelayed(() -> {
                startMain();

            }, 3000);
        }
    }

    private void loadInterSplash3SameTime() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(SplashActivity.this, MaticAdKeys.INTER_SPLASH_HIGH, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                mInterSplashHigh = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                interChecker++;
            }
        });
        InterstitialAd.load(SplashActivity.this, MaticAdKeys.INTER_SPLASH_MEDIUM, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                mInterSplashMedium = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                interChecker++;
            }
        });
        InterstitialAd.load(SplashActivity.this, MaticAdKeys.INTER_SPLASH, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                mInterSplash = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                interChecker++;
            }
        });
    }

    private boolean isSplashAvailable() {
        if (isShowingSplash) return false;
        return mInterSplashHigh != null || mInterSplashMedium != null || mInterSplash != null;
    }

    private void checkShowInterSplash() {

        handlerChecker.postDelayed(new Runnable() {
            @Override
            public void run() {
                timeChecker++;
                if (isSplashAvailable()) {
                    showInterSplash();

                } else if (timeChecker < TIME_OUT && interChecker < 3) {
                    handlerChecker.postDelayed(this, 1000);
                } else {
                    startMain();
                }

            }
        }, 5000);

    }

    KProgressHUD hud;

    void showDialog() {
        try {
            if (!(SplashActivity.this).isFinishing()) {
                hud = KProgressHUD.create(SplashActivity.this)
                        .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                        .setLabel("Loading ad...")
                        .setCancellable(false)
                        .setAnimationSpeed(1)
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
                hud = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void showInterSplash() {
        if (isSplashAvailable()) {
            if (mInterSplashHigh != null) {
                showDialog();
                mInterSplashHigh.setFullScreenContentCallback(fullScreenContentCallback);
                new Handler().postDelayed(() -> {
                    hideDialog();
                    mInterSplashHigh.show(SplashActivity.this);
                }, 800);


            } else if (mInterSplashMedium != null) {
                mInterSplashMedium.setFullScreenContentCallback(fullScreenContentCallback);
                showDialog();
                new Handler().postDelayed(() -> {
                    hideDialog();
                    mInterSplashMedium.show(SplashActivity.this);
                }, 800);
            } else if (mInterSplash != null) {
                mInterSplash.setFullScreenContentCallback(fullScreenContentCallback);
                showDialog();
                new Handler().postDelayed(() -> {
                    hideDialog();
                    mInterSplash.show(SplashActivity.this);
                }, 800);
            } else {
                startMain();
            }

        } else {
            startMain();
        }
    }

    private void requestTCM() {
        ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(this)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .build();

        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()

                .setConsentDebugSettings(debugSettings)
                .setTagForUnderAgeOfConsent(false)
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        //  consentInformation.reset();
        consentInformation.requestConsentInfoUpdate(
                this,
                params,
                (ConsentInformation.OnConsentInfoUpdateSuccessListener) () -> {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                            this,
                            (ConsentForm.OnConsentFormDismissedListener) loadAndShowError -> {
                                if (loadAndShowError != null) {
                                    // Consent gathering failed.
                                    Log.w(TAG, String.format("%s: %s",
                                            loadAndShowError.getErrorCode(),
                                            loadAndShowError.getMessage()));
                                }

                                startLoadAd();
                            }
                    );

                },
                (ConsentInformation.OnConsentInfoUpdateFailureListener) requestConsentError -> {
                    // Consent gathering failed.
                    Log.w(TAG, "consent requestConsentError " + String.format("%s: %s",
                            requestConsentError.getErrorCode(),
                            requestConsentError.getMessage()));
                    startLoadAd();
                });

    }

    private void loadInterStart() {
        if (myPreference.isShowAds() && myPreference.getOK()) {
            MaticAd.getInstance().loadInterstitialAd3SameTime(SplashActivity.this, MaticAdKeys.INTER_FIRST_OPEN_HIGH, MaticAdKeys.INTER_FIRST_OPEN_MEDIUM, MaticAdKeys.INTER_FIRST_OPEN, new MaticAdCallBack());
        }
    }


    private void startMain() {
        if (myPreference.isFirstOpen()) {
            startActivity(new Intent(SplashActivity.this, LanguageActivity.class));
            finish();
        } else {
            startActivity(new Intent(SplashActivity.this, YogaActivity.class));
            finish();
        }
    }

    void requestNativeLanguage() {
        MaticAd.getInstance().loadNativeAd3Sametime(SplashActivity.this, MaticAdKeys.NATIVE_LANGUAGE_HIGH, MaticAdKeys.NATIVE_LANGUAGE_MEDIUM, MaticAdKeys.NATIVE_LANGUAGE, new MaticAdCallBack() {
            @Override
            public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                FitnessApplication.getApplication().getStorageCommon().nativeAdLanguage.postValue(nativeAd);
            }

            @Override
            public void onAdFailedToLoad(@Nullable AdError adError) {
                super.onAdFailedToLoad(adError);
                FitnessApplication.getApplication().getStorageCommon().nativeAdLanguage.postValue(null);
            }
        });
    }


    void requestNativeOnBoard() {

        MaticAd.getInstance().loadNativeAd3Sametime(SplashActivity.this, MaticAdKeys.NATIVE_ONBOARD_HIGH, MaticAdKeys.NATIVE_ONBOARD_MEDIUM, MaticAdKeys.NATIVE_ONBOARD, new MaticAdCallBack() {
            @Override
            public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                FitnessApplication.getApplication().getStorageCommon().nativeAdOnBoard.postValue(nativeAd);
            }

            @Override
            public void onAdFailedToLoad(@Nullable AdError adError) {
                super.onAdFailedToLoad(adError);
                FitnessApplication.getApplication().getStorageCommon().nativeAdOnBoard.postValue(null);
            }
        });
    }

    void requestNativeSetup() {
        FitnessApplication.getApplication().getStorageCommon().nativeSetup.postValue(null);

//        MaticAd.getInstance().loadNativeAd3Sametime(SplashActivity.this, MaticAdKeys.NATIVE_SETUP_HIGH, MaticAdKeys.NATIVE_SETUP_MEDIUM, MaticAdKeys.NATIVE_SETUP, new MaticAdCallBack() {
//            @Override
//            public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
//                super.onNativeAdLoaded(nativeAd);
//                Log.d("HAHA", "nativeOnBoard1 onNativeAdLoaded");
//                FitnessApplication.getApplication().getStorageCommon().nativeSetup.postValue(nativeAd);
//            }
//
//            @Override
//            public void onAdFailedToLoad(@Nullable AdError adError) {
//                super.onAdFailedToLoad(adError);
//                FitnessApplication.getApplication().getStorageCommon().nativeSetup.postValue(null);
//            }
//        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String lang = preferences.getString("ST_LANGUAGE", "");
        if (!lang.isEmpty() && lang.length() > 2) {
            lang = lang.substring(0, 2);
        } else {
            lang = Locale.getDefault().getLanguage();
        }
        super.attachBaseContext(LanguageHelper.setLanguage(newBase, lang));

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Hide the nav bar and status bar4
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
//    private void showSystemUI() {
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//    }

}
