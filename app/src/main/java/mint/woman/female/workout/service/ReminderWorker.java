package mint.woman.female.workout.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import mint.woman.female.workout.utils.Database;

public class ReminderWorker extends Worker {
    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        int reminderID = getInputData().getInt("REMINDER_ID", 0);
        Database.newInstance(getApplicationContext(), "workout.db").updateActiveReminder(reminderID, 0);
        return Result.success();
    }
}