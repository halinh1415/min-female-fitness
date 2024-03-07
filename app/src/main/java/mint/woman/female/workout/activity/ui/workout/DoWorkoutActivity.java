package mint.woman.female.workout.activity.ui.workout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.main.YogaActivity;
import mint.woman.female.workout.activity.ui.report.ReportActivity;
import mint.woman.female.workout.custom.DialogQuit;
import mint.woman.female.workout.custom.ProgressLineView;
import mint.woman.female.workout.fragment.PlayFragment;
import mint.woman.female.workout.fragment.RestFragment;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.HistoryItem;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.model.Tip;
import mint.woman.female.workout.utils.AudioAppManager;
import mint.woman.female.workout.utils.FitnessConstUtils;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressLint({"SetTextI18n", "NonConstantResourceId"})
public class DoWorkoutActivity extends AppCompatActivity implements RestFragment.OnRestFragmentInteractionListener, PlayFragment.OnReadyFragmentInteractionListener, DialogQuit.OnQuitListener {
    private final SimpleDateFormat mFormatDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    public static int discover = 0;
    private  final String support_language ="ar@de@en@es@fa@fr@in_ID@it@ja@ko@nl@pl@pt@ru@tr@zh_CN@zh_TW";
    PlanObject mPlanObject;
    ProgramObject mProgramObject;
    Bundle mBundle;
    int currentExerciseCount = 0, totalExercise;
    long startPause, endPause, pauseTime, startExercise, endExercise, frequency_ads = 10;
    MediaPlayer musicPlayer, ringPlayer, tickPlayer;
    MyPreference myPreference;
    @BindView(R.id.workout_progress)
    ProgressLineView mProgressLineView;
    boolean mIsReady = false;
    DialogQuit mQuitDialog;
    int mFlagAds, mDayIndex;
    @BindView(R.id.img_workout)
    ImageView mWorkoutImg;
    @BindView(R.id.txt_progress)
    TextView mProgress;
    @BindView(R.id.txt_workout_time)
    TextView mWorkoutTime;

    HashMap<Integer,ExerciseObject> hashMap;

    WorkoutViewModel mWorkoutViewModel;
    HistoryItem mHistoryItem;
    long startTime = 0;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            mWorkoutTime.setText(String.format("%02d:%02d", minutes, seconds));

            timerHandler.postDelayed(this, 500);
        }
    };
    AudioAppManager mAudioAppManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_workout);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initView();
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

    @Override
    protected void onDestroy() {

        if (mAudioAppManager != null) {
            mAudioAppManager.release();
        }
        if (musicPlayer != null) {
            musicPlayer.release();
        }
        if (tickPlayer != null) {
            tickPlayer.release();
        }
        timerHandler.removeCallbacks(timerRunnable);
        super.onDestroy();

    }


    private void initView() {
        myPreference = new MyPreference(this);
        mWorkoutViewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);
        mWorkoutViewModel.getAllHistory().observe(this, historyItems -> {
            for (HistoryItem historyItem : historyItems
            ) {
                Log.d("HAHA", " name:" + historyItem.getName() + " calories:" + historyItem.getCalories());
            }

        });


        frequency_ads = myPreference.openCount() > 10 ? 5 : FitnessConstUtils.FREQUENCY_AD;
        mBundle = getIntent().getExtras();
        if (mBundle != null) {
            mPlanObject = mBundle.getParcelable("PLAN_OBJECT");
            int id = mBundle.getInt("PROGRAM_ID",0);
            mProgramObject = FitnessApplication.getApplication().getProgram(id);
            discover = mProgramObject.getDiscover();
            mDayIndex = mBundle.getInt("DAY_NUMBER", 0);
            currentExerciseCount = mBundle.getInt("PROGRESS", 0);
        }
        discover = mProgramObject.getDiscover();
        hashMap = FitnessApplication.getApplication().mJsonHelper.getHashMapExercise(discover);

        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);


        totalExercise = mPlanObject.getActionList().size();
        Glide.with(this).load(Uri.parse("file:///android_asset/images/img_rest.webp")).into(mWorkoutImg);

        mProgressLineView.setProgress((float) currentExerciseCount);
        mProgressLineView.setMax(mPlanObject.getActionList().size());


        mIsReady = false;
        mAudioAppManager = new AudioAppManager(DoWorkoutActivity.this, myPreference.getLanguage());

        mWorkoutViewModel.isPause().observe(this, isPause -> {
            if (isPause) {
                if (mIsReady) {
                    startPause = Calendar.getInstance().getTimeInMillis();
                }
                try {
                    RestFragment restFragment = (RestFragment) getSupportFragmentManager().findFragmentByTag("Rest");
                    if (restFragment != null) {
                        restFragment.onPauseRest();
                    }
                    PlayFragment readyFragment = (PlayFragment) getSupportFragmentManager().findFragmentByTag("Ready");
                    if (readyFragment != null) {
                        readyFragment.onPauseReady();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mAudioAppManager.pause();
            } else {
                if (mIsReady) {
                    endPause = Calendar.getInstance().getTimeInMillis();
                    pauseTime += (endPause - startPause);
                    // startPause = endPause = 0;
                }
                try {

                    RestFragment restFragment = (RestFragment) getSupportFragmentManager().findFragmentByTag("Rest");
                    if (restFragment != null) {
                        restFragment.onResumeRest();
                    }
                    PlayFragment readyFragment = (PlayFragment) getSupportFragmentManager().findFragmentByTag("Ready");
                    if (readyFragment != null) {
                        readyFragment.onResumeReady();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mAudioAppManager.resume();
            }
        });
        mWorkoutViewModel.getMusicSong().observe(this, musicSong -> {
            if (musicPlayer != null) {
                musicPlayer.release();
            }
            int resourceId = getResources().getIdentifier(musicSong, "raw", getPackageName());
            if (resourceId > 0) {
                musicPlayer = MediaPlayer.create(this, resourceId);
                musicPlayer.setLooping(true);
                musicPlayer.setVolume(myPreference.getMusicVolume(), myPreference.getMusicVolume());
                musicPlayer.start();
            }
        });
        mWorkoutViewModel.getMusicVolume().observe(this, volume -> {
            if (musicPlayer != null) {
                musicPlayer.release();
            }
            int resourceId = getResources().getIdentifier(myPreference.getMusicName(), "raw", getPackageName());
            if (resourceId > 0) {
                musicPlayer = MediaPlayer.create(this, resourceId);
                musicPlayer.setLooping(true);
                musicPlayer.setVolume(myPreference.getMusicVolume(), myPreference.getMusicVolume());
                musicPlayer.start();
            }
        });
        mWorkoutViewModel.isMusicOn().observe(this, isMusicOn -> {
            if (isMusicOn) {
                if (musicPlayer != null) {
                    musicPlayer.release();
                }
                int songId = getResources().getIdentifier(myPreference.getMusicName(), "raw", getPackageName());
                musicPlayer = MediaPlayer.create(this, songId);
                musicPlayer.setLooping(true);
                musicPlayer.setVolume(myPreference.getMusicVolume(), myPreference.getMusicVolume());
                musicPlayer.start();
            } else {
                if (musicPlayer != null) {
                    musicPlayer.release();
                }
            }
        });

        createHistory();
        mQuitDialog = new DialogQuit();
        mQuitDialog.setQuitListener(this);
        mProgress.setText("" + (currentExerciseCount + 1) + "/" + totalExercise);
        String progress = getString(R.string.txt_next_of_exercise) + " " + (currentExerciseCount + 1) + "/" + totalExercise;
        mAudioAppManager.setMale(myPreference.getGender() == 0);
        if (myPreference.isSpeakerOn()) {
            mAudioAppManager.playText(getString(R.string.txt_ready_to_go_the_next));
            if (getActionObjectCurrent().unit.contains("s")) {
                mAudioAppManager.playText("" + getActionObjectCurrent().time);
                mAudioAppManager.playText(getString(R.string.txt_seconds));
            } else {
                mAudioAppManager.playText("" + getActionObjectCurrent().time);
            }
            mAudioAppManager.playText(getExerciseObjectCurrent().getName());
            if (getExerciseObjectCurrent().getAlternation()&& getActionObjectCurrent().unit.isEmpty()){
                mAudioAppManager.playText(""+getActionObjectCurrent().time/2);
                mAudioAppManager.playText(getString(R.string.td_each_side));
            }
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.content_workout, RestFragment.newInstance( mPlanObject.mActionList.get(currentExerciseCount), 123, progress), "Rest").commitAllowingStateLoss();

    }

    @Override
    public void onBackPressed() {
        mWorkoutViewModel.setPause(true);
        if (!mQuitDialog.isAdded()) {
            try {
                mQuitDialog.show(getSupportFragmentManager(), "Quit");
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            super.onBackPressed();
        }


        //  super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mProgress.setText("" + (currentExerciseCount + 1) + "/" + totalExercise);
        mProgressLineView.setProgress(currentExerciseCount);
    }


    private void createHistory() {
        mHistoryItem = new HistoryItem();
        if (mProgramObject.getTotal() != 2) {
            mHistoryItem.setName(mProgramObject.getName() + " - " + getString(R.string.txt_day) + " " + (mDayIndex + 1));
        } else {
            mHistoryItem.setName(mProgramObject.getName() + " - Level " + mProgramObject.getLevel());
        }
        mHistoryItem.setImage(mProgramObject.getImage());
        mHistoryItem.setStartTime(Calendar.getInstance().getTimeInMillis());
        mHistoryItem.setDate(TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTimeInMillis()));
        mHistoryItem.setStart(mFormatDateTime.format(Calendar.getInstance().getTimeInMillis()));
        mHistoryItem.setProgramId(mProgramObject.getId());
        mHistoryItem.setDayIndex(mDayIndex);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (musicPlayer != null) {
            musicPlayer.release();
        }

        if (currentExerciseCount < totalExercise) {
            mWorkoutViewModel.setPause(true);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFlagAds == 1) {
            mFlagAds = 0;
            previousExercise();
        } else if (mFlagAds == 2) {
            mFlagAds = 0;
            onFinishReady();
        } else if (mFlagAds == 3) {
            mFlagAds = 0;
            finish();
        }
        if (myPreference.isMusicOn()) {
            if (musicPlayer != null) {
                musicPlayer.release();
            }
            int songId = getResources().getIdentifier(myPreference.getMusicName(), "raw", getPackageName());
            musicPlayer = MediaPlayer.create(this, songId);
            musicPlayer.setLooping(true);
            musicPlayer.setVolume(myPreference.getMusicVolume(), myPreference.getMusicVolume());
            musicPlayer.start();
        }
        if (currentExerciseCount < totalExercise) {
            mWorkoutViewModel.setPause(false);
        }
    }

    @Override
    public void onFinishRest() {
        mIsReady = true;
        if (myPreference.isRingOn()) {
            if (ringPlayer != null) {
                ringPlayer.release();
            }
            ringPlayer = MediaPlayer.create(this, R.raw.start);
            ringPlayer.setVolume(0.6f, 0.6f);
            ringPlayer.setLooping(false);
            ringPlayer.start();
        }
        pauseTime = 0;
        startExercise = Calendar.getInstance().getTimeInMillis();
        getSupportFragmentManager().beginTransaction().replace(R.id.content_workout, PlayFragment.newInstance(mPlanObject.getActionList().get(currentExerciseCount), currentExerciseCount, totalExercise), "Ready").commitAllowingStateLoss();
        if (myPreference.isSpeakerOn()) {
            mAudioAppManager.addTextAndClear(getString(R.string.txt_start));
            mAudioAppManager.playText("" + getActionObjectCurrent().time);
            if (!getActionObjectCurrent().unit.isEmpty()) {
                mAudioAppManager.playText(getString(R.string.txt_seconds));
                mAudioAppManager.playTextWithListener(getExerciseObjectCurrent().getName(),() -> {
                    Log.d("HAHA", "FINISH audio secons!");
                    PlayFragment playFragment = (PlayFragment) getSupportFragmentManager().findFragmentByTag("Ready");
                    if (playFragment != null) {
                        playFragment.playExercise();
                    }
                });
            } else if (getExerciseObjectCurrent().getAlternation() && getActionObjectCurrent().unit.isEmpty()) {
                mAudioAppManager.playText(getExerciseObjectCurrent().getName());
                mAudioAppManager.playText("" + getActionObjectCurrent().time / 2);
                mAudioAppManager.playTextWithListener(getString(R.string.td_each_side), () -> {
                    Log.d("HAHA", "FINISH audio! each side");
                    PlayFragment playFragment = (PlayFragment) getSupportFragmentManager().findFragmentByTag("Ready");
                    if (playFragment != null) {
                        playFragment.playExercise();
                    }
                    if (myPreference.getCountOption() ==0){
                        playTip();
                    }
                });
            } else {
                mAudioAppManager.playTextWithListener(getExerciseObjectCurrent().getName(), () -> {
                    Log.d("HAHA", "FINISH audio! one side");
                    PlayFragment playFragment = (PlayFragment) getSupportFragmentManager().findFragmentByTag("Ready");
                    if (playFragment != null) {
                        playFragment.playExercise();
                    }
                    if (myPreference.getCountOption() ==0){
                        playTip();
                    }
                });
            }

        }

    }


    @Override
    public void updateRestTime(int time) {
        try {
            if ((myPreference.isSpeakerOn())) {
                if (time < 4 && time > 0) {
                    mAudioAppManager.playText("" + time);
                }
            }
            if (myPreference.isTick()) {
                if (tickPlayer != null) {
                    tickPlayer.release();
                }
                tickPlayer = MediaPlayer.create(this, R.raw.tick);
                tickPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void startRestTime(int time) {


    }

    @Override
    public void updateReady(int timeLeft) {
        mIsReady = true;
        if (myPreference.isSpeakerOn()) {

            if (getActionObjectCurrent().unit.isEmpty()) {
                mAudioAppManager.playText("" + timeLeft);
                Log.d("HAHA", "time left= " + timeLeft);
                if (myPreference.isTick()) {
                    if (tickPlayer != null) {
                        tickPlayer.release();
                    }
                    tickPlayer = MediaPlayer.create(this, R.raw.beep);
                    tickPlayer.start();
                }
            } else {
                if (timeLeft == getActionObjectCurrent().time / 2) {
                    mAudioAppManager.playText(getString(R.string.td_v_half_time));
                }
                if (getActionObjectCurrent().time - timeLeft == 5) {
                    playTip();
                }
                if (myPreference.isTick()) {
                    if (tickPlayer != null) {
                        tickPlayer.release();
                    }
                    tickPlayer = MediaPlayer.create(this, R.raw.tick);
                    tickPlayer.start();
                }
            }

        }


    }

    void playTip() {
        String lang = myPreference.getLanguage();
        if (lang.length()>2){
            lang = lang.substring(0,2);
        }
        if (support_language.contains(lang)){
            List<Tip> tips = getExerciseObjectCurrent().getTips();
            if (!tips.isEmpty()) {
                int rand = new Random().nextInt(tips.size());
                mAudioAppManager.playText(tips.get(rand).getText());
            }
        }

    }


    @Override
    public void updateProgressWorkout(float progress) {
        mProgressLineView.setProgress(currentExerciseCount + progress);
    }

    String toTitleCase(String input) {
        StringBuilder titleCase = new StringBuilder(input.length());
        boolean nextTitleCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }

            titleCase.append(c);
        }

        return titleCase.toString();
    }
    @Override
    public void onFinishReady() {


        if (myPreference.isRingOn()) {
            if (ringPlayer != null) {
                ringPlayer.release();
            }
            ringPlayer = MediaPlayer.create(this, R.raw.start);
            ringPlayer.setLooping(false);
            ringPlayer.start();
        }
        endExercise = Calendar.getInstance().getTimeInMillis();
        int durationTimeTemp = (int) Math.abs((endExercise - startExercise - pauseTime) / 1000);
        // pauseTime = 0;
        mHistoryItem.addDurationTime(durationTimeTemp);
        pauseTime = 0;
        mIsReady = false;

        if (currentExerciseCount < totalExercise - 1) {
            currentExerciseCount++;
            mProgressLineView.setProgress(currentExerciseCount);


            mProgress.setText("" + (currentExerciseCount + 1) + "/" + totalExercise);
            String progress = getString(R.string.txt_next_of_exercise) + " " + (currentExerciseCount + 1) + "/" + totalExercise;
            int rest = myPreference.getRestTime() + 1;
            if (myPreference.isSpeakerOn()) {
                mAudioAppManager.addTextAndClear(getString(R.string.txt_take_a_rest_the_next));
                mAudioAppManager.playText("" + getActionObjectCurrent().time);
                if (!getActionObjectCurrent().unit.isEmpty()){
                    mAudioAppManager.playText(getString(R.string.txt_seconds));
                }
                mAudioAppManager.playText(getExerciseObjectCurrent().getName());
                if (getExerciseObjectCurrent().getAlternation()&& getActionObjectCurrent().unit.isEmpty()){
                    mAudioAppManager.playText(""+getActionObjectCurrent().time/2);
                    mAudioAppManager.playText(getString(R.string.td_each_side));
                }
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.content_workout, RestFragment.newInstance( mPlanObject.mActionList.get(currentExerciseCount), rest, progress), "Rest").commitAllowingStateLoss();
        } else {
            myPreference.saveProgressWorkout(mProgramObject.getId(), mDayIndex, 0);
            myPreference.setSaveProgressPercent(mProgramObject.getId(), mDayIndex, 1);
            mHistoryItem.setEndTime(Calendar.getInstance().getTimeInMillis());
            float cal = (mHistoryItem.getDuration() / 3600.0f) * FitnessConstUtils.CALORIES_CONST * (myPreference.getCurrentWeightKG() / 65.0f);
            mHistoryItem.setCalories((int) cal);
            mHistoryItem.setPassed(true);
            mWorkoutViewModel.insertHistory(mHistoryItem);
            mWorkoutViewModel.insertDate(mHistoryItem.getDate());
            if (mDayIndex + 1 > myPreference.getPlanProgress(mProgramObject.getId())) {
                myPreference.setPlanProgress(mProgramObject.getId(), mDayIndex + 1);
            }

            Intent homeIntent = new Intent(DoWorkoutActivity.this, YogaActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
            Intent historyIntent = new Intent(DoWorkoutActivity.this, ReportActivity.class);
            startActivity(historyIntent);
            Intent intent = new Intent(DoWorkoutActivity.this, FinishedActivity.class);
            mBundle.putParcelable("HISTORY", mHistoryItem);
            intent.putExtras(mBundle);
            startActivity(intent);
            finish();
        }
    }


    @Override
    public void onNextExercise() {
        onFinishReady();
    }

    @Override
    public void onPreviousExercise() {
        previousExercise();
    }

    private void previousExercise() {
        if (currentExerciseCount > 0) {
            currentExerciseCount--;
            mProgressLineView.setProgress(currentExerciseCount);
            String progress = getString(R.string.txt_next_of_exercise) + " " + (currentExerciseCount + 1) + "/" + totalExercise;
            int rest = myPreference.getRestTime();

            getSupportFragmentManager().beginTransaction().replace(R.id.content_workout, RestFragment.newInstance(   getActionObjectCurrent(), rest, progress), "Rest").commitAllowingStateLoss();
        }
    }

    @Override
    public void onTextToSpeech(String voice) {

    }


    @OnClick(R.id.img_back_activity)
    public void onClick(View view) {
        onBackPressed();
    }


    private ExerciseObject getExerciseObject(PlanObject.ActionObject actionObject) {
        return hashMap.get(actionObject.actionId);
    }

    private ExerciseObject getExerciseObjectCurrent() {
        return hashMap.get( mPlanObject.mActionList.get(currentExerciseCount).actionId);
    }

    private PlanObject.ActionObject getActionObjectCurrent() {
        return mPlanObject.mActionList.get(currentExerciseCount);
    }


    @Override
    public void onQuitWorkout() {
        mWorkoutViewModel.setPause(false);
        mHistoryItem.setEndTime(Calendar.getInstance().getTimeInMillis());
        mHistoryItem.setPassed(false);
        float cal = (mHistoryItem.getDuration() / 3600.0f) * FitnessConstUtils.CALORIES_CONST * (myPreference.getCurrentWeightKG() / 65.0f);
        mHistoryItem.setCalories((int) cal);
        mWorkoutViewModel.insertHistory(mHistoryItem);
        mWorkoutViewModel.insertDate(mHistoryItem.getDate());
        myPreference.setSaveProgress(mProgramObject.getId(), mDayIndex, currentExerciseCount);
        float percent = (float) currentExerciseCount / totalExercise;
        Log.d("HAHA", "save percent =" + percent);
        myPreference.setSaveProgressPercent(mProgramObject.getId(), mDayIndex, percent);
        finish();
    }

    @Override
    public void onNoQuitWorkout() {
        mWorkoutViewModel.setPause(false);
        try {
            RestFragment restFragment = (RestFragment) getSupportFragmentManager().findFragmentByTag("Rest");
            if (restFragment != null) {
                restFragment.onResumeRest();
            }
            PlayFragment readyFragment = (PlayFragment) getSupportFragmentManager().findFragmentByTag("Ready");
            if (readyFragment != null) {
                readyFragment.onResumeReady();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
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
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Hide the nav bar and status bar4
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
//    private void showSystemUI() {
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//    }
}
