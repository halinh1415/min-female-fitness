package mint.woman.female.workout.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.minfitness.decode.Decoder;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * File lịch tập do người dùng tạo / sửa (My Workout, sửa lịch chương trình có sẵn), mã hoá như dữ liệu
 * gốc, lưu ở filesDir/custom/ (giống WomenWorkout).
 *
 * <p>Bản cũ ghi vào filesDir/i/ mà không tạo thư mục (Decoder ném RuntimeException → crash) và lúc
 * đọc lại tìm trong assets nên không bao giờ thấy file. File cũ ở filesDir/i/ (nếu đã ghi được) vẫn
 * được đọc làm dự phòng.</p>
 */
public final class CustomPlanFiles {

    private static final String DIR = "custom";
    private static final String LEGACY_DIR = "i";
    /** Tiền tố file của bài tự tạo; "custom_plan_new_" là plan cá nhân (JSON thường, xử lý riêng). */
    private static final String USER_PREFIX = "custom_plan_";
    private static final String PERSONAL_PREFIX = "custom_plan_new_";

    private CustomPlanFiles() {
    }

    /** File lịch của bài tập do người dùng tạo trong My Workout. */
    public static boolean isUserPlan(@Nullable String fileName) {
        return fileName != null && fileName.startsWith(USER_PREFIX) && !fileName.startsWith(PERSONAL_PREFIX);
    }

    @NonNull
    private static File dir(@NonNull Context context) {
        File dir = new File(context.getFilesDir(), DIR);
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        return dir;
    }

    /** File đã lưu cho [fileName] (bản mới, hoặc bản cũ ở filesDir/i/), null nếu chưa có. */
    @Nullable
    public static File find(@NonNull Context context, @Nullable String fileName) {
        if (fileName == null || fileName.isEmpty()) return null;
        File file = new File(new File(context.getFilesDir(), DIR), fileName);
        if (file.isFile()) return file;
        File legacy = new File(new File(context.getFilesDir(), LEGACY_DIR), fileName);
        return legacy.isFile() ? legacy : null;
    }

    /** Lưu lịch (JSON) đã mã hoá. @return true nếu ghi thành công. */
    public static boolean save(@NonNull Context context, @NonNull String fileName, @NonNull String json) {
        try {
            File out = new File(dir(context), fileName);
            Decoder.encryptStringToFile(json.getBytes(StandardCharsets.UTF_8), out.getPath());
            return out.isFile();
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            return false;
        }
    }

    /** Đọc JSON đã lưu, null nếu không có / không giải mã được. */
    @Nullable
    public static String read(@NonNull Context context, @Nullable String fileName) {
        File file = find(context, fileName);
        if (file == null) return null;
        try {
            byte[] decoded = Decoder.decryptInput(file.getPath());
            return decoded == null ? null : new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            return null;
        }
    }

    public static void delete(@NonNull Context context, @Nullable String fileName) {
        if (fileName == null || fileName.isEmpty()) return;
        //noinspection ResultOfMethodCallIgnored
        new File(new File(context.getFilesDir(), DIR), fileName).delete();
        //noinspection ResultOfMethodCallIgnored
        new File(new File(context.getFilesDir(), LEGACY_DIR), fileName).delete();
    }
}
