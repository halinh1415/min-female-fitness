package mint.woman.female.workout.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mint.woman.female.workout.model.PlanObject;

/**
 * Bài tập người dùng đã chỉnh ở màn Preview (số reps/giây, đổi động tác, thứ tự) và bấm Lưu.
 *
 * <p>Lưu theo từng ngày của từng bài (discover + file kế hoạch + ngày). Không lưu các động tác
 * khởi động / giãn cơ do WorkoutExtras chèn thêm (added); chúng vẫn được thêm lại mỗi lần nạp bài.
 * DemoDataProvider.setData dùng bản này thay cho danh sách gốc; Đặt lại thì xoá.</p>
 */
public final class PlanEditStore {

    private static final String PREF_NAME = "plan_edit_pref";

    private PlanEditStore() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static String key(int discover, String plan, int day) {
        return discover + "_" + plan + "_" + day;
    }

    public static void save(Context context, int discover, String plan, int day, List<PlanObject.ActionObject> actions) {
        JSONArray array = new JSONArray();
        try {
            for (PlanObject.ActionObject action : actions) {
                if (action.added) continue;
                JSONObject item = new JSONObject();
                item.put("id", action.actionId);
                item.put("time", action.time);
                item.put("unit", action.getUnit());
                item.put("actype", action.actype);
                item.put("rest", action.rest);
                array.put(item);
            }
        } catch (JSONException e) {
            return;
        }
        if (array.length() == 0) return;
        prefs(context).edit().putString(key(discover, plan, day), array.toString()).apply();
    }

    /** Danh sách đã lưu, hoặc null nếu ngày này chưa chỉnh (hoặc dữ liệu hỏng). */
    public static List<PlanObject.ActionObject> load(Context context, int discover, String plan, int day) {
        String json = prefs(context).getString(key(discover, plan, day), null);
        if (json == null) return null;
        try {
            JSONArray array = new JSONArray(json);
            List<PlanObject.ActionObject> list = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                list.add(new PlanObject.ActionObject(item.getInt("id"), item.getInt("time"),
                        item.optString("unit", ""), item.optInt("actype"), item.optInt("rest", 10)));
            }
            return list.isEmpty() ? null : list;
        } catch (JSONException e) {
            return null;
        }
    }

    public static boolean has(Context context, int discover, String plan, int day) {
        return prefs(context).contains(key(discover, plan, day));
    }

    public static void clear(Context context, int discover, String plan, int day) {
        prefs(context).edit().remove(key(discover, plan, day)).apply();
    }
}
