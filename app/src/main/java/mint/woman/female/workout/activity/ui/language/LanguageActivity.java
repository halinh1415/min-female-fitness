package mint.woman.female.workout.activity.ui.language;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.boarding.BoardingActivity;
import mint.woman.female.workout.activity.ui.language.LanguageAdapter.IChangeLanguage;
import mint.woman.female.workout.admodule.MaticAd;
import mint.woman.female.workout.databinding.ActivityLanguageBinding;
import mint.woman.female.workout.databinding.NativeLanguageLoadingBinding;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.RemoteConfigKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class LanguageActivity extends AppCompatActivity implements IChangeLanguage {
    ActivityLanguageBinding binding;

    private LanguageAdapter adapter;
    List<Language> languages;
    MyPreference myPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLanguageBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        initView();
    }

    private void initView() {
        myPreference = new MyPreference(this);
        initLanguage();
        if (FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.ENABLE_LANGUAGE_NEXT)){
            binding.imgDone.setVisibility(View.VISIBLE);
        }else{
            binding.imgDone.setVisibility(View.INVISIBLE);
        }


        if (myPreference.isShowAds()  && myPreference.getOK() && FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.NATIVE_LANGUAGE)) {
            FitnessApplication.getApplication().getStorageCommon().nativeAdLanguage.observe(LanguageActivity.this, (nativeAd) -> {
                if (nativeAd != null) {
                    MaticAd.getInstance().populateNativeAdView(LanguageActivity.this, nativeAd,R.layout.native_ad_language_layout, binding.layoutAdNative, binding.loading.shimmerContainerNative);
                } else {
                    binding.layoutAdNative.setVisibility(View.GONE);
                }
            });

        } else {
            binding.layoutAdNative.setVisibility(View.GONE);
        }
        binding.imgDone.setOnClickListener(this::finishLanguage);

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

    void finishLanguage(View view) {
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
        languages.add(new Language("en", "English", R.drawable.ic_language_english, false));
        languages.add(new Language("de", "Deutsch", R.drawable.ic_language_german, false));
        languages.add(new Language("fr", "Français", R.drawable.ic_language_french, false));
        languages.add(new Language("es", "Español", R.drawable.ic_language_spain, false));
        languages.add(new Language("pt", "Português", R.drawable.ic_language_portuguese, false));
        languages.add(new Language("zh_CN", "简体中文", R.drawable.ic_language_chinese, false));
        languages.add(new Language("zh_TW", "繁体中文", R.drawable.ic_language_taiwan, false));
        languages.add(new Language("it", "Italiano", R.drawable.ic_language_italian, false));
        languages.add(new Language("pl", "Polski", R.drawable.ic_language_poland, false));
        languages.add(new Language("nlL", "Nederlands", R.drawable.ic_language_netherlands, false));
        languages.add(new Language("ja", "日本語", R.drawable.ic_language_japanese, false));
        languages.add(new Language("ko", "한국어", R.drawable.ic_language_south_korea, false));
        languages.add(new Language("tr", "Türkçe", R.drawable.ic_language_turkey, false));
        languages.add(new Language("ar", "العربية", R.drawable.ic_language_arabic, false));
        languages.add(new Language("in_ID", "Indonesia", R.drawable.ic_language_indonesia, false));
        languages.add(new Language("ru", "Русский", R.drawable.ic_language_russia, false));
        languages.add(new Language("vi", "Tiếng Việt", R.drawable.ic_language_vietnamese, false));

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
            languages.add(2,defaultLanguage);
        }
        adapter = new LanguageAdapter(this, languages, this);
        //adapter.setDefault(lang);
        binding.rcLanguage.setAdapter(adapter);
        binding.rcLanguage.setLayoutManager(new LinearLayoutManager(this));
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
        if (!lang.isEmpty() ) {
            lang = lang.substring(0, 2);
        } else {
            lang = Locale.getDefault().getLanguage();
        }
        super.attachBaseContext(LanguageHelper.setLanguage(newBase, lang));

    }


    @Override
    public void onChangeLanguage(String code) {
        binding.imgDone.setVisibility(View.VISIBLE);
    }
}