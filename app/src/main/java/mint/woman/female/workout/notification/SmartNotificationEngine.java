package mint.woman.female.workout.notification;

import android.content.Context;

import mint.woman.female.workout.R;

public class SmartNotificationEngine {

    public static NotificationContent evaluate(Context context, PracticeStats stats, int nowHour) {
        if (stats == null) return null;
        if (stats.isDidCompleteToday()) return null;

        if (shouldSendRecovery(stats)) {
            return new NotificationContent(
                    NotificationType.RECOVERY_DAY,
                    context.getString(R.string.noti_recovery_title),
                    context.getString(R.string.noti_recovery_message),
                    "app://practice/recovery"
            );
        }

        if (shouldProtectStreak(stats, nowHour)) {
            return new NotificationContent(
                    NotificationType.STREAK_PROTECT,
                    context.getString(R.string.noti_streak_title, stats.getStreakCount()),
                    context.getString(R.string.noti_streak_message),
                    "app://practice/quick"
            );
        }

        if (stats.getMissedDays() == 1) {
            return new NotificationContent(
                    NotificationType.GENTLE_COMEBACK,
                    context.getString(R.string.noti_comeback_title),
                    context.getString(R.string.noti_comeback_message),
                    "app://practice/comeback"
            );
        }

        if (stats.getMissedDays() >= 2 && stats.getMissedDays() <= 4) {
            return new NotificationContent(
                    NotificationType.REENGAGE_LIGHT,
                    context.getString(R.string.noti_reengage_title),
                    context.getString(R.string.noti_reengage_message),
                    "app://practice/comeback"
            );
        }

        return new NotificationContent(
                NotificationType.DAILY_REMINDER,
                context.getString(R.string.noti_daily_title),
                context.getString(R.string.noti_daily_message),
                "app://home/discover"
        );
    }

    private static boolean shouldSendRecovery(PracticeStats stats) {
        return stats.getConsecutiveActiveDays() >= 6;
    }

    private static boolean shouldProtectStreak(PracticeStats stats, int nowHour) {
        return stats.getStreakCount() >= 3 && nowHour >= 18 && nowHour <= 21;
    }
}