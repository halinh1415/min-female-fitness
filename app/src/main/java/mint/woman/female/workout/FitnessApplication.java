package mint.woman.female.workout;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;

import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.utils.Database;
import mint.woman.female.workout.utils.JsonHelper;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.NetworkUtil;
import mint.woman.female.workout.utils.StorageCommon;

import java.util.HashMap;
import java.util.Locale;

public class FitnessApplication extends Application {


    public JsonHelper mJsonHelper;
    public Database mDatabase;
    private Gson mGSon;
    private static FitnessApplication sInstance;
    private static FitnessApplication context;
     private HashMap<Integer, ProgramObject> allProgram =null;
    protected StorageCommon storageCommon;

    public StorageCommon  getStorageCommon() {
        return storageCommon;
    }

    public HashMap<Integer,ProgramObject> getAllProgram(){
        if (allProgram ==null){
            allProgram = mJsonHelper.getAllCategoryHashMap();
        }
        return allProgram;
    }
    public ProgramObject getProgram(int id){
        if (allProgram ==null){
            allProgram = mJsonHelper.getAllCategoryHashMap();
        }
        return allProgram.get(id);
    }
    public static FitnessApplication getApplication() {
        return context;
    }

    public static FitnessApplication self() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        context = this;
        NetworkUtil.initNetwork(this);
        FirebaseApp.initializeApp(this);
        storageCommon = new StorageCommon();
        mGSon = new Gson();
        mJsonHelper = JsonHelper.newInstance(getApplicationContext(), getKey(getPackageName()));
        allProgram = mJsonHelper.getAllCategoryHashMap();
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

    public Gson getGSon() {
        return mGSon;
    }
}
