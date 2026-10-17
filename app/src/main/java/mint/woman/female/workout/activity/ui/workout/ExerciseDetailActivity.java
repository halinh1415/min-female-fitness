package mint.woman.female.workout.activity.ui.workout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.minfitness.decode.Decoder;

import mint.woman.female.workout.custom.MuscleMapView;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.common.adapter.VideoDemoAdapter;
import mint.woman.female.workout.custom.CustomViewPager;
import mint.woman.female.workout.databinding.ActivityExerciseDetailBinding;
import mint.woman.female.workout.model.Customfields;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.utils.ExerciseHelper;
import mint.woman.female.workout.utils.FitnessConstUtils;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

import java.util.Locale;

public class ExerciseDetailActivity extends AppCompatActivity {
    // @BindView(R.id.videoView)
    // CustomVideoView mVideoView;
  ActivityExerciseDetailBinding binding;
    ExerciseObject mExerciseObject;
    VideoDemoAdapter mAdapter;
    MyPreference myPreference;
    Boolean flagAds = false;
    int actionId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExerciseDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initView();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        myPreference = new MyPreference(this);
        Bundle mBundle = getIntent().getExtras();
        try {
            if (mBundle != null) {
                actionId = mBundle.getInt("actionId");
                mExerciseObject = FitnessApplication.getApplication().getHashMapExercise().get(actionId);
                if (FitnessApplication.getApplication().getDiscover() == 0) {
                    Decoder.decryptToFile(getAssets(), "i/v/video_" + actionId, getCacheDir() + "/temp.mp4");
                } else {
                    Decoder.decryptInputToFile(getFilesDir() + "/discover/" + FitnessApplication.getApplication().getDiscover() + "/" + FitnessApplication.getApplication().getVersion() + "/v/video_" + actionId, getCacheDir() + "/temp.mp4");
                }
                FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
                crashlytics.setCustomKey("ACTION_ID", actionId);

                binding.contentExercise.tvExerciseName.setText(mExerciseObject.getName());

                //mBenefits.setText(mExerciseObject.getBenefits());
                setTitle(mExerciseObject.getName());

                binding.contentExercise.tvExerciseDescription.setText(mExerciseObject.getIntroduce());

                //   mEquipment.setText(mExerciseObject.getEquipment());
                mAdapter = new VideoDemoAdapter(getSupportFragmentManager(), mExerciseObject.getImagePath(), ExerciseHelper.getInstance().getUrl(mExerciseObject.getId()));
                binding.viewPager.setAdapter(mAdapter);
                binding.tabLayout.setupWithViewPager(  binding.viewPager);
                binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {

                        if (position == 1) binding.contentExercise.flAdplaceholder.setVisibility(View.GONE);
                        else  binding.contentExercise.flAdplaceholder.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
                Customfields customfields = mExerciseObject.getCustomfields();
                if (customfields == null) {
                    binding.contentExercise.cvMuscle.setVisibility(View.GONE);
                    return;
                }
                ChipGroup chipGroup = findViewById(R.id.chipGroup);
                MuscleMapView muscleMap = binding.contentExercise.muscleMap;
                if (customfields.getMuscleMajor()!= null) {
                    for (String major : customfields.getMuscleMajor()) {
                        Chip chip = (Chip) LayoutInflater.from(this).inflate(R.layout.chip_item, chipGroup, false);

                        int resId = FitnessConstUtils.getMuscleId(Integer.parseInt(major));
                        if (resId > 0) {
                            chip.setText(getString(resId));
                            chipGroup.addView(chip);
                            muscleMap.bindChip(chip, major);
                        }

                    }
                }
                if (customfields.getMuscleMinor() != null) {
                    for (String minor : customfields.getMuscleMinor()) {
                        Chip chip = (Chip) LayoutInflater.from(this).inflate(R.layout.chip_item, chipGroup, false);
                        chip.setChipIconResource(R.drawable.ic_red_2);
                        int resId = FitnessConstUtils.getMuscleId(Integer.parseInt(minor));
                        if (resId > 0) {
                            chip.setText(getString(resId));
                            chipGroup.addView(chip);
                            muscleMap.bindChip(chip, minor);
                        }
                    }
                }
                muscleMap.setMuscleGroups(customfields.getMuscleMajor(), customfields.getMuscleMinor());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (flagAds) {
            flagAds = false;
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Home Button!
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            int uiMode = overrideConfiguration.uiMode;
            overrideConfiguration.setTo(getBaseContext().getResources().getConfiguration());
            overrideConfiguration.uiMode = uiMode;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }


}
