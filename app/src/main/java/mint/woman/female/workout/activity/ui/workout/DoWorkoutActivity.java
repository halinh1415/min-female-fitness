package mint.woman.female.workout.activity.ui.workout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;

import mint.woman.female.workout.activity.ui.history.HistoryActivity;
import mint.woman.female.workout.utils.EdgeToEdgeHelper;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.graphics.Insets;
import android.graphics.Color;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.utils.WorkoutDataValidation;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.main.YogaActivity;
import mint.woman.female.workout.activity.ui.report.ReportActivity;
import mint.woman.female.workout.admodule.MaticAd;
import mint.woman.female.workout.admodule.MaticAdCallBack;
import mint.woman.female.workout.custom.DialogQuit;
import mint.woman.female.workout.databinding.ActivityDoWorkoutBinding;
import mint.woman.female.workout.fragment.PlayFragment;
import mint.woman.female.workout.fragment.RestFragment;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.HistoryItem;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.model.Tip;
import mint.woman.female.workout.utils.FitnessConstUtils;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.WorkoutVideoCache;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressLint({"SetTextI18n", "NonConstantResourceId"})
public class DoWorkoutActivity extends AppCompatActivity implements RestFragment.OnRestFragmentInteractionListener, PlayFragment.OnReadyFragmentInteractionListener, DialogQuit.OnQuitListener, EdgeToEdgeHelper.HandlesInsets {
    private final OnBackPressedCallback mBackCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            handleBack();
        }
    };

    private final SimpleDateFormat mFormatDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    public static int discover = 0;
    public static int version = 1003;
    private final String support_language = "ar@de@en@es@fa@fr@in_ID@it@ja@ko@nl@pl@pt@ru@tr@zh_CN@zh_TW";

    ActivityDoWorkoutBinding binding;

    ProgramObject mProgramObject;
    Bundle mBundle;
    int currentExerciseCount = 0, totalExercise;
    long startPause, endPause, pauseTime, startExercise, endExercise;
    /** Nhạc nền: chuẩn bị bất đồng bộ, giải phóng ở luồng nền (prepare/release đồng bộ trên main dễ gây ANR). */
    MediaPlayer musicPlayer;
    private String musicPlayerPath;
    private boolean musicPrepared;
    /** Tiếng chuông / tick / beep: nạp sẵn một lần, phát không chặn main thread (trước đây tạo MediaPlayer mỗi giây). */
    private SoundPool soundPool;
    private int soundStart, soundTick, soundBeep;
    private static final ExecutorService MEDIA_RELEASE = Executors.newSingleThreadExecutor();
    MyPreference myPreference;

    boolean mIsReady = false;
    DialogQuit mQuitDialog;
    int mFlagAds, mDayIndex;


    HashMap<Integer, ExerciseObject> hashMap;
    List<PlanObject.ActionObject> actionObjectList;

    WorkoutViewModel mWorkoutViewModel;
    HistoryItem mHistoryItem;
    long startTime = 0;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            binding.txtWorkoutTime.setText(String.format("%02d:%02d", minutes, seconds));

            timerHandler.postDelayed(this, 500);
        }
    };
    AudioAppManager mAudioAppManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getOnBackPressedDispatcher().addCallback(this, mBackCallback);
        binding = ActivityDoWorkoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyInsets();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initSounds();
        initView();
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

    @Override
    protected void onDestroy() {

        if (mAudioAppManager != null) {
            mAudioAppManager.release();
        }
        stopMusic();
        if (soundPool != null) {
            SoundPool pool = soundPool;
            soundPool = null;
            MEDIA_RELEASE.execute(pool::release);
        }
        timerHandler.removeCallbacks(timerRunnable);
        if (isFinishing()) {
            WorkoutVideoCache.clear(this);
        }
        super.onDestroy();

    }


    private void initView() {
        myPreference = new MyPreference(this);
        mWorkoutViewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);
        mBundle = getIntent().getExtras();
        binding.imgBackActivity.setOnClickListener(view -> {
            onBackPressed();
        });
        if (mBundle != null) {
            int id = mBundle.getInt("PROGRAM_ID", 0);
            mProgramObject = FitnessApplication.getApplication().getProgram(id);
            if (mProgramObject == null) {
                WorkoutDataValidation.report(this, "workout", id, -1, -1,
                        mBundle.getInt("DAY_NUMBER", -1), -1, -1, "missing_program");
                finish();
                return;
            }
            discover = mProgramObject.getDiscover();
            version = mProgramObject.getVersion();
            mDayIndex = mBundle.getInt("DAY_NUMBER", 0);
            currentExerciseCount = mBundle.getInt("PROGRESS", 0);
            actionObjectList = mBundle.getParcelableArrayList("ACTION_LIST");
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.setCustomKey("PROGRAM_ID", mProgramObject.getId());
            crashlytics.setCustomKey("ACTION_SIZE", actionObjectList == null ? 0 : actionObjectList.size());
            discover = mProgramObject.getDiscover();
            hashMap = FitnessApplication.getApplication().mJsonHelper.getHashMapExercise(discover, mProgramObject.getVersion());
            if (!WorkoutDataValidation.validate(this, "workout", mProgramObject.getId(), discover, version,
                    mDayIndex, actionObjectList, hashMap)) {
                finish();
                return;
            }

            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
            totalExercise = actionObjectList.size();
            String progress = getString(R.string.txt_next_of_exercise) + " " + (currentExerciseCount + 1) + "/" + totalExercise;

            Glide.with(this).load(Uri.parse("file:///android_asset/images/img_rest.webp")).into(binding.imgWorkout);


            if (currentExerciseCount >= actionObjectList.size()) {
                currentExerciseCount = 0;
            }
            mAudioAppManager = new AudioAppManager(DoWorkoutActivity.this, myPreference.getLanguage());
            binding.workoutProgress.setProgress((float) currentExerciseCount);
            binding.workoutProgress.setMax(actionObjectList.size());

            mIsReady = false;


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
                changeMusic();
            });
            mWorkoutViewModel.getMusicVolume().observe(this, volume -> {
                changeMusic();
            });
            mWorkoutViewModel.isMusicOn().observe(this, isMusicOn -> {
                changeMusic();
            });

            createHistory();
            try {
                binding.txtProgress.setText("" + (currentExerciseCount + 1) + "/" + totalExercise);

                if (myPreference.isSpeakerOn()) {
                   // mAudioAppManager.playText(getString(R.string.txt_ready_to_go_the_next));
                    if (getActionObjectCurrent().getUnit().contains("s")) {
                        mAudioAppManager.playText(getString(R.string.wp_ready_to_go));
                        mAudioAppManager.playText(getString(R.string.v_start_with));
                        mAudioAppManager.playText(""+getActionObjectCurrent().time);
                        mAudioAppManager.playText(getString(R.string.wp_seconds));

                    } else {
                        mAudioAppManager.playText(getString(R.string.wp_ready_to_go));
                        mAudioAppManager.playText(getString(R.string.v_start_with));
                        mAudioAppManager.playText(""+getActionObjectCurrent().time);
                    }
                    mAudioAppManager.playText(getExerciseObjectCurrent().getName());
                    if (getExerciseObjectCurrent().getAlternation() && getActionObjectCurrent().getUnit().isEmpty()) {
                        mAudioAppManager.playText("" + getActionObjectCurrent().time / 2);
                        mAudioAppManager.playText(getString(R.string.wp_each_side));
                    }
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.content_workout, RestFragment.newInstance(getActionObjectCurrent(), 123, progress), "Rest").commitAllowingStateLoss();
            }catch (Exception e){
                e.printStackTrace();
                crashlytics.setCustomKey("CRASH_EX", e.toString());
            }
        }



    }
    void changeMusic() {
        startMusic();
    }

    private void initSounds() {
        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();
        soundStart = soundPool.load(this, R.raw.start, 1);
        soundTick = soundPool.load(this, R.raw.tick, 1);
        soundBeep = soundPool.load(this, R.raw.beep, 1);
    }

    private void playEffect(int soundId, float volume) {
        if (soundPool != null && soundId != 0) {
            soundPool.play(soundId, volume, volume, 1, 0, 1f);
        }
    }

    /** Bật / đổi / chỉnh âm lượng nhạc nền theo cài đặt; gọi lại nhiều lần không tạo lại player. */
    private void startMusic() {
        String musicPath = getFilesDir() + "/music/" + myPreference.getMusicFile();
        if (!myPreference.isMusicOn() || !existsData(musicPath)) {
            stopMusic();
            return;
        }
        float volume = myPreference.getMusicVolume();
        if (musicPlayer != null && musicPath.equals(musicPlayerPath)) {
            // Đang prepare thì onPrepared sẽ tự đọc âm lượng mới.
            if (musicPrepared) musicPlayer.setVolume(volume, volume);
            return;
        }
        stopMusic();
        MediaPlayer player = new MediaPlayer();
        musicPlayer = player;
        musicPlayerPath = musicPath;
        musicPrepared = false;
        try {
            player.setDataSource(musicPath);
            player.setLooping(true);
            player.setOnPreparedListener(mp -> {
                if (mp != musicPlayer || isFinishing()) return;
                musicPrepared = true;
                float v = myPreference.getMusicVolume();
                mp.setVolume(v, v);
                mp.start();
            });
            player.setOnErrorListener((mp, what, extra) -> {
                if (mp == musicPlayer) stopMusic();
                return true;
            });
            player.prepareAsync();
        } catch (Exception e) {
            Log.w("DoWorkoutActivity", "music failed: " + e.getMessage());
            stopMusic();
        }
    }

    private void stopMusic() {
        MediaPlayer player = musicPlayer;
        musicPlayer = null;
        musicPlayerPath = null;
        musicPrepared = false;
        if (player != null) {
            MEDIA_RELEASE.execute(() -> {
                try {
                    player.release();
                } catch (Exception ignored) {
                }
            });
        }
    }

    private boolean existsData(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }
    boolean quitShow = false;

    /**
     * Nút Back (cả cử chỉ Back của Android 16 / SDK 36: predictive back không gọi onBackPressed()).
     * Đăng ký qua OnBackPressedDispatcher trong onCreate; gọi onBackPressed() trong code vẫn chạy vào đây.
     */
    private void handleBack() {
        mWorkoutViewModel.setPause(true);
        if (!quitShow){
            showQuitDialog();
        }else{
            finishBack();
        }


        //  finishBack();
    }

    /** Thoát như super.onBackPressed() cũ: tạm tắt callback để dispatcher xử lý mặc định. */
    private void finishBack() {
        mBackCallback.setEnabled(false);
        getOnBackPressedDispatcher().onBackPressed();
        mBackCallback.setEnabled(true);
    }
    void showQuitDialog() {
        try {
            if (isFinishing() || isDestroyed()) return;
            if ((mQuitDialog != null && mQuitDialog.isAdded())
                    || getSupportFragmentManager().findFragmentByTag("Quit") != null) return;
            // Tạo mới mỗi lần để hộp thoát hiện đúng tiến độ hiện tại.
            long elapsedSec = startTime > 0 ? (System.currentTimeMillis() - startTime) / 1000 : 0;
            mQuitDialog = DialogQuit.newInstance(mProgramObject != null ? mProgramObject.getId() : 0, mDayIndex,
                    currentExerciseCount, totalExercise, elapsedSec);
            mQuitDialog.setQuitListener(this);
            quitShow = true;
            // Không gọi executePendingTransactions trong KeyEvent: các transaction bài tập kế tiếp
            // (inflate + decode ảnh) có thể gây ANR.
            mQuitDialog.show(getSupportFragmentManager(), "Quit");
        } catch (Exception e) {
            quitShow = false;
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        binding.txtProgress.setText("" + (currentExerciseCount + 1) + "/" + totalExercise);
        binding.workoutProgress.setProgress(currentExerciseCount);
    }


    private void createHistory() {
        mHistoryItem = new HistoryItem();
        if (mProgramObject.getTotal() != 2) {
            mHistoryItem.setName(mProgramObject.getName() + " - " + getString(R.string.txt_day) + " " + (mDayIndex + 1));
        } else {
            mHistoryItem.setName(mProgramObject.getName() + " - Level " + mProgramObject.getLevel());
        }
        mHistoryItem.setStartTime(Calendar.getInstance().getTimeInMillis());
        mHistoryItem.setDateId(Calendar.getInstance().getTimeInMillis());
        mHistoryItem.setStart(mFormatDateTime.format(Calendar.getInstance().getTimeInMillis()));
        mHistoryItem.setProgramId(mProgramObject.getId());
        mHistoryItem.setDayIndex(mDayIndex);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopMusic();

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
        startMusic();
        if (currentExerciseCount < totalExercise) {
            mWorkoutViewModel.setPause(false);
        }
    }

    @Override
    public void onFinishRest() {
        mIsReady = true;
        if (myPreference.isRingOn()) {
            playEffect(soundStart, 0.6f);
        }
        pauseTime = 0;
        startExercise = Calendar.getInstance().getTimeInMillis();
        getSupportFragmentManager().beginTransaction().replace(R.id.content_workout, PlayFragment.newInstance(getActionObjectCurrent(), currentExerciseCount, totalExercise), "Ready").commitAllowingStateLoss();
        if (myPreference.isSpeakerOn()) {
            mAudioAppManager.addTextAndClear(getString(R.string.wp_start));
            mAudioAppManager.playText("" + getActionObjectCurrent().time);
            if (!getActionObjectCurrent().getUnit().isEmpty()) {
                mAudioAppManager.playText(getString(R.string.wp_seconds));
                mAudioAppManager.playTextWithListener(getExerciseObjectCurrent().getName(), () -> {

                    PlayFragment playFragment = (PlayFragment) getSupportFragmentManager().findFragmentByTag("Ready");
                    if (playFragment != null) {
                        playFragment.playExercise();
                    }
                });
            } else if (getExerciseObjectCurrent().getAlternation() && getActionObjectCurrent().getUnit().isEmpty()) {
                mAudioAppManager.playText(getExerciseObjectCurrent().getName());
                mAudioAppManager.playText("" + getActionObjectCurrent().time / 2);
                mAudioAppManager.playTextWithListener(getString(R.string.wp_each_side), () -> {

                    PlayFragment playFragment = (PlayFragment) getSupportFragmentManager().findFragmentByTag("Ready");
                    if (playFragment != null) {
                        playFragment.playExercise();
                    }
                    if (myPreference.getCountOption() == 0) {
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
                    if (myPreference.getCountOption() == 0) {
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
                playEffect(soundTick, 1f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void updateReady(int timeLeft) {
        mIsReady = true;
        if (myPreference.isSpeakerOn()) {

            if (getActionObjectCurrent().getUnit().isEmpty()) {
                mAudioAppManager.playText("" + timeLeft);
                Log.d("HAHA", "time left= " + timeLeft);
                if (myPreference.isTick()) {
                    playEffect(soundBeep, 1f);
                }
            } else {
                if (timeLeft == getActionObjectCurrent().time / 2) {
                    mAudioAppManager.playText(getString(R.string.wp_v_half_time));
                }
                if (getActionObjectCurrent().time - timeLeft == 5) {
                    playTip();
                }
                if (myPreference.isTick()) {
                    playEffect(soundTick, 1f);
                }
            }

        }


    }

    void playTip() {
        String lang = myPreference.getLanguage();
        if (lang.length() > 2) {
            lang = lang.substring(0, 2);
        }
        if (support_language.contains(lang)) {
            List<Tip> tips = getExerciseObjectCurrent().getTips();
            if (!tips.isEmpty()) {
                int rand = new Random().nextInt(tips.size());
                mAudioAppManager.playText(tips.get(rand).getText());
            }
        }

    }


    @Override
    public void updateProgressWorkout(float progress) {
        binding.workoutProgress.setProgress(currentExerciseCount + progress);
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
            playEffect(soundStart, 1f);
        }
        endExercise = Calendar.getInstance().getTimeInMillis();
        int durationTimeTemp = (int) Math.abs((endExercise - startExercise - pauseTime) / 1000);
        // pauseTime = 0;
        mHistoryItem.addDurationTime(durationTimeTemp);
        pauseTime = 0;
        mIsReady = false;

        if (currentExerciseCount < totalExercise - 1) {
            currentExerciseCount++;
            binding.workoutProgress.setProgress(currentExerciseCount);


            binding.txtProgress.setText("" + (currentExerciseCount + 1) + "/" + totalExercise);
            String progress = getString(R.string.txt_next_of_exercise) + " " + (currentExerciseCount + 1) + "/" + totalExercise;
            if (myPreference.isSpeakerOn()) {
                mAudioAppManager.addTextAndClear(getString(R.string.wp_have_a_rest));
                mAudioAppManager.playText("" + getActionObjectCurrent().time);
                if (!getActionObjectCurrent().getUnit().isEmpty()) {
                    mAudioAppManager.playText(getString(R.string.wp_seconds));
                }
                mAudioAppManager.playText(getExerciseObjectCurrent().getName());
                if (getExerciseObjectCurrent().getAlternation() && getActionObjectCurrent().getUnit().isEmpty()) {
                    mAudioAppManager.playText("" + getActionObjectCurrent().time / 2);
                    mAudioAppManager.playText(getString(R.string.wp_each_side));
                }
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.content_workout, RestFragment.newInstance(getActionObjectCurrent(),getActionObjectCurrent().rest >0?getActionObjectCurrent().rest: myPreference.getRestTime(), progress), "Rest").commitAllowingStateLoss();
        } else {
            myPreference.saveProgressWorkout(mProgramObject.getId(), mDayIndex, 0);
            myPreference.setSaveProgressPercent(mProgramObject.getId(), mDayIndex, 1);
            mHistoryItem.setEndTime(Calendar.getInstance().getTimeInMillis());

            float cal = (mHistoryItem.getDuration() / 3600.0f) * FitnessConstUtils.CALORIES_CONST * (myPreference.getCurrentWeightKG() / 65.0f);
            mHistoryItem.setCalories((int) cal);
            mHistoryItem.setPassed(true);
            mWorkoutViewModel.insertHistory(mHistoryItem);
            if (mDayIndex + 1 > myPreference.getPlanProgress(mProgramObject.getId()) && mProgramObject.getType()== 1) {
                myPreference.setPlanProgress(mProgramObject.getId(), mDayIndex + 1);
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user!= null){
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("programs");
                    userRef.child(String.valueOf(mProgramObject.getId())).setValue(mDayIndex+1);
                }
            }
            myPreference.setSaveProgress(mProgramObject.getId(), mDayIndex, 0);
            Intent homeIntent = new Intent(DoWorkoutActivity.this, YogaActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            homeIntent.putExtra("IS_REPORT", true);
            startActivity(homeIntent);
            Intent historyIntent = new Intent(DoWorkoutActivity.this, HistoryActivity.class);
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
            binding.workoutProgress.setProgress(currentExerciseCount);
            String progress = getString(R.string.txt_next_of_exercise) + " " + (currentExerciseCount + 1) + "/" + totalExercise;

            getSupportFragmentManager().beginTransaction().replace(R.id.content_workout, RestFragment.newInstance(getActionObjectCurrent(), getActionObjectCurrent().rest >0?getActionObjectCurrent().rest: myPreference.getRestTime(), progress), "Rest").commitAllowingStateLoss();
        }
    }

    @Override
    public void onTextToSpeech(String voice) {

    }





    private ExerciseObject getExerciseObjectCurrent() {
        return hashMap.get(getActionObjectCurrent().actionId);
    }

    private PlanObject.ActionObject getActionObjectCurrent() {
        if (currentExerciseCount < actionObjectList.size()){
            return actionObjectList.get(currentExerciseCount);
        }else {
            return actionObjectList.get(0);
        }
    }


    @Override
    public void onQuitWorkout() {
        quitShow = false;
        mWorkoutViewModel.setPause(false);
        mHistoryItem.setEndTime(Calendar.getInstance().getTimeInMillis());
        mHistoryItem.setPassed(false);
        float cal = (mHistoryItem.getDuration() / 3600.0f) * FitnessConstUtils.CALORIES_CONST * (myPreference.getCurrentWeightKG() / 65.0f);
        mHistoryItem.setCalories((int) cal);
        mWorkoutViewModel.insertHistory(mHistoryItem);
        myPreference.setSaveProgress(mProgramObject.getId(), mDayIndex, currentExerciseCount);
        float percent = (float) currentExerciseCount / totalExercise;
        myPreference.setSaveProgressPercent(mProgramObject.getId(), mDayIndex, percent);
        if (MaticAd.getInstance().isAvailableInter()){
            MaticAd.getInstance().showPriorityInterstitialAd(DoWorkoutActivity.this,new MaticAdCallBack(){
                @Override
                public void onNextAction() {
                    super.onNextAction();
                    finish();
                }
            });
        }else{
            finish();
        }

    }

    @Override
    public void onNoQuitWorkout() {
        quitShow = false;
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

    /**
     * Màn tập ẩn status bar/nav bar (immersive) nên tự xử lý inset, không để EdgeToEdgeHelper phủ dải
     * màu nền (trắng) lên chỗ status bar. Chỉ chừa vùng tai thỏ/camera (và thanh hệ thống nếu đang
     * hiện), tô cùng màu với nội dung đang hiển thị: trắng khi đang tập, đen khi nghỉ / chuẩn bị.
     */
    private void applyInsets() {
        View root = binding.getRoot();
        setBarAreaColor(false);
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentStarted(@NonNull FragmentManager fm, @NonNull Fragment f) {
                if (f.getId() == R.id.content_workout) setBarAreaColor(f instanceof PlayFragment);
            }
        }, false);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void setBarAreaColor(boolean light) {
        binding.getRoot().setBackgroundColor(light ? Color.WHITE : Color.BLACK);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightNavigationBars(light);
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
