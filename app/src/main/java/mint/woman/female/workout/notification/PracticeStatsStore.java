package mint.woman.female.workout.notification;

import android.content.Context;
import android.content.SharedPreferences;

public class PracticeStatsStore {
    private static final String PREF_NAME = "practice_stats_pref";

    private static final String KEY_STREAK = "streak_count";
    private static final String KEY_CONSECUTIVE = "consecutive_active_days";
    private static final String KEY_MISSED = "missed_days";
    private static final String KEY_COMPLETE_TODAY = "did_complete_today";
    private static final String KEY_PREFERRED_HOUR = "preferred_hour";
    private static final String KEY_PREFERRED_MINUTE = "preferred_minute";
    private static final String KEY_LAST_COMPLETED = "last_completed_at";

    public static void save(Context context, PracticeStats stats) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit()
                .putInt(KEY_STREAK, stats.getStreakCount())
                .putInt(KEY_CONSECUTIVE, stats.getConsecutiveActiveDays())
                .putInt(KEY_MISSED, stats.getMissedDays())
                .putBoolean(KEY_COMPLETE_TODAY, stats.isDidCompleteToday())
                .putInt(KEY_PREFERRED_HOUR, stats.getPreferredHour())
                .putInt(KEY_PREFERRED_MINUTE, stats.getPreferredMinute())
                .putLong(KEY_LAST_COMPLETED, stats.getLastCompletedAt())
                .apply();
    }

    public static PracticeStats get(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return new PracticeStats(
                sp.getInt(KEY_STREAK, 0),
                sp.getInt(KEY_CONSECUTIVE, 0),
                sp.getInt(KEY_MISSED, 0),
                sp.getBoolean(KEY_COMPLETE_TODAY, false),
                sp.getInt(KEY_PREFERRED_HOUR, 19),
                sp.getInt(KEY_PREFERRED_MINUTE, 0),
                sp.getLong(KEY_LAST_COMPLETED, 0L)
        );
    }

    public static void markCompletedToday(Context context) {
        PracticeStats stats = get(context);
        stats.setDidCompleteToday(true);
        save(context, stats);
    }

    public static void resetDailyCompletion(Context context) {
        PracticeStats stats = get(context);
        stats.setDidCompleteToday(false);
        save(context, stats);
    }
}