package mint.woman.female.workout.activity.ui.boarding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.make.dots.dotsindicator.DotsIndicator;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.firstsetup.FirstSetupActivity;
import mint.woman.female.workout.activity.ui.language.LanguageActivity;
import mint.woman.female.workout.admodule.MaticAd;
import mint.woman.female.workout.admodule.MaticAdCallBack;
import mint.woman.female.workout.admodule.MaticAdKeys;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mint.woman.female.workout.utils.RemoteConfigKeys;

public class BoardingActivity extends AppCompatActivity {
    private static final String TAG = "BoardingActivity" ;
    int[] boardingTitles = new int[]{R.string.txt_title_onboard_1, R.string.txt_title_onboard_2, R.string.txt_title_onboard_3, R.string.txt_title_onboard_4, R.string.txt_title_onboard_5};

    String[] imgIntros = new String[]{"onboard_intro_1.png", "onboard_intro_2.png", "onboard_intro_3.png", "onboard_intro_4.png", "onboard_intro_5.png"};
    int[] boardingDescriptions = new int[]{R.string.txt_description_onboard_1, R.string.txt_description_onboard_2, R.string.txt_description_onboard_3, R.string.txt_description_onboard_4, R.string.txt_description_onboard_5};

    @BindView(R.id.vpOnboard)
    ViewPager vpOnboard;

    @BindView(R.id.dotsIndicator)
    DotsIndicator dotsIndicator;
    @BindView(R.id.layoutAdNative)
    FrameLayout layoutAdNative;
    @BindView(R.id.shimmer_container_native)
    ShimmerFrameLayout shimmerFrameLayout;
    @BindView(R.id.tvNext)
    TextView tvNext;
    @BindView(R.id.tvSkip)
    TextView tvSkip;

    int currentPosition = 0;
    BoardingAdapter boardingAdapter;
    MyPreference myPreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boarding);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        myPreference = new MyPreference(this);
        Shader textShader = new LinearGradient(0, 0, 200, tvNext.getTextSize(),
                new int[]{
                        Color.parseColor("#3F51B5"),
                        Color.parseColor("#2196F3"),
                        Color.parseColor("#00BCD4"),
                }, null, Shader.TileMode.CLAMP);
        tvNext.getPaint().setShader(textShader);

        boardingAdapter = new BoardingAdapter(getSupportFragmentManager());
        vpOnboard.setAdapter(boardingAdapter);
        dotsIndicator.setViewPager(vpOnboard);
        if (myPreference.isShowAds()  && myPreference.getOK() && FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.NATIVE_ONBOARD)) {
            FitnessApplication.getApplication().getStorageCommon().nativeAdOnBoard.observe(BoardingActivity.this, (nativeAd) -> {
                if (nativeAd != null) {
                    MaticAd.getInstance().populateNativeAdView(BoardingActivity.this, nativeAd,R.layout.native_admob_medium_layout, layoutAdNative, shimmerFrameLayout);
                } else {
                    layoutAdNative.setVisibility(View.GONE);
                }
            });

        } else {
            layoutAdNative.setVisibility(View.GONE);
        }

    }
    @Override
    public void onBackPressed() {
        if (currentPosition >0){
            currentPosition --;
            vpOnboard.setCurrentItem(currentPosition);
        }else{
            super.onBackPressed();
        }

    }
    void requestNewNativeOnBoarding(){
        if (myPreference.isShowAds() && myPreference.getOK() ){
            //FitnessApplication.get(this).getStorageCommon().nativeAdOnBoard.postValue(null);
            Log.d(TAG, "loadAds requestNewNativeOnBoarding");
            MaticAd.getInstance().loadNativeAd3Alternative(BoardingActivity.this, MaticAdKeys.NATIVE_ONBOARD_HIGH,MaticAdKeys.NATIVE_ONBOARD_MEDIUM,MaticAdKeys.NATIVE_ONBOARD, new MaticAdCallBack() {
                @Override
                public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    Log.d(TAG, "loadAds nativeAdOnBoard onNativeAdLoaded");
                    FitnessApplication.getApplication().getStorageCommon().nativeAdOnBoard.postValue(nativeAd);
                }

            });
        }
    }
    @OnClick(R.id.tvNext)
    void nextIntro() {
        if (currentPosition < boardingTitles.length - 1) {

            currentPosition++;
            //FirebaseAnalytics.getInstance(this).logEvent("scr_onboard_" + (currentPosition + 1), new Bundle());
            requestNewNativeOnBoarding();
            vpOnboard.setCurrentItem(currentPosition);
        } else {
            startActivity(new Intent(BoardingActivity.this, FirstSetupActivity.class));
            finish();
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
    protected void onDestroy() {
        super.onDestroy();
        FitnessApplication.getApplication().getStorageCommon().nativeAdOnBoard.postValue(null);
    }


    class BoardingAdapter extends FragmentPagerAdapter {


        public BoardingAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return BoardingFragment.newInstance(getString(boardingTitles[position]), getString(boardingDescriptions[position]), imgIntros[position]);
        }

        @Override
        public int getCount() {
            return boardingTitles.length;
        }
    }
}