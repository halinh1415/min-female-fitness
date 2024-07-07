package mint.woman.female.workout.activity.ui.workout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.minfitness.decode.Decoder;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.common.adapter.VideoDemoAdapter;
import mint.woman.female.workout.custom.CustomViewPager;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.utils.FitnessConstUtils;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ExerciseDetailActivity extends AppCompatActivity {
    // @BindView(R.id.videoView)
    // CustomVideoView mVideoView;
    @BindView(R.id.tv_exercise_name)
    TextView mExerciseName;
    @BindView(R.id.tv_exercise_description)
    TextView mExerciseDescription;

    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;
    @BindView(R.id.viewPager)
    CustomViewPager mViewPager;
    @BindView(R.id.fl_adplaceholder)
    FrameLayout frameNativeAds;
    @BindView(R.id.img_muscle)
    ImageView imMuscle;
    ExerciseObject mExerciseObject;
    VideoDemoAdapter mAdapter;
    MyPreference myPreference;
    Boolean flagAds = false;
    int actionId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
                    Decoder.decryptToFile(getAssets(),"i/v/video_" + actionId,getCacheDir() + "/temp.mp4");
                } else {
                    Decoder.decryptInputToFile(getFilesDir() + "/discover/" +FitnessApplication.getApplication().getDiscover()+"/"+FitnessApplication.getApplication().getVersion()  + "/v/video_" + actionId, getCacheDir() + "/temp.mp4");
                }


                mExerciseName.setText(mExerciseObject.getName());

                //mBenefits.setText(mExerciseObject.getBenefits());
                setTitle( mExerciseObject.getName());

                mExerciseDescription.setText(mExerciseObject.getIntroduce());

                //   mEquipment.setText(mExerciseObject.getEquipment());
                mAdapter = new VideoDemoAdapter(getSupportFragmentManager(), mExerciseObject.getImagePath(), mExerciseObject.getVideoUrl());
                mViewPager.setAdapter(mAdapter);
                mTabLayout.setupWithViewPager(mViewPager);
                mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {

                        if (position == 1) frameNativeAds.setVisibility(View.GONE);
                        else frameNativeAds.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Bitmap bitmap1 = ((BitmapDrawable) getResources().getDrawable(R.drawable.img_abs_1)).getBitmap();
//
//        Bitmap bitmap2 = ((BitmapDrawable) getResources().getDrawable(R.drawable.img_body)).getBitmap();
//        Bitmap combine = combineImages(bitmap1,bitmap2);
        String muscle = mExerciseObject.getMuscle();
        String[] dd = muscle.split("-");
        Bitmap body = BitmapFactory.decodeResource(getResources(),
                R.drawable.img_body);
        for (int i = 0; i < dd.length; i++) {
            int mus = FitnessConstUtils.getMuscle(Integer.valueOf(dd[i]),i==0);
            body = overlay(body,BitmapFactory.decodeResource(getResources(), mus));
        }
        imMuscle.setImageBitmap(body);


    }
    public Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmOverlay; }
    private Bitmap combineImages(Bitmap bitmap1, Bitmap bitmap2) {

        int width = Math.max(bitmap1.getWidth(), bitmap2.getWidth());
        int height = bitmap1.getHeight() + bitmap2.getHeight();

        Bitmap combinedBitmap = Bitmap.createBitmap(width, height, bitmap1.getConfig());
        Canvas canvas = new Canvas(combinedBitmap);

        // Draw the first image at the top
        canvas.drawBitmap(bitmap1, 0, 0, null);

        // Draw the second image below the first one
        canvas.drawBitmap(bitmap2, 0, bitmap1.getHeight(), null);

        return combinedBitmap;
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
        if (!lang.isEmpty() && lang.length() > 2) {
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
