package mint.woman.female.workout.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;
import java.util.Map;

import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.PlanObject;

/**
 * Tự điều chỉnh độ khó theo đánh giá cuối buổi (0 = quá khó .. 4 = quá dễ, như FEEL_&lt;id&gt;).
 *
 * <p>Mỗi chương trình có một hệ số % (mặc định 100). Đánh giá cộng/trừ vào hệ số, giới hạn
 * {@value #MIN_PERCENT}..{@value #MAX_PERCENT}. DemoDataProvider nhân số reps / giây của các động
 * tác chính với hệ số này (không áp dụng cho ngày người dùng đã tự chỉnh và lưu).</p>
 */
public final class DifficultyStore {

    public static final int MIN_PERCENT = 60;
    public static final int MAX_PERCENT = 160;
    private static final int[] FEEL_DELTA = {-15, -8, 0, 8, 15};
    private static final String PREF_NAME = "difficulty_pref";

    private DifficultyStore() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static int percent(Context context, int programId) {
        return prefs(context).getInt(String.valueOf(programId), 100);
    }

    /** Đặt hệ số = base + mức của đánh giá; truyền base cố định để chọn lại đánh giá không bị cộng dồn. */
    public static void applyFeel(Context context, int programId, int basePercent, int feel) {
        if (programId <= 0 || feel < 0 || feel >= FEEL_DELTA.length) return;
        int value = Math.max(MIN_PERCENT, Math.min(MAX_PERCENT, basePercent + FEEL_DELTA[feel]));
        prefs(context).edit().putInt(String.valueOf(programId), value).apply();
    }

    public static void reset(Context context, int programId) {
        prefs(context).edit().remove(String.valueOf(programId)).apply();
    }

    /** Nhân reps / giây của động tác chính theo hệ số; bài tính giờ làm tròn 5 giây. */
    public static void scale(Context context, int programId, List<PlanObject.ActionObject> list,
                             Map<Integer, ExerciseObject> exercises) {
        int percent = percent(context, programId);
        if (percent == 100) return;
        for (PlanObject.ActionObject action : list) {
            if (action.added) continue;
            ExerciseObject exercise = exercises.get(action.actionId);
            String unit = !action.getUnit().isEmpty() ? action.getUnit()
                    : exercise != null ? exercise.getUnit() : "";
            if (!unit.isEmpty()) {
                action.time = Math.max(10, Math.round(action.time * percent / 500f) * 5);
            } else if (exercise != null && Boolean.TRUE.equals(exercise.getAlternation())) {
                // Reps tính cả hai bên: giữ số chẵn.
                action.time = Math.max(2, Math.round(action.time * percent / 200f) * 2);
            } else {
                action.time = Math.max(1, Math.round(action.time * percent / 100f));
            }
        }
    }
}
