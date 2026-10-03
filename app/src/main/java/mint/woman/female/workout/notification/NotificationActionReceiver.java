package mint.woman.female.workout.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Nút "Nhắc sau 1 giờ" trên thông báo: ẩn thông báo, hẹn alarm 1 giờ sau rồi hiện lại
 * đúng nội dung đó.
 */
public class NotificationActionReceiver extends BroadcastReceiver {

    static final String ACTION_SNOOZE = "mint.woman.female.workout.action.SNOOZE_REMINDER";
    static final String ACTION_SNOOZE_FIRE = "mint.woman.female.workout.action.SNOOZE_FIRE";

    private static final String EXTRA_ID = "noti_id";
    private static final String EXTRA_TITLE = "noti_title";
    private static final String EXTRA_MESSAGE = "noti_message";
    private static final String EXTRA_LINK = "noti_link";
    private static final String EXTRA_CHANNEL = "noti_channel";

    private static final int REQUEST_SNOOZE = 20401;
    private static final int REQUEST_SNOOZE_FIRE = 20402;
    private static final long SNOOZE_MILLIS = 60 * 60 * 1000L;

    static PendingIntent snoozeIntent(Context context, NotificationContent content, String channelId, int notificationId) {
        Intent intent = new Intent(context, NotificationActionReceiver.class)
                .setAction(ACTION_SNOOZE);
        putContent(intent, content, channelId, notificationId);
        return PendingIntent.getBroadcast(context, REQUEST_SNOOZE + notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static void putContent(Intent intent, NotificationContent content, String channelId, int notificationId) {
        intent.putExtra(AppNotificationHelper.EXTRA_NOTIFICATION_TYPE, content.getType().name())
                .putExtra(EXTRA_ID, notificationId)
                .putExtra(EXTRA_TITLE, content.getTitle())
                .putExtra(EXTRA_MESSAGE, content.getMessage())
                .putExtra(EXTRA_LINK, content.getDeepLink())
                .putExtra(EXTRA_CHANNEL, channelId);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Context appContext = context.getApplicationContext();
        String action = intent.getAction();
        if (ACTION_SNOOZE.equals(action)) {
            NotificationManagerCompat.from(appContext).cancel(intent.getIntExtra(EXTRA_ID, 0));
            scheduleFire(appContext, intent);
            Bundle bundle = new Bundle();
            bundle.putString("notification_type", intent.getStringExtra(AppNotificationHelper.EXTRA_NOTIFICATION_TYPE));
            FirebaseAnalytics.getInstance(appContext).logEvent("notification_snoozed", bundle);
        } else if (ACTION_SNOOZE_FIRE.equals(action)) {
            NotificationType type;
            try {
                type = NotificationType.valueOf(intent.getStringExtra(AppNotificationHelper.EXTRA_NOTIFICATION_TYPE));
            } catch (RuntimeException e) {
                type = NotificationType.DAILY_REMINDER;
            }
            NotificationContent content = new NotificationContent(
                    type,
                    intent.getStringExtra(EXTRA_TITLE),
                    intent.getStringExtra(EXTRA_MESSAGE),
                    intent.getStringExtra(EXTRA_LINK)
            );
            PendingResult pendingResult = goAsync();
            AppNotificationHelper.showStyledAsync(appContext, content,
                    intent.getStringExtra(EXTRA_CHANNEL), intent.getIntExtra(EXTRA_ID, 0), pendingResult::finish);
        }
    }

    private static void scheduleFire(Context context, Intent source) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        Intent intent = new Intent(context, NotificationActionReceiver.class).setAction(ACTION_SNOOZE_FIRE);
        if (source.getExtras() != null) intent.putExtras(source.getExtras());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_SNOOZE_FIRE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        // Nhắc lại không cần chính xác từng giây: alarm thường, cho phép khi Doze.
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + SNOOZE_MILLIS, pendingIntent);
    }
}
