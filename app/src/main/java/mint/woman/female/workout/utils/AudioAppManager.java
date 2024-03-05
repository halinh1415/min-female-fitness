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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import mint.woman.female.workout.R;

public class AudioAppManager {

    private static final String TAG = "AudioAppManager";
    private  final String support_language ="ar@de@en@es@fa@fr@in_ID@it@ja@ko@nl@pl@pt@ru@tr@zh_CN@zh_TW";
    private Context context;
    private LinkedList<String> audioResources;
    private MediaPlayer mediaPlayer;
    private TextToSpeech tts;
    private int currentIndex;
    private boolean ttsReady = false, isMale = false;
    private Locale locale;
    IAudioMangerFinishListener finishListener = null;
    public boolean isPause = false;

    Runnable r = new Runnable() {

        public void run() {
            if (!isPause) {
                if (audioResources.size() > 0) {
                    if (!mediaPlayer.isPlaying() && !tts.isSpeaking()) {
                        playAssets();
                    }
                } else if (!mediaPlayer.isPlaying() && !tts.isSpeaking()) {
                    if (finishListener != null) {
                        finishListener.onFinish();
                        finishListener = null;
                    }
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
        this.audioResources = new LinkedList<>();
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
    }

    public void playTextWithListener(String text, IAudioMangerFinishListener finishListener) {
        audioResources.add(text);
        this.finishListener = finishListener;
      //  Log.d("HAHA", "add with listener: " + text+" stack= "+audioResources.toString());
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
        return "audio/" + getLocaleApp() + "/" + (isMale ? "male" : "female");
    }

    public void playAssets() {
        if (audioResources.isEmpty()) {
            return; // No audio resources to play
        }

        if (mediaPlayer.isPlaying() || tts.isSpeaking()) {
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
            String filePath = getBasePath() + "/" + AppEncrypt.getName(content) + ".mp3";


            if (isFilePresent(filePath)) {
                mediaPlayer.reset();
                Log.d(TAG, "Play audio: " + filePath);
                File audioFile = new File(context.getFilesDir(),filePath);
                mediaPlayer.setDataSource(audioFile.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> {
                    // Play the next audio file when the current one completes
                    playAssets();
                });
            } else {
                if (ttsReady) {
                    //Log.d("HAHA","SPEAK :"+content+" size:"+audioResources.toString());
                    tts.speak(content, TextToSpeech.QUEUE_ADD, null, "");

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