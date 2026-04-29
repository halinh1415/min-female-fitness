package mint.woman.female.workout.activity.ui.workout;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import mint.woman.female.workout.R;
import mint.woman.female.workout.utils.AppEncrypt;
import mint.woman.female.workout.utils.MyPreference;

public class AudioAppManager {

    private static final String TAG = "AudioAppManager";
    private  final String support_language ="ar@de@en@es@fa@fr@in_ID@it@ja@ko@nl@pl@pt@ru@tr@zh_CN@zh_TW";
    private Context context;
    private LinkedList<String> audioResources;
    private MediaPlayer mediaPlayer;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private Locale locale;
    IAudioMangerFinishListener finishListener = null;
    public boolean isPause = false;
    public MyPreference myPreference;

    Runnable checkQueueRunnable = new Runnable() {

        public void run() {
            if (!isPause) {
                if (audioResources.size() > 0) {
                    if (!mediaPlayer.isPlaying() && !tts.isSpeaking()) {
                        playAssets();
                    }
                } else if ( !mediaPlayer.isPlaying() && !tts.isSpeaking()) {
                    if (finishListener != null) {
                        finishListener.onFinish();
                        finishListener = null;
                    }
                }
            }
            handler.postDelayed(this, 500);
        }
    };
    Handler handler = new Handler();


    public AudioAppManager(Context context, String languageCode) {
        this.context = context;
        this.audioResources = new LinkedList<>();
        this.mediaPlayer = new MediaPlayer();
        myPreference = MyPreference.newInstance(context);
        this.tts = new TextToSpeech(context, i -> {
            if (i == TextToSpeech.SUCCESS) {
                if (!languageCode.isEmpty()) {
                    tts.setLanguage(new Locale(languageCode));

                }
                Log.d(TAG, "Play tts ready! ");
                ttsReady = true;
            }
        });

        this.locale = new Locale(languageCode);
        handler.postDelayed(checkQueueRunnable, 500);
    }

    public String getLocaleApp() {
        if (locale.getLanguage().contains("zh")) {
            return locale.getLanguage();
        }
        String language = locale.getLanguage();
        if (!language.isEmpty() && language.length() > 2) {
            language = language.substring(0, 2);
        } else {
            language = Locale.getDefault().getLanguage();
            if (!language.isEmpty() && language.length() > 2){
                language = language.substring(0, 2);
            }
        }
        return language;
    }


    public void playText(String text) {
        audioResources.add(text);
//        if (!mediaPlayer.isPlaying() && !tts.isSpeaking() && ttsReady) {
//            playAssets();
//        }
    }

    public void playTextWithListener(String text, IAudioMangerFinishListener finishListener) {
        audioResources.add(text);
        this.finishListener = finishListener;
//        if (!mediaPlayer.isPlaying() && !tts.isSpeaking()) {
//            playAssets();
//        }

    }

    public void addText(String... texts) {

        audioResources.addAll(Arrays.asList(texts));
    }

    public void addTextAndClear(String text) {
        if (tts!= null && tts.isSpeaking()){
            tts.stop();
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        audioResources.clear();
        audioResources.add(text);
    }

    public String getBasePath() {
        return context.getFilesDir()+"/voice/" + myPreference.getLanguage() + "/" + myPreference.getVoiceModel(myPreference.getLanguage());
    }

    public void playAssets() {
        if (audioResources.isEmpty()) return;

        // Kiểm tra trạng thái
        if (mediaPlayer.isPlaying() || tts.isSpeaking() || !ttsReady) return;

        // Lấy nội dung
        String content = "";
        if (audioResources.size() > 0) {
            content = audioResources.pop();
        }
        if (content.isEmpty()) {
            playAssets(); // Đệ quy tìm tiếp nếu rỗng
            return;
        }

        final String finalContent = content; // Biến final để dùng trong Thread

        // Logic kiểm tra voice custom
        boolean isCustomVoice = !myPreference.getVoiceModel(myPreference.getLanguage()).equals("default")
                && FirebaseRemoteConfig.getInstance().getBoolean("enable_voice");

        if (isCustomVoice) {
            String filePath = getBasePath() + "/" + AppEncrypt.getName(finalContent);
            File encryptedFile = new File(filePath);

            if (!encryptedFile.exists()) {
                speakWithTTS(finalContent);
                return;
            }

            // --- KHẮC PHỤC ANR: CHẠY TRÊN THREAD RIÊNG ---
            new Thread(() -> {
                File tempFile = new File(context.getCacheDir(), "temp_voice.mp3");
                boolean success = false;

                // 1. Thực hiện giải mã (Nặng) ở Thread phụ
                try (FileInputStream fis = new FileInputStream(encryptedFile);
                     FileOutputStream fos = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        for (int i = 0; i < bytesRead; i++) {
                            buffer[i] ^= 0xAB;
                        }
                        fos.write(buffer, 0, bytesRead);
                    }
                    success = true;
                } catch (Exception e) {
                    Log.e(TAG, "Decryption failed: " + e.getMessage());
                }

                // 2. Quay lại Main Thread để phát nhạc
                final boolean finalSuccess = success;
                handler.post(() -> {
                    if (finalSuccess) {
                        try {
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(tempFile.getAbsolutePath());
                            // Dùng prepareAsync thay cho prepare để tránh block thêm
                            mediaPlayer.setOnPreparedListener(mp -> mp.start());
                            mediaPlayer.prepareAsync();

                            mediaPlayer.setOnCompletionListener(mp -> {
                                // tempFile.delete(); // Xóa file tạm nếu cần
                                playAssets();
                            });
                            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                                speakWithTTS(finalContent); // Lỗi thì fallback về TTS
                                return true;
                            });

                        } catch (Exception e) {
                            speakWithTTS(finalContent);
                        }
                    } else {
                        speakWithTTS(finalContent);
                    }
                });
            }).start();
            // --- KẾT THÚC THREAD ---

        } else {
            speakWithTTS(finalContent);
        }
    }

    private void speakWithTTS(String content) {
        if (ttsReady) {
            tts.speak(content, TextToSpeech.QUEUE_ADD, null, "");
            tts.setOnUtteranceCompletedListener(utteranceId -> {
                playAssets();
            });
        } else {
            // Nếu TTS chưa sẵn sàng, đợi 1 chút rồi thử lại hoặc bỏ qua
            handler.postDelayed(() -> playAssets(), 500);
        }
    }

//    private boolean isAssetExists(String pathInAssetsDir) {
//        AssetManager assetManager = context.getAssets();
//        InputStream inputStream = null;
//        try {
//            inputStream = assetManager.open(pathInAssetsDir);
//            if (null != inputStream) {
//                inputStream.close();
//                return true;
//            }
//        } catch (IOException e) {
//            return false;
//        }
//        return false;
//    }


    public void stop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    public void release() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (handler != null) {
            handler.removeCallbacks(checkQueueRunnable);
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    public void pause(){
        isPause = true;
        if (tts.isSpeaking()){
            tts.stop();
        }
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
    }
    public void resume(){
        isPause = false;
    }

    public interface IAudioMangerFinishListener {
        void onFinish();
    }
}