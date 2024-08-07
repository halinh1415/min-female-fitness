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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import mint.woman.female.workout.BuildConfig;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.custom.CustomMyWorkoutActivity;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.common.adapter.DayAdapter;
import mint.woman.female.workout.custom.ExpandableTextView;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProgramActivity extends AppCompatActivity implements DayAdapter.ClickListener {

    @BindView(R.id.rc_program)
    RecyclerView mRcProgram;
    @BindView(R.id.txt_program_description)
    ExpandableTextView mProgramDescription;
    @BindView(R.id.img_banner)
    ImageView mBanner;
    @BindView(R.id.image_workout)
    ImageView mImageWorkout;
    @BindView(R.id.progressBar)
    ProgressBar mPlanProgress;
    @BindView(R.id.tv_program_progress)
    TextView mPlanCount;
    @BindView(R.id.adView)
    AdView mAdBanner;
    @BindView(R.id.btn_goto)
    Button mBtnGoto;
    @BindView(R.id.tv_program_name)
    TextView mProgramName;

    List<PlanObject> mPlanObjectList;
    int sizePlan, progress;
    ProgramObject mProgramObject;
    Bundle mBundle;
    MyPreference myPreference;
    DayAdapter mDayAdapter;
    boolean flagRefresh = false;
    private MenuItem menuItemAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);
        initView();
        requestNewAdBanner();
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

    private void initView() {
        try {
            myPreference = new MyPreference(this);
            mBundle = getIntent().getExtras();
            if (mBundle != null) {
                mProgramObject = mBundle.getParcelable("PLAN");
            }


            mPlanObjectList = FitnessApplication.getApplication().mJsonHelper.getListPlan(mProgramObject.getPlan(), mProgramObject.getDiscover(),mProgramObject.getVersion());
            sizePlan = mPlanObjectList.size();
            progress = myPreference.getPlanProgress(mProgramObject.getId());
            setTitle(getString(R.string.txt_challenge_7x4));
            mProgramName.setText(mProgramObject.getName().toUpperCase());
            mProgramDescription.setVisibility(View.GONE);
            mRcProgram.setLayoutManager(new LinearLayoutManager(this));
            mRcProgram.setNestedScrollingEnabled(false);
            mDayAdapter = new DayAdapter(mPlanObjectList, progress, this);
            mRcProgram.setAdapter(mDayAdapter);
            Glide.with(getApplicationContext()).load(Uri.parse("file:///android_asset/images/" + mProgramObject.getBackground())).into(mBanner);

            Glide.with(getApplicationContext()).load(Uri.parse("file:///android_asset/images/" + mProgramObject.getImage())).into(mImageWorkout);
            mPlanProgress.setMax(sizePlan);
            mPlanProgress.setProgress(progress);
            mPlanCount.setText("" + (sizePlan - progress) + " " + getString(R.string.txt_day_left));
            if (progress < mPlanObjectList.size()) {
                mBtnGoto.setText(getText(R.string.txt_go_to) + " " + getString(R.string.txt_day) + " " + (progress + 1));
            }
            if (progress >= mPlanObjectList.size()) {
                restartProgress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (flagRefresh) {
            flagRefresh = false;
            initView();
        }
    }

    void requestNewAdBanner() {
        mAdBanner = findViewById(R.id.adView);
        mAdBanner.setVisibility(View.GONE);
        if (myPreference.isShowAds() && myPreference.getOK()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdBanner.loadAd(adRequest);
            mAdBanner.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdBanner.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @OnClick(R.id.btn_goto)
    void onClick() {
        gotoDay(progress);
    }

    private void gotoDay(int idDay) {
        try {
            if (mPlanObjectList.size() == 0) {
                return;
            }
            if (mPlanObjectList.get(idDay).mActionList.size() == 0) {
                mBundle.putInt("DAY_NUMBER", idDay);
                flagRefresh = true;
                Intent intent = new Intent(ProgramActivity.this, RestTimeActivity.class);
                intent.putExtras(mBundle);
                startActivity(intent);
            } else {
                flagRefresh = true;
                Intent intent = new Intent(ProgramActivity.this, PreviewActivity.class);
                mBundle.putInt("DAY_NUMBER", idDay);
                intent.putExtras(mBundle);
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                Intent intent = new Intent(ProgramActivity.this, CustomMyWorkoutActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("PROGRAM_ID", mProgramObject.getId());
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_week, menu);

        if (mProgramObject != null && mProgramObject.getType() == 3) {
            menuItemAdd.setVisible(false);
        }

        if (BuildConfig.DEBUG) {
            menu.findItem(R.id.action_edit).setVisible(true);
        } else {
            menu.findItem(R.id.action_edit).setVisible(false);
        }
        return true;
    }


    @Override
    public void onDayViewClicked(@NonNull PlanObject object, int position) {
        if (position < progress + 1) {
            gotoDay(position);
        } else {
            Toast.makeText(this, getString(R.string.txt_please_finish_workout), Toast.LENGTH_SHORT).show();
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
}


