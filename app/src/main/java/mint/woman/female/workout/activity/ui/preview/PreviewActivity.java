package mint.woman.female.workout.activity.ui.preview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONArray;
import org.json.JSONObject;
import mint.woman.female.workout.activity.ui.settings.CoachOptionActivity;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.custom.CustomMyWorkoutActivity;
import mint.woman.female.workout.utils.Database;
import mint.woman.female.workout.utils.DifficultyStore;
import mint.woman.female.workout.activity.ui.premium.PremiumActivity;
import mint.woman.female.workout.activity.ui.count.CountManagerActivity;
import mint.woman.female.workout.activity.ui.workout.DoWorkoutActivity;
import mint.woman.female.workout.activity.ui.workout.ExerciseDetailActivity;
import mint.woman.female.workout.activity.ui.workout.SelectExerciseActivity;
import mint.woman.female.workout.admodule.MaticAd;
import mint.woman.female.workout.admodule.MaticAdCallBack;
import mint.woman.female.workout.admodule.MaticAdKeys;
import mint.woman.female.workout.common.data.DemoDataProvider;
import mint.woman.female.workout.custom.DialogEditExercise;
import mint.woman.female.workout.custom.DialogEditWorkout;
import mint.woman.female.workout.custom.DialogUnlockTraining;
import mint.woman.female.workout.custom.EditExerciseListener;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.model.Tip;
import mint.woman.female.workout.retrofit.ApiUtils;
import mint.woman.female.workout.utils.AppEncrypt;
import mint.woman.female.workout.utils.DownloadAndUnzipWorker;
import mint.woman.female.workout.utils.DownloadVoiceWorker;
import mint.woman.female.workout.utils.FitnessConstUtils;
import mint.woman.female.workout.utils.JsonHelper;
import mint.woman.female.workout.utils.WorkoutDataValidation;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.RemoteConfigKeys;
import mint.woman.female.workout.utils.EdgeToEdgeHelper;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


@SuppressLint({"NonConstantResourceId", "DefaultLocale", "SetTextI18n"})
/**
 * Xem trước buổi tập. Giao diện Jetpack Compose ({@link PreviewScreen}); toàn bộ logic (tải dữ liệu,
 * mở khóa, quảng cáo, sửa bài) vẫn ở Activity này và chỉ cập nhật {@link PreviewUiState}.
 */
public class PreviewActivity extends AppCompatActivity implements PreviewActions, DialogEditWorkout.OnEditFragmentInteractionListener, EditExerciseListener, DialogUnlockTraining.OnUnlockTrainingListener, EdgeToEdgeHelper.HandlesInsets {

    private static final String TAG = "PreviewActivity";
    private final PreviewUiState state = new PreviewUiState();
    DemoDataProvider mDemoDataProvider;
    MyPreference myPreference;
    Bundle mBundle;
    boolean flagEdit = false, flagUnlock = false;
    int saveProgress = 0;
    private ProgramObject mProgramObject;
    private int mIndexDay, currentActionIndex;
    private JsonHelper mJsonHelper;
    private DialogUnlockTraining dialogUnlockTraining;
    private boolean isRewardedAdLoading = false;
    private Handler handler;
	 private List<Integer> mProgramLocks = new ArrayList<>();

    PreviewViewModel mPreviewViewModel;
    // Called when ad is dismissed.
// Don't forget to set the ad reference to null so you
// don't show the ad a second time.
    private RewardedAd mRewardedVideoAd = null;
    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Bundle bundle = result.getData().getExtras();
                    if (bundle != null) {
                        int id = bundle.getInt("actionId");
                        int time = bundle.getInt("time");
                        String unit = bundle.getString("unit");
                        mDemoDataProvider.replaceAction(currentActionIndex, new PlanObject.ActionObject(id, time, unit, 0, 10));
                        refreshItems();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Header là ảnh tối: icon status bar màu trắng.
        EdgeToEdge.enable(this, SystemBarStyle.dark(android.graphics.Color.TRANSPARENT));
        super.onCreate(savedInstanceState);
        PreviewCompose.attach(this, state, this);
        initView();

        loadInterStart();
    }

    private void loadInterStart() {
        if (myPreference.isShowAds() && myPreference.getOK() && FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.INTER_START)) {
            MaticAd.getInstance().loadInterstitialAd3SameTime(PreviewActivity.this, MaticAdKeys.INTER_START_HIGH, MaticAdKeys.INTER_START_MEDIUM, MaticAdKeys.INTER_START, new MaticAdCallBack());
        }
    }


    private void initView() {
        myPreference = new MyPreference(this);
        mBundle = getIntent().getExtras();
        handler = new Handler();
        mPreviewViewModel = new ViewModelProvider(this).get(PreviewViewModel.class);
        mProgramLocks.add(47);
        mProgramLocks.add(971);
        mProgramLocks.add(1110);
        mProgramLocks.add(1140);
        mProgramLocks.add(1150);
        mProgramLocks.add(1151);
        if (mBundle != null) {
            int id = mBundle.getInt("PROGRAM_ID", 0);
            mProgramObject = FitnessApplication.getApplication().getProgram(id);
            if (mProgramObject == null) {
                Toast.makeText(this, R.string.ch_removed, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            mIndexDay = mBundle.getInt("DAY_NUMBER");
            FitnessApplication.getApplication().setDiscover(mProgramObject.getDiscover());
            FitnessApplication.getApplication().setVersion(mProgramObject.getVersion());
            mJsonHelper = FitnessApplication.getApplication().mJsonHelper;
            state.setProgram(mProgramObject);
            state.setCanEdit(mProgramObject.getDiscover() == 0);
            if (mProgramObject.getType() == 1 || mProgramObject.getType() == 3) {
                state.setLevelText(getString(R.string.txt_day) + " " + (mIndexDay + 1));
            } else if (mProgramObject.getType() == 2) {
                state.setLevelText("Level " + mProgramObject.getLevel());
            }
            state.setMinutes(mProgramObject.getTotal());
            state.setButtonText(getString(R.string.txt_go_to));
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.setCustomKey("PROGRAM_ID", mProgramObject.getId());
            crashlytics.setCustomKey("VERSION", mProgramObject.getVersion());
            crashlytics.setCustomKey("PROGRAM_DAY", mIndexDay);
            crashlytics.setCustomKey("LANGUAGE", myPreference.getLanguage());
            mPreviewViewModel.getDownloadStatus().observe(PreviewActivity.this, downloadStatus -> {
                switch (downloadStatus) {
                    case DONE:
                        state.setButtonEnabled(true);
                        if (mDemoDataProvider != null) {
                            updateButton();
                        } else {
                            state.setButtonMode(PreviewButtonMode.GO);
                            state.setButtonText(getString(R.string.txt_go_to));
                            state.setButtonProgress(1f);
                            state.setShowRestart(false);
                        }
                        break;
                    case FAIL:
                        Toast.makeText(PreviewActivity.this,"Check your internet and retry!", Toast.LENGTH_SHORT).show();
                        state.setShowRestart(false);
                        state.setButtonEnabled(true);
                        state.setButtonProgress(1f);
                        state.setButtonMode(PreviewButtonMode.RETRY);
                        state.setButtonText(getString(R.string.retry));
                        break;
                    case NEED_DOWNLOAD:
                        state.setButtonProgress(1f);
                        state.setButtonMode(PreviewButtonMode.NEED_DOWNLOAD);
                    case DOWNLOADING:
                        if (downloadStatus == DownloadStatus.DOWNLOADING) state.setButtonMode(PreviewButtonMode.DOWNLOADING);
                        state.setButtonText(getString(R.string.download));
                        state.setShowRestart(false);
                        break;
                }
            });

            if (mProgramObject.getDiscover() > 0) {
                File file = new File(getFilesDir(), "discover" + mProgramObject.getDiscover() + "/" + mProgramObject.getVersion());
                if (file.exists()) {
                    file.mkdirs();
                }

            }
            if (mProgramObject.getPremium() == 1) {
                if (myPreference.isPremiumMember()) {
                    if (mProgramObject.getDiscover() > 0 && checkExploreData()) {
                        mPreviewViewModel.setDownloadStatus(DownloadStatus.NEED_DOWNLOAD);
                        state.setItemsFrom(null);
                        startDownload();
                        return;
                    }
                } else {
                    showUnlock();
                }
            } else {
                if (mProgramObject.getDiscover() > 0 && checkExploreData()) {
                    mPreviewViewModel.setDownloadStatus(DownloadStatus.NEED_DOWNLOAD);
                    state.setItemsFrom(null);
                    startDownload();
                    return;
                }
                requestNewAdBanner();
            }
            updateData();
        }
    }

    boolean checkExploreData() {
        return !existsData(FitnessApplication.getApplication().getExploreBasePath() + "/workout") || !existsData(FitnessApplication.getApplication().getExploreBasePath() + "/language/en");
    }


    void updateData() {
        if (mProgramObject.getDiscover() > 0 && checkExploreData()) {
            mPreviewViewModel.setDownloadStatus(DownloadStatus.NEED_DOWNLOAD);
            state.setItemsFrom(null);
            return;
        }
        mDemoDataProvider = new DemoDataProvider(PreviewActivity.this, mProgramObject.getDiscover(), mProgramObject.getVersion());
        mDemoDataProvider.setExtras(mProgramObject, myPreference.isWarmUpCoolDown(), myPreference.isNoJump());
        mDemoDataProvider.setData(mProgramObject.getPlan(), mIndexDay);
        state.setWarmUpVisible(mDemoDataProvider.supportsWarmUp());
        state.setWarmUpOn(myPreference.isWarmUpCoolDown());
        state.setDifficultyPercent(mDemoDataProvider.getAppliedPercent());
        refreshItems();
        updatePlan();
        startDownLoadAudio();
    }
    public void saveFileListToJson(List<String> fileList, String jsonFilePath) {
        JSONArray jsonArray = new JSONArray(fileList);
        try (FileOutputStream fos = new FileOutputStream(new File(jsonFilePath))) {
            fos.write(jsonArray.toString().getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void startDownLoadAudio() {
        String model = myPreference.getVoiceModel(myPreference.getLanguage());
        if (!model.equals("default")) {
            String voicePath = getFilesDir() + "/voice/" + myPreference.getLanguage() + "/" + myPreference.getVoiceModel(myPreference.getLanguage()) + "/";

            File dir = new File(voicePath);

            if (!dir.exists()) {
                dir.mkdirs();
            }
            List<String> fileList = new ArrayList<>();
            fileList.add(AppEncrypt.getName(getString(R.string.wp_start)));
            fileList.add(AppEncrypt.getName(getString(R.string.v_start_with)));
            fileList.add(AppEncrypt.getName(getString(R.string.wp_ready_to_go)));
            fileList.add(AppEncrypt.getName(getString(R.string.wp_v_half_time)));
            fileList.add(AppEncrypt.getName(String.format(getString(R.string.tts_test_des),myPreference.getVoiceModel(myPreference.getLanguage()))));
            fileList.add(AppEncrypt.getName(getString(R.string.wp_seconds)));
            fileList.add(AppEncrypt.getName(getString(R.string.wp_each_side)));
            fileList.add(AppEncrypt.getName(getString(R.string.wp_the_next)));
            fileList.add(AppEncrypt.getName(getString(R.string.wp_have_a_rest)));
            for (int i =1;i<100;i++) {
                String fileName = AppEncrypt.getName(""+i);
                fileList.add(fileName);

            }
            List<ExerciseObject> allExercise = FitnessApplication.getApplication().mJsonHelper.getAllExercise(mProgramObject.getDiscover(), mProgramObject.getVersion());
            for (ExerciseObject exerciseObject : allExercise
            ) {
                fileList.add(AppEncrypt.getName(exerciseObject.getName()));
                for (Tip tip : exerciseObject.getTips()) {
                    fileList.add(AppEncrypt.getName(tip.getText()));
                }
            }
            Iterator<String> iterator = fileList.iterator();
            while (iterator.hasNext()) {
                String fileName = iterator.next();
                if (new File(voicePath, fileName).exists()) {
                    iterator.remove();
                }
            }
            if (!fileList.isEmpty()) {
                Data.Builder data = new Data.Builder();
                String jsonFilePath = voicePath + "/file_list.json";
                saveFileListToJson(fileList, jsonFilePath);
                data.putString("FILE_LIST_PATH", jsonFilePath);
                Log.d("DownloadVoiceWorker", "Size: " + fileList.size());
                data.putString("BASE_PATH", voicePath);
                data.putString("BASE_URL", ApiUtils.BASE_APP_URL + "voice/" + myPreference.getLanguage() + "/" + myPreference.getVoiceModel(myPreference.getLanguage()) + "/");
                OneTimeWorkRequest syncWorkRequest =
                        new OneTimeWorkRequest.Builder(DownloadVoiceWorker.class)
                                .addTag("Sync")
                                .setInputData(data.build())
                                .build();

                WorkManager.getInstance(PreviewActivity.this).enqueue(syncWorkRequest);
                WorkManager.getInstance(this).getWorkInfoByIdLiveData(syncWorkRequest.getId()).observe(this, workInfo -> {
                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        Log.d("HAHA", "download Done!");
                    }
                    if (workInfo.getState() == WorkInfo.State.FAILED){

                    }
                });
            }
        }
    }


    void startDownload() {
        // showDownloadDialog();
        Data.Builder data = new Data.Builder();
        data.putString("audio_url", "https://min-fitness.uk/min-fitness/female_workout/discover_3d/" + mProgramObject.getDiscover() + "_" + mProgramObject.getVersion() + ".zip");
        data.putString("destination_folder", "discover/" + mProgramObject.getDiscover() + "/" + mProgramObject.getVersion());

        OneTimeWorkRequest syncWorkRequest =
                new OneTimeWorkRequest.Builder(DownloadAndUnzipWorker.class)
                        .addTag("Sync")
                        .setInputData(data.build())
                        .build();

        WorkManager.getInstance(PreviewActivity.this).enqueue(syncWorkRequest);
        state.setButtonEnabled(false);
        mPreviewViewModel.setDownloadStatus(DownloadStatus.DOWNLOADING);
        state.setButtonProgress(0f);
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(syncWorkRequest.getId()).observe(this, workInfo -> {
            Data progress = workInfo.getProgress();
            int value = progress.getInt("PROGRESS", 0);
            state.setButtonProgress(value / 100f);
            state.setButtonText(getString(R.string.download) + "..." + value + "%");
            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                mPreviewViewModel.setDownloadStatus(DownloadStatus.DONE);
                updateData();
                Toast.makeText(PreviewActivity.this, "Download Done!", Toast.LENGTH_SHORT).show();

            }
            if (workInfo.getState() == WorkInfo.State.FAILED) {
                mPreviewViewModel.setDownloadStatus(DownloadStatus.FAIL);
                showAlertInternetConnect();
            }
        });
    }

    void showAlertInternetConnect() {
        //  Toast.makeText(PreviewActivity.this, "Download Fail!", Toast.LENGTH_SHORT).show();
        // Lỗi đã hiện qua trạng thái FAIL (Toast + nút Retry).
    }


    private void showAlertRepsEdit(final int exerciseIndex, PlanObject.ActionObject actionObject) {
        currentActionIndex = exerciseIndex;
        DialogEditWorkout dialogEditWorkout = DialogEditWorkout.newInstance(actionObject);
        dialogEditWorkout.show(getSupportFragmentManager(), "ACTION_EDITOR");
    }


    private void loadRewardedVideoAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        if (myPreference.getOK()) {
            isRewardedAdLoading = true;
            RewardedAd.load(this, MaticAdKeys.REWARD_AD,
                    adRequest, new RewardedAdLoadCallback() {

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            mRewardedVideoAd = rewardedAd;
                            if (isRewardedAdLoading){
                                handler.removeCallbacks(timeoutRunnable);
                                showReward();
                            }

                            // loadInterReward();
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            if (isRewardedAdLoading) {
                                handler.removeCallbacks(timeoutRunnable);
                                loadInterReward();
                            }
                        }
                    });

        }
        if (handler!= null){
            handler.postDelayed(timeoutRunnable,5000);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCoachVoice();
        // Tập dở rồi quay lại: cập nhật ngay nút "Continue x%" (trước đây phải mở lại màn).
        if (mDemoDataProvider != null && mProgramObject != null) {
            updatePlan();
        }
    }

    private String shownVoiceModel;

    private void refreshCoachVoice() {
        if (myPreference == null) return;
        String language = myPreference.getLanguage();
        boolean available = false;
        if (!language.isEmpty() && FirebaseRemoteConfig.getInstance().getBoolean("enable_voice")) {
            try {
                JSONObject voices = new JSONObject(FirebaseRemoteConfig.getInstance().getString("voice"));
                Iterator<String> keys = voices.keys();
                while (keys.hasNext()) {
                    if (voices.optString(keys.next()).contains(language)) available = true;
                }
            } catch (org.json.JSONException e) {
                Log.w(TAG, "Invalid coach voice configuration", e);
            }
        }
        String model = myPreference.getVoiceModel(language);
        String label = "default".equals(model) ? getString(R.string.default_text)
                : "woman".equals(model) ? getString(R.string.name_female)
                : "man".equals(model) ? getString(R.string.name_male)
                : "Bella".equals(model) ? "Jennifer" : model;
        state.setVoice(available ? label : null);
        if (shownVoiceModel != null && !shownVoiceModel.equals(model) && mDemoDataProvider != null) {
            startDownLoadAudio();
        }
        shownVoiceModel = model;
    }

    @Override
    public void onCoachVoice() {
        startActivity(new Intent(this, CoachOptionActivity.class));
    }

    @Override
    protected void onDestroy() {
        if (adView != null) adView.destroy();
        super.onDestroy();
    }

    private final Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRewardedAdLoading) {
                isRewardedAdLoading = false;
                loadInterReward();
            }
        }
    };

    private void loadInterReward() {
        if (myPreference.getOK()) {
            MaticAd.getInstance().loadInterstitialAd(PreviewActivity.this, MaticAdKeys.INTER_PRO, new MaticAdCallBack() {
                @Override
                public void onInterstitialLoaded(@Nullable InterstitialAd interstitialAd) {
                    super.onInterstitialLoaded(interstitialAd);
                    showInterReward(interstitialAd);
                }

                @Override
                public void onAdFailedToLoad(@Nullable AdError adError) {
                    super.onAdFailedToLoad(adError);
                    loadRewardFail();

                }
            });
        }
    }

    public void showInterReward(InterstitialAd interstitialAd) {
        if (interstitialAd!= null){
            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    dismissUnlock();
                }
            });
            interstitialAd.show(PreviewActivity.this);
        }

    }

    private AdView adView;

    void requestNewAdBanner() {
        state.setBanner(null);
        if (myPreference.isShowAds() && myPreference.getOK()) {
            if (adView == null) {
                adView = new AdView(this);
                adView.setAdSize(AdSize.BANNER);
                adView.setAdUnitId(getString(R.string.ad_banner_unit_id));
            }
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    state.setBanner(adView);
                }
            });
            adView.loadAd(new AdRequest.Builder().build());
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

    // ------------------------------------------------------------ chế độ sửa (trước đây ở menu toolbar)

    @Override
    public void onEdit() {
        flagEdit = true;
        state.setEditMode(true);
    }

    @Override
    public void onSave() {
        flagEdit = false;
        state.setEditMode(false);
        File file = new File(getFilesDir().getPath() + "/i");
        if (!file.exists()) {
            file.mkdirs();
        }
        myPreference.setVersionPlan(mProgramObject.getPlan(), JsonHelper.VERSION_DAY_PLAN);
        mDemoDataProvider.saveEdits();
        updatePlan();
        Toast.makeText(this, R.string.txt_plan_saved, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResetDifficulty() {
        DifficultyStore.reset(this, mProgramObject.getId());
        updateData();
    }

    @Override
    public void onToggleWarmUp() {
        // Bật/tắt khởi động + giãn cơ: danh sách đổi nên tiến độ đang dở của bài này về 0.
        myPreference.setWarmUpCoolDown(!myPreference.isWarmUpCoolDown());
        myPreference.saveProgressWorkout(mProgramObject.getId(), mIndexDay, 0);
        updateData();
    }

    @Override
    public void onResetPlan() {
        if (mDemoDataProvider == null) return;
        if (mProgramObject.getType() <= 2) {
            mDemoDataProvider.clearEdits();
            mDemoDataProvider.resetData(mProgramObject.getPlan());
            Toast.makeText(this, R.string.txt_plan_restored, Toast.LENGTH_SHORT).show();
        }
        refreshItems();
        updatePlan();
    }

    private void refreshItems() {
        state.setItemsFrom(mDemoDataProvider == null ? null : mDemoDataProvider.getActionObjectList());
    }

    @SuppressLint("DefaultLocale")
    private void updatePlan() {
        int minutes = Math.max(1, Math.round(mDemoDataProvider.getTotalTime() / 60f));
        state.setMinutes(minutes);
        float cal = (mDemoDataProvider.getTotalTime() / 3600.0f) * FitnessConstUtils.CALORIES_CONST * (myPreference.getCurrentWeightKG() / 65.0f);
        state.setCalories(Math.round(cal));
        saveProgress = myPreference.getSaveProgress(mProgramObject.getId(), mIndexDay);
        updateButton();
    }

    /** Nút chính: "Continue x%" nếu đang tập dở (kèm nút bắt đầu lại), không thì "Go!". */
    @SuppressLint("DefaultLocale")
    private void updateButton() {
        if (mDemoDataProvider != null && saveProgress > 0 && saveProgress < mDemoDataProvider.getItemCount()) {
            float fraction = saveProgress / (float) mDemoDataProvider.getItemCount();
            state.setShowRestart(true);
            state.setButtonMode(PreviewButtonMode.CONTINUE);
            state.setButtonProgress(fraction);
            state.setButtonText(getString(R.string.continue_text) + String.format(" %.0f", fraction * 100) + "%");
        } else {
            state.setShowRestart(false);
            state.setButtonMode(PreviewButtonMode.GO);
            state.setButtonProgress(1f);
            state.setButtonText(getString(R.string.txt_go_to));
        }
    }

    ActivityResultLauncher<Intent> resultCount = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    updateData();
                }
            });


    @Override
    public void onAudio() {
        goAudioOptions(null);
    }

    void goAudioOptions(View view) {
        resultCount.launch(new Intent(PreviewActivity.this, CountManagerActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1010) {
            Bundle extras = data.getExtras();
            ArrayList<Integer> addList = extras.getIntegerArrayList("ADD_ACTIONS");
            if (addList != null && addList.size() > 0) {
                int start = mDemoDataProvider.getItemCount();
                for (Integer action : addList) {
                    mDemoDataProvider.addItem(action);
                }
                refreshItems();
                //mAdapter.notifyItemRangeInserted(start, addList.size());
            }
        }

        if (resultCode == RESULT_OK && requestCode == 1112) {
            Bundle extras = data.getExtras();
            boolean premiumMember = extras.getBoolean("PREMIUM_MEMBER", false);
            if (premiumMember) {
                dismissUnlock();
            }
        }
    }

    // ------------------------------------------------------------ PreviewActions

    @Override
    public void onBack() {
        if (flagEdit) {
            onSave();
        } else {
            finish();
        }
    }

    @Override
    public void onItemClick(int index) {
        if (mDemoDataProvider == null || index >= mDemoDataProvider.getItemCount()) return;
        PlanObject.ActionObject action = mDemoDataProvider.getItem(index);
        if (flagEdit) {
            onRepsEdit(index, action);
        } else {
            onDetail(action.actionId);
        }
    }

    @Override
    public void onReplace(int index) {
        if (mDemoDataProvider == null || index >= mDemoDataProvider.getItemCount()) return;
        onReplace(index, mDemoDataProvider.getItem(index));
    }

    @Override
    public void onMove(int from, int to) {
        if (mDemoDataProvider == null) return;
        int count = mDemoDataProvider.getItemCount();
        if (from < 0 || to < 0 || from >= count || to >= count) return;
        mDemoDataProvider.moveItem(from, to);
        refreshItems();
    }

    @Nullable
    @Override
    public ExerciseObject exercise(int actionId) {
        return mDemoDataProvider == null ? null : mDemoDataProvider.getExerciseItem(actionId);
    }

    @Override
    public int discover() {
        return mProgramObject == null ? 0 : mProgramObject.getDiscover();
    }

    @Override
    public int version() {
        return FitnessApplication.getApplication().getVersion();
    }

    public void onRepsEdit(int position, PlanObject.ActionObject actionObject) {
        //showAlertRepsEdit(position, actionObject);
        currentActionIndex = position;
        DialogEditExercise dialogEditWorkout = DialogEditExercise.newInstance(actionObject.actionId, actionObject.time, 1, 200, actionObject.getUnit());
        dialogEditWorkout.setEditExerciseListener(this);
        dialogEditWorkout.show(getSupportFragmentManager(), "Edit");
    }


    public void onReplace(int position, PlanObject.ActionObject actionObject) {

        currentActionIndex = position;


        Bundle bundle = new Bundle();
        bundle.putInt("actionId", actionObject.actionId);
        bundle.putInt("time", actionObject.time);
        bundle.putInt("group", FitnessApplication.getApplication().getHashMapExercise().get(actionObject.actionId).getClassid());
        Intent intent = new Intent(PreviewActivity.this, ReplaceActivity.class);
        intent.putExtras(bundle);
        // Intent intent = new Intent(mContext, YoutubeDemoActivity.class);
        resultLauncher.launch(intent);
    }

    public void onDetail(int actionId) {
        Intent intent = new Intent(PreviewActivity.this, ExerciseDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("actionId", actionId);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    @Override
    public void onGo() {
        gotoWorkout(null);
    }

    /** Nút "Bắt đầu lại" khi đang tập dở (không đặt tên onRestart: trùng lifecycle của Activity). */
    @Override
    public void onStartOver() {
        reset(null);
    }

    void gotoWorkout(View view) {
        DownloadStatus downloadStatus = mPreviewViewModel.getDownloadStatus().getValue();
        if (downloadStatus == DownloadStatus.NEED_DOWNLOAD || downloadStatus == DownloadStatus.FAIL) {
            startDownload();
            return;
        }
        if (downloadStatus == DownloadStatus.DOWNLOADING) {
            Toast.makeText(this, "Downloading!", Toast.LENGTH_SHORT).show();
            return;
        }
        mBundle.putInt("PROGRESS", saveProgress);
        if (MaticAd.getInstance().isAvailableInter() && mProgramObject.getPremium() != 1 && FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.GO_BEFORE)) {
            MaticAd.getInstance().showPriorityInterstitialAd(PreviewActivity.this, new MaticAdCallBack() {
                @Override
                public void onNextAction() {
                    super.onNextAction();
                    goWorkout();
                }
            });
        } else {
            goWorkout();
        }
    }


    void reset(View view) {
        mBundle.putInt("PROGRESS", 0);
        if (MaticAd.getInstance().isAvailableInter() && mProgramObject.getPremium() != 1 && FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.GO_BEFORE)) {
            MaticAd.getInstance().showPriorityInterstitialAd(PreviewActivity.this, new MaticAdCallBack() {
                @Override
                public void onNextAction() {
                    super.onNextAction();
                    goWorkout();
                }
            });
        } else {
            goWorkout();
        }
    }

    void goWorkout() {
        if (mProgramObject == null || mDemoDataProvider == null) {
            WorkoutDataValidation.report(this, "preview", mBundle == null ? -1 : mBundle.getInt("PROGRAM_ID", -1),
                    -1, -1, mBundle == null ? -1 : mBundle.getInt("DAY_NUMBER", -1), -1, -1, "missing_program_or_provider");
            Toast.makeText(this, "No workout!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Bài tự tạo chưa thêm động tác cho ngày này: không phải lỗi dữ liệu, mở màn sửa.
        List<PlanObject.ActionObject> actions = mDemoDataProvider.getActionObjectList();
        if (mProgramObject.getId() > Database.MY_WORKOUT_ID_BASE && (actions == null || actions.isEmpty())) {
            Toast.makeText(this, R.string.my_workout_day_empty, Toast.LENGTH_SHORT).show();
            Intent edit = new Intent(this, CustomMyWorkoutActivity.class);
            edit.putExtra("CUSTOM_ID", mProgramObject.getId());
            startActivity(edit);
            return;
        }
        // Recheck after any interstitial callback, using the same catalog as the workout screen.
        if (!WorkoutDataValidation.validate(this, "preview", mProgramObject.getId(),
                mProgramObject.getDiscover(), mProgramObject.getVersion(), mBundle.getInt("DAY_NUMBER", -1),
                mDemoDataProvider.getActionObjectList(), FitnessApplication.getApplication().mJsonHelper
                        .getHashMapExercise(mProgramObject.getDiscover(), mProgramObject.getVersion()))) {
            Toast.makeText(this, "No workout!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mDemoDataProvider.getActionObjectList() != null && !mDemoDataProvider.getActionObjectList().isEmpty()) {
            mBundle.putParcelableArrayList("ACTION_LIST", (ArrayList<? extends Parcelable>) mDemoDataProvider.getActionObjectList());
            mBundle.putInt("SIZE", mDemoDataProvider.getActionObjectList().size());
            Intent intent = new Intent(PreviewActivity.this, DoWorkoutActivity.class);
            intent.putExtras(mBundle);
            startActivity(intent);
        } else {
            Toast.makeText(this, "No workout!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onUpdateAction(PlanObject.ActionObject actionObject) {
        mDemoDataProvider.updateItem(currentActionIndex, actionObject);
        refreshItems();
        updatePlan();
    }

    @Override
    public void onChange(int action, int time, String unit) {
        // Thay bằng bản sao thay vì sửa tại chỗ: danh sách Compose giữ cùng object nên không vẽ lại số mới.
        if (currentActionIndex < 0 || currentActionIndex >= mDemoDataProvider.getItemCount()) return;
        PlanObject.ActionObject edited = mDemoDataProvider.getItem(currentActionIndex).clone();
        edited.time = time;
        mDemoDataProvider.updateItem(currentActionIndex, edited);
        refreshItems();
        updatePlan();
    }

    private boolean existsData(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }


    void showReward() {
        if (mRewardedVideoAd == null) {
            Toast.makeText(this, "No ads to show!", Toast.LENGTH_SHORT).show();
            return;
        }

        mRewardedVideoAd.setFullScreenContentCallback(new FullScreenContentCallback() {

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {

                mRewardedVideoAd = null;
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                mRewardedVideoAd = null;
                // Đóng video trước khi nhận thưởng: cho bấm "Xem video" lại.
                if (dialogUnlockTraining != null && !flagUnlock) dialogUnlockTraining.loadDone();
            }
        });
        mRewardedVideoAd.show(PreviewActivity.this, rewardItem -> {
            dismissUnlock();
        });
    }

    void loadRewardFail() {
        Toast.makeText(PreviewActivity.this, getString(R.string.load_video_fail), Toast.LENGTH_SHORT).show();
        if (dialogUnlockTraining != null) {
            dialogUnlockTraining.loadDone();
        }

    }

    void showUnlock() {
        try {
            if (dialogUnlockTraining == null) {
                dialogUnlockTraining = DialogUnlockTraining.newInstance(mProgramObject,
                        !mProgramLocks.contains(mProgramObject.getId()) && myPreference.getOK());
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            dialogUnlockTraining.show(fragmentManager, "UnlockTraining");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void dismissUnlock() {
        flagUnlock = true;
        if (dialogUnlockTraining != null) {
            dialogUnlockTraining.dismiss();
        }
        if (mProgramObject.getDiscover() > 0 && checkExploreData()) {
            startDownload();
        }

    }

    @Override
    public void watchVideo() {
        if (!myPreference.getOK()) {
            loadRewardFail();
            return;
        }
        if (FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.PRO_VIDEO_REWARD)){
            loadRewardedVideoAd();
        } else {
            loadInterReward();
        }
    }

    ActivityResultLauncher<Intent> resultPremium = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Bundle bundle = result.getData().getExtras();
                    if (bundle != null) {
                        boolean premiumMember = bundle.getBoolean("PREMIUM_MEMBER", false);
                        if (premiumMember) {
                            dismissUnlock();
                        }
                    }
                }
            });

    @Override
    public void goPremium() {
        Intent goPremium = new Intent(PreviewActivity.this, PremiumActivity.class);
        resultPremium.launch(goPremium);
        //startActivityForResult(goPremium, 1112);
    }

    @Override
    public void backActivity() {
        finish();
    }

    @Override
    public void onCancel() {
        finish();
    }

}
