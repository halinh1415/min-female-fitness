package mint.woman.female.workout;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;

import mint.woman.female.workout.activity.SplashActivity;
import mint.woman.female.workout.activity.premium.PremiumActivity;
import mint.woman.female.workout.admodule.AppOpenManager;
import mint.woman.female.workout.admodule.MaticAdKeys;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.utils.Database;
import mint.woman.female.workout.utils.JsonHelper;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.NetworkUtil;
import mint.woman.female.workout.utils.StorageCommon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class FitnessApplication extends Application {


    public JsonHelper mJsonHelper;
    public Database mDatabase;
    private static FitnessApplication sInstance;
    private static FitnessApplication context;
    private int version =1001,discover =0;
    protected StorageCommon storageCommon;
    private List<PlanObject.ActionObject> actionList = new ArrayList<>();

    public StorageCommon  getStorageCommon() {
        return storageCommon;
    }



    public List<PlanObject.ActionObject> getActionList() {
        return actionList;
    }

    public void setActionList(List<PlanObject.ActionObject> actionList) {
        this.actionList.clear();
        this.actionList.addAll(actionList);
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
        return mJsonHelper.getAllCategoryHashMap().get(id);
    }

    public static FitnessApplication getApplication() {
        return context;
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
        context = this;
        NetworkUtil.initNetwork(this);
        FirebaseApp.initializeApp(this);
        AppOpenManager.getInstance().init(this, MaticAdKeys.OPEN_RESUME);
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(PremiumActivity.class);
        //AppOpenManager.getInstance().disableAppResumeWithActivity(LockScreenActivity.class);
        AppOpenManager.getInstance().setAdResumeHighId(MaticAdKeys.OPEN_RESUME_HIGH);
        AppOpenManager.getInstance().setAdResumeMediumId(MaticAdKeys.OPEN_RESUME_MEDIUM);
        storageCommon = new StorageCommon();
        mJsonHelper = JsonHelper.newInstance(getApplicationContext(), getKey(getPackageName()));
        mDatabase = Database.newInstance(getApplicationContext(), "workout.db");
        setTheme();

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
        if (!lang.isEmpty() && lang.length() > 2) {
            lang = lang.substring(0, 2);
        } else {
            lang = Locale.getDefault().getLanguage();
        }
        super.attachBaseContext(LanguageHelper.setLanguage(newBase, lang));

    }

}
