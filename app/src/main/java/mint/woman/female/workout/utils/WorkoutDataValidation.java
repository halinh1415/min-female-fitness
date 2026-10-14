package mint.woman.female.workout.utils;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.List;
import java.util.Map;

import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.PlanObject;

/** Validate the actual workout catalog, rather than just the presence of action IDs. */
public final class WorkoutDataValidation {
    private WorkoutDataValidation() {}

    /** -1 means valid; otherwise the index of the first invalid action (0 for an empty list). */
    public static int invalidActionIndex(List<PlanObject.ActionObject> actions,
                                        Map<Integer, ExerciseObject> exercises) {
        if (actions == null || actions.isEmpty()) return 0;
        for (int i = 0; i < actions.size(); i++) {
            PlanObject.ActionObject action = actions.get(i);
            if (action == null || exercises == null || exercises.get(action.actionId) == null) return i;
        }
        return -1;
    }

    public static boolean validate(Context context, String stage, int programId, int discover,
                                   int version, int dayIndex, List<PlanObject.ActionObject> actions,
                                   Map<Integer, ExerciseObject> exercises) {
        int index = invalidActionIndex(actions, exercises);
        if (index == -1) return true;
        PlanObject.ActionObject action = actions != null && index < actions.size() ? actions.get(index) : null;
        String reason = actions == null || actions.isEmpty() ? "empty_actions"
                : action == null ? "null_action" : "missing_exercise";
        report(context, stage, programId, discover, version, dayIndex, index,
                action == null ? -1 : action.actionId, reason);
        return false;
    }

    public static void report(Context context, String stage, int programId, int discover,
                              int version, int dayIndex, int actionIndex, int actionId, String reason) {
        String details = "Workout data error: stage=" + stage + ", reason=" + reason
                + ", programId=" + programId + ", actionId=" + actionId
                + ", discover=" + discover + ", version=" + version
                + ", dayIndex=" + dayIndex + ", actionIndex=" + actionIndex;
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.setCustomKey("PROGRAM_ID", programId);
        crashlytics.setCustomKey("ACTION_ID", actionId);
        crashlytics.setCustomKey("WORKOUT_DISCOVER", discover);
        crashlytics.setCustomKey("WORKOUT_VERSION", version);
        crashlytics.setCustomKey("WORKOUT_DAY_INDEX", dayIndex);
        crashlytics.setCustomKey("WORKOUT_ACTION_INDEX", actionIndex);
        crashlytics.setCustomKey("WORKOUT_ERROR_STAGE", stage);
        crashlytics.setCustomKey("WORKOUT_ERROR_REASON", reason);
        crashlytics.log(details);
        // IDs are also in the exception itself, so later custom-key updates cannot hide the plan.
        crashlytics.recordException(new IllegalStateException(details));
        Bundle event = new Bundle();
        event.putString("stage", stage);
        event.putString("reason", reason);
        event.putInt("program_id", programId);
        event.putInt("action_id", actionId);
        event.putInt("discover", discover);
        event.putInt("data_version", version);
        event.putInt("day_index", dayIndex);
        event.putInt("action_index", actionIndex);
        FirebaseAnalytics.getInstance(context.getApplicationContext()).logEvent("workout_data_error", event);
    }
}
