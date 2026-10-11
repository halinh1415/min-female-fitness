package mint.woman.female.workout.notification;


import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class NotificationScheduler {

    private static final String WORK_NAME = "smart_notification_work";

    public static void schedule(Context context) {
        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(SmartNotificationWorker.class, 6, TimeUnit.HOURS)
                        // Job định kỳ mới đặt sẽ chạy NGAY lần đầu → máy mới cài / xoá dữ liệu nhận thông báo
                        // khi đang ở màn Splash. Lùi lần chạy đầu một chu kỳ (máy đã có job: KEEP, không đổi).
                        .setInitialDelay(6, TimeUnit.HOURS)
                        .build();

        // KEEP: được gọi ở mỗi lần process khởi động (kể cả khởi động ngầm do alarm / job / FCM).
        // UPDATE làm WorkManager huỷ và đặt lại job của JobScheduler mỗi lần như vậy - thừa việc
        // trên thiết bị hạn chế job chặt (Android 16, Realme/ColorOS) và có thể dừng job đang chạy.
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    public static void cancel(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }
}