package mint.woman.female.workout.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.minfitness.decode.Decoder;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mint.woman.female.workout.FitnessApplication;

/**
 * Giải mã video bài tập ra {@code cache/temp.mp4} ở luồng nền.
 *
 * <p>Video mã hoá tới ~1MB: đọc asset + giải mã native + ghi file trên main thread làm đứng
 * màn tập (RestFragment) và có thể gây ANR khi bộ nhớ chậm. Mọi lần giải mã chạy tuần tự trên
 * một luồng; {@link #whenReady} xếp sau lần giải mã đang chờ nên người đọc {@code temp.mp4}
 * (RestFragment, PlayFragment) không bao giờ mở file đang ghi dở.</p>
 *
 * <p>Ghi ra file tạm rồi đổi tên để {@code temp.mp4} luôn là một video hoàn chỉnh, kể cả khi
 * VideoView cũ vẫn đang đọc.</p>
 */
public final class WorkoutVideoDecoder {

    private static final String TAG = "WorkoutVideoDecoder";
    public static final String VIDEO_FILE_NAME = "temp.mp4";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "workout-video-decoder");
        thread.setPriority(Thread.NORM_PRIORITY - 1);
        return thread;
    });
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public interface Callback {
        /** Gọi trên main thread; {@code available} = đã có {@code temp.mp4} để phát. */
        void onVideoReady(boolean available);
    }

    private WorkoutVideoDecoder() {
    }

    @NonNull
    public static File videoFile(@NonNull Context context) {
        return new File(context.getCacheDir(), VIDEO_FILE_NAME);
    }

    /** Xếp lịch giải mã video của {@code actionId}; không chặn main thread. */
    public static void decode(@NonNull Context context, int discover, int actionId) {
        final Context appContext = context.getApplicationContext();
        final int version = FitnessApplication.getApplication().getVersion();
        EXECUTOR.execute(() -> decodeBlocking(appContext, discover, version, actionId));
    }

    /** Gọi {@code callback} trên main thread sau khi các lần giải mã đã xếp lịch xong. */
    public static void whenReady(@NonNull Context context, @NonNull Callback callback) {
        final Context appContext = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            File target = videoFile(appContext);
            boolean available = target.exists() && target.length() > 0;
            MAIN.post(() -> callback.onVideoReady(available));
        });
    }

    private static void decodeBlocking(Context context, int discover, int version, int actionId) {
        File target = videoFile(context);
        File part = new File(context.getCacheDir(), VIDEO_FILE_NAME + ".part");
        try {
            if (discover == 0) {
                Decoder.decryptToFile(context.getAssets(), "i/v/video_" + actionId, part.getPath());
            } else {
                Decoder.decryptInputToFile(context.getFilesDir() + "/discover/" + discover + "/" + version + "/v/video_" + actionId, part.getPath());
            }
            if (part.length() == 0 || !part.renameTo(target)) {
                throw new IllegalStateException("No decoded video for action " + actionId);
            }
        } catch (Exception e) {
            Log.w(TAG, "Unable to decode video for action " + actionId, e);
            // Không để lại video của bài trước dưới tên temp.mp4.
            target.delete();
        } finally {
            part.delete();
        }
    }
}
