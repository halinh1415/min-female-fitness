package mint.woman.female.workout.notification;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

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
}