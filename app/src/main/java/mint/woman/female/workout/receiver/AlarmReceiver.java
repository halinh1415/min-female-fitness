package mint.woman.female.workout.receiver;


import static android.content.Context.NOTIFICATION_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.SplashActivity;
import mint.woman.female.workout.activity.ui.locknotify.LockScreenActivity;
import mint.woman.female.workout.model.ReminderItem;
import mint.woman.female.workout.service.ReminderWorker;
import mint.woman.female.workout.utils.RemoteConfigKeys;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class AlarmReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_CHANNEL_ID_REMINDER = "mint.woman.female.workout.reminder";
    private static final String TAG = "AlarmReceiver";
    static String DefaultNotificationChannelId = "default";
    static String DefaultNotificationChannelName = "Default";
    static String DefaultNotificationChannelDesc = "Default notification channel";
    public static final String ID_ONE_TIME = "ID_ONE_TIME";
    public static final String ONE_TIME = "ONE_TIME";
    public static final String DAILY_TIME = "DAILY_TIME";
    public static final String ON_TIME = "ON_TIME";

    private static final int NOTIFY_DAILY_ID_CODE = 5678;
    private static final int REMINDER_DAILY_REQUEST_CODE = 10000;

    public static void cancelAlarm(Context context, ReminderItem reminderItem) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                alarmManager.cancel(PendingIntent.getBroadcast(context, reminderItem.id * 10, new Intent(context, AlarmReceiver.class), PendingIntent.FLAG_MUTABLE));
            } else {
                alarmManager.cancel(PendingIntent.getBroadcast(context, reminderItem.id * 10, new Intent(context, AlarmReceiver.class), PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE));
            }
            if (reminderItem.repeat > 0) {
                for (int i = 0; i < 7; i++) {
                    int bit = getBit(reminderItem.repeat, i);
                    if (bit == 1) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            alarmManager.cancel(PendingIntent.getBroadcast(context, reminderItem.id * 10 + i, new Intent(context, AlarmReceiver.class), PendingIntent.FLAG_MUTABLE));
                        } else {
                            alarmManager.cancel(PendingIntent.getBroadcast(context, reminderItem.id * 10 + i, new Intent(context, AlarmReceiver.class), PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE));

                        }
                    }
                }
            }
        }
    }

    public static void setAlarmDaily(Context context, String time) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date date;
        try {
            date = simpleDateFormat.parse(time);
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra(DAILY_TIME, true);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, date.getHours());
            calendar.set(Calendar.MINUTE, date.getMinutes());
            calendar.set(Calendar.SECOND, 0);
            if (calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
                calendar.add(Calendar.DATE, 1);
            }
            PendingIntent pendingIntent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getBroadcast(context, REMINDER_DAILY_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            } else {
                pendingIntent = PendingIntent.getBroadcast(context, REMINDER_DAILY_REQUEST_CODE, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
            }
            if (alarmManager != null) {
                Log.d(TAG, "set reminder daily time=" + time);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()){
                        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                    }else{
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                    }
                }else{
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                }

            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void setAlarm(Context context, ReminderItem reminderItem) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat simpleDateDAY = new SimpleDateFormat("HH:mm dd/mm/yyyy", Locale.getDefault());
        Date date;

        try {
            date = simpleDateFormat.parse(reminderItem.time);

            if (reminderItem.repeat == 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, date.getHours());
                calendar.set(Calendar.MINUTE, date.getMinutes());
                calendar.set(Calendar.SECOND, 0);
                if (calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
                    calendar.add(Calendar.DATE, 1);
                }
                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.putExtra(ONE_TIME, true);
                intent.putExtra(ID_ONE_TIME, reminderItem.id);
                PendingIntent pendingIntent;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    pendingIntent = PendingIntent.getBroadcast(context, reminderItem.id * 10, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                } else {
                    pendingIntent = PendingIntent.getBroadcast(context, reminderItem.id * 10, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
                }
                if (alarmManager != null) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()){
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        }else{
                            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        }
                    }else{
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    }
                }
            } else {
                for (int i = 0; i < 7; i++) {
                    int bit = getBit(reminderItem.repeat, i);
                    if (bit == 1) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, date.getHours());
                        calendar.set(Calendar.MINUTE, date.getMinutes());
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.DAY_OF_WEEK, (i + 1));
                        if (calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
                            calendar.add(Calendar.DATE, 7);
                        }
                        Log.d(TAG, "set alarm on :" + date.getHours() + " : " + date.getMinutes() + simpleDateDAY.format(calendar.getTime()));
                        Intent intent = new Intent(context, AlarmReceiver.class);
                        intent.putExtra(ONE_TIME, false);
                        intent.putExtra(ID_ONE_TIME, reminderItem.id * 10 + i);
                        PendingIntent pendingIntent;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            pendingIntent = PendingIntent.getBroadcast(context, reminderItem.id * 10 + i, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                        } else {
                            pendingIntent = PendingIntent.getBroadcast(context, reminderItem.id * 10 + i, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
                        }
                        if (alarmManager != null) {
                            // alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 60*1000, pendingIntent);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (alarmManager.canScheduleExactAlarms()){
                                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
                                }else{
                                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
                                }
                            }else{
                                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
                            }


                        }
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static int getBit(int n, int k) {
        return (n >> k) & 1;
    }

    @SuppressLint({"WakelockTimeout", "InvalidWakeLockTag"})
    @Override
    public void onReceive(Context context, Intent intent) {
        // Dựng thông báo (decode bitmap) và gọi WorkManager ngoài main thread: alarm thường đánh
        // thức process đang chết, main thread còn bận Application.onCreate.
        final PendingResult pendingResult = goAsync();
        final Context appContext = context.getApplicationContext();
        final Bundle extras = intent.getExtras();
        ReceiverExecutor.run(() -> {
            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            WakeLock wl = null;
            if (powerManager != null) {
                wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "muay_thai_workout");
                wl.acquire(10_000);
            }
            try {
                showNotification(appContext, "");
                if (extras != null && extras.getBoolean(ONE_TIME)) {
                    Log.d(TAG, "onReceive one time notification!!!");
                    Data myData = new Data.Builder()
                            .putInt("REMINDER_ID", extras.getInt(ID_ONE_TIME))
                            .build();
                    OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class).setInputData(myData).build();
                    WorkManager.getInstance(appContext).enqueue(oneTimeWorkRequest);
                } else {
                    Log.d(TAG, "onReceive repeat notification!!!");
                }
            } finally {
                if (wl != null && wl.isHeld()) {
                    wl.release();
                }
                pendingResult.finish();
            }
        });
    }

    public void showNotification(Context context, String title) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_REMINDER,
                    "Reminder",
                    NotificationManager.IMPORTANCE_DEFAULT);
            nm.createNotificationChannel(channel);
        }
        Intent notifyIntent = new Intent(context, SplashActivity.class);
// Set the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);


        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                context, 0, notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Builder notificationBuilder = new Builder(context, NOTIFICATION_CHANNEL_ID_REMINDER);
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }
        notificationBuilder.setSmallIcon(R.drawable.ic_exercise, 1)
                .setContentTitle(context.getString(R.string.txt_time_to_workout))
                .setContentIntent(notifyPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(1)
                .setAutoCancel(true);
        if (Build.VERSION.SDK_INT >= 23) {

            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getApplicationContext().getResources(), R.drawable.ic_workout));
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, notificationBuilder.build());
        }
    }
    public static void scheduleLockscreenWidget(Context context, String time) {
        Log.d(TAG, "scheduleLockscreenWidget " + time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd-MM-yyyy");
        long intervalMillis = AlarmManager.INTERVAL_DAY;
//        if (BuildConfig.build_debug) {
//            intervalMillis = 300000L;
//        }
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra(DAILY_TIME, true);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(context, REMINDER_DAILY_REQUEST_CODE, alarmIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context, REMINDER_DAILY_REQUEST_CODE, alarmIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date date;

        try {
            date = simpleDateFormat.parse(time);
            long currentTimeMillis = Calendar.getInstance().getTimeInMillis();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, date.getHours());
            calendar.set(Calendar.MINUTE, date.getMinutes());
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() < currentTimeMillis) {
                calendar.add(Calendar.HOUR_OF_DAY, 24);
            }
            Log.d(TAG, "scheduleLockscreenWidget at time =" + dateFormat.format(calendar.getTimeInMillis()));
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()){
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervalMillis, pendingIntent);
                }else{
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervalMillis, pendingIntent);
                }
            }else{
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervalMillis, pendingIntent);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "scheduleLockscreenWidget error: " + e.toString());
        }
        // Set time to show widget

    }
    public static void createNotificationChannels(Context context) {
        Log.d(TAG, "createNotificationChannels!!!!");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel.

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(DefaultNotificationChannelId, DefaultNotificationChannelName, importance);
            mChannel.setDescription(DefaultNotificationChannelDesc);
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(mChannel);
        }
    }
    public static void showNotificationLock(Context context) {
        Log.d(TAG, "sendLockscreenWidgetNotification!!!!");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "No notification permission", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Intent notifyIntent = new Intent(context, SplashActivity.class);
// Set the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);


        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                context, 0, notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent fullScreenIntent = new Intent(context, LockScreenActivity.class);

        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context, 0, fullScreenIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Builder notificationBuilder = new Builder(context, DefaultNotificationChannelId);

        notificationBuilder.setSmallIcon(R.drawable.ic_exercise)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.txt_reminder_content_1))
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setContentIntent(notifyPendingIntent);
//                .setFullScreenIntent(fullScreenPendingIntent, true);
        if (FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.ENABLE_LOCK_WIDGET)) {
            notificationBuilder.setFullScreenIntent(fullScreenPendingIntent, true);
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFY_DAILY_ID_CODE);
            notificationManager.notify(NOTIFY_DAILY_ID_CODE, notificationBuilder.build());
        }
    }

}
