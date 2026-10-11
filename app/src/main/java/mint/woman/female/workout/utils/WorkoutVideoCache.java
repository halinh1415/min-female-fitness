package mint.woman.female.workout.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.minfitness.decode.Decoder;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Giải mã video động tác cho màn tập (Nghỉ / Tập) ở luồng nền.
 *
 * <p>Trước đây RestFragment giải mã cả file mp4 ngay trong onViewCreated trên main thread mỗi lần
 * sang bài kế, và hai fragment dùng chung một file temp.mp4 - máy yếu bị khựng / ANR, và PlayFragment
 * có thể đọc file đang ghi dở. Nay mỗi động tác có file riêng, ghi ra file tạm rồi đổi tên (không ai
 * đọc file dở), giải mã một lần trong buổi tập, kết quả trả về main thread.</p>
 */
public final class WorkoutVideoCache {

    private static final String TAG = "WorkoutVideoCache";
    private static final String DIR = "workout_video";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "workout-video");
        thread.setPriority(Thread.NORM_PRIORITY - 1);
        return thread;
    });
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public interface Callback {
        /** Chạy trên main thread; {@code video} null nếu không giải mã được. */
        void onReady(@Nullable File video);
    }

    private WorkoutVideoCache() {
    }

    /** File video đã giải mã của động tác (có thể chưa tồn tại). */
    @NonNull
    public static File fileFor(@NonNull Context context, int discover, int version, int actionId) {
        return new File(new File(context.getCacheDir(), DIR), discover + "_" + version + "_" + actionId + ".mp4");
    }

    /** Có sẵn file thì gọi lại ngay (vẫn qua main handler), chưa có thì giải mã ở luồng nền. */
    public static void prepare(@NonNull Context context, int discover, int version, int actionId,
                               @NonNull Callback callback) {
        Context app = context.getApplicationContext();
        File target = fileFor(app, discover, version, actionId);
        EXECUTOR.execute(() -> {
            File result = decryptIfNeeded(app, target, discover, version, actionId) ? target : null;
            MAIN.post(() -> callback.onReady(result));
        });
    }

    /** Xoá video của buổi tập; xếp hàng sau các lần giải mã đang chờ nên không xoá file đang ghi. */
    public static void clear(@NonNull Context context) {
        File dir = new File(context.getApplicationContext().getCacheDir(), DIR);
        EXECUTOR.execute(() -> {
            File[] files = dir.listFiles();
            if (files == null) return;
            for (File file : files) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        });
    }

    private static boolean decryptIfNeeded(Context app, File target, int discover, int version, int actionId) {
        if (target.length() > 0) return true;
        File dir = target.getParentFile();
        if (dir != null && !dir.isDirectory() && !dir.mkdirs()) return false;
        File temp = new File(dir, target.getName() + ".part");
        try {
            if (discover == 0) {
                Decoder.decryptToFile(app.getAssets(), "i/v/video_" + actionId, temp.getPath());
            } else {
                Decoder.decryptInputToFile(app.getFilesDir() + "/discover/" + discover + "/" + version
                        + "/v/video_" + actionId, temp.getPath());
            }
            return temp.length() > 0 && temp.renameTo(target);
        } catch (Exception e) {
            Log.w(TAG, "decrypt video_" + actionId + " failed: " + e.getMessage());
            return false;
        } finally {
            //noinspection ResultOfMethodCallIgnored
            temp.delete();
        }
    }
}
