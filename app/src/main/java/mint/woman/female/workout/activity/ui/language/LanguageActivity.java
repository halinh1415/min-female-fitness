package mint.woman.female.workout.activity.ui.language;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.boarding.BoardingActivity;
import mint.woman.female.workout.activity.ui.language.LanguageAdapter.IChangeLanguage;
import mint.woman.female.workout.admodule.MaticAd;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.RemoteConfigKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LanguageActivity extends AppCompatActivity implements IChangeLanguage {
    @BindView(R.id.rcLanguage)
    RecyclerView rcLanguage;
    @BindView(R.id.layoutAdNative)
    FrameLayout layoutAdNative;
    @BindView(R.id.shimmer_container_native)
    ShimmerFrameLayout shimmerFrameLayout;

    private LanguageAdapter adapter;
    List<Language> languages;
    MyPreference myPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        myPreference = new MyPreference(this);
        initLanguage();

        if (myPreference.isShowAds()  && myPreference.getOK() && FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.NATIVE_LANGUAGE)) {
            FitnessApplication.getApplication().getStorageCommon().nativeAdLanguage.observe(LanguageActivity.this, (nativeAd) -> {
                if (nativeAd != null) {
                    MaticAd.getInstance().populateNativeAdView(LanguageActivity.this, nativeAd,R.layout.native_ad_language_layout, layoutAdNative, shimmerFrameLayout);
                } else {
                    layoutAdNative.setVisibility(View.GONE);
                }
            });

        } else {
            layoutAdNative.setVisibility(View.GONE);
        }

    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }else {

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
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                // Hide the nav bar and status bar4
        );
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    //    void requestNativeLanguage(){
////        AperoNativeAdView aperoNativeAdView = findViewById(R.id.aperoNativeAds);
////        aperoNativeAdView.loadNativeAd(this, BuildConfig.native_language);
//        AperoAd.getInstance().loadNativeAdResultCallback(LanguageFirstOpenActivity.this, BuildConfig.native_language,R.layout.custom_native_admob_medium, new AperoAdCallback(){
//            @Override
//            public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {
//                super.onNativeAdLoaded(nativeAd);
//                AperoAd.getInstance().populateNativeAdView(
//                        LanguageFirstOpenActivity.this,
//                        nativeAd,
//                        layoutAdNative,
//                        shimmerFrameLayout
//                );
//            }
//
//            @Override
//            public void onAdFailedToLoad(@Nullable ApAdError adError) {
//                super.onAdFailedToLoad(adError);
//                layoutAdNative.setVisibility(View.GONE);
//            }
//        });
//    }
    @OnClick(R.id.imgDone)
    void finishLanguage() {
        if (!adapter.getLanguageCode().isEmpty()) {
            myPreference.setLanguage(adapter.getLanguageCode());
            Bundle bundle = new Bundle();
            bundle.putString("language_code",adapter.getLanguageCode());
        }
        Intent intent = new Intent(LanguageActivity.this, BoardingActivity.class);
        intent.addFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        startActivity(intent);
        FitnessApplication.getApplication().getStorageCommon().nativeAdLanguage.postValue(null);
        finish();
    }

    private void initLanguage() {
        languages = new ArrayList<>();
        languages.add(new Language("en_US", "English", R.drawable.ic_language_english, false));
        languages.add(new Language("de_DE", "Deutsch", R.drawable.ic_language_german, false));
        languages.add(new Language("fr_FR", "Français", R.drawable.ic_language_french, false));
        languages.add(new Language("es_US", "Español", R.drawable.ic_language_spain, false));
        languages.add(new Language("pt_PT", "Português", R.drawable.ic_language_portuguese, false));
        languages.add(new Language("zh_CN", "Chinese", R.drawable.ic_language_chinese, false));
        languages.add(new Language("it_IT", "Italiano", R.drawable.ic_language_italian, false));
        languages.add(new Language("pl_PL", "Polski", R.drawable.ic_language_poland, false));
        languages.add(new Language("nl_NL", "Nederlands", R.drawable.ic_language_netherlands, false));
        languages.add(new Language("ja_JP", "日本語", R.drawable.ic_language_japanese, false));
        languages.add(new Language("ko_KR", "한국어", R.drawable.ic_language_south_korea, false));
        languages.add(new Language("tr_TR", "Türkçe", R.drawable.ic_language_turkey, false));
        languages.add(new Language("ar_EG", "العربية", R.drawable.ic_language_arabic, false));
        languages.add(new Language("in_ID", "Indonesia", R.drawable.ic_language_indonesia, false));
        languages.add(new Language("ru_RU", "Русский", R.drawable.ic_language_russia, false));
        languages.add(new Language("vi_VN", "Tiếng Việt", R.drawable.ic_language_vietnamese, false));

        String lang = Locale.getDefault().getLanguage();
        int checkLanguage = -1;
        for (int i = 0; i < languages.size(); i++) {
            if (languages.get(i).getCode().contains(lang)){
                checkLanguage = i;
                break;
            }
        }
        if (checkLanguage != -1){
            Language defaultLanguage = languages.remove(checkLanguage);
            languages.add(0,defaultLanguage);
        }
        adapter = new LanguageAdapter(this, languages, this);
        adapter.setDefault(lang);
        rcLanguage.setAdapter(adapter);
        rcLanguage.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FitnessApplication.getApplication().getStorageCommon().nativeAdLanguage.postValue(null);
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
    public void onChangeLanguage(String code) {

    }
}