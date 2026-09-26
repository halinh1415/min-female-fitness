package mint.woman.female.workout.activity.ui.workout;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mint.woman.female.workout.utils.AppEncrypt;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.TtsExecutor;

public class AudioAppManager {

    private static final String TAG = "AudioAppManager";
    private static final int TTS_RETRY_LIMIT = 20;
    private static final long TTS_RETRY_DELAY_MS = 500L;

    private volatile Context context;
    private final LinkedList<String> audioResources = new LinkedList<>();
    private MediaPlayer mediaPlayer;
    /** Touched from the main handler and from the TTS thread. */
    private volatile TextToSpeech tts;
    private boolean ttsReady = false;
    private int ttsRetryCount = 0;
    private Locale locale;
    public MyPreference myPreference;

    // Kept public for callers that read it; resume()/pause() manage it.
    public boolean isPause = false;

    private IAudioMangerFinishListener finishListener = null;
    private IAudioMangerFinishListener delayedListener = null;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService decryptExecutor = Executors.newSingleThreadExecutor();

    private Runnable delayedPlayRunnable = null;
    private Runnable delayedPlayWithListenerRunnable = null;

    private volatile boolean released = false;
    private boolean playing = false;
    private boolean preparing = false;
    /** Bumped on the main handler, read from the TTS thread to drop stale work. */
    private volatile int sessionId = 0;
    private long nextPlaybackId = 0;
    private long activeCustomPlaybackId = 0;
    private String currentUtteranceId;
    private File activeTempFile;


    public AudioAppManager(Context context, String languageCode) {
        this.context = context.getApplicationContext();
        this.mediaPlayer = new MediaPlayer();
        myPreference = MyPreference.newInstance(context);
        this.locale = buildLocale(languageCode);
        initTtsAsync(this.context);
    }

    /**
     * Builds the engine off the main thread: the constructor queries the package manager and binds
     * the engine service, and every later call takes a lock the framework holds across binder calls
     * to that engine. See {@link TtsExecutor}.
     */
    private void initTtsAsync(Context appContext) {
        TtsExecutor.run(() -> {
            if (released) return;
            TextToSpeech engine = new TextToSpeech(appContext,
                    status -> TtsExecutor.run(() -> configureTts(status)));
            tts = engine;
            if (released) {
                tts = null;
                shutdownEngine(engine);
            }
        });
    }

    /**
     * Runs on the TTS thread. Because that thread is serial, the engine field is already assigned
     * by the time this is queued from the init callback.
     */
    private void configureTts(int status) {
        TextToSpeech engine = tts;
        if (released || engine == null) return;
        if (status != TextToSpeech.SUCCESS) {
            Log.w(TAG, "TTS init failed: " + status);
            return;
        }
        try {
            int result = engine.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "TTS language not supported: " + locale + ", fallback to default");
                engine.setLanguage(Locale.getDefault());
            }
        } catch (Exception e) {
            Log.w(TAG, "setLanguage failed: " + e.getMessage());
        }
        try {
            engine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override public void onStart(String utteranceId) { }
                @Override public void onDone(String utteranceId) {
                    handler.post(() -> onUtteranceFinished(utteranceId));
                }
                @Override public void onError(String utteranceId) {
                    handler.post(() -> onUtteranceFinished(utteranceId));
                }
                @Override public void onStop(String utteranceId, boolean interrupted) {
                    handler.post(() -> onUtteranceFinished(utteranceId));
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "setOnUtteranceProgressListener failed: " + e.getMessage());
            return;
        }
        handler.post(() -> {
            if (released) return;
            Log.d(TAG, "Play tts ready! ");
            ttsReady = true;
            ttsRetryCount = 0;
            startNextIfIdle();
        });
    }

    private static void shutdownEngine(TextToSpeech engine) {
        if (engine == null) return;
        try { engine.stop(); } catch (Exception ignored) { }
        try { engine.shutdown(); } catch (Exception ignored) { }
    }

    private static Locale buildLocale(String languageCode) {
        if (languageCode == null || languageCode.isEmpty()) {
            return Locale.getDefault();
        }
        String normalized = languageCode.trim().replace('-', '_');
        String[] parts = normalized.split("_");
        String lang = parts[0].toLowerCase(Locale.US);
        // Legacy "in" code maps to ISO "id" for Indonesian.
        if ("in".equals(lang)) lang = "id";
        if (parts.length >= 2 && !parts[1].isEmpty()) {
            return new Locale(lang, parts[1].toUpperCase(Locale.US));
        }
        return new Locale(lang);
    }

    public String getLocaleApp() {
        Locale target = locale != null ? locale : Locale.getDefault();
        String language = target.getLanguage();
        if (language == null || language.isEmpty()) {
            language = Locale.getDefault().getLanguage();
        }
        if ("in".equalsIgnoreCase(language)) return "id";
        if (language != null && language.toLowerCase(Locale.US).startsWith("zh")) {
            return target.toString();
        }
        if (language != null && language.length() > 2) {
            return language.substring(0, 2).toLowerCase(Locale.US);
        }
        return language;
    }


    public void playText(String text) {
        if (text == null || text.isEmpty()) return;
        handler.post(() -> {
            if (released) return;
            audioResources.add(text);
            startNextIfIdle();
        });
    }

    public void playTextWithListener(String text, IAudioMangerFinishListener listener) {
        handler.post(() -> {
            if (released) return;
            if (text == null || text.isEmpty()) {
                if (listener != null) listener.onFinish();
                return;
            }
            audioResources.add(text);
            if (listener != null) {
                IAudioMangerFinishListener previous = finishListener;
                if (previous == null) {
                    finishListener = listener;
                } else {
                    finishListener = () -> {
                        try { previous.onFinish(); } catch (Exception ignored) { }
                        try { listener.onFinish(); } catch (Exception ignored) { }
                    };
                }
            }
            startNextIfIdle();
        });
    }

    public void addText(String... texts) {
        if (texts == null || texts.length == 0) return;
        List<String> safe = new ArrayList<>();
        for (String t : texts) {
            if (t != null && !t.isEmpty()) safe.add(t);
        }
        if (safe.isEmpty()) return;
        handler.post(() -> {
            if (released) return;
            audioResources.addAll(safe);
            startNextIfIdle();
        });
    }

    public void addTextAndClear(String text) {
        handler.post(() -> {
            if (released) return;
            invalidateCurrentLocked();
            audioResources.clear();
            if (text != null && !text.isEmpty()) audioResources.add(text);
            startNextIfIdle();
        });
    }

    public String getBasePath() {
        if (context == null || myPreference == null) return "";
        return context.getFilesDir() + "/voice/" + myPreference.getLanguage() + "/" + myPreference.getVoiceModel(myPreference.getLanguage());
    }

    /**
     * Schedule a text to be played after a delay (milliseconds).
     * Safe to call from any thread.
     *
     * @param text the text to play
     * @param delayMillis delay in milliseconds before the text is queued
     */
    public void playTextWithDelay(String text, long delayMillis) {
        if (text == null) return;
        handler.post(() -> {
            if (released) return;
            if (delayedPlayRunnable != null) {
                handler.removeCallbacks(delayedPlayRunnable);
                delayedPlayRunnable = null;
            }
            final int session = sessionId;
            delayedPlayRunnable = () -> {
                delayedPlayRunnable = null;
                if (released || session != sessionId || isPause) return;
                if (text.isEmpty()) return;
                audioResources.add(text);
                startNextIfIdle();
            };
            handler.postDelayed(delayedPlayRunnable, Math.max(0, delayMillis));
        });
    }

    /**
     * Schedule a text to be played after a delay and notify when the queue drains.
     *
     * @param text the text to play
     * @param delayMillis delay in milliseconds before the text is queued
     * @param listener listener to be called when playback completes
     */
    public void playTextWithDelay(String text, long delayMillis, IAudioMangerFinishListener listener) {
        if (text == null) return;
        handler.post(() -> {
            if (released) return;
            if (delayedPlayWithListenerRunnable != null) {
                handler.removeCallbacks(delayedPlayWithListenerRunnable);
                delayedPlayWithListenerRunnable = null;
                delayedListener = null;
            }
            final int session = sessionId;
            delayedListener = listener;
            delayedPlayWithListenerRunnable = () -> {
                delayedPlayWithListenerRunnable = null;
                IAudioMangerFinishListener pending = delayedListener;
                delayedListener = null;
                if (released || session != sessionId || isPause) return;
                if (!text.isEmpty()) audioResources.add(text);
                if (pending != null) {
                    IAudioMangerFinishListener previous = finishListener;
                    if (previous == null) {
                        finishListener = pending;
                    } else {
                        finishListener = () -> {
                            try { previous.onFinish(); } catch (Exception ignored) { }
                            try { pending.onFinish(); } catch (Exception ignored) { }
                        };
                    }
                }
                startNextIfIdle();
            };
            handler.postDelayed(delayedPlayWithListenerRunnable, Math.max(0, delayMillis));
        });
    }

    public void playAssets() {
        handler.post(this::startNextIfIdle);
    }

    private void startNextIfIdle() {
        if (released || isPause || playing || preparing) return;
        String content = null;
        while (!audioResources.isEmpty()) {
            String next = audioResources.poll();
            if (next != null && !next.isEmpty()) {
                content = next;
                break;
            }
        }
        if (content == null) {
            notifyFinishIfIdle();
            return;
        }
        playing = true;
        final String finalContent = content;
        boolean isCustomVoice = false;
        try {
            isCustomVoice = myPreference != null
                    && !"default".equals(myPreference.getVoiceModel(myPreference.getLanguage()))
                    && FirebaseRemoteConfig.getInstance().getBoolean("enable_voice");
        } catch (Exception e) {
            Log.w(TAG, "RemoteConfig check failed, fallback to TTS: " + e.getMessage());
        }

        if (isCustomVoice) {
            String filePath = getBasePath() + "/" + AppEncrypt.getName(finalContent);
            File encryptedFile = new File(filePath);
            if (!encryptedFile.exists()) {
                speakWithTTS(finalContent);
                return;
            }
            playCustomVoice(encryptedFile, finalContent);
        } else {
            speakWithTTS(finalContent);
        }
    }

    private void playCustomVoice(File encryptedFile, String fallbackText) {
        final int session = sessionId;
        final long playbackId = ++nextPlaybackId;
        activeCustomPlaybackId = playbackId;
        preparing = true;
        decryptExecutor.execute(() -> {
            File tempFile;
            try {
                File cacheDir = context != null ? context.getCacheDir() : null;
                if (cacheDir == null) throw new IllegalStateException("context released");
                tempFile = File.createTempFile("voice_", ".mp3", cacheDir);
            } catch (Exception e) {
                Log.e(TAG, "Temp file failed: " + e.getMessage());
                handler.post(() -> {
                    if (released || session != sessionId) return;
                    preparing = false;
                    speakWithTTS(fallbackText);
                });
                return;
            }
            try (FileInputStream fis = new FileInputStream(encryptedFile);
                 FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    if (Thread.currentThread().isInterrupted()) throw new InterruptedException("cancelled");
                    for (int i = 0; i < bytesRead; i++) {
                        buffer[i] ^= 0xAB;
                    }
                    fos.write(buffer, 0, bytesRead);
                }
            } catch (InterruptedException e) {
                deleteQuietly(tempFile);
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                Log.e(TAG, "Decryption failed: " + e.getMessage());
                deleteQuietly(tempFile);
                handler.post(() -> {
                    if (released || session != sessionId) return;
                    preparing = false;
                    speakWithTTS(fallbackText);
                });
                return;
            }
            final File doneFile = tempFile;
            handler.post(() -> onDecrypted(session, doneFile, fallbackText));
        });
    }

    private void onDecrypted(int session, File tempFile, String fallbackText) {
        if (released || session != sessionId) {
            deleteQuietly(tempFile);
            return;
        }
        if (isPause) {
            deleteQuietly(tempFile);
            preparing = false;
            playing = false;
            return;
        }
        deleteQuietly(activeTempFile);
        activeTempFile = tempFile;
        try {
            if (mediaPlayer == null) mediaPlayer = new MediaPlayer();
            try { mediaPlayer.reset(); } catch (IllegalStateException ignored) { }
            mediaPlayer.setDataSource(tempFile.getAbsolutePath());
            mediaPlayer.setOnPreparedListener(mp -> {
                if (released || session != sessionId) return;
                preparing = false;
                try {
                    mp.start();
                } catch (IllegalStateException e) {
                    Log.w(TAG, "MediaPlayer start failed: " + e.getMessage());
                    onCustomVoiceFinished(session, true, fallbackText);
                }
            });
            mediaPlayer.setOnCompletionListener(mp -> onCustomVoiceFinished(session, false, fallbackText));
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                onCustomVoiceFinished(session, true, fallbackText);
                return true;
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, "Error playing custom voice: " + e.getMessage());
            onCustomVoiceFinished(session, true, fallbackText);
        }
    }

    private void onCustomVoiceFinished(int session, boolean fallbackToTts, String fallbackText) {
        if (released || session != sessionId) return;
        preparing = false;
        deleteQuietly(activeTempFile);
        activeTempFile = null;
        if (fallbackToTts) {
            speakWithTTS(fallbackText);
            return;
        }
        playing = false;
        startNextIfIdle();
    }

    private void speakWithTTS(String content) {
        if (released) {
            playing = false;
            return;
        }
        if (!ttsReady || tts == null) {
            if (tts == null || ttsRetryCount >= TTS_RETRY_LIMIT) {
                Log.w(TAG, "TTS unavailable, dropping item");
                playing = false;
                preparing = false;
                startNextIfIdle();
                return;
            }
            ttsRetryCount++;
            audioResources.addFirst(content);
            playing = false;
            handler.postDelayed(() -> {
                if (!released) startNextIfIdle();
            }, TTS_RETRY_DELAY_MS);
            return;
        }
        ttsRetryCount = 0;
        final String utteranceId = UUID.randomUUID().toString();
        currentUtteranceId = utteranceId;
        final int session = sessionId;
        TtsExecutor.run(() -> {
            TextToSpeech engine = tts;
            if (engine == null || released || session != sessionId) return;
            int result;
            try {
                try { engine.stop(); } catch (Exception ignored) { }
                result = engine.speak(content, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
            } catch (Exception e) {
                Log.w(TAG, "TTS speak failed: " + e.getMessage());
                result = TextToSpeech.ERROR;
            }
            if (result == TextToSpeech.ERROR) {
                Log.w(TAG, "TTS speak rejected, skipping item");
                handler.post(() -> {
                    if (released || session != sessionId) return;
                    // A newer utterance may already own the queue; only this one may skip ahead.
                    if (!utteranceId.equals(currentUtteranceId)) return;
                    currentUtteranceId = null;
                    playing = false;
                    startNextIfIdle();
                });
            }
        });
    }

    private void onUtteranceFinished(String utteranceId) {
        if (released) return;
        if (currentUtteranceId == null || !currentUtteranceId.equals(utteranceId)) return;
        currentUtteranceId = null;
        playing = false;
        startNextIfIdle();
    }

    private void notifyFinishIfIdle() {
        if (playing || preparing || !audioResources.isEmpty()) return;
        IAudioMangerFinishListener pending = finishListener;
        finishListener = null;
        if (pending != null) {
            try {
                pending.onFinish();
            } catch (Exception e) {
                Log.w(TAG, "Finish listener failed: " + e.getMessage());
            }
        }
    }

    /** Must run on the main handler thread. */
    private void invalidateCurrentLocked() {
        sessionId++;
        playing = false;
        preparing = false;
        currentUtteranceId = null;
        try {
            if (mediaPlayer != null) {
                try {
                    if (isMediaPlayerPlaying()) mediaPlayer.stop();
                } catch (Exception ignored) { }
                try { mediaPlayer.reset(); } catch (Exception ignored) { }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error invalidating mediaPlayer: " + e.getMessage());
        }
        TtsExecutor.run(() -> {
            TextToSpeech engine = tts;
            if (engine == null) return;
            try { engine.stop(); } catch (Exception e) {
                Log.w(TAG, "Error invalidating TTS: " + e.getMessage());
            }
        });
        deleteQuietly(activeTempFile);
        activeTempFile = null;
    }

    private void cancelDelayedLocked() {
        if (delayedPlayRunnable != null) {
            handler.removeCallbacks(delayedPlayRunnable);
            delayedPlayRunnable = null;
        }
        if (delayedPlayWithListenerRunnable != null) {
            handler.removeCallbacks(delayedPlayWithListenerRunnable);
            delayedPlayWithListenerRunnable = null;
            delayedListener = null;
        }
    }

    private static void deleteQuietly(File file) {
        if (file == null) return;
        try {
            if (file.exists() && !file.delete()) {
                Log.w(TAG, "Could not delete temp file: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            Log.w(TAG, "Delete temp failed: " + e.getMessage());
        }
    }


    public void stop() {
        handler.post(() -> {
            if (released) return;
            cancelDelayedLocked();
            invalidateCurrentLocked();
            audioResources.clear();
            finishListener = null;
        });
    }

    public void skip() {
        stop();
    }

    public void release() {
        if (released) return;
        released = true;
        isPause = true;
        // Cancel anything already queued before scheduling our own cleanup, so it
        // still runs even though removeCallbacksAndMessages(null) is called next.
        handler.removeCallbacksAndMessages(null);
        decryptExecutor.shutdownNow();
        // audioResources, mediaPlayer, tts, etc. are only ever touched on the main
        // handler thread; release() may be called from any thread, so its cleanup
        // must be posted too, or it can race a callback mid-flight on that thread.
        handler.post(() -> {
            sessionId++;
            audioResources.clear();
            finishListener = null;
            delayedListener = null;
            delayedPlayRunnable = null;
            delayedPlayWithListenerRunnable = null;
            deleteQuietly(activeTempFile);
            activeTempFile = null;
            final TextToSpeech engine = tts;
            tts = null;
            ttsReady = false;
            if (engine != null) {
                TtsExecutor.run(() -> shutdownEngine(engine));
            }
            if (mediaPlayer != null) {
                try { mediaPlayer.reset(); } catch (Exception ignored) { }
                try { mediaPlayer.release(); } catch (Exception ignored) { }
                mediaPlayer = null;
            }
            context = null;
        });
    }

    public void pause() {
        handler.post(() -> {
            if (released) return;
            isPause = true;
            cancelDelayedLocked();
            invalidateCurrentLocked();
        });
    }

    public void resume() {
        handler.post(() -> {
            if (released) return;
            if (!isPause) return;
            isPause = false;
            startNextIfIdle();
        });
    }

    /**
     * Safely check if media player is playing.
     * Returns false if mediaPlayer is null or in an invalid state.
     */
    private boolean isMediaPlayerPlaying() {
        try {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        } catch (IllegalStateException e) {
            Log.w(TAG, "MediaPlayer in invalid state: " + e.getMessage());
            return false;
        }
    }

    public interface IAudioMangerFinishListener {
        void onFinish();
    }
}
