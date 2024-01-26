package mint.woman.female.workout.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AudioAppManager {

    private static final String TAG = "AudioAppManager";
    private Context context;
    private List<String> audioResources;
    private MediaPlayer mediaPlayer;
    private TextToSpeech tts;
    private int currentIndex;
    private boolean ttsReady = false, isMale = false;
    private Locale locale;

    Runnable r = new Runnable() {

        public void run() {

            if (!mediaPlayer.isPlaying()) {
                if (currentIndex < audioResources.size()) {
                    // currentIndex++;
                   // Log.d(TAG, "Play audio check! ");
                    playAssets();
                }
            }
            handler.postDelayed(this, 1000);
        }
    };
    Handler handler = new Handler();

    public void setMale(boolean male) {
        isMale = male;
    }

    public AudioAppManager(Context context, String languageCode) {
        this.context = context;
        this.audioResources = new ArrayList<>();
        this.mediaPlayer = new MediaPlayer();
        this.currentIndex = 0;
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
        handler.postDelayed(r, 1000);
    }

    public String getLocaleApp() {
        if (locale.getLanguage().contains("zh")) {
            return locale.getLanguage();
        }
        String language = locale.getLanguage();
        return language.substring(0, 2);
    }


    public void playText(String text) {
        audioResources.add(text);
    }


    public void addText(String... texts) {
        audioResources.clear();
        currentIndex = 0;
        audioResources.addAll(Arrays.asList(texts));
    }

    public void addTextAndClear(String text) {
        if (tts!= null && tts.isSpeaking()){
            tts.stop();
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        audioResources.clear();
        currentIndex = 0;
        audioResources.add(text);
    }

    public String getBasePath() {
        return "audio/" + getLocaleApp() + "/" + (isMale ? "male" : "female");
    }

    public void playAssets() {
        if (audioResources.isEmpty()) {
            return; // No audio resources to play
        }

        if (mediaPlayer.isPlaying()) {
           return;
        }
        if (tts.isSpeaking()) {
            return;
        }
        Log.d(TAG, "Play voice: current =" +currentIndex+" in size="+audioResources.size());
        try {
            if (audioResources.get(currentIndex).isEmpty()) {
                return;
            }
            String filePath = getBasePath() + "/" + AppEncrypt.getName(audioResources.get(currentIndex)) + ".mp3";


            if (isFilePresent(filePath)) {
                mediaPlayer.reset();
                Log.d(TAG, "Play audio: " + filePath);
                File audioFile = new File(context.getFilesDir(),filePath);
                mediaPlayer.setDataSource(audioFile.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> {
                    // Play the next audio file when the current one completes
                    currentIndex++;
                    if (currentIndex < audioResources.size()) {
                        playAssets();
                    } else {
                        currentIndex = 0;
                        audioResources.clear();
                    }
                });
            } else {

                if (ttsReady) {
                    tts.speak(audioResources.get(currentIndex), TextToSpeech.QUEUE_ADD, null, "");
                    currentIndex++;
                    if (currentIndex < audioResources.size()) {
                    }else{
                        currentIndex = 0;
                        audioResources.clear();
                    }
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
    public boolean isFilePresent(String fileName) {
        try {
            File file = new File(context.getFilesDir(), fileName);
            return file.exists();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

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
            handler.removeCallbacks(r);
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }


}