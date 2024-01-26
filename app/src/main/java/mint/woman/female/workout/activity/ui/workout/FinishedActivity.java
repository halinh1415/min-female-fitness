package mint.woman.female.workout.activity.ui.workout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.reminder.ReminderActivity;
import mint.woman.female.workout.custom.DialogRating;
import mint.woman.female.workout.custom.SelectReminderEveryDayDialog;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.model.HistoryItem;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.utils.Database;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

@SuppressLint({"NonConstantResourceId", "UseSwitchCompatOrMaterialCode"})
public class FinishedActivity extends AppCompatActivity  {

    @BindView(R.id.adView)
    AdView mAdBanner;
    @BindView(R.id.tv_completed)
    TextView txtCompleted;
    @BindView(R.id.txt_exercise_count)
    TextView txtExerciseCount;
    @BindView(R.id.txt_kcal_count)
    TextView txtCalCount;
    @BindView(R.id.txt_time_count)
    TextView txtTimeCount;
    @BindView(R.id.sw_google_fit)
    Switch mSWGoogleFit;
    @BindView(R.id.img_finished_banner)
    ImageView mBanner;
    @BindView(R.id.txt_reminder_set)
    TextView mReminderSet;
    @BindView(R.id.lottieView)
    LottieAnimationView lottieAnimationView;
    PlanObject mPlanObject;
    ProgramObject mProgramObject;
    HistoryItem mHistoryItem;
    MediaPlayer mCongratulationPlayer;
    MyPreference myPreference;
    int flagAds;
    Database mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initView();
        requestNewAdBanner();
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void initView() {
        myPreference = new MyPreference(this);
        mSWGoogleFit.setChecked(myPreference.isSyncGoogleFit());
        Bundle mBundle = getIntent().getExtras();
        mDatabase = FitnessApplication.getApplication().mDatabase;
        lottieAnimationView.setAnimation("success.json");
        lottieAnimationView.playAnimation();

       // Glide.with(this).load(R.drawable.finished_banner).into(mBanner);
        if (mBundle != null) {
            mPlanObject = mBundle.getParcelable("PLAN_OBJECT");
            mProgramObject = mBundle.getParcelable("PLAN");
            mHistoryItem = mBundle.getParcelable("HISTORY");

            mReminderSet.setText(mDatabase.getReminderSet());
            float kCalories = 0;
            if (mHistoryItem != null) {
                kCalories = (float) mHistoryItem.getCalories();
                txtCalCount.setText(String.format("%.0f", kCalories));
                txtCompleted.setText(mHistoryItem.getName()+"\n" + getString(R.string.txt_completed));
                txtExerciseCount.setText("" + mPlanObject.getActionList().size());
                int time = mHistoryItem.getRealDuration();
                int minutes = time / 60;
                int seconds = time % 60;
                txtTimeCount.setText(String.format("%2d:%02d", minutes, seconds));
            }
            if (myPreference.isSyncGoogleFit()) {
                FitnessOptions fitnessOptions = FitnessOptions.builder()
                        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_WRITE)
                        .build();
                if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
                    myPreference.setSyncGoogleFit(false);
                    GoogleSignIn.requestPermissions(
                            this, // your activity
                            999,
                            GoogleSignIn.getLastSignedInAccount(this),
                            fitnessOptions);
                } else {
                    accessGoogleFit();
                }
            }
        }
        if (mCongratulationPlayer != null) mCongratulationPlayer.release();
        if (myPreference.isMusicOn()) {
            mCongratulationPlayer = MediaPlayer.create(this, R.raw.cheer);
            mCongratulationPlayer.setLooping(false);
            mCongratulationPlayer.start();
        }
        if (!myPreference.isRated() && myPreference.getShowRate() && myPreference.openCount() % 3 == 1) {
            DialogRating dialogPreRating = new DialogRating();
            dialogPreRating.show(getSupportFragmentManager(), "rate");
        }
        if (!myPreference.isSetReminder()) {
            SelectReminderEveryDayDialog selectReminderEveryDayDialog = new SelectReminderEveryDayDialog();
            selectReminderEveryDayDialog.show(getSupportFragmentManager(), "SelectReminder");
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






    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Home Button!
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (flagAds == 1) {
            flagAds = 0;
            finish();
        }
        if (flagAds == 2) {
            flagAds = 0;
            goAgain();
        }
    }

    @OnClick({R.id.btn_share,R.id.txt_feedback,R.id.txt_edit_reminder,R.id.btn_finish})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_share:
                try {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBody = "https://play.google.com/store/apps/details?id=" + getPackageName();
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.txt_feedback:
                sendEmail();
                break;
            case R.id.txt_edit_reminder:
                startActivity(new Intent(FinishedActivity.this, ReminderActivity.class));
                break;
            case R.id.btn_finish:
                finish();
                break;
        }
    }
    public void sendEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto: minfitfeedback@gmail.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[Feedback] Female Workout");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        startActivity(Intent.createChooser(emailIntent, "Send Email"));
    }
    private void goAgain() {
        finish();
    }

//    private void goHome() {
//        Intent intent = new Intent(this, FitnessActivity.class);
//        startActivity(intent);
//        finish();
//    }


    @OnCheckedChanged(R.id.sw_google_fit)
    void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == R.id.sw_google_fit) {
            if (b) {
                FitnessOptions fitnessOptions = FitnessOptions.builder()
                        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_WRITE)
                        .build();
                if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
                    myPreference.setSyncGoogleFit(false);
                    GoogleSignIn.requestPermissions(
                            this, // your activity
                            999,
                            GoogleSignIn.getLastSignedInAccount(this),
                            fitnessOptions);
                } else {
                    // accessGoogleFit();
                }
            } else {
                myPreference.setSyncGoogleFit(false);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999) {
            if (resultCode == Activity.RESULT_OK) {
                myPreference.setSyncGoogleFit(true);
                mSWGoogleFit.setChecked(true);
                accessGoogleFit();
            } else {
                myPreference.setSyncGoogleFit(false);
                mSWGoogleFit.setChecked(false);
            }
        }

    }

    private void accessGoogleFit() {

// Create a session with metadata about the activity.
        Session session = new Session.Builder()
                .setName(mHistoryItem.getName())
                .setDescription(mHistoryItem.getName())
                .setIdentifier("YOGA_WORKOUT_PLAN_" + mHistoryItem.getStartTime())
                .setActivity(FitnessActivities.YOGA)
                .setStartTime(mHistoryItem.getStartTime(), TimeUnit.MILLISECONDS)
                .setEndTime(mHistoryItem.getEndTime(), TimeUnit.MILLISECONDS)
                .build();

// Build a session insert request
        DataSource caloriesDS = new DataSource.Builder()
                .setAppPackageName(this.getPackageName())
                .setDataType(DataType.TYPE_CALORIES_EXPENDED)
                .setAppPackageName(getApplicationContext())
                .setType(DataSource.TYPE_RAW)
                .build();
        float calories = (float) mHistoryItem.getCalories();

//        DataPoint dataPoint = DataPoint.builder(caloriesDS).setTimeInterval(mHistoryItem.getStartTime(),mHistoryItem.getEndTime(),TimeUnit.MILLISECONDS).setField(Field.FIELD_CALORIES,calories).build();
//        DataSet kbDataSet = DataSet.builder(caloriesDS)
//                .add(dataPoint)
//                .build();
        // Create a data set of the run speeds to include in the session.
        DataSet kbDataSet = DataSet.create(caloriesDS);
        DataPoint caloriesDP = kbDataSet.createDataPoint().setTimeInterval(mHistoryItem.getStartTime(), mHistoryItem.getEndTime(), TimeUnit.MILLISECONDS);

        caloriesDP.getValue(Field.FIELD_CALORIES).setFloat(calories);
        kbDataSet.add(caloriesDP);


        SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                .setSession(session)
                .addDataSet(kbDataSet)
                .build();

        Fitness.getSessionsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .insertSession(insertRequest)
                .addOnSuccessListener(aVoid -> {
                    Log.i("HAHA", "activitySegmentKB insert was successful!");
                })
                .addOnFailureListener(e -> Log.i("HAHA", "There was a problem inserting the activitySegmentKB: " +
                        e.getLocalizedMessage()));

    }

}
