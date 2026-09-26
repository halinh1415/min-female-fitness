package mint.woman.female.workout.utils;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Serial background thread for every {@link android.speech.tts.TextToSpeech} call.
 *
 * <p>Every TextToSpeech method (speak/stop/isSpeaking/setLanguage/shutdown, and the constructor)
 * goes through {@code TextToSpeech.runAction}, which takes the engine connection lock. That same
 * lock is held by the framework's setup task while it makes binder calls into the TTS engine
 * service. When the engine is slow to answer - common on some OEM builds - any TTS call made from
 * the main thread blocks until the binder round trip returns, which shows up as an ANR.
 *
 * <p>Running the calls here keeps the main thread free. The single thread also keeps the calls
 * ordered, so a queued {@code stop()} can never overtake the {@code speak()} before it.
 */
public final class TtsExecutor {

    private static final String TAG = "TtsExecutor";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "tts-ops");
        thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    });

    private TtsExecutor() {
    }

    /** Runs {@code action} on the shared TTS thread. Never throws to the caller. */
    public static void run(Runnable action) {
        if (action == null) return;
        try {
            EXECUTOR.execute(() -> {
                try {
                    action.run();
                } catch (Throwable t) {
                    Log.w(TAG, "TTS action failed: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "Could not schedule TTS action: " + e.getMessage());
        }
    }
}
