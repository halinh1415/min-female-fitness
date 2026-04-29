package mint.woman.female.workout.activity.ui.firstsetup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.make.dots.dotsindicator.DotsIndicator;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.boarding.BoardingActivity;
import mint.woman.female.workout.activity.ui.firstsetup.content.FocusAreaFragment;
import mint.woman.female.workout.activity.ui.firstsetup.content.GenderSetupFragment;
import mint.woman.female.workout.activity.ui.firstsetup.content.ReminderSetupFragment;
import mint.woman.female.workout.activity.ui.firstsetup.content.UserInfoFragment;
import mint.woman.female.workout.activity.ui.firstsetup.content.WorkoutLevelSetupFragment;
import mint.woman.female.workout.activity.ui.firstsetup.content.YearOldSetupFragment;
import mint.woman.female.workout.activity.ui.guide.GuidePersonalPlanActivity;
import mint.woman.female.workout.activity.ui.main.YogaActivity;
import mint.woman.female.workout.admodule.MaticAd;
import mint.woman.female.workout.admodule.MaticAdCallBack;
import mint.woman.female.workout.custom.CustomViewPager;
import mint.woman.female.workout.databinding.ActivityFirstSetupBinding;
import mint.woman.female.workout.databinding.LoadingNativeMediumBinding;
import mint.woman.female.workout.receiver.AlarmReceiver;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

import java.util.Locale;
import mint.woman.female.workout.utils.RemoteConfigKeys;

public class FirstSetupActivity extends AppCompatActivity {
    private static final String TAG = "FirstSetupActivity";
    ActivityFirstSetupBinding binding;


    UserSetupAdapter userSetupAdapter;


    int currentPosition = 0;
    MyPreference myPreference;
    SetupViewModel setupViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFirstSetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupViewModel = new ViewModelProvider(this).get(SetupViewModel.class);

        initView();
    }

    private void initView() {
        myPreference = new MyPreference(this);
        userSetupAdapter = new UserSetupAdapter(getSupportFragmentManager());
        binding.vpSetup.setAdapter(userSetupAdapter);
        binding.dotsIndicator.setViewPager(binding.vpSetup);

        if (myPreference.isShowAds()  && myPreference.getOK() && FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.NATIVE_SETUP)) {
            FitnessApplication.getApplication().getStorageCommon().nativeSetup.observe(FirstSetupActivity.this, (nativeAd) -> {
                if (nativeAd != null) {
                    MaticAd.getInstance().populateNativeAdView(FirstSetupActivity.this, nativeAd,R.layout.native_admob_medium_layout, binding.layoutAdNative, binding.loading.shimmerContainerNative);
                } else {
                    binding.layoutAdNative.setVisibility(View.GONE);
                }
            });

        } else {
            binding.layoutAdNative.setVisibility(View.GONE);
        }
        binding.btnNext.setOnClickListener(this::goNext);
    }

    @Override
    public void onBackPressed() {
        if (currentPosition >0){
            currentPosition --;
            binding.vpSetup.setCurrentItem(currentPosition);
        }else{
            super.onBackPressed();
        }

    }


    void goNext(View view) {
        FirebaseAnalytics.getInstance(this).logEvent("click_next_scr_setup", new Bundle());
        if (currentPosition < 5) {
            if (currentPosition == 2) {
                myPreference.setCurrentWeight(setupViewModel.getWeightCm());
                myPreference.setCurrentHeight(setupViewModel.getHeightCm());
            }

            currentPosition++;
            binding.tvSetupStep.setText(getString(R.string.txt_step) + " " + (currentPosition + 1) + "/" + userSetupAdapter.getCount());
            binding.vpSetup.setCurrentItem(currentPosition);
        } else {
            if (Build.VERSION.SDK_INT >= 33) {
                if (ContextCompat.checkSelfPermission(FirstSetupActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(FirstSetupActivity.this, Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(FirstSetupActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS,Manifest.permission.SCHEDULE_EXACT_ALARM},1111);
                    return;
                }
            }
            nextAction();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1111){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG,"onRequestPermissionsResult PERMISSION_GRANTED");
                nextAction();
            } else {
                nextAction();
                Log.d(TAG,"onRequestPermissionsResult PERMISSION_DENIED");

            }
        }

    }

    private void finishSetup() {
        if (FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.INTER_FIRST_OPEN) &&MaticAd.getInstance().isAvailableInter() ){
            MaticAd.getInstance().showPriorityInterstitialAd(FirstSetupActivity.this,new MaticAdCallBack(){
                @Override
                public void onNextAction() {
                    super.onNextAction();
                    nextAction();
                }
            });
        }else{
          nextAction();
        }

    }
    void nextAction(){
        myPreference.setFirstOpen(false);
        String time = String.format("%02d:%02d",setupViewModel.getHour(),setupViewModel.getMinute());
        myPreference.setTimeRemindDaily(time);
        AlarmReceiver.scheduleLockscreenWidget(FirstSetupActivity.this,time);
        startActivity(new Intent(FirstSetupActivity.this, GuidePersonalPlanActivity.class));
        finish();
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
    class UserSetupAdapter extends FragmentPagerAdapter {


        public UserSetupAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new GenderSetupFragment();
                case 1:
                    return new YearOldSetupFragment();
                case 2:
                    return new UserInfoFragment();

                case 3:
                    return new WorkoutLevelSetupFragment();
                case 4:
                    return new FocusAreaFragment();
                default:
                    return new ReminderSetupFragment();
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    }
}