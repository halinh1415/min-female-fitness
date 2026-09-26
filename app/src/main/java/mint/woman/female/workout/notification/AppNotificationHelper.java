package mint.woman.female.workout.notification;



import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.main.YogaActivity;
import mint.woman.female.workout.utils.MyPreference;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Calendar;


public class AppNotificationHelper {

    public static final String CHANNEL_ID = "smart_practice_channel";
    private static final String CHANNEL_NAME = "Smart Practice";
    private static final String CHANNEL_DESC = "Smart workout reminders";

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);

            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * 📊 Track notification sent event
     */
    public static void trackNotificationSent(Context context, String notificationType) {
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context);
        MyPreference myPreference = MyPreference.newInstance(context);

        Bundle bundle = new Bundle();
        bundle.putString("notification_type", notificationType);
        bundle.putLong("timestamp", System.currentTimeMillis());

        analytics.logEvent("notification_sent", bundle);

        // 📊 Increment sent count in local storage
        myPreference.incrementNotificationSentCount();
    }

    /**
     * 📊 Track notification permission requested
     */
    public static void trackNotificationPermissionRequested(Context context) {
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        bundle.putLong("timestamp", System.currentTimeMillis());
        bundle.putInt("open_count", MyPreference.newInstance(context).openCount());
        analytics.logEvent("notification_permission_requested", bundle);
    }

    /**
     * 📊 Track notification permission result
     */
    public static void trackNotificationPermissionResult(Context context, boolean granted) {
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context);
        MyPreference myPreference = MyPreference.newInstance(context);

        Bundle bundle = new Bundle();
        bundle.putString("permission_status", granted ? "granted" : "denied");
        bundle.putLong("timestamp", System.currentTimeMillis());
        bundle.putInt("open_count", myPreference.openCount());

        analytics.logEvent("notification_permission_result", bundle);

        // Track tỉ lệ allow
        if (granted) {
            myPreference.incrementNotificationAllowCount();
        } else {
            myPreference.incrementNotificationDeniedCount();
        }
    }

    /**
     * 📊 Track notification clicked
     */
    public static void trackNotificationClicked(Context context, String notificationType) {
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context);
        MyPreference myPreference = MyPreference.newInstance(context);

        Bundle bundle = new Bundle();
        bundle.putString("notification_type", notificationType);
        bundle.putLong("click_time", System.currentTimeMillis());

        analytics.logEvent("notification_clicked", bundle);

        // Increment click count
        myPreference.incrementNotificationClickCount();
    }

    /**
     * 📊 Track retention - khi user quay lại app
     */
    public static void trackNotificationRetention(Context context) {
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context);
        MyPreference myPreference = MyPreference.newInstance(context);

        long lastNotificationTime = myPreference.getLastNotificationTime();
        if (lastNotificationTime > 0) {
            long timeSinceNotification = System.currentTimeMillis() - lastNotificationTime;
            int daysSinceNotification = (int) (timeSinceNotification / (24 * 60 * 60 * 1000));

            // Track nếu user quay lại trong 7 ngày
            if (daysSinceNotification <= 7) {
                Bundle bundle = new Bundle();
                bundle.putInt("days_since_notification", daysSinceNotification);
                bundle.putString("retention_status", daysSinceNotification <= 1 ? "same_day" :
                                                     daysSinceNotification <= 7 ? "within_7_days" : "after_7_days");

                analytics.logEvent("notification_retention", bundle);

                // Increment retention count
                myPreference.incrementNotificationRetentionCount();
            }
        }
    }

    public static void showNotification(Context context, NotificationContent content) {
        if (content == null) return;

        Intent intent = new Intent(context, YogaActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(content.getDeepLink()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                content.getType().ordinal(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | pendingIntentFlagImmutable()
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_fitness)
                .setContentTitle(content.getTitle())
                .setContentText(content.getMessage())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content.getMessage()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= 23) {

            builder.setLargeIcon(BitmapFactory.decodeResource(context.getApplicationContext().getResources(), R.drawable.ic_workout));
        }
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        manager.notify(content.getType().ordinal() + 1000, builder.build());
    }

    private static int pendingIntentFlagImmutable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PendingIntent.FLAG_IMMUTABLE;
        }
        return 0;
    }

    /**
     * 📅 Hiển thị time picker để người dùng chọn thời gian nhắc nhở
     * @param context Activity context
     * @param programName Tên chương trình
     * @param duration Thời lượng (phút)
     * @param callback Callback khi user chọn time
     */
    public static void scheduleNotificationWithTimePicker(Context context, String programName, int duration,
                                                          TimePickerCallback callback) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                (view, selectedHour, selectedMinute) -> {
                    Log.d("📅 Notification", "User selected time: " + selectedHour + ":" + selectedMinute);

                    // Tạo notification schedule cho thời gian được chọn
                    scheduleNotificationAtTime(context, programName, duration, selectedHour, selectedMinute, callback);
                },
                hour, minute, true);

        timePickerDialog.setTitle("⏰ Chọn thời gian nhắc nhở tập ngày mai");
        timePickerDialog.show();
    }

    /**
     * 📢 Schedule notification cho thời gian cụ thể
     */
    private static void scheduleNotificationAtTime(Context context, String programName, int duration,
                                                   int hour, int minute, TimePickerCallback callback) {
        Calendar notificationTime = Calendar.getInstance();
        notificationTime.add(Calendar.DAY_OF_YEAR, 1); // Ngày mai
        notificationTime.set(Calendar.HOUR_OF_DAY, hour);
        notificationTime.set(Calendar.MINUTE, minute);
        notificationTime.set(Calendar.SECOND, 0);

        // Set alarm manager để gửi notification
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        notificationIntent.putExtra("programName", programName);
        notificationIntent.putExtra("duration", duration);

        int requestCode = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | pendingIntentFlagImmutable());

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        notificationTime.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                        notificationTime.getTimeInMillis(), pendingIntent);
            }
        }

        Log.d("📅 Notification", "✅ Notification scheduled for: " +
              String.format("%02d:%02d", hour, minute) + " - " + programName);

        // Track notification scheduled
        trackNotificationScheduled(context, programName, hour, minute);

        // Callback
        if (callback != null) {
            callback.onTimeSelected(hour, minute);
        }
    }

    /**
     * 📊 Track notification scheduled event
     */
    private static void trackNotificationScheduled(Context context, String programName, int hour, int minute) {
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        bundle.putString("program_name", programName);
        bundle.putString("scheduled_time", String.format("%02d:%02d", hour, minute));
        bundle.putLong("timestamp", System.currentTimeMillis());

        analytics.logEvent("notification_scheduled", bundle);
    }

    /**
     * Callback interface cho time picker selection
     */
    public interface TimePickerCallback {
        void onTimeSelected(int hour, int minute);
    }
}

