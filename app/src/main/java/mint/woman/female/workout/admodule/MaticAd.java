package mint.woman.female.workout.admodule;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.concurrent.atomic.AtomicInteger;

import mint.woman.female.workout.custom.progress.KProgressHUD;

public class MaticAd {
    private static final String TAG = "MaticAd";
    private static volatile MaticAd instance;

    public static synchronized MaticAd getInstance() {
        if (instance == null) {
            instance = new MaticAd();
        }

        return instance;
    }

    KProgressHUD hud;
    InterstitialAd mInterHigh, mInterMedium, mInterNormal;
    String idInterHigh = "", idInterMedium = "", idInterNormal = "";
    boolean isShowingInter = false;
    public void populateNativeAdView(Activity activity, NativeAd nativeAd, int layoutNative, FrameLayout adPlaceHolder, ShimmerFrameLayout containerShimmerLoading) {
        containerShimmerLoading.setVisibility(View.GONE);
        adPlaceHolder.removeAllViews();
        NativeAdView adView = (NativeAdView) activity.getLayoutInflater()
                .inflate(layoutNative, null);
        Admob.getInstance().populateNativeAdView(nativeAd, adView);
        adPlaceHolder.addView(adView);

    }

    public void loadNativeAd(Activity activity, String idNativeAd, MaticAdCallBack maticAdCallBack) {

        AdLoader adLoader = new AdLoader.Builder(activity, idNativeAd)
                .forNativeAd(nativeAd -> {

                    maticAdCallBack.onNativeAdLoaded(nativeAd);

                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        maticAdCallBack.onAdFailedToLoad(adError);
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();


        adLoader.loadAd(new AdRequest.Builder().build());

    }

    //int currentLevel = -1;
    public void loadNativeAd3Sametime(Activity activity, String idHigh, String idMedium, String idNativeAd, MaticAdCallBack maticAdCallBack) {
        AtomicInteger currentLevel = new AtomicInteger(-1);
        int error = 0;

        AdLoader adLoader = new AdLoader.Builder(activity, idNativeAd)
                .forNativeAd(nativeAd -> {
                    Log.d(TAG, "loadAds Native normal loaded");
                    if (currentLevel.get() < 0) {
                        Log.d(TAG, "loadAds Native normal set");
                        currentLevel.set(0);
                        maticAdCallBack.onNativeAdLoaded(nativeAd);
                    }

                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        maticAdCallBack.onAdFailedToLoad(adError);
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();

        AdLoader adLoaderHigh = new AdLoader.Builder(activity, idHigh)
                .forNativeAd(nativeAd -> {
                    Log.d(TAG, "loadAds Native high loaded");
                    if (currentLevel.get() < 2) {
                        Log.d(TAG, "loadAds Native high set");
                        currentLevel.set(2);
                        maticAdCallBack.onNativeAdLoaded(nativeAd);
                    }

                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        maticAdCallBack.onAdFailedToLoad(adError);
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();
        AdLoader adLoaderMedium = new AdLoader.Builder(activity, idMedium)
                .forNativeAd(nativeAd -> {
                    Log.d(TAG, "loadAds Native medium loaded");
                    if (currentLevel.get() < 1) {
                        Log.d(TAG, "loadAds Native medium set");
                        currentLevel.set(1);
                        maticAdCallBack.onNativeAdLoaded(nativeAd);
                    }

                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        maticAdCallBack.onAdFailedToLoad(adError);
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();

        adLoaderHigh.loadAd(new AdRequest.Builder().build());
        adLoaderMedium.loadAd(new AdRequest.Builder().build());
        adLoader.loadAd(new AdRequest.Builder().build());

    }
    public void loadNativeAd3Alternative(Activity activity, String idHigh, String idMedium, String idNativeAd, MaticAdCallBack maticAdCallBack) {
        int error = 0;



        AdLoader adLoaderHigh = new AdLoader.Builder(activity, idHigh)
                .forNativeAd(nativeAd -> {
                    Log.d(TAG, "loadAds Native high loaded");
                    maticAdCallBack.onNativeAdLoaded(nativeAd);

                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        AdLoader adLoaderMedium = new AdLoader.Builder(activity, idMedium)
                                .forNativeAd(nativeAd -> {
                                    Log.d(TAG, "loadAds Native medium loaded");
                                    maticAdCallBack.onNativeAdLoaded(nativeAd);
                                })
                                .withAdListener(new AdListener() {
                                    @Override
                                    public void onAdFailedToLoad(LoadAdError adError) {
                                        AdLoader adLoader = new AdLoader.Builder(activity, idNativeAd)
                                                .forNativeAd(nativeAd -> {
                                                    Log.d(TAG, "loadAds Native normal loaded");
                                                    maticAdCallBack.onNativeAdLoaded(nativeAd);

                                                })
                                                .withAdListener(new AdListener() {
                                                    @Override
                                                    public void onAdFailedToLoad(LoadAdError adError) {
                                                        maticAdCallBack.onAdFailedToLoad(adError);
                                                    }
                                                })
                                                .withNativeAdOptions(new NativeAdOptions.Builder()
                                                        // Methods in the NativeAdOptions.Builder class can be
                                                        // used here to specify individual options settings.
                                                        .build())
                                                .build();
                                        adLoader.loadAd(new AdRequest.Builder().build());
                                    }
                                })
                                .withNativeAdOptions(new NativeAdOptions.Builder()
                                        // Methods in the NativeAdOptions.Builder class can be
                                        // used here to specify individual options settings.
                                        .build())
                                .build();
                        adLoaderMedium.loadAd(new AdRequest.Builder().build());
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();
        adLoaderHigh.loadAd(new AdRequest.Builder().build());


    }

    public void loadInterstitialAd(Activity activity, String idInterstitialAd, MaticAdCallBack maticAdCallBack) {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(activity, idInterstitialAd, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.

                        mInterNormal = interstitialAd;
                        maticAdCallBack.onInterstitialLoaded(interstitialAd);

                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        maticAdCallBack.onAdFailedToLoad(loadAdError);
                    }
                });

    }

    public void loadInterstitialAd3SameTime(Activity activity, String idInterHigh, String idInterMedium, String idInterNormal, MaticAdCallBack maticAdCallBack) {
        this.idInterHigh = idInterHigh;
        this.idInterMedium = idInterMedium;
        this.idInterNormal = idInterNormal;
        mInterHigh = null;
        mInterNormal = null;
        mInterMedium = null;
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(activity, idInterNormal, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        super.onAdLoaded(interstitialAd);
                        mInterNormal = interstitialAd;
                        maticAdCallBack.onInterstitialLoaded(interstitialAd);
                        Log.i(TAG, "loadAds Inter Normal onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, "loadAds Inter Normal " + loadAdError.toString());
                    }
                });
        InterstitialAd.load(activity, idInterMedium, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                mInterMedium = interstitialAd;
                Log.i(TAG, "loadAds Inter medium onAdLoaded");

            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d(TAG, "loadAds Inter medium " + loadAdError.toString());

            }
        });
        InterstitialAd.load(activity, idInterHigh, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                mInterHigh = interstitialAd;

                Log.i(TAG, "loadAds Inter high onAdLoaded");

            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d(TAG, "loadAds Inter High " + loadAdError.toString());

            }
        });


    }

    public void loadInterstitialAd3Alternative(Activity activity, String idInterHigh, String idInterMedium, String idInterNormal, MaticAdCallBack maticAdCallBack) {
        this.idInterHigh = idInterHigh;
        this.idInterMedium = idInterMedium;
        this.idInterNormal = idInterNormal;
        mInterHigh = null;
        mInterNormal = null;
        mInterMedium = null;
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(activity, idInterHigh, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                mInterHigh = interstitialAd;
                maticAdCallBack.onInterstitialLoaded(interstitialAd);


            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                InterstitialAd.load(activity, idInterMedium, adRequest, new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        super.onAdLoaded(interstitialAd);
                        mInterMedium = interstitialAd;
                        maticAdCallBack.onInterstitialLoaded(interstitialAd);

                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        InterstitialAd.load(activity, idInterNormal, adRequest,
                                new InterstitialAdLoadCallback() {
                                    @Override
                                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                        super.onAdLoaded(interstitialAd);
                                        mInterNormal = interstitialAd;
                                        maticAdCallBack.onInterstitialLoaded(interstitialAd);

                                    }

                                    @Override
                                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                        // Handle the error
                                        maticAdCallBack.onAdFailedToLoad(loadAdError);

                                    }
                                });
                    }
                });

            }
        });




    }
    void showDialog(Activity activity) {

        try {
            if (!(activity).isFinishing()) {
                hud = KProgressHUD.create(activity)
                        .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                        .setLabel("Welcome Taekwondo App!")
                        .setDetailsLabel("Loading...")
                        .setCancellable(true)
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

    public void showInterstitialAd(Activity activity, InterstitialAd interstitialAd, MaticAdCallBack maticAdCallBack) {
        if (interstitialAd != null) {
            showDialog(activity);
            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    maticAdCallBack.onNextAction();
                }
            });
            new Handler().postDelayed(() -> {
                hideDialog();
                interstitialAd.show(activity);

            }, 800);
        } else {
            maticAdCallBack.onNextAction();
        }
    }

    public boolean isAvailableInter() {
        if (isShowingInter) return false;
        return mInterHigh != null || mInterMedium != null || mInterNormal != null;
    }

    public void showPriorityInterstitialAd(Activity activity, MaticAdCallBack maticAdCallBack) {
        if (isAvailableInter()) {
            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    isShowingInter = false;
                    maticAdCallBack.onNextAction();
                    mInterHigh = null;
                    mInterMedium = null;
                    mInterNormal = null;
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    isShowingInter = false;
                    maticAdCallBack.onNextAction();
                    mInterHigh = null;
                    mInterMedium = null;
                    mInterNormal = null;
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                    isShowingInter = true;
                    maticAdCallBack.onInterShow();
                }
            };
            if (mInterHigh != null) {
                mInterHigh.setFullScreenContentCallback(fullScreenContentCallback);
                mInterHigh.show(activity);

            } else if (mInterMedium != null) {
                mInterMedium.setFullScreenContentCallback(fullScreenContentCallback);
                mInterMedium.show(activity);
            } else if (mInterNormal != null) {
                mInterNormal.setFullScreenContentCallback(fullScreenContentCallback);
                mInterNormal.show(activity);
            }
        } else {
            maticAdCallBack.onNextAction();
        }

    }

    public void showPriorityInterstitialAd(Activity activity, MaticAdCallBack maticAdCallBack, boolean isReload) {
        if (isAvailableInter()) {
            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    isShowingInter = false;
                    maticAdCallBack.onNextAction();
                    mInterHigh = null;
                    mInterMedium = null;
                    mInterNormal = null;
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    isShowingInter = false;
                    maticAdCallBack.onNextAction();
                    mInterHigh = null;
                    mInterMedium = null;
                    mInterNormal = null;
                    if (isReload) {
                        loadInterstitialAd3SameTime(activity, idInterHigh, idInterMedium, idInterHigh, maticAdCallBack);
                    }
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                    isShowingInter = true;
                    maticAdCallBack.onInterShow();
                }
            };
            if (mInterHigh != null) {
                mInterHigh.setFullScreenContentCallback(fullScreenContentCallback);
                mInterHigh.show(activity);

            } else if (mInterMedium != null) {
                mInterMedium.setFullScreenContentCallback(fullScreenContentCallback);
                mInterMedium.show(activity);
            } else if (mInterNormal != null) {
                mInterNormal.setFullScreenContentCallback(fullScreenContentCallback);
                mInterNormal.show(activity);
            }
        } else {
            maticAdCallBack.onNextAction();
        }

    }


}

