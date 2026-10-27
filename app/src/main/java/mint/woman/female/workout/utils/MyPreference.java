package mint.woman.female.workout.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import mint.woman.female.workout.R;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyPreference {
    public static MyPreference myPreference;
    private SimpleDateFormat mFormatLastWorkout = new SimpleDateFormat("dd/MMMM/yyyy", Locale.getDefault());
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    private final Context mAppContext;

    public MyPreference(Context context) {
        mAppContext = context.getApplicationContext();
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
        mEditor.apply();
    }

    public int getCurrentId() {
        return mSharedPreferences.getInt("CURRENT_ID", 0);
    }

    public void setCurrentId(int id) {
        mEditor.putInt("CURRENT_ID", id);
        mEditor.apply();
    }

    public boolean isAutoNext() {
        return mSharedPreferences.getBoolean("IS_AUTO_NEXT", true);
    }

    public void setAutoNext(boolean value) {
        mEditor.putBoolean("IS_AUTO_NEXT", value);
        mEditor.apply();
    }

    public boolean isMusicOn() {
        return mSharedPreferences.getBoolean("MUSIC_ON", true);
    }

    public void setMusic(boolean value) {
        mEditor.putBoolean("MUSIC_ON", value);
        mEditor.apply();
    }

    public boolean isRingOn() {
        return mSharedPreferences.getBoolean("RING_ON", true);
    }

    public void setRing(boolean value) {
        mEditor.putBoolean("RING_ON", value);
        mEditor.apply();
    }

    public float getMusicVolume() {
        int soundVolume = mSharedPreferences.getInt("MUSIC_VOLUME", 40);
        float volume = (float) (1 - (Math.log(100 - soundVolume) / Math.log(100)));
        return volume;
    }

    public String getMusicName() {
        return mSharedPreferences.getString("BACK_GROUND_MUSIC", "Default");
    }

    public void setMusicName(String name) {
        mEditor.putString("BACK_GROUND_MUSIC", name);
        mEditor.apply();
    }

    public void setMusicFile(String file) {
        mEditor.putString("BACK_GROUND_MUSIC_FILE", file);
        mEditor.apply();
    }

    public String getMusicFile() {
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
        mEditor.apply();
    }

    public boolean isSpeakerOn() {
        return mSharedPreferences.getBoolean("VOICE_ON", true);
    }

    public void setSpeaker(boolean value) {
        mEditor.putBoolean("VOICE_ON", value);
        mEditor.apply();
    }


    public boolean isKg() {
        return mSharedPreferences.getBoolean("IS_KG", true);
    }

    public void setUnitKg(boolean value) {
        mEditor.putBoolean("IS_KG", value);
        mEditor.apply();
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

    public void setCurrentWeight(float value) {
        mEditor.putFloat("CURRENT_WEIGHT", value);
        mEditor.apply();
    }

    public float getCurrentHeight() {
//        float value = mSharedPreferences.getFloat("CURRENT_HEIGHT", 175f);
//
//        return value;
        float value = mSharedPreferences.getFloat("CURRENT_HEIGHT", 175f);
        if (isKg()) {
            return value;
        } else {
            return (value / 30.48f);
        }
    }

    public float getCurrentWaist() {
        float value = mSharedPreferences.getFloat("CURRENT_WAIST", 75f);
        if (isKg()) {
            return value;
        } else {
            return (value / 30.48f);
        }
    }

    public void setCurrentWaist(float value) {

        mEditor.putFloat("CURRENT_WAIST", value);
        mEditor.apply();
    }

    public void setCurrentHeight(float value) {
        mEditor.putFloat("CURRENT_HEIGHT", value);
        mEditor.apply();
    }

    public String getWeightUnit() {
        return isKg() ? "Kg" : "Lbs";
    }

    public String getHeightUnit() {
        return isKg() ? "Cm" : "Ft";

    }

    public String getTTScode() {
        return getLanguage();
    }

    public void increaseOpenCount() {
        mEditor.putInt("OPEN_COUNT", openCount() + 1);
        mEditor.apply();
    }

    public int openCount() {
        return mSharedPreferences.getInt("OPEN_COUNT", 0);
    }

    public boolean isRated() {
        return mSharedPreferences.getBoolean("IS_RATED", false);
    }

    public void setRated() {
        mEditor.putBoolean("IS_RATED", true);
        mEditor.apply();
    }

    /** Tự thêm khởi động + giãn cơ vào buổi tập (mặc định bật, tắt ở màn Preview / Cài đặt). */
    public boolean isWarmUpCoolDown() {
        return mSharedPreferences.getBoolean("WARM_UP_COOL_DOWN", true);
    }

    public void setWarmUpCoolDown(boolean value) {
        mEditor.putBoolean("WARM_UP_COOL_DOWN", value);
        mEditor.apply();
    }

    /** Không tập động tác bật nhảy (đau gối / lưng dưới, chọn ở onboarding hoặc Cài đặt). */
    public boolean isNoJump() {
        return mSharedPreferences.getBoolean("NO_JUMP", false);
    }

    public void setNoJump(boolean value) {
        mEditor.putBoolean("NO_JUMP", value);
        mEditor.apply();
    }

    /** Câu hỏi đau khớp ở onboarding: 0 không, 1 đầu gối, 2 lưng dưới, 3 cả hai. */
    public int getJointPain() {
        return mSharedPreferences.getInt("JOINT_PAIN", 0);
    }

    public void setJointPain(int value) {
        mEditor.putInt("JOINT_PAIN", value);
        mEditor.apply();
    }

    /** Người dùng đã tự chọn thời gian nghỉ (Cài đặt hoặc màn nghỉ): dùng thay cho thời gian nghỉ ghi trong bài. */
    public boolean hasCustomRestTime() {
        return mSharedPreferences.contains("REST_TIME");
    }

    public void setRestTime(int seconds) {
        mEditor.putString("REST_TIME", String.valueOf(seconds));
        mEditor.apply();
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
        mEditor.apply();
    }

    public boolean isShowDiscover() {
        return mSharedPreferences.getBoolean("IS_SHOW_DISCOVER", true);
    }

    public void setShowDiscover(boolean value) {
        mEditor.putBoolean("IS_SHOW_DISCOVER", value);
        mEditor.apply();
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
        mEditor.apply();
    }

    public String getLanguage() {
        String locale = mSharedPreferences.getString("ST_LANGUAGE", "");
        if (locale.isEmpty() || locale.equals("zh")) {
            // Chưa chọn ngôn ngữ (bỏ qua màn ngôn ngữ): khớp ngôn ngữ máy với mã dữ liệu, vd "zh" → zh_CN, "id" → in_ID
            String matched = LanguageHelper.matchSystemLanguage();
            locale = matched != null ? matched : Locale.getDefault().getLanguage().toLowerCase();
        }
        if (locale.contains("zh") || locale.contains("in")) {
            return locale.replace("-", "_");
        }
        if (locale.length() > 2) {
            return locale.substring(0, 2);
        }
        return locale;
    }

    public void setLanguage(String code) {
        mEditor.putString("ST_LANGUAGE", code);
        mEditor.apply();
    }


    public boolean getShowRate() {
        return mSharedPreferences.getBoolean("IS_SHOW_RATE", false);
    }

    public void setShowRate(boolean value) {
        mEditor.putBoolean("IS_SHOW_RATE", value);
        mEditor.apply();
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
        mEditor.apply();
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
        mEditor.apply();
    }

    public int getPushUpLevel() {
        return mSharedPreferences.getInt("PUSH_UP_LEVEL", 10);
    }

    public void setPushUpLevel(int value) {
        mEditor.putInt("PUSH_UP_LEVEL", value);
        mEditor.apply();
    }

    public boolean isSetReminder() {
        return mSharedPreferences.getBoolean("IS_SET_REMINDER", false);
    }

    public void setReminder(boolean value) {
        mEditor.putBoolean("IS_SET_REMINDER", value);
        mEditor.apply();
    }

    public boolean getStatusAdsId(int id) {
        return mSharedPreferences.getBoolean("STATUS_ADS_" + id, true);
    }

    public void setStatusAdsId(int id, boolean value) {
        mEditor.putBoolean("STATUS_ADS_" + id, value);
        mEditor.apply();
    }

    public int getGender() {
        return Integer.valueOf(mSharedPreferences.getString("USER_GENDER", "0"));
    }

    public void setGender(int value) {
        mEditor.putString("USER_GENDER", String.valueOf(value));
        mEditor.apply();
    }

    public int getAge() {
        return Integer.valueOf(mSharedPreferences.getString("USER_AGE", "0"));
    }

    public void setAge(int value) {
        mEditor.putString("USER_AGE", String.valueOf(value));
        mEditor.apply();
    }

    public int getVersionPlan(String plan) {
        return mSharedPreferences.getInt("VERSION_" + plan, 0);
    }

    public void setVersionPlan(String plan, int version) {
        mEditor.putInt("VERSION_" + plan, version);
        mEditor.apply();
    }

    public void setPremiumMember(boolean value) {
        mEditor.putBoolean("PREMIUM_MEMBER", value);
        mEditor.apply();
        if (value) {
            setShowAds(false);
        }
    }

    public boolean isPremiumMember() {
        return mSharedPreferences.getBoolean("PREMIUM_MEMBER", false);
    }

    public void setSyncGoogleFit(boolean value) {
        mEditor.putBoolean("SYNC_FIT", value);
        mEditor.apply();
    }

    public boolean isSyncGoogleFit() {
        return mSharedPreferences.getBoolean("SYNC_FIT", false);
    }

    public void setRemind(boolean value) {
        mEditor.putBoolean("FIRST_REMIND", value);
        mEditor.apply();
    }

    public boolean isRemind() {
        return mSharedPreferences.getBoolean("FIRST_REMIND", true);
    }

    public void setStandardDiet(boolean value) {
        mEditor.putBoolean("STANDARD_DIET", value);
        mEditor.apply();
    }

    public boolean isStandardDiet() {
        return mSharedPreferences.getBoolean("STANDARD_DIET", true);
    }

    public boolean isShoppingOk(int id) {
        return mSharedPreferences.getBoolean("SHOPPING_OK_" + id, false);
    }

    public void setShoppingOk(int id, boolean value) {
        mEditor.putBoolean("SHOPPING_OK_" + id, value);
        mEditor.apply();
    }

    public boolean isTick() {
        return mSharedPreferences.getBoolean("IS_TICK", true);
    }

    public int getPagerSaved() {
        return mSharedPreferences.getInt("PAGER_SAVED", 0);
    }

    public void setPagerSaved(int value) {
        mEditor.putInt("PAGER_SAVED", value);
        mEditor.apply();
    }

    public String getTheme() {
        return mSharedPreferences.getString("THEME", "light");
    }

    public int getGoalOfWeek() {
        return mSharedPreferences.getInt("GOAL_OF_WEEK", 5);
    }

    public int getFirstDayOfWeek() {
        return mSharedPreferences.getInt("FIRST_DAY_OF_WEEK", 0);
    }

    public void setFirstDayOfWeek(int value) {
        mEditor.putInt("FIRST_DAY_OF_WEEK", value);
        mEditor.apply();

    }

    public void setGoalOfWeek(int value) {
        mEditor.putInt("GOAL_OF_WEEK", value);
        mEditor.apply();

    }

    public String getTimeRemindDaily() {
        return mSharedPreferences.getString("TIME_REMIND_DAILY", "20:00");
    }

    /** Người dùng đã chọn giờ nhắc hằng ngày (onboarding hoặc màn nhắc tập), không phải giá trị mặc định. */
    public boolean hasDailyReminderTime() {
        return mSharedPreferences.contains("TIME_REMIND_DAILY");
    }

    public void setTimeRemindDaily(String value) {
        mEditor.putString("TIME_REMIND_DAILY", value);
        mEditor.apply();
    }

    public boolean isFirstOpen() {
        return mSharedPreferences.getBoolean("IS_FIRST_OPEN", true);
    }

    public void setFirstOpen(boolean value) {
        mEditor.putBoolean("IS_FIRST_OPEN", value);
        mEditor.apply();
    }

    public boolean isTranslate() {
        return mSharedPreferences.getBoolean("IS_TRANSLATE_NAME", true);
    }

    public void setTranslate(boolean value) {
        mEditor.putBoolean("IS_TRANSLATE_NAME", value);
        mEditor.apply();
    }

    public int getSaveProgress(int programId, int day) {
        return mSharedPreferences.getInt("SAVE_PROGRESS_" + programId + "_" + day, 0);
    }

    public void setSaveProgress(int programId, int day, int value) {
        mEditor.putInt("SAVE_PROGRESS_" + programId + "_" + day, value);
        mEditor.apply();
    }

    public float getSaveProgressPercent(int programId, int day) {
        return mSharedPreferences.getFloat("SAVE_PROGRESS_PERCENT_" + programId + "_" + day, 0.0f);
    }

    public void setSaveProgressPercent(int programId, int day, float value) {
        mEditor.putFloat("SAVE_PROGRESS_PERCENT_" + programId + "_" + day, value);
        mEditor.apply();
    }

    public void saveProgressWorkout(int id, int mDayIndex, int currentExerciseCount) {
        mEditor.putInt("CURRENT_PROGRESS_" + id + "DAY_" + mDayIndex, currentExerciseCount);
        mEditor.apply();
    }

    public int getSaveProgressWorkout(int id, int mDayIndex) {
        return mSharedPreferences.getInt("CURRENT_PROGRESS_" + id + "DAY_" + mDayIndex, 0);
    }


    public int getCountOption() {
        return mSharedPreferences.getInt("COUNT_OPTION", 1);
    }

    public void setCountOption(int option) {
        mEditor.putInt("COUNT_OPTION", option);
        mEditor.apply();

    }

    public long getBackUpTime() {
        return mSharedPreferences.getLong("BACKUP_TIME", 0);
    }

    public void setBackUpTime(long time) {
        mEditor.putLong("BACKUP_TIME", time);
        mEditor.apply();
    }

    /** Thời điểm hết hạn của gói ưu đãi 50% (Lucky Draw / flash sale), 0 nếu chưa có. */
    public long getPromoEndTime() {
        return mSharedPreferences.getLong("PROMO_END_TIME", 0);
    }

    public void setPromoEndTime(long time) {
        mEditor.putLong("PROMO_END_TIME", time);
        mEditor.apply();
    }

    public String getVoiceModel(String language) {
        return mSharedPreferences.getString("voice_" + language, defaultVoiceModel(language));
    }

    /** Chưa chọn giọng: English hoặc ngôn ngữ có giọng thu sẵn (Remote Config `voice`) → giọng nữ "woman", còn lại TTS máy. */
    public static String defaultVoiceModel(String language) {
        return language.contains("en") || isVoiceSupported(language, "woman") ? "woman" : "default";
    }

    /** Remote Config `voice` = {"woman":"en@fr@vi","man":...}; khớp đúng từng mã ngôn ngữ như màn chọn giọng. */
    public static boolean isVoiceSupported(String language, String key) {
        try {
            FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
            if (!config.getBoolean("enable_voice")) return false;
            String codes = new JSONObject(config.getString("voice")).optString(key, "");
            return ("@" + codes + "@").contains("@" + language + "@");
        } catch (Exception e) {
            return false;
        }
    }

    public void setVoiceModel(String language, String model) {
        mEditor.putString("voice_" + language, model);
        mEditor.apply();
    }

    public void setVoiceModelName(String language, String modelName) {
        mEditor.putString("coach_" + language, modelName);
        mEditor.apply();
    }

    public String getVoiceModelName(String language) {
        // Chưa chọn giọng thì tên HLV theo giọng mặc định, khớp tên lúc chọn ở màn giọng (và tên file câu giới thiệu).
        String fallback = "woman".equals(getVoiceModel(language)) ? mAppContext.getString(R.string.name_female) : "Default";
        return mSharedPreferences.getString("coach_" + language, fallback);
    }
    public void saveLastWorkout(long millis) {
        mEditor.putLong("LAST_WORKOUT", millis);
        mEditor.apply();
    }
    public long getLastWorkout() {
        return mSharedPreferences.getLong("LAST_WORKOUT", 0);
    }

    // Onboarding-related getters/setters
    public String getUserTargetFocus() {
        return mSharedPreferences.getString("USER_TARGET_FOCUS", "Relaxation");
    }

    public void setUserTargetFocus(String value) {
        mEditor.putString("USER_TARGET_FOCUS", value);
        mEditor.apply();
    }

    public boolean requiresSeated() {
        return mSharedPreferences.getBoolean("REQUIRES_SEATED", false);
    }

    public void setRequiresSeated(boolean value) {
        mEditor.putBoolean("REQUIRES_SEATED", value);
        mEditor.apply();
    }

    public int getAvgDuration() {
        return mSharedPreferences.getInt("AVG_DURATION", 15);
    }

    public void setAvgDuration(int value) {
        mEditor.putInt("AVG_DURATION", value);
        mEditor.apply();
    }

    public String getUserExperience() {
        return mSharedPreferences.getString("USER_EXPERIENCE", "beginner");
    }

    public void setUserExperience(String value) {
        mEditor.putString("USER_EXPERIENCE", value);
        mEditor.apply();
    }
    public boolean isOnboarded() {
        return mSharedPreferences.getBoolean("IS_ONBOARDED", false);
    }

    public void setOnboarded(boolean value) {
        mEditor.putBoolean("IS_ONBOARDED", value);
        mEditor.apply();
    }

    // ============ Onboarding quiz v2 ============

    /** One of: lose_weight, tone, flexibility, strength */
    public String getUserGoal() {
        return mSharedPreferences.getString("USER_GOAL", "tone");
    }

    public void setUserGoal(String value) {
        mEditor.putString("USER_GOAL", value);
        mEditor.apply();
    }

    /** Comma separated focus keys, e.g. "abs,butt". */
    public String getUserFocusAreas() {
        return mSharedPreferences.getString("USER_FOCUS_AREAS", "body");
    }

    public void setUserFocusAreas(String value) {
        mEditor.putString("USER_FOCUS_AREAS", value);
        mEditor.apply();
    }

    /** Target weight, always stored in kg. */
    public float getTargetWeightKg() {
        return mSharedPreferences.getFloat("TARGET_WEIGHT", 0f);
    }

    public void setTargetWeightKg(float value) {
        mEditor.putFloat("TARGET_WEIGHT", value);
        mEditor.apply();
    }
    public boolean isEnableIntro() {
        return mSharedPreferences.getBoolean("ENABLE_INTRO", true);
    }

    // ============ 📱 NOTIFICATION TRACKING METRICS ============

    public void incrementNotificationRequestCount() {
        int count = mSharedPreferences.getInt("NOTIFICATION_REQUEST_COUNT", 0);
        mEditor.putInt("NOTIFICATION_REQUEST_COUNT", count + 1);
        mEditor.apply();
    }

    public int getNotificationRequestCount() {
        return mSharedPreferences.getInt("NOTIFICATION_REQUEST_COUNT", 0);
    }

    public void incrementNotificationAllowCount() {
        int count = mSharedPreferences.getInt("NOTIFICATION_ALLOW_COUNT", 0);
        mEditor.putInt("NOTIFICATION_ALLOW_COUNT", count + 1);
        mEditor.apply();
    }

    public int getNotificationAllowCount() {
        return mSharedPreferences.getInt("NOTIFICATION_ALLOW_COUNT", 0);
    }

    public void incrementNotificationDeniedCount() {
        int count = mSharedPreferences.getInt("NOTIFICATION_DENIED_COUNT", 0);
        mEditor.putInt("NOTIFICATION_DENIED_COUNT", count + 1);
        mEditor.apply();
    }

    public int getNotificationDeniedCount() {
        return mSharedPreferences.getInt("NOTIFICATION_DENIED_COUNT", 0);
    }

    public double getNotificationAllowRate() {
        int allowed = getNotificationAllowCount();
        int total = getNotificationRequestCount();
        return total > 0 ? (allowed / (double) total) * 100 : 0.0;
    }

    public void incrementNotificationSentCount() {
        int count = mSharedPreferences.getInt("NOTIFICATION_SENT_COUNT", 0);
        mEditor.putInt("NOTIFICATION_SENT_COUNT", count + 1);
        mEditor.putLong("LAST_NOTIFICATION_TIME", System.currentTimeMillis());
        mEditor.apply();
    }

    public int getNotificationSentCount() {
        return mSharedPreferences.getInt("NOTIFICATION_SENT_COUNT", 0);
    }

    public void incrementNotificationClickCount() {
        int count = mSharedPreferences.getInt("NOTIFICATION_CLICK_COUNT", 0);
        mEditor.putInt("NOTIFICATION_CLICK_COUNT", count + 1);
        mEditor.apply();
    }

    public int getNotificationClickCount() {
        return mSharedPreferences.getInt("NOTIFICATION_CLICK_COUNT", 0);
    }

    public double getNotificationClickRate() {
        int clicked = getNotificationClickCount();
        int sent = getNotificationSentCount();
        return sent > 0 ? (clicked / (double) sent) * 100 : 0.0;
    }

    public void incrementNotificationRetentionCount() {
        int count = mSharedPreferences.getInt("NOTIFICATION_RETENTION_COUNT", 0);
        mEditor.putInt("NOTIFICATION_RETENTION_COUNT", count + 1);
        mEditor.apply();
    }

    public int getNotificationRetentionCount() {
        return mSharedPreferences.getInt("NOTIFICATION_RETENTION_COUNT", 0);
    }

    public double getNotificationRetentionRate() {
        int retention = getNotificationRetentionCount();
        int sent = getNotificationSentCount();
        return sent > 0 ? (retention / (double) sent) * 100 : 0.0;
    }

    public long getLastNotificationTime() {
        return mSharedPreferences.getLong("LAST_NOTIFICATION_TIME", 0);
    }

    public NotificationMetrics getNotificationMetrics() {
        return new NotificationMetrics(
                getNotificationRequestCount(),
                getNotificationAllowCount(),
                getNotificationDeniedCount(),
                getNotificationAllowRate(),
                getNotificationSentCount(),
                getNotificationClickCount(),
                getNotificationClickRate(),
                getNotificationRetentionCount(),
                getNotificationRetentionRate()
        );
    }

    public static class NotificationMetrics {
        public int totalRequested;
        public int totalAllowed;
        public int totalDenied;
        public double allowRate;
        public int totalSent;
        public int totalClicked;
        public double clickRate;
        public int totalRetention;
        public double retentionRate;

        public NotificationMetrics(int totalRequested, int totalAllowed, int totalDenied,
                                   double allowRate, int totalSent, int totalClicked,
                                   double clickRate, int totalRetention, double retentionRate) {
            this.totalRequested = totalRequested;
            this.totalAllowed = totalAllowed;
            this.totalDenied = totalDenied;
            this.allowRate = allowRate;
            this.totalSent = totalSent;
            this.totalClicked = totalClicked;
            this.clickRate = clickRate;
            this.totalRetention = totalRetention;
            this.retentionRate = retentionRate;
        }

        @Override
        public String toString() {
            return "NotificationMetrics{" +
                    "totalRequested=" + totalRequested +
                    ", totalAllowed=" + totalAllowed +
                    ", totalDenied=" + totalDenied +
                    ", allowRate=" + String.format("%.2f%%", allowRate) +
                    ", totalSent=" + totalSent +
                    ", totalClicked=" + totalClicked +
                    ", clickRate=" + String.format("%.2f%%", clickRate) +
                    ", totalRetention=" + totalRetention +
                    ", retentionRate=" + String.format("%.2f%%", retentionRate) +
                    '}';
        }
    }
    public boolean isMigrateHistory() {
        return mSharedPreferences.getBoolean("IS_MIGRATE", false);
    }
    public void setMigrateHistory(boolean value) {
        mEditor.putBoolean("IS_MIGRATE", value);
        mEditor.apply();
    }
}
