package mint.woman.female.workout;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.work.Configuration;

import mint.woman.female.workout.activity.SplashActivity;
import mint.woman.female.workout.activity.ui.onboarding.onboard.OnboardFlowActivity;
import mint.woman.female.workout.activity.ui.premium.PremiumActivity;
import mint.woman.female.workout.admodule.AppOpenManager;
import mint.woman.female.workout.admodule.MaticAdKeys;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.notification.AppNotificationHelper;
import mint.woman.female.workout.notification.NotificationScheduler;
import mint.woman.female.workout.utils.AnrReporter;
import mint.woman.female.workout.utils.Database;
import mint.woman.female.workout.utils.EdgeToEdgeHelper;
import mint.woman.female.workout.utils.JsonHelper;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.NetworkUtil;
import mint.woman.female.workout.utils.StorageCommon;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FitnessApplication extends Application implements Configuration.Provider {

    private static final String TAG = "FitnessApplication";
    private final ExecutorService startupExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "deferred-startup");
        thread.setPriority(Thread.NORM_PRIORITY - 1);
        return thread;
    });

    public JsonHelper mJsonHelper;
    public Database mDatabase;
    private static FitnessApplication sInstance;
    private int version =1003,discover =0;
    protected StorageCommon storageCommon;

    public StorageCommon  getStorageCommon() {
        return storageCommon;
    }



    public ProgramObject getProgram(int id){

        if (id == 20){
            id = 879;
        }else if (id == 21){
            id = 881;
        }else if (id == 22){
            id = 880;
        }else if (id ==23){
            id = 882;
        }
        if (id > Database.MY_WORKOUT_ID_BASE) {
            // Bài tự tạo trong My Workout (ID riêng, lưu ở file JSON, không nằm trong chương trình có sẵn).
            return mint.woman.female.workout.utils.MyWorkoutStore.find(this, id);
        }
        return mJsonHelper.getAllCategoryHashMap().get(id);
    }

    public static FitnessApplication getApplication() {
        return sInstance;
    }

    public HashMap<Integer, ExerciseObject> getHashMapExercise(){
        return mJsonHelper.getHashMapExercise(discover,version);
    }
    public String getExploreBasePath(){
        return getFilesDir().getPath()+"/discover/" + discover +"/"+version;
    }
    public static FitnessApplication self() {
        return sInstance;
    }

    public int getVersion() {
        return version;
    }


    public void setVersion(int version) {
        this.version = version;
    }

    public int getDiscover() {
        return discover;
    }

    public void setDiscover(int discover) {
        this.discover = discover;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        // targetSdk 36: edge-to-edge can no longer be opted out -> restore legacy insets handling
        EdgeToEdgeHelper.register(this);
        NetworkUtil.initNetwork(this);
        storageCommon = new StorageCommon();
        AppOpenManager appOpenManager = AppOpenManager.getInstance();
        // Configure IDs/exclusions before registering lifecycle callbacks. App-open ads are
        // loaded after an Activity is resumed, not on the Application cold-start path.
        appOpenManager.setAdResumeHighId(MaticAdKeys.OPEN_RESUME_HIGH);
        appOpenManager.setAdResumeMediumId(MaticAdKeys.OPEN_RESUME_MEDIUM);
        appOpenManager.disableAppResumeWithActivity(SplashActivity.class);
        appOpenManager.disableAppResumeWithActivity(PremiumActivity.class);
        appOpenManager.disableAppResumeWithActivity(OnboardFlowActivity.class);
        appOpenManager.init(this, MaticAdKeys.OPEN_RESUME);
        //AppOpenManager.getInstance().disableAppResumeWithActivity(LockScreenActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = getProcessName();
            if (!getPackageName().equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }
        mJsonHelper = JsonHelper.newInstance(getApplicationContext());
        // Giải mã sẵn danh sách bài ở luồng phụ trong lúc Splash chạy; Home lấy từ cache (như Tai Chi).
        final JsonHelper helper = mJsonHelper;
        startupExecutor.execute(helper::prewarmPrograms);
        mDatabase = Database.newInstance(getApplicationContext(), "workout.db");
        setTheme();
        startupExecutor.execute(() -> {
            try {
                AppNotificationHelper.createChannel(getApplicationContext());
                NotificationScheduler.schedule(getApplicationContext());
                // Gửi lý do các ANR trước đó (Play Console chỉ thấy "nativePollOnce").
                AnrReporter.reportRecentAnrs(getApplicationContext());
            } catch (RuntimeException error) {
                Log.w(TAG, "Unable to schedule notifications", error);
            }
        });
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder().build();
    }

    public void setTheme() {
        switch (MyPreference.newInstance(this).getTheme()) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public String getKey(String name){
        return "aaBALAXIbuaa9988";
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
