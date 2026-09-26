package mint.woman.female.workout.receiver;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Luồng nền dùng chung cho BroadcastReceiver (kèm goAsync()). Việc trong onReceive chạy trên main
 * thread, mà khi receiver đánh thức process đang chết thì main thread còn bận khởi động app, nên
 * đẩy phần I/O (WorkManager, database, decode ảnh) ra đây.
 */
final class ReceiverExecutor {

    private static final String TAG = "ReceiverExecutor";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "receiver-work");
        thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    });

    private ReceiverExecutor() {
    }

    static void run(Runnable task) {
        EXECUTOR.execute(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                Log.w(TAG, "receiver work failed", t);
            }
        });
    }
}
