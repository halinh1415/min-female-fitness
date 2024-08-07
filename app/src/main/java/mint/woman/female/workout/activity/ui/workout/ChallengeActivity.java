package mint.woman.female.workout.activity.ui.workout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import mint.woman.female.workout.BuildConfig;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.custom.CustomMyWorkoutActivity;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.common.adapter.ChallengeAdapter;
import mint.woman.female.workout.common.adapter.IDayClickListener;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.retrofit.ApiUtils;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChallengeActivity extends AppCompatActivity implements IDayClickListener {


    @BindView(R.id.adView)
    AdView mAdBanner;
    @BindView(R.id.rc_challenge)
    RecyclerView rcChallenge;
    @BindView(R.id.img_banner)
    ImageView mBanner;
    @BindView(R.id.image_workout)
    ImageView mImageWorkout;
    @BindView(R.id.progressBar)
    ProgressBar mPlanProgress;
    @BindView(R.id.tv_progress)
    TextView mPlanCount;
    ChallengeAdapter challengeAdapter;
    MyPreference myPreference;
    Bundle mBundle;
    ProgramObject mProgramObject;
    int progress =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CollapsingToolbarLayout toolBarLayout = findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());


        initView();
        requestNewAdBanner();
    }

    private void initView() {
        myPreference = new MyPreference(this);
        mBundle = getIntent().getExtras();
        if (mBundle != null) {
            int id = mBundle.getInt("PROGRAM_ID",0);
            mProgramObject = FitnessApplication.getApplication().getProgram(id);
          //  mProgramObject = mBundle.getParcelable("PLAN");
        }
        if (mProgramObject == null) {
            Toast.makeText(this, "This program is removed", Toast.LENGTH_SHORT).show();
            return;
        }
        setTitle(mProgramObject.getName().toUpperCase());
        progress = myPreference.getPlanProgress(mProgramObject.getId());
        challengeAdapter = new ChallengeAdapter(this,mProgramObject.getTotal());
        challengeAdapter.setProgress(progress);
        rcChallenge.setLayoutManager(new GridLayoutManager(this, 1));
        rcChallenge.setAdapter(challengeAdapter);
        String baseUrl = "file:///android_asset/images/";
        if (mProgramObject.getDiscover() >0){
            baseUrl = ApiUtils.BASE_URL_IMAGES;
        }
        Glide.with(ChallengeActivity.this).load(Uri.parse(baseUrl + mProgramObject.getBackground())).into(mBanner);

        Glide.with(ChallengeActivity.this).load(Uri.parse(baseUrl + mProgramObject.getImage())).into(mImageWorkout);
        mPlanProgress.setMax(mProgramObject.getTotal());
        mPlanProgress.setProgress(progress);
        mPlanCount.setText("" + (mProgramObject.getTotal() - progress) + " " + getString(R.string.txt_day_left));
        if (progress >= mProgramObject.getTotal()) {
            restartProgress();
        }

    }
    void requestNewAdBanner() {
        mAdBanner = findViewById(R.id.adView);
        if (myPreference.isShowAds() && myPreference.getOK()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdBanner.loadAd(adRequest);
            mAdBanner.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    mAdBanner.setVisibility(View.GONE);
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdBanner.setVisibility(View.VISIBLE);
                }
            });
        }else {
            mAdBanner.setVisibility(View.GONE);
        }
    }

    @Override
    public void goDay(int day) {
        try {
            if (day >= mProgramObject.getTotal()) {
                Toast.makeText(this, "Full Workout!", Toast.LENGTH_SHORT).show();
                restartProgress();
                return;
            }
            if (day > progress){

                Toast.makeText(ChallengeActivity.this, getString(R.string.txt_please_finish_workout), Toast.LENGTH_SHORT).show();
            }else{
                Intent intent = new Intent(ChallengeActivity.this, PreviewActivity.class);
                mBundle.putInt("DAY_NUMBER", day);
                intent.putExtras(mBundle);
                startActivity(intent);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @OnClick(R.id.btn_goto)
    void goWorkout(){
        goDay(progress);
    }

    private void restartProgress() {
        AlertDialog.Builder restart = new AlertDialog.Builder(this);
        restart.setTitle(getString(R.string.txt_restart_progress) + " " + mProgramObject.getName());
        restart.setNegativeButton(getString(android.R.string.cancel), null);
        restart.setPositiveButton(getString(android.R.string.ok), (dialogInterface, i) -> {
            myPreference.setPlanProgress(mProgramObject.getId(), 0);
            initView();
        });
        restart.show();
    }
    private void showDialogSetProgress() {
        if (true)
        {
            NumberPicker numberPicker = new NumberPicker(this);
            numberPicker.setMaxValue(mProgramObject.getTotal()-1);
            numberPicker.setMinValue(0);
            numberPicker.setValue(myPreference.getPlanProgress(mProgramObject.getId()));
            String[] displayDays = new String[mProgramObject.getTotal()];
            for (int i = 0; i <  mProgramObject.getTotal(); i++) {
                displayDays[i] = getString(R.string.txt_day)+" "+(i+1);
            }
            numberPicker.setDisplayedValues(displayDays);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.txt_start_day));
            builder.setMessage(getString(R.string.txt_choose_day));
            builder.setView(numberPicker);
            builder.setPositiveButton("OK", (dialog, which) -> {
                myPreference.setPlanProgress(mProgramObject.getId(), numberPicker.getValue());
                initView();
            });
            builder.setNegativeButton("CANCEL",null);
            builder.show();
        }else {
           // Toast.makeText(ChallengeActivity.this, "Please join Premium member", Toast.LENGTH_SHORT).show();
           // startActivity(new Intent(ChallengeActivity.this, PremiumActivity.class));
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_week, menu);
        if (BuildConfig.DEBUG) {
            menu.findItem(R.id.action_edit).setVisible(true);
            menu.findItem(R.id.action_set_progress).setVisible(true);
        } else {
            menu.findItem(R.id.action_edit).setVisible(false);
            menu.findItem(R.id.action_set_progress).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Home Button!
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_reset:
                restartProgress();
                return true;
            case R.id.action_edit:
                Intent intent = new Intent(ChallengeActivity.this, CustomMyWorkoutActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("PROGRAM_ID",mProgramObject.getId());
             //   bundle.putParcelable("PLAN", mProgramObject);
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            case R.id.action_set_progress:
                showDialogSetProgress();
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