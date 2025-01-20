package mint.woman.female.workout.activity.ui.preview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.premium.PremiumActivity;
import mint.woman.female.workout.activity.ui.count.CountManagerActivity;
import mint.woman.female.workout.activity.ui.workout.DoWorkoutActivity;
import mint.woman.female.workout.activity.ui.workout.ExerciseDetailActivity;
import mint.woman.female.workout.activity.ui.workout.SelectExerciseActivity;
import mint.woman.female.workout.admodule.MaticAd;
import mint.woman.female.workout.admodule.MaticAdCallBack;
import mint.woman.female.workout.admodule.MaticAdKeys;
import mint.woman.female.workout.common.adapter.PreviewAdapter;
import mint.woman.female.workout.common.adapter.ExercisePreAdapter;
import mint.woman.female.workout.common.data.DemoDataProvider;
import mint.woman.female.workout.custom.CustomAppBarLayoutBehavior;
import mint.woman.female.workout.custom.DialogEditExercise;
import mint.woman.female.workout.custom.DialogEditWorkout;
import mint.woman.female.workout.custom.DialogUnlockTraining;
import mint.woman.female.workout.custom.EditExerciseListener;
import mint.woman.female.workout.custom.OnStartDragListener;
import mint.woman.female.workout.custom.SimpleItemTouchHelperCallback;
import mint.woman.female.workout.databinding.ActivityPreviewBinding;
import mint.woman.female.workout.databinding.ContentPreviewBinding;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.retrofit.ApiUtils;
import mint.woman.female.workout.utils.DownloadAndUnzipWorker;
import mint.woman.female.workout.utils.FitnessConstUtils;
import mint.woman.female.workout.utils.JsonHelper;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.RemoteConfigKeys;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@SuppressLint({"NonConstantResourceId", "DefaultLocale", "SetTextI18n"})
public class PreviewActivity extends AppCompatActivity implements PreviewAdapter.EventDemo, OnStartDragListener, DialogEditWorkout.OnEditFragmentInteractionListener, EditExerciseListener, DialogUnlockTraining.OnUnlockTrainingListener {

    private static final String TAG = "PreviewActivity";
    ActivityPreviewBinding binding;
    ContentPreviewBinding contentPreviewBinding;


    PreviewAdapter mPreviewAdapter;
    DemoDataProvider mDemoDataProvider;
    MyPreference myPreference;
    Bundle mBundle;
    boolean flagEdit = false, flagUnlock = false;
    Menu menu;
    int saveProgress = 0;
    private ProgramObject mProgramObject;
    private int mIndexDay, currentActionIndex;
    private JsonHelper mJsonHelper;
    private ItemTouchHelper mItemTouchHelper;
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
                        mPreviewAdapter.notifyItemChanged(currentActionIndex);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPreviewBinding.inflate(getLayoutInflater());
        contentPreviewBinding = binding.contentPreview;
        setContentView(binding.getRoot());


        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initView();
        requestNewAdBanner();
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
        binding.fabAudio.setOnClickListener(this::goAudioOptions);
        binding.btnGoto.setOnClickListener(this::gotoWorkout);
        binding.txtReset.setOnClickListener(this::reset);
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
                Toast.makeText(this, "This program is removed", Toast.LENGTH_SHORT).show();
                return;
            }
            mIndexDay = mBundle.getInt("DAY_NUMBER");
            FitnessApplication.getApplication().setDiscover(mProgramObject.getDiscover());
            FitnessApplication.getApplication().setVersion(mProgramObject.getVersion());
          //  binding.tvWorkoutDescription.setVisibility(View.GONE);
            mJsonHelper = FitnessApplication.getApplication().mJsonHelper;
            String baseUrl = "file:///android_asset/images/";
            if (mProgramObject.getDiscover() > 0) {
                baseUrl = ApiUtils.BASE_URL_IMAGES;
            }
            Glide.with(this).load(Uri.parse(baseUrl + mProgramObject.getBackground())).into(binding.imgBanner);
            Glide.with(getApplicationContext()).load(Uri.parse(baseUrl + mProgramObject.getImage())).into(binding.imageWorkout);
            if (mProgramObject.getType() == 1 || mProgramObject.getType() == 3) {
                binding.tvLevel.setText(getString(R.string.txt_day) + " " + (mIndexDay + 1));
            } else if (mProgramObject.getType() == 2) {
                binding.tvLevel.setText("Level " + mProgramObject.getLevel());
            }
            String htmlContent = "" + mProgramObject.getTotal() + " " + getString(R.string.minutes) + "";
            binding.tvWorkoutIntro.setText(htmlContent);
            binding.tvWorkoutDescription.setText(mProgramObject.getDescription());
            setTitle(mProgramObject.getName().toUpperCase());
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.setCustomKey("PROGRAM_ID", mProgramObject.getId());
            crashlytics.setCustomKey("VERSION", mProgramObject.getVersion());
            crashlytics.setCustomKey("PROGRAM_DAY", mIndexDay);
            crashlytics.setCustomKey("LANGUAGE", myPreference.getLanguage());
            mPreviewViewModel.getDownloadStatus().observe(PreviewActivity.this, downloadStatus -> {
                switch (downloadStatus) {
                    case DONE:
                        binding.btnGoto.setEnabled(true);
                        binding.txtStart.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        binding.txtStart.setText(getString(R.string.txt_go_to));
                        binding.txtReset.setVisibility(View.GONE);
                        binding.btnGoto.setVisibility(View.VISIBLE);
                        if (saveProgress > 0 && saveProgress < mDemoDataProvider.getItemCount()) {
                            binding.txtReset.setVisibility(View.VISIBLE);
                            binding.progressReadyBottom.setMax(mDemoDataProvider.getItemCount());
                            binding.progressReadyBottom.setProgress(saveProgress);
                            float percent = saveProgress * 100f / mDemoDataProvider.getItemCount();
                            binding.txtStart.setText(getString(R.string.continue_text) + String.format(" %.0f", percent) + "%");
                        } else {
                            binding.progressReadyBottom.setMax(100);
                            binding.progressReadyBottom.setProgress(100);
                            binding.txtStart.setText(getString(R.string.txt_go_to));
                            binding.txtReset.setVisibility(View.GONE);
                        }
                        break;
                    case FAIL:
                        Toast.makeText(PreviewActivity.this,"Check your internet and retry!", Toast.LENGTH_SHORT).show();
                        binding.txtReset.setVisibility(View.GONE);
                        binding.btnGoto.setEnabled(true);
                        binding.progressReadyBottom.setProgress(100);
                        binding.txtStart.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_settings_backup_restore_24, 0, 0, 0);
                        binding.txtStart.setText(getString(R.string.retry));
                        binding.btnGoto.setVisibility(View.VISIBLE);
                        break;
                    case NEED_DOWNLOAD:
                        binding.progressReadyBottom.setProgress(100);
                        binding.progressReadyBottom.setMax(100);
                        binding.txtStart.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cloud_download_black_24dp, 0, 0, 0);
                    case DOWNLOADING:
                        binding.txtStart.setText(getString(R.string.download));
                        binding.txtReset.setVisibility(View.GONE);
                        binding.btnGoto.setVisibility(View.VISIBLE);

                        break;
                }
            });

            DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
            contentPreviewBinding.rcDemo.addItemDecoration(divider);
            contentPreviewBinding.rcDemo.setLayoutManager(new GridLayoutManager(this, 1));
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
                        ExercisePreAdapter exercisePreAdapter = new ExercisePreAdapter();
                        contentPreviewBinding.rcDemo.setAdapter(exercisePreAdapter);
                        startDownload();
                        return;
                    }
                } else {
                    showUnlock();
                }
            } else {
                if (mProgramObject.getDiscover() > 0 && checkExploreData()) {
                    mPreviewViewModel.setDownloadStatus(DownloadStatus.NEED_DOWNLOAD);
                    ExercisePreAdapter exercisePreAdapter = new ExercisePreAdapter();
                    contentPreviewBinding.rcDemo.setAdapter(exercisePreAdapter);
                    startDownload();
                    return;
                }
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
            ExercisePreAdapter exercisePreAdapter = new ExercisePreAdapter();
            contentPreviewBinding.rcDemo.setAdapter(exercisePreAdapter);
            return;
        }
        mDemoDataProvider = new DemoDataProvider(PreviewActivity.this, mProgramObject.getDiscover(), mProgramObject.getVersion());
        mDemoDataProvider.setData(mProgramObject.getPlan(), mIndexDay);
        mPreviewAdapter = new PreviewAdapter(this, this, this, mDemoDataProvider, mProgramObject.getDiscover());
        contentPreviewBinding.rcDemo.setAdapter(mPreviewAdapter);
        contentPreviewBinding.rcDemo.setNestedScrollingEnabled(false);
        updatePlan();
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
        binding.btnGoto.setEnabled(false);
        binding.txtStart.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cloud_download_black_24dp, 0, 0, 0);
        mPreviewViewModel.setDownloadStatus(DownloadStatus.DOWNLOADING);
        binding.progressReadyBottom.setMax(100);
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(syncWorkRequest.getId()).observe(this, workInfo -> {
            Data progress = workInfo.getProgress();
            int value = progress.getInt("PROGRESS", 0);
            binding.progressReadyBottom.setProgress(value);
            binding.txtStart.setText(getString(R.string.download) + "..." + value + "%");
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
        View view = findViewById(R.id.preview_layout);
        Snackbar.make(view, "Download Fail!", Snackbar.LENGTH_SHORT);
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
    protected void onDestroy() {
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

    void requestNewAdBanner() {
        binding.adView.setVisibility(View.GONE);
        if (myPreference.isShowAds() && myPreference.getOK()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            binding.adView.loadAd(adRequest);
            binding.adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    binding.adView.setVisibility(View.VISIBLE);
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        this.menu = menu;
        if (mProgramObject != null && mProgramObject.getDiscover() > 0) {
            menu.findItem(R.id.action_edit).setVisible(false);
        }
        return true;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Home Button!
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_save:
                flagEdit = false;
                menu.setGroupVisible(R.id.group_edit, false);
                menu.findItem(R.id.action_edit).setVisible(true);
                mPreviewAdapter.setFlagEdit(false);
                binding.appBar.setExpanded(true, true);
                CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) binding.appBar.getLayoutParams();
                ((CustomAppBarLayoutBehavior) layoutParams.getBehavior()).setScrollBehavior(true);
                contentPreviewBinding.rcDemo.setNestedScrollingEnabled(false);
                if (mProgramObject.getDiscover() == 0) {
                    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mPreviewAdapter);
                    mItemTouchHelper = new ItemTouchHelper(callback);
                    mItemTouchHelper.attachToRecyclerView(contentPreviewBinding.rcDemo);
                }
                File file = new File(getFilesDir().getPath() + "/i");
                if (!file.exists()) {
                    file.mkdirs();
                }
                myPreference.setVersionPlan(mProgramObject.getPlan(), JsonHelper.VERSION_DAY_PLAN);
                //Decoder.encryptStringToFile(mDemoDataProvider.convertPlanToJsonString().getBytes(), getFilesDir().getPath() + "/i/" + mProgramObject.getPlan());
                updatePlan();
                Toast.makeText(this, "Save!!!", Toast.LENGTH_SHORT).show();


                return true;
            case R.id.action_add:
                Intent chooseExercise = new Intent(PreviewActivity.this, SelectExerciseActivity.class);
                mBundle.putInt("PARENT", 0);
                chooseExercise.putExtras(mBundle);
                startActivityForResult(chooseExercise, 1010);
                return true;
            case R.id.action_reset:
                if (mProgramObject.getType() <= 2) {
                    mDemoDataProvider.resetData(mProgramObject.getPlan());
                }

                updatePlan();
                mPreviewAdapter.notifyDataSetChanged();
                //mAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_edit:

                flagEdit = true;
                menu.findItem(R.id.action_edit).setVisible(false);
                menu.setGroupVisible(R.id.group_edit, true);
                menu.findItem(R.id.action_add).setVisible(false);
                mPreviewAdapter.setFlagEdit(flagEdit);
                binding.appBar.setExpanded(false, true);
                if (mProgramObject.getDiscover() == 0) {
                    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mPreviewAdapter);
                    mItemTouchHelper = new ItemTouchHelper(callback);
                    mItemTouchHelper.attachToRecyclerView(contentPreviewBinding.rcDemo);
                }
                contentPreviewBinding.rcDemo.setNestedScrollingEnabled(true);
                CoordinatorLayout.LayoutParams layoutParams1 = (CoordinatorLayout.LayoutParams) binding.appBar.getLayoutParams();
                ((CustomAppBarLayoutBehavior) layoutParams1.getBehavior()).setScrollBehavior(false);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("DefaultLocale")
    private void updatePlan() {
        int minutes = mDemoDataProvider.getTotalTime() / 60;
        String htmlContent = "<h>" + minutes + " " + getString(R.string.minutes) + " &#8226; " + mDemoDataProvider.getItemCount() + " " + getString(R.string.txt_exercises) + "<h>";
        binding.tvWorkoutIntro.setText(Html.fromHtml(htmlContent));
        float cal = (mDemoDataProvider.getTotalTime() / 3600.0f) * FitnessConstUtils.CALORIES_CONST * (myPreference.getCurrentWeightKG() / 65.0f);
        saveProgress = myPreference.getSaveProgress(mProgramObject.getId(), mIndexDay);
        if (saveProgress > 0 && saveProgress < mDemoDataProvider.getItemCount()) {
            binding.txtReset.setVisibility(View.VISIBLE);
            binding.progressReadyBottom.setMax(mDemoDataProvider.getItemCount());
            binding.progressReadyBottom.setProgress(saveProgress);
            float percent = saveProgress * 100f / mDemoDataProvider.getItemCount();
            binding.txtStart.setText(getString(R.string.continue_text) + String.format(" %.0f", percent) + "%");
        } else {
            binding.progressReadyBottom.setMax(100);
            binding.progressReadyBottom.setProgress(100);
            binding.txtStart.setText(getString(R.string.txt_go_to));
            binding.txtReset.setVisibility(View.GONE);
        }
    }

    ActivityResultLauncher<Intent> resultCount = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    updateData();
                }
            });


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
                mPreviewAdapter.notifyItemRangeInserted(start, addList.size());
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

    @Override
    public void onRepsEdit(int position, PlanObject.ActionObject actionObject) {
        //showAlertRepsEdit(position, actionObject);
        currentActionIndex = position;
        DialogEditExercise dialogEditWorkout = DialogEditExercise.newInstance(actionObject.actionId, actionObject.time, 1, 200);
        dialogEditWorkout.setEditExerciseListener(this);
        dialogEditWorkout.show(getSupportFragmentManager(), "Edit");
    }


    @Override
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

    @Override
    public void onDetail(int actionId) {
        Intent intent = new Intent(PreviewActivity.this, ExerciseDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("actionId", actionId);
        intent.putExtras(bundle);
        startActivity(intent);
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
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onUpdateAction(PlanObject.ActionObject actionObject) {
        mDemoDataProvider.updateItem(currentActionIndex, actionObject);
        mPreviewAdapter.notifyItemChanged(currentActionIndex);
    }

    @Override
    public void onChange(int action, int time, String unit) {
        mDemoDataProvider.updateTime(currentActionIndex, time);
        mPreviewAdapter.notifyItemChanged(currentActionIndex);
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
                dialogUnlockTraining = DialogUnlockTraining.newInstance(mProgramLocks.contains(mProgramObject.getId()));
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
