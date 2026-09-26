package mint.woman.female.workout.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import mint.woman.female.workout.model.ReminderItem;
import mint.woman.female.workout.receiver.AlarmReceiver;
import mint.woman.female.workout.utils.Database;

import java.util.List;

public class ReminderSetWorker extends Worker {
    public ReminderSetWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    private void setAlarmsFromDatabase(List<ReminderItem> list) {
        for (ReminderItem reminder : list
        ) {
            if (reminder.active == 1) {
                AlarmReceiver.setAlarm(getApplicationContext(), reminder);
            }
        }
    }
    @NonNull
    @Override
    public Result doWork() {

        Database mDatabase = Database.newInstance(getApplicationContext(), "workout.db");
        setAlarmsFromDatabase(mDatabase.getReminder());
        return Result.success();
    }
}