package mint.woman.female.workout.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import mint.woman.female.workout.R;

/**
 * 📢 BroadcastReceiver để xử lý khi AlarmManager trigger
 * Gửi notification nhắc nhở người dùng tập luyện
 */
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Lấy dữ liệu từ intent
        String programName = intent.getStringExtra("programName");
        int duration = intent.getIntExtra("duration", 0);

        if (programName == null) {
            programName = "Tai Chi - Qigong";
        }

        NotificationContent content = new NotificationContent(
                NotificationType.DAILY_REMINDER,  // Type
                programName,                      // Title
                context.getString(R.string.x_minutes, duration),  // Message
                "🎯 " + programName               // Deep link
        );

        AppNotificationHelper.showNotification(context, content);
        AppNotificationHelper.trackNotificationSent(context, "reminder");

    }
}

