package mint.woman.female.workout.notification;

import android.app.ActivityManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import mint.woman.female.workout.utils.MyPreference;

import java.util.Calendar;

public class SmartNotificationWorker extends Worker {

    private static final int NOTIFICATION_START_HOUR = 5;  // 06:00
    private static final int NOTIFICATION_END_HOUR = 22;   // 21:59

    public SmartNotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        // Chưa xong lần mở đầu (Splash / ngôn ngữ / onboarding) hoặc người dùng đang mở app → không nhắc tập
        if (MyPreference.newInstance(context).isFirstOpen() || isAppInForeground()) {
            return Result.success();
        }

        AppNotificationHelper.createChannel(context);

        PracticeStats stats = PracticeStatsStore.get(context);

        Calendar calendar = Calendar.getInstance();
        int nowHour = calendar.get(Calendar.HOUR_OF_DAY);

        // Only send Smart notifications from 06:00 to 21:59 local time.
        if (nowHour < NOTIFICATION_START_HOUR || nowHour > NOTIFICATION_END_HOUR) {
            return Result.success();
        }

        NotificationContent content = SmartNotificationEngine.evaluate(context,stats, nowHour);

        if (content != null) {
            AppNotificationHelper.showNotification(context, content);
        }

        return Result.success();
    }

    private static boolean isAppInForeground() {
        ActivityManager.RunningAppProcessInfo info = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(info);
        return info.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
    }
}