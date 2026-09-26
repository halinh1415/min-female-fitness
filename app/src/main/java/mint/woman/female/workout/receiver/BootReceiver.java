package mint.woman.female.workout.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import mint.woman.female.workout.service.ReminderSetWorker;
import mint.woman.female.workout.utils.MyPreference;

public class BootReceiver extends BroadcastReceiver {

    private static final String BOOT_COMPLETED =
            "android.intent.action.BOOT_COMPLETED";
    private static final String QUICKBOOT_POWERON =
            "android.intent.action.QUICKBOOT_POWERON";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("HAHA1", "restart alarm");
        if (action != null && (action.equals(BOOT_COMPLETED) ||
                action.equals(QUICKBOOT_POWERON))) {
            // Chạy ngoài main thread: lúc khởi động máy process được tạo ngầm, WorkManager
            // (khởi tạo theo yêu cầu - Configuration.Provider) mở database ngay lần gọi đầu tiên.
            // goAsync() giữ receiver sống tới khi xong, trong giới hạn thời gian của broadcast.
            final PendingResult pendingResult = goAsync();
            final Context appContext = context.getApplicationContext();
            ReceiverExecutor.run(() -> {
                try {
                    String remindDaily = MyPreference.newInstance(appContext).getTimeRemindDaily();
                    AlarmReceiver.setAlarmDaily(appContext, remindDaily);
                    OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(ReminderSetWorker.class).build();
                    WorkManager.getInstance(appContext).enqueue(oneTimeWorkRequest);
                } finally {
                    pendingResult.finish();
                }
            });

        }
    }

}