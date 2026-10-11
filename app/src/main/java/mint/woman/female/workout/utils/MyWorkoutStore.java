package mint.woman.female.workout.utils;

import android.content.Context;
import android.util.AtomicFile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.model.ProgramObject;

/**
 * Danh sách bài tự tạo ("Đào tạo của tôi"), lưu ở filesDir/custom/my_workouts.json (cùng thư mục với
 * file lịch từng bài, xem {@link CustomPlanFiles}). Thay cho bảng my_workout trong workout.db: DB đó
 * copy từ assets, không có cơ chế nâng cấp, và thông tin bài (SQL) với lịch (file) nằm hai nơi.
 *
 * <p>Ghi bằng {@link AtomicFile} (ghi file tạm rồi mới thay) để tắt app giữa chừng không hỏng dữ
 * liệu. Lần đầu chạy tự chuyển các bài cũ trong SQL sang, giữ nguyên ID (đã dời sang dải
 * ≥ {@link Database#MY_WORKOUT_ID_BASE}) nên tiến độ lưu theo ID vẫn còn.</p>
 */
public final class MyWorkoutStore {

    private static final String DIR = "custom";
    private static final String FILE = "my_workouts.json";
    private static final String PREF_MIGRATED = "MY_WORKOUTS_MIGRATED_FROM_SQL";
    /** Bài tự tạo luôn là loại 3 (danh sách ngày, giống thử thách). */
    private static final int TYPE_CUSTOM = 3;

    @Nullable
    private static List<ProgramObject> cache;

    private MyWorkoutStore() {
    }

    @NonNull
    private static AtomicFile file(@NonNull Context context) {
        File dir = new File(context.getFilesDir(), DIR);
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        return new AtomicFile(new File(dir, FILE));
    }

    /** Danh sách bài (bản sao), mới tạo ở cuối. */
    @NonNull
    public static synchronized List<ProgramObject> list(@NonNull Context context) {
        return new ArrayList<>(load(context));
    }

    /** Bài theo ID, null nếu không có. */
    @Nullable
    public static synchronized ProgramObject find(@NonNull Context context, int id) {
        for (ProgramObject p : load(context)) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    /**
     * Tạo bài mới với lịch [planJson]: cấp ID, ghi file lịch rồi mới ghi danh sách.
     * @return bài vừa tạo, null nếu ghi file thất bại.
     */
    @Nullable
    public static synchronized ProgramObject create(@NonNull Context context, @NonNull String name, int days,
                                                    @NonNull String image, @NonNull String planJson) {
        List<ProgramObject> list = load(context);
        int id = Database.MY_WORKOUT_ID_BASE + 1;
        for (ProgramObject p : list) id = Math.max(id, p.getId() + 1);
        ProgramObject workout = build(id, name, days, image, "custom_plan_" + id + ".json");
        if (!CustomPlanFiles.save(context, workout.getPlan(), planJson)) return null;
        list.add(workout);
        if (!save(context, list)) {
            list.remove(workout);
            CustomPlanFiles.delete(context, workout.getPlan());
            return null;
        }
        return workout;
    }

    /** Cập nhật tên / số ngày / ảnh của bài đã có. */
    public static synchronized boolean update(@NonNull Context context, @NonNull ProgramObject workout) {
        List<ProgramObject> list = load(context);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == workout.getId()) {
                list.set(i, workout);
                return save(context, list);
            }
        }
        return false;
    }

    /** Xoá bài và file lịch của nó. */
    public static synchronized void delete(@NonNull Context context, int id) {
        List<ProgramObject> list = load(context);
        ProgramObject removed = null;
        for (ProgramObject p : list) {
            if (p.getId() == id) {
                removed = p;
                break;
            }
        }
        if (removed == null) return;
        list.remove(removed);
        if (save(context, list)) CustomPlanFiles.delete(context, removed.getPlan());
    }

    // ------------------------------------------------------------------ đọc / ghi

    @NonNull
    private static List<ProgramObject> load(@NonNull Context context) {
        if (cache != null) return cache;
        List<ProgramObject> list = new ArrayList<>();
        AtomicFile atomic = file(context);
        if (atomic.getBaseFile().exists()) {
            try {
                String json = new String(atomic.readFully(), StandardCharsets.UTF_8);
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject o = array.getJSONObject(i);
                    list.add(build(o.getInt("id"), o.optString("name"), o.optInt("total", 1),
                            o.optString("image", "custom_workout.webp"), o.getString("plan")));
                }
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        } else {
            list = migrateFromSql(context);
        }
        cache = list;
        return list;
    }

    private static boolean save(@NonNull Context context, @NonNull List<ProgramObject> list) {
        AtomicFile atomic = file(context);
        FileOutputStream out = null;
        try {
            JSONArray array = new JSONArray();
            for (ProgramObject p : list) {
                array.put(new JSONObject()
                        .put("id", p.getId())
                        .put("name", p.getName())
                        .put("total", p.getTotal())
                        .put("image", p.getImage())
                        .put("plan", p.getPlan()));
            }
            out = atomic.startWrite();
            out.write(array.toString().getBytes(StandardCharsets.UTF_8));
            atomic.finishWrite(out);
            cache = list;
            return true;
        } catch (Exception e) {
            if (out != null) atomic.failWrite(out);
            FirebaseCrashlytics.getInstance().recordException(e);
            cache = null; // đọc lại từ file ở lần sau
            return false;
        }
    }

    /** Lần đầu: chép các bài trong bảng my_workout (SQL) sang file JSON. */
    @NonNull
    private static List<ProgramObject> migrateFromSql(@NonNull Context context) {
        List<ProgramObject> list = new ArrayList<>();
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_MIGRATED, false)) {
            return list;
        }
        try {
            Database db = FitnessApplication.getApplication().mDatabase;
            if (db != null) {
                for (ProgramObject p : db.getMyWorkoutList()) {
                    if (p.getPlan() == null || p.getPlan().isEmpty()) continue;
                    list.add(build(p.getId(), p.getName(), p.getTotal(), p.getImage(), p.getPlan()));
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        if (save(context, list)) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PREF_MIGRATED, true).apply();
        }
        return list;
    }

    @NonNull
    private static ProgramObject build(int id, String name, int days, String image, String plan) {
        ProgramObject p = new ProgramObject();
        p.setId(id);
        p.setName(name);
        p.setTotal(Math.max(1, days));
        p.setImage(image == null || image.isEmpty() ? "custom_workout.webp" : image);
        p.setPlan(plan);
        p.setType(TYPE_CUSTOM);
        p.setLevel(1);
        p.setPremium(0);
        return p;
    }
}
