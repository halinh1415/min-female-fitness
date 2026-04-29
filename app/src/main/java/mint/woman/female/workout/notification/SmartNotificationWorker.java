package mint.woman.female.workout.notification;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;

public class SmartNotificationWorker extends Worker {

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

        NotificationContent content = SmartNotificationEngine.evaluate(context,stats, nowHour);

        if (content != null) {
            AppNotificationHelper.showNotification(context, content);
        }

        return Result.success();
    }
}