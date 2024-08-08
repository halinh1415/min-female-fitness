package mint.woman.female.workout.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyPreference {
    public static MyPreference myPreference;
    private SimpleDateFormat mFormatLastWorkout = new SimpleDateFormat("dd/MMMM/yyyy", Locale.getDefault());
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public MyPreference(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = mSharedPreferences.edit();
    }

    public static MyPreference newInstance(Context context) {
        if (myPreference == null) {
            myPreference = new MyPreference(context);
        }
        return myPreference;
    }

    public int getPlanProgress(int id) {
        return mSharedPreferences.getInt("PLAN_PROGRESS_" + id, 0);
    }

    public void setPlanProgress(int id, int progress) {
        mEditor.putInt("PLAN_PROGRESS_" + id, progress);
        mEditor.commit();
    }

    public int getCurrentId() {
        return mSharedPreferences.getInt("CURRENT_ID", 0);
    }

    public void setCurrentId(int id) {
        mEditor.putInt("CURRENT_ID", id);
        mEditor.commit();
    }

    public boolean isAutoNext() {
        return mSharedPreferences.getBoolean("IS_AUTO_NEXT", true);
    }

    public void setAutoNext(boolean value) {
        mEditor.putBoolean("IS_AUTO_NEXT", value);
        mEditor.commit();
    }

    public boolean isMusicOn() {
        return mSharedPreferences.getBoolean("MUSIC_ON", true);
    }

    public void setMusic(boolean value) {
        mEditor.putBoolean("MUSIC_ON", value);
        mEditor.commit();
    }

    public boolean isRingOn() {
        return mSharedPreferences.getBoolean("RING_ON", true);
    }

    public void setRing(boolean value) {
        mEditor.putBoolean("RING_ON", value);
        mEditor.commit();
    }

    public float getMusicVolume() {
        int soundVolume = mSharedPreferences.getInt("MUSIC_VOLUME", 90);
        float volume = (float) (1 - (Math.log(100 - soundVolume) / Math.log(100)));
        return volume;
    }

    public String getMusicName() {
        return mSharedPreferences.getString("BACK_GROUND_MUSIC", "Default");
    }
    public void setMusicName(String name){
        mEditor.putString("BACK_GROUND_MUSIC", name);
        mEditor.commit();
    }
    public void setMusicFile(String file){
        mEditor.putString("BACK_GROUND_MUSIC_FILE", file);
        mEditor.commit();
    }
    public String getMusicFile(){
        return mSharedPreferences.getString("BACK_GROUND_MUSIC_FILE", "Default.mp3");
    }

    public float getSpeakerVolume() {
        int soundVolume = mSharedPreferences.getInt("VOICE_VOLUME", 90);
        float volume = (float) (1 - (Math.log(100 - soundVolume) / Math.log(100)));
        return volume;
    }

    public float getSpeed() {
        return mSharedPreferences.getFloat("EXERCISE_SPEED", 1.0f);
    }

    public void setSpeed(float value) {
        mEditor.putFloat("EXERCISE_SPEED", value);
        mEditor.commit();
    }

    public boolean isSpeakerOn() {
        return mSharedPreferences.getBoolean("VOICE_ON", true);
    }

    public void setSpeaker(boolean value) {
        mEditor.putBoolean("VOICE_ON", value);
        mEditor.commit();
    }


    public boolean isKg() {
        return mSharedPreferences.getBoolean("IS_KG", true);
    }
    public void setUnitKg(boolean value){
        mEditor.putBoolean("IS_KG", value);
        mEditor.commit();
    }



    public float getCurrentWeightKG() {
        return mSharedPreferences.getFloat("CURRENT_WEIGHT", 65.0f);
    }

    public float getCurrentHeightCm() {
        return mSharedPreferences.getFloat("CURRENT_HEIGHT", 175.0f);
    }
    public float getCurrentWaistCm() {
        return mSharedPreferences.getFloat("CURRENT_WAIST", 75.0f);
    }

    public float getCurrentHeightM() {
        return mSharedPreferences.getFloat("CURRENT_HEIGHT", 175.0f) / 100;
    }

    public float getCurrentWeight() {
        float value = mSharedPreferences.getFloat("CURRENT_WEIGHT", 65.0f);
        return isKg() ? value : (value * 2.20462f);
    }

    public void  setCurrentWeight(float value) {
        mEditor.putFloat("CURRENT_WEIGHT", value);
        mEditor.commit();
    }

    public float getCurrentHeight() {
//        float value = mSharedPreferences.getFloat("CURRENT_HEIGHT", 175f);
//
//        return value;
        float value = mSharedPreferences.getFloat("CURRENT_HEIGHT", 175f);
        if (isKg()){
            return value;
        }else{
            return (value / 30.48f);
        }
    }

    public float getCurrentWaist() {
        float value = mSharedPreferences.getFloat("CURRENT_WAIST", 75f);
        if (isKg()){
            return value;
        }else {
            return (value / 30.48f);
        }
    }
    public void setCurrentWaist(float value) {

        mEditor.putFloat("CURRENT_WAIST", value);
        mEditor.commit();
    }
    public void setCurrentHeight(float value) {
        mEditor.putFloat("CURRENT_HEIGHT", value);
        mEditor.commit();
    }

    public String getWeightUnit() {
        return isKg() ? "Kg" : "Lbs";
    }

    public String getHeightUnit() {
        return isKg()?"Cm":"Ft";

    }

    public String getTTScode() {
        return getLanguage();
    }

    public void increaseOpenCount() {
        mEditor.putInt("OPEN_COUNT", openCount() + 1);
        mEditor.commit();
    }

    public int openCount() {
        return mSharedPreferences.getInt("OPEN_COUNT", 0);
    }

    public boolean isRated() {
        return mSharedPreferences.getBoolean("IS_RATED", false);
    }

    public void setRated() {
        mEditor.putBoolean("IS_RATED", true);
        mEditor.commit();
    }

    public int getRestTime() {
        int value = Integer.valueOf(mSharedPreferences.getString("REST_TIME", "20"));
        return value;
    }

    public boolean isShowAds() {
        if (isPremiumMember()) return false;
        return mSharedPreferences.getBoolean("IS_SHOW_ADS", true);
    }

    public void setShowAds(boolean value) {
        mEditor.putBoolean("IS_SHOW_ADS", value);
        mEditor.commit();
    }

    public boolean isShowDiscover() {
        return mSharedPreferences.getBoolean("IS_SHOW_DISCOVER", true);
    }

    public void setShowDiscover(boolean value) {
        mEditor.putBoolean("IS_SHOW_DISCOVER", value);
        mEditor.commit();
    }

    public void restartProgress() {
        for (int i = 0; i < 10; i++) {
            setPlanProgress(i, 0);
        }
    }

    public boolean getOK() {
        return mSharedPreferences.getBoolean("OK_SPLASH", true);
    }

    public void setOK(boolean value) {
        mEditor.putBoolean("OK_SPLASH", value);
        mEditor.commit();
    }

    public String getLanguage() {
        String locale =  mSharedPreferences.getString("ST_LANGUAGE", "");
        if (locale.isEmpty()){
            locale =  Locale.getDefault().getLanguage().toLowerCase();
        }
        return locale;
    }
    public void setLanguage(String code) {
        mEditor.putString("ST_LANGUAGE",code);
        mEditor.commit();
    }


    public boolean getShowRate() {
        return mSharedPreferences.getBoolean("IS_SHOW_RATE", false);
    }

    public void setShowRate(boolean value) {
        mEditor.putBoolean("IS_SHOW_RATE", value);
        mEditor.commit();
    }

    public int getProgressDiet() {
        int ret = 0;
        for (int i = 0; i <= 30; i++) {
            if (isDietDone(i)) ret++;
        }
        return ret;
    }

    public void resetDietProgress() {
        for (int i = 0; i <= 30; i++) {
            setDietDone(i, false);
        }
    }

    public boolean isDietDone(int day) {
        return mSharedPreferences.getBoolean("IS_DONE_DIET" + day, false);
    }

    public void setDietDone(int day, boolean value) {
        mEditor.putBoolean("IS_DONE_DIET" + day, value);
        mEditor.commit();
    }

    public String getLastWorkout(int id) {
        String time = mSharedPreferences.getString("LAST_WORKOUT_" + id, "");
        if (!time.isEmpty()) {
            return mFormatLastWorkout.format(new Date(Long.valueOf(time)));
        }
        return "";
    }

    public void setLastWorkout(int id, String value) {
        mEditor.putString("LAST_WORKOUT_" + id, value);
        mEditor.commit();
    }

    public int getPushUpLevel() {
        return mSharedPreferences.getInt("PUSH_UP_LEVEL", 10);
    }

    public void setPushUpLevel(int value) {
        mEditor.putInt("PUSH_UP_LEVEL", value);
        mEditor.commit();
    }

    public boolean isSetReminder() {
        return mSharedPreferences.getBoolean("IS_SET_REMINDER", false);
    }

    public void setReminder(boolean value) {
        mEditor.putBoolean("IS_SET_REMINDER", value);
        mEditor.commit();
    }

    public boolean getStatusAdsId(int id) {
        return mSharedPreferences.getBoolean("STATUS_ADS_" + id, true);
    }

    public void setStatusAdsId(int id, boolean value) {
        mEditor.putBoolean("STATUS_ADS_" + id, value);
        mEditor.commit();
    }

    public int getGender() {
        return Integer.valueOf(mSharedPreferences.getString("USER_GENDER", "0"));
    }

    public void setGender(int value) {
        mEditor.putString("USER_GENDER", String.valueOf(value));
        mEditor.commit();
    }

    public int getAge() {
        return Integer.valueOf(mSharedPreferences.getString("USER_AGE", "0"));
    }

    public void setAge(int value) {
        mEditor.putString("USER_AGE", String.valueOf(value));
        mEditor.commit();
    }

    public int getVersionPlan(String plan) {
        return mSharedPreferences.getInt("VERSION_" + plan, 0);
    }

    public void setVersionPlan(String plan, int version) {
        mEditor.putInt("VERSION_" + plan, version);
        mEditor.commit();
    }

    public void setPremiumMember(boolean value) {
        mEditor.putBoolean("PREMIUM_MEMBER", value);
        mEditor.commit();
        if (value) {
            setShowAds(false);
        }
    }

    public boolean isPremiumMember() {
        return mSharedPreferences.getBoolean("PREMIUM_MEMBER", false);
    }

    public void setSyncGoogleFit(boolean value) {
        mEditor.putBoolean("SYNC_GOOGLE_FIT", value);
        mEditor.commit();
    }

    public boolean isSyncGoogleFit() {
        return mSharedPreferences.getBoolean("SYNC_GOOGLE_FIT", false);
    }

    public void setRemind(boolean value) {
        mEditor.putBoolean("FIRST_REMIND", value);
        mEditor.commit();
    }

    public boolean isRemind() {
        return mSharedPreferences.getBoolean("FIRST_REMIND", true);
    }

    public void setStandardDiet(boolean value) {
        mEditor.putBoolean("STANDARD_DIET", value);
        mEditor.commit();
    }

    public boolean isStandardDiet() {
        return mSharedPreferences.getBoolean("STANDARD_DIET", true);
    }

    public boolean isShoppingOk(int id) {
        return mSharedPreferences.getBoolean("SHOPPING_OK_" + id, false);
    }

    public void setShoppingOk(int id, boolean value) {
        mEditor.putBoolean("SHOPPING_OK_" + id, value);
        mEditor.commit();
    }
    public boolean isTick(){
        return mSharedPreferences.getBoolean("IS_TICK", true);
    }

    public int getPagerSaved() {
        return mSharedPreferences.getInt("PAGER_SAVED", 0);
    }

    public void setPagerSaved(int value) {
        mEditor.putInt("PAGER_SAVED", value);
        mEditor.commit();
    }

    public String getTheme() {
        return mSharedPreferences.getString("THEME", "light");
    }
    public int getGoalOfWeek() {
        return mSharedPreferences.getInt("GOAL_OF_WEEK",5);
    }
    public int getFirstDayOfWeek() {
        return mSharedPreferences.getInt("FIRST_DAY_OF_WEEK",0);
    }
    public void setFirstDayOfWeek(int value) {
        mEditor.putInt("FIRST_DAY_OF_WEEK",value);
        mEditor.commit();

    }
    public void setGoalOfWeek(int value) {
        mEditor.putInt("GOAL_OF_WEEK",value);
        mEditor.commit();

    }
    public String getTimeRemindDaily(){
        return mSharedPreferences.getString("TIME_REMIND_DAILY","20:00");
    }
    public void setTimeRemindDaily(String value){
        mEditor.putString("TIME_REMIND_DAILY",value);
        mEditor.commit();
    }
    public boolean isFirstOpen() {
        return mSharedPreferences.getBoolean("IS_FIRST_OPEN", true);
    }
    public void setFirstOpen(boolean value){
        mEditor.putBoolean("IS_FIRST_OPEN", value);
        mEditor.commit();
    }
    public boolean isTranslate() {
        return mSharedPreferences.getBoolean("IS_TRANSLATE_NAME", true);
    }
    public void setTranslate(boolean value){
        mEditor.putBoolean("IS_TRANSLATE_NAME", value);
        mEditor.commit();
    }
    public int getSaveProgress(int programId, int day) {
        return mSharedPreferences.getInt("SAVE_PROGRESS_" + programId + "_" + day, 0);
    }

    public void setSaveProgress(int programId, int day, int value) {
        mEditor.putInt("SAVE_PROGRESS_" + programId + "_" + day, value);
        mEditor.commit();
    }
    public float getSaveProgressPercent(int programId, int day) {
        return mSharedPreferences.getFloat("SAVE_PROGRESS_PERCENT_" + programId + "_" + day, 0.0f);
    }
    public void setSaveProgressPercent(int programId, int day, float value) {
        mEditor.putFloat("SAVE_PROGRESS_PERCENT_" + programId + "_" + day, value);
        mEditor.commit();
    }
    public void saveProgressWorkout(int id, int mDayIndex, int currentExerciseCount) {
        mEditor.putInt("CURRENT_PROGRESS_" + id + "DAY_" + mDayIndex, currentExerciseCount);
        mEditor.commit();
    }

    public int getSaveProgressWorkout(int id, int mDayIndex) {
        return mSharedPreferences.getInt("CURRENT_PROGRESS_" + id + "DAY_" + mDayIndex, 0);
    }


    public int getCountOption() {
        return mSharedPreferences.getInt("COUNT_OPTION",1);
    }
    public void setCountOption(int option){
        mEditor.putInt("COUNT_OPTION",option);
        mEditor.commit();

    }
    public long getBackUpTime(){
        return mSharedPreferences.getLong("BACKUP_TIME",0);
    }
    public void setBackUpTime(long time){
        mEditor.putLong("BACKUP_TIME",time);
        mEditor.commit();
    }
}
