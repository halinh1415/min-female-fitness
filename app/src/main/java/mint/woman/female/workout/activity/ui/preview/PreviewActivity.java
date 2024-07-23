package mint.woman.female.workout.activity.ui.preview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
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
import mint.woman.female.workout.custom.ExpandableTextView;
import mint.woman.female.workout.custom.LoadingDialogFragment;
import mint.woman.female.workout.custom.OnStartDragListener;
import mint.woman.female.workout.custom.SimpleItemTouchHelperCallback;
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
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressLint({"NonConstantResourceId", "DefaultLocale", "SetTextI18n"})
public class PreviewActivity extends AppCompatActivity implements PreviewAdapter.EventDemo, OnStartDragListener, DialogEditWorkout.OnEditFragmentInteractionListener, EditExerciseListener, DialogUnlockTraining.OnUnlockTrainingListener {

    private static final String TAG = "PreviewActivity";
    @BindView(R.id.rc_demo)
    RecyclerView mPreviewRc;
    @BindView(R.id.img_banner)
    ImageView mBanner;
    @BindView(R.id.image_workout)
    ImageView mImageWorkout;
    @BindView(R.id.app_bar)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.adView)
    AdView mAdBanner;

    @BindView(R.id.tv_workout_intro)
    TextView tvWorkoutIntro;

    @BindView(R.id.tv_level)
    TextView mPlanLevel;
    @BindView(R.id.tv_workout_description)
    TextView mWorkoutDescription;

    @BindView(R.id.progress_ready_bottom)
    ProgressBar mProgressReady;
    @BindView(R.id.btn_goto)
    View mGoBtn;
    @BindView(R.id.txt_start)
    TextView mProgressTxt;
    @BindView(R.id.txt_reset)
    TextView mResetBtn;
    @BindView(R.id.txt_program_description)
    ExpandableTextView mProgramDescription;
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
        setContentView(R.layout.activity_preview);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initView();
        requestNewAdBanner();
        loadInterStart();
    }

    private void loadInterStart() {
        if (myPreference.isShowAds() && myPreference.getOK() && FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.INTER_START) && mProgramObject.getPremium() != 1) {
            MaticAd.getInstance().loadInterstitialAd3SameTime(PreviewActivity.this, MaticAdKeys.INTER_START_HIGH, MaticAdKeys.INTER_START_MEDIUM, MaticAdKeys.INTER_START, new MaticAdCallBack());
        }
    }


    private void initView() {
        myPreference = new MyPreference(this);
        mBundle = getIntent().getExtras();
        mPreviewViewModel = new ViewModelProvider(this).get(PreviewViewModel.class);
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
            mProgramDescription.setVisibility(View.GONE);
            mJsonHelper = FitnessApplication.getApplication().mJsonHelper;
            String baseUrl = "file:///android_asset/images/";
            if (mProgramObject.getDiscover() > 0) {
                baseUrl = ApiUtils.BASE_URL_IMAGES;
            }
            Glide.with(this).load(Uri.parse(baseUrl + mProgramObject.getBackground())).into(mBanner);
            Glide.with(getApplicationContext()).load(Uri.parse(baseUrl + mProgramObject.getImage())).into(mImageWorkout);
            if (mProgramObject.getType() == 1 || mProgramObject.getType() == 3) {
                mPlanLevel.setText(getString(R.string.txt_day) + " " + (mIndexDay + 1));
            } else if (mProgramObject.getType() == 2) {
                mPlanLevel.setText("Level " + mProgramObject.getLevel());
            }
            String htmlContent = "" + mProgramObject.getTotal() + " " + getString(R.string.minutes) + "";
            tvWorkoutIntro.setText(htmlContent);
            mWorkoutDescription.setText(mProgramObject.getDescription());
            setTitle(mProgramObject.getName().toUpperCase());
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.setCustomKey("PROGRAM_ID", mProgramObject.getId());
            crashlytics.setCustomKey("VERSION", mProgramObject.getVersion());
            crashlytics.setCustomKey("PROGRAM_DAY", mIndexDay);
            crashlytics.setCustomKey("LANGUAGE", myPreference.getLanguage());
            mPreviewViewModel.getDownloadStatus().observe(PreviewActivity.this, downloadStatus -> {
                switch (downloadStatus) {
                    case DONE:
                        mGoBtn.setEnabled(true);
                        mProgressTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        mProgressTxt.setText(getString(R.string.txt_go_to));
                        mResetBtn.setVisibility(View.GONE);
                        mGoBtn.setVisibility(View.VISIBLE);
                        if (saveProgress > 0 && saveProgress < mDemoDataProvider.getItemCount()) {
                            mResetBtn.setVisibility(View.VISIBLE);
                            mProgressReady.setMax(mDemoDataProvider.getItemCount());
                            mProgressReady.setProgress(saveProgress);
                            float percent = saveProgress * 100f / mDemoDataProvider.getItemCount();
                            mProgressTxt.setText(getString(R.string.continue_text) + String.format(" %.0f", percent) + "%");
                        } else {
                            mProgressReady.setMax(100);
                            mProgressReady.setProgress(100);
                            mProgressTxt.setText(getString(R.string.txt_go_to));
                            mResetBtn.setVisibility(View.GONE);
                        }
                        break;
                    case FAIL:
                        mResetBtn.setVisibility(View.GONE);
                        mGoBtn.setEnabled(true);
                        mProgressReady.setProgress(100);
                        mProgressTxt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_settings_backup_restore_24, 0, 0, 0);
                        mProgressTxt.setText(getString(R.string.retry));
                        mGoBtn.setVisibility(View.VISIBLE);
                        break;
                    case NEED_DOWNLOAD:
                        mProgressReady.setProgress(100);
                        mProgressReady.setMax(100);
                        mProgressTxt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cloud_download_black_24dp, 0, 0, 0);
                    case DOWNLOADING:
                        mProgressTxt.setText(getString(R.string.download));
                        mResetBtn.setVisibility(View.GONE);
                        mGoBtn.setVisibility(View.VISIBLE);

                        break;
                }
            });

            DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
            mPreviewRc.addItemDecoration(divider);
            if (mProgramObject.getPremium() == 1 && !myPreference.isPremiumMember()) {
                showUnlock();
                //loadRewardedVideoAd();
            }
            mPreviewRc.setLayoutManager(new GridLayoutManager(this, 1));
            if (mProgramObject.getDiscover() > 0) {
                File file = new File(getFilesDir(), "discover" + mProgramObject.getDiscover() + "/" + mProgramObject.getVersion());
                if (file.exists()) {
                    file.mkdirs();
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
            mPreviewRc.setAdapter(exercisePreAdapter);
            return;
        }
        mDemoDataProvider = new DemoDataProvider(PreviewActivity.this, mProgramObject.getDiscover(), mProgramObject.getVersion());
        mDemoDataProvider.setData(mProgramObject.getPlan(), mIndexDay);
        mPreviewAdapter = new PreviewAdapter(this, this, this, mDemoDataProvider, mProgramObject.getDiscover());
        mPreviewRc.setAdapter(mPreviewAdapter);
        mPreviewRc.setNestedScrollingEnabled(false);
        updatePlan();
    }


    void startDownload() {
        showDownloadDialog();
        Data.Builder data = new Data.Builder();
        data.putString("audio_url", "https://min-fitness.uk/min-fitness/female_workout/discover_3d/" + mProgramObject.getDiscover() + "_" + mProgramObject.getVersion() + ".zip");
        data.putString("destination_folder", "discover/" + mProgramObject.getDiscover() + "/" + mProgramObject.getVersion());

        OneTimeWorkRequest syncWorkRequest =
                new OneTimeWorkRequest.Builder(DownloadAndUnzipWorker.class)
                        .addTag("Sync")
                        .setInputData(data.build())
                        .build();

        WorkManager.getInstance(PreviewActivity.this).enqueue(syncWorkRequest);
        mGoBtn.setEnabled(false);
        mProgressTxt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cloud_download_black_24dp, 0, 0, 0);
        mPreviewViewModel.setDownloadStatus(DownloadStatus.DOWNLOADING);
        mProgressReady.setMax(100);
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(syncWorkRequest.getId()).observe(this, workInfo -> {
            Data progress = workInfo.getProgress();
            int value = progress.getInt("PROGRESS", 0);
            mProgressReady.setProgress(value);

            mProgressTxt.setText(getString(R.string.download) + "..." + value + "%");
            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                hideDialog();
                mPreviewViewModel.setDownloadStatus(DownloadStatus.DONE);
                updateData();
                Toast.makeText(PreviewActivity.this, "Download Done!", Toast.LENGTH_SHORT).show();

            }
            if (workInfo.getState() == WorkInfo.State.FAILED) {
                mPreviewViewModel.setDownloadStatus(DownloadStatus.FAIL);
                showAlertInternetConnect();
                hideDialog();
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

            RewardedAd.load(this, MaticAdKeys.REWARD_AD,
                    adRequest, new RewardedAdLoadCallback() {

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            mRewardedVideoAd = rewardedAd;
                            showReward();
                           // loadInterReward();
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            loadInterReward();
                        }
                    });

        }
    }

    private void loadInterReward() {
        if (myPreference.getOK()) {
            MaticAd.getInstance().loadInterstitialAd(PreviewActivity.this, MaticAdKeys.INTER_PRO, new MaticAdCallBack() {
                @Override
                public void onInterstitialLoaded(@Nullable InterstitialAd interstitialAd) {
                    super.onInterstitialLoaded(interstitialAd);
                    hideDialog();
                    showInterReward();
                }

                @Override
                public void onAdFailedToLoad(@Nullable AdError adError) {
                    super.onAdFailedToLoad(adError);
                    Toast.makeText(PreviewActivity.this, "Failed to load!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    public void showInterReward(){
        MaticAd.getInstance().showPriorityInterstitialAd(PreviewActivity.this,new MaticAdCallBack(){
            @Override
            public void onNextAction() {
                super.onNextAction();
                dismissUnlock();
            }
        });
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
                mAppBarLayout.setExpanded(true, true);
                CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
                ((CustomAppBarLayoutBehavior) layoutParams.getBehavior()).setScrollBehavior(true);
                mPreviewRc.setNestedScrollingEnabled(false);
                if (mProgramObject.getDiscover() == 0) {
                    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mPreviewAdapter);
                    mItemTouchHelper = new ItemTouchHelper(callback);
                    mItemTouchHelper.attachToRecyclerView(mPreviewRc);
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
                mAppBarLayout.setExpanded(false, true);
                if (mProgramObject.getDiscover() == 0) {
                    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mPreviewAdapter);
                    mItemTouchHelper = new ItemTouchHelper(callback);
                    mItemTouchHelper.attachToRecyclerView(mPreviewRc);
                }
                mPreviewRc.setNestedScrollingEnabled(true);
                CoordinatorLayout.LayoutParams layoutParams1 = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
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
        tvWorkoutIntro.setText(Html.fromHtml(htmlContent));
        float cal = (mDemoDataProvider.getTotalTime() / 3600.0f) * FitnessConstUtils.CALORIES_CONST * (myPreference.getCurrentWeightKG() / 65.0f);
        saveProgress = myPreference.getSaveProgress(mProgramObject.getId(), mIndexDay);
        if (saveProgress > 0 && saveProgress < mDemoDataProvider.getItemCount()) {
            mResetBtn.setVisibility(View.VISIBLE);
            mProgressReady.setMax(mDemoDataProvider.getItemCount());
            mProgressReady.setProgress(saveProgress);
            float percent = saveProgress * 100f / mDemoDataProvider.getItemCount();
            mProgressTxt.setText(getString(R.string.continue_text) + String.format(" %.0f", percent) + "%");
        } else {
            mProgressReady.setMax(100);
            mProgressReady.setProgress(100);
            mProgressTxt.setText(getString(R.string.txt_go_to));
            mResetBtn.setVisibility(View.GONE);
        }
    }

    ActivityResultLauncher<Intent> resultCount = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    updateData();
                }
            });

    @OnClick(R.id.fab_audio)
    void goAudioOptions() {
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

    @OnClick(R.id.btn_goto)
    void gotoWorkout() {
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
        if (MaticAd.getInstance().isAvailableInter() && mProgramObject.getPremium() != 1) {
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

    @OnClick(R.id.txt_reset)
    void reset() {
        mBundle.putInt("PROGRESS", 0);
        if (MaticAd.getInstance().isAvailableInter()) {
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

    LoadingDialogFragment loadingDialog;

    boolean isShowLoading = false;

    void showDialog() {
        try {
            if (!isShowLoading) {
                if (loadingDialog == null) {
                    loadingDialog = new LoadingDialogFragment();
                }
                isShowLoading = true;
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.executePendingTransactions();
                loadingDialog.show(fragmentManager, "loading_dialog");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void showDownloadDialog() {
        try {
            if (!isShowLoading) {
                if (loadingDialog == null) {
                    loadingDialog = LoadingDialogFragment.newInstance("download.json", getString(R.string.downloading));
                }
                isShowLoading = true;
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.executePendingTransactions();
                loadingDialog.show(fragmentManager, "loading_dialog");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void hideDialog() {
        try {
            isShowLoading = false;
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void showReward() {
        hideDialog();
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
            // Handle the reward.
            Log.d(TAG, "The user earned the reward.");
            dismissUnlock();

        });
    }

    void showUnlock() {
        try {
            if (dialogUnlockTraining == null) {
                dialogUnlockTraining = new DialogUnlockTraining();
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

    }

    @Override
    public void watchVideo() {
        showDialog();
        loadRewardedVideoAd();


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
