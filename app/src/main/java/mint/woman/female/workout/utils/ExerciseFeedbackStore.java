package mint.woman.female.workout.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.PlanObject;

/**
 * 👍/👎 cho từng động tác, bấm ở màn nghỉ sau khi tập xong động tác đó.
 *
 * <p>Động tác bị 👎 được {@link #swapDisliked} đổi sang động tác tập chung nhóm cơ, cùng kiểu
 * giờ/reps, cùng kiểu tập từng bên, không khó hơn nếu có lựa chọn, chưa có trong bài và không bị 👎. Chỉ áp dụng cho bài có sẵn
 * của app khi người dùng chưa tự chỉnh ngày đó (PlanEditStore).</p>
 */
public final class ExerciseFeedbackStore {

    public static final int NONE = 0;
    public static final int LIKE = 1;
    public static final int DISLIKE = -1;

    private static final String PREF_NAME = "exercise_feedback_pref";

    private ExerciseFeedbackStore() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static String key(int discover, int actionId) {
        return discover + "_" + actionId;
    }

    public static int get(Context context, int discover, int actionId) {
        return prefs(context).getInt(key(discover, actionId), NONE);
    }

    public static void set(Context context, int discover, int actionId, int value) {
        if (value == NONE) {
            prefs(context).edit().remove(key(discover, actionId)).apply();
        } else {
            prefs(context).edit().putInt(key(discover, actionId), value).apply();
        }
    }

    public static Set<Integer> disliked(Context context, int discover) {
        Set<Integer> ids = new HashSet<>();
        String prefix = discover + "_";
        for (Map.Entry<String, ?> entry : prefs(context).getAll().entrySet()) {
            if (entry.getKey().startsWith(prefix) && Integer.valueOf(DISLIKE).equals(entry.getValue())) {
                try {
                    ids.add(Integer.parseInt(entry.getKey().substring(prefix.length())));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return ids;
    }

    /** Đổi các động tác bị 👎 trong danh sách sang động tác tương tự; giữ nguyên số reps / giây. */
    public static void swapDisliked(Context context, int discover, List<PlanObject.ActionObject> list,
                                    Map<Integer, ExerciseObject> exercises) {
        Set<Integer> disliked = disliked(context, discover);
        if (disliked.isEmpty()) return;
        Set<Integer> used = new HashSet<>();
        for (PlanObject.ActionObject action : list) used.add(action.actionId);
        for (PlanObject.ActionObject action : list) {
            if (!disliked.contains(action.actionId)) continue;
            int swap = alternativeFor(action.actionId, exercises, disliked, used);
            if (swap > 0) {
                action.actionId = swap;
                used.add(swap);
            }
        }
    }

    private static int alternativeFor(int actionId, Map<Integer, ExerciseObject> exercises,
                                      Set<Integer> disliked, Set<Integer> used) {
        ExerciseObject source = exercises.get(actionId);
        if (source == null || source.getClassid() == null) return -1;
        boolean timed = !source.getUnit().isEmpty();
        boolean alternation = Boolean.TRUE.equals(source.getAlternation());
        boolean jumped = Boolean.TRUE.equals(source.getJumped());
        boolean noEquipment = source.getQicai() == null || source.getQicai().isEmpty();
        int best = -1;
        long bestScore = Long.MAX_VALUE;
        for (Map.Entry<Integer, ExerciseObject> entry : exercises.entrySet()) {
            int id = entry.getKey();
            ExerciseObject candidate = entry.getValue();
            if (id == actionId || disliked.contains(id) || used.contains(id)) continue;
            if (timed == candidate.getUnit().isEmpty()) continue;
            if (alternation != Boolean.TRUE.equals(candidate.getAlternation())) continue;
            if (candidate.getGroupactions() != null && !candidate.getGroupactions().isEmpty()) continue;
            // Không đổi sang động tác nhảy / cần dụng cụ nếu động tác gốc không như vậy.
            if (!jumped && Boolean.TRUE.equals(candidate.getJumped())) continue;
            if (noEquipment && candidate.getQicai() != null && !candidate.getQicai().isEmpty()) continue;
            // Phải tập chung ít nhất một nhóm cơ (muscle dạng "3-4-12"); không có thông tin cơ thì cần cùng classid.
            int shared = sharedMuscles(source.getMuscle(), candidate.getMuscle());
            boolean sameClass = source.getClassid().equals(candidate.getClassid());
            if (shared == 0 && !(isBlank(source.getMuscle()) && sameClass)) continue;
            // Ưu tiên: chung nhiều nhóm cơ; không khó hơn (khó hơn bị trừ nặng hơn dễ hơn);
            // cùng classid; strength gần nhất; id gần nhất.
            int levelDiff = level(candidate.getLevelid()) - level(source.getLevelid());
            long score = (10 - Math.min(shared, 10)) * 100_000_000L
                    + (levelDiff > 0 ? levelDiff * 20_000_000L : -levelDiff * 10_000_000L)
                    + (sameClass ? 0 : 1_000_000L)
                    + Math.abs(value(source.getStrength()) - value(candidate.getStrength())) * 1_000L
                    + Math.abs(id - actionId);
            if (score < bestScore) {
                bestScore = score;
                best = id;
            }
        }
        return best;
    }

    private static int sharedMuscles(String a, String b) {
        if (a == null || b == null) return 0;
        Set<String> left = new HashSet<>();
        for (String part : a.split("[-,;|]")) {
            if (!part.trim().isEmpty()) left.add(part.trim().toLowerCase());
        }
        int count = 0;
        for (String part : b.split("[-,;|]")) {
            if (left.contains(part.trim().toLowerCase())) count++;
        }
        return count;
    }

    private static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    private static int level(String levelId) {
        try {
            return Integer.parseInt(levelId.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static int value(Integer number) {
        return number == null ? 0 : number;
    }
}
