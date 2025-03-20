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
        if (audioResources.isEmpty()) {
           // handler.post(checkQueueRunnable); // Nếu danh sách rỗng, bắt đầu kiểm tra
            return;
        }

       // handler.removeCallbacks(checkQueueRunnable); // Dừng kiểm tra khi có nội dung để phát

        if (mediaPlayer.isPlaying() || tts.isSpeaking() || !ttsReady) {
            return;
        }


       // Log.d(TAG, "Play voice: current in size=" + audioResources.size());
        try {
            String content = "";
            if (audioResources.size() > 0) {
                content = audioResources.pop();
            }
            if (content.isEmpty()) {
                return;
            }
            String filePath = getBasePath() + "/" + AppEncrypt.getName(content);


            if (!myPreference.getVoiceModel(myPreference.getLanguage()).equals("default") && FirebaseRemoteConfig.getInstance().getBoolean("enable_voice")) {
                File encryptedFile = new File(filePath);
                if (!encryptedFile.exists()) {

                    if (ttsReady) {
                        //Log.d("HAHA","SPEAK :"+content+" size:"+audioResources.toString());
                        tts.speak(content, TextToSpeech.QUEUE_ADD, null, "");
                        tts.setOnUtteranceCompletedListener(utteranceId -> {
                            playAssets();
                        });
                    }
                    return;
                }
                File tempFile = new File(context.getCacheDir(), "temp_voice.mp3");


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
                }


                mediaPlayer.reset();
                mediaPlayer.setDataSource(tempFile.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> {
                    tempFile.delete();
                    playAssets();
                });
            } else {
                if (ttsReady) {
                    //Log.d("HAHA","SPEAK :"+content+" size:"+audioResources.toString());
                    tts.speak(content, TextToSpeech.QUEUE_ADD, null, "");
                    tts.setOnUtteranceCompletedListener(utteranceId -> {
                        playAssets();
                    });
                }


            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Play Exception: " + e.toString());
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