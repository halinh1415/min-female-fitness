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
            String remindDaily = MyPreference.newInstance(context).getTimeRemindDaily();
            AlarmReceiver.setAlarmDaily(context,remindDaily);
            OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(ReminderSetWorker.class).build();
            WorkManager.getInstance(context).enqueue(oneTimeWorkRequest);

        }
    }

}