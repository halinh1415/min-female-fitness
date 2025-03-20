package mint.woman.female.workout.activity.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.databinding.ActivityCoachOptionBinding;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.retrofit.ApiUtils;
import mint.woman.female.workout.utils.AppEncrypt;
import mint.woman.female.workout.utils.DownloadVoiceWorker;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

public class CoachOptionActivity extends AppCompatActivity implements VoiceAdapter.OnItemClickListener {

    MyPreference myPreference;
    ActivityCoachOptionBinding binding;
    private TextToSpeech tts;
    VoiceViewModel voiceViewModel;
    MediaPlayer mediaPlayer;
    private VoiceAdapter voiceAdapter;
    String defaultEngine;
    Locale defaultLocale;
    private List<VoiceItem> voiceList;
    private boolean isDownloading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCoachOptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();

    }

    private void initView() {
        mediaPlayer = new MediaPlayer();
        setTitle(getString(R.string.coach_voice));
        voiceViewModel = new ViewModelProvider(this).get(VoiceViewModel.class);
        myPreference = MyPreference.newInstance(this);

        voiceViewModel.setSelectModel(myPreference.getVoiceModel(myPreference.getLanguage()));
        voiceAdapter = new VoiceAdapter( this,getFilesDir()+"/voice/"+myPreference.getLanguage()+"/",this);
        binding.rcVoice.setAdapter(voiceAdapter);
        binding.rcVoice.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        this.tts = new TextToSpeech(this, i -> {
            if (i == TextToSpeech.SUCCESS) {
                if (!myPreference.getLanguage().isEmpty()) {
                    tts.setLanguage(new Locale(myPreference.getLanguage()));
                    defaultEngine = tts.getDefaultEngine();
                    defaultLocale = tts.getDefaultVoice().getLocale();
                    if (voiceAdapter != null){
                        voiceAdapter.setDefaultTTs(defaultEngine,defaultLocale);
                    }
                }
            }
        });
        voiceAdapter.setSelectVoice(myPreference.getVoiceModel(myPreference.getLanguage()));
        voiceList = new ArrayList<>();
        voiceList.add(new VoiceItem(R.drawable.ic_coach_default, getString(R.string.default_text), "default"));
        if (FirebaseRemoteConfig.getInstance().getBoolean("enable_voice")) {
            String remoteVoice = FirebaseRemoteConfig.getInstance().getString("voice");
            try {
                JSONObject jsonObject = new JSONObject(remoteVoice);

                if (jsonObject.getString("Adam").contains(myPreference.getLanguage())) {
                    voiceList.add(new VoiceItem(R.drawable.ic_coach_jennifer, "Jennifer", "Bella"));
                }
                if (jsonObject.getString("Bella").contains(myPreference.getLanguage())) {
                    voiceList.add(new VoiceItem(R.drawable.ic_coach_adam, "Adam", "Adam"));
                }
                if (jsonObject.getString("woman").contains(myPreference.getLanguage())) {
                    voiceList.add(new VoiceItem(R.drawable.ic_coach_woman, getString(R.string.name_female), "woman"));
                }
                if (jsonObject.getString("man").contains(myPreference.getLanguage())) {
                    voiceList.add(new VoiceItem(R.drawable.ic_coach_male, getString(R.string.name_male), "man"));
                }


            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }else {
            myPreference.setVoiceModel(myPreference.getLanguage(),"default");
            myPreference.setVoiceModelName(myPreference.getLanguage(),"Default");
            voiceAdapter.setSelectVoice("default");
        }
        voiceAdapter.setVoiceList(voiceList);




    }

    public String getBasePath() {
        return getFilesDir()+"/voice/" + myPreference.getLanguage() + "/" + myPreference.getVoiceModel(myPreference.getLanguage());
    }
    void playTest(VoiceItem voiceItem){
        try {

            String filePath =  getFilesDir()+"/voice/" + myPreference.getLanguage() + "/" +voiceItem.getValue() + "/" + AppEncrypt.getName(String.format(getString(R.string.tts_test_des),voiceItem.getName()));

            Log.d("HAHA","play test voice");
            File encryptedFile = new File(filePath);
            if (!encryptedFile.exists()) {
                Log.e("HAHA", "File không tồn tại: " + filePath);
                return;
            }
            File tempFile = new File(getCacheDir(), "temp_voice.mp3");
            voiceAdapter.setVoicePlay(voiceItem.getName());
            // Giải mã XOR và lưu vào file tạm
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

            // Phát file tạm với MediaPlayer
            mediaPlayer.reset();
            mediaPlayer.setDataSource(tempFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> {
                //mp.release();
                voiceAdapter.setVoicePlay("");
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void saveFileListToJson(List<String> fileList, String jsonFilePath) {
        JSONArray jsonArray = new JSONArray(fileList);
        try (FileOutputStream fos = new FileOutputStream(new File(jsonFilePath))) {
            fos.write(jsonArray.toString().getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void startDownLoad(VoiceItem voiceItem){
        String voicePath = getFilesDir()+"/voice/"+myPreference.getLanguage()+"/"+voiceItem.getValue()+"/";
        File dir = new File(voicePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        List<String> fileList = new ArrayList<>();
        fileList.add(AppEncrypt.getName(getString(R.string.wp_start)));
        fileList.add(AppEncrypt.getName(getString(R.string.v_start_with)));
        fileList.add(AppEncrypt.getName(getString(R.string.wp_ready_to_go)));
        fileList.add(AppEncrypt.getName(getString(R.string.wp_v_half_time)));
        fileList.add(AppEncrypt.getName(String.format(getString(R.string.tts_test_des),voiceItem.getName())));
        fileList.add(AppEncrypt.getName(getString(R.string.wp_seconds)));
        fileList.add(AppEncrypt.getName(getString(R.string.wp_each_side)));
        fileList.add(AppEncrypt.getName(getString(R.string.wp_the_next)));
        fileList.add(AppEncrypt.getName(getString(R.string.wp_have_a_rest)));
//        Log.d("DownloadVoiceWorker",getString(R.string.wp_each_side)+" : "+AppEncrypt.getName(getString(R.string.wp_each_side)));
        List<ExerciseObject> allExercise =  FitnessApplication.getApplication().mJsonHelper.getAllExercise(0,0);
        for (ExerciseObject exerciseObject:allExercise
             ) {
            fileList.add(AppEncrypt.getName(exerciseObject.getName()));
//            for (Tip tip: exerciseObject.getTips()){
//                fileList.add(AppEncrypt.getName(tip.getText()));
//            }
        }

        for (int i =1;i<50;i++) {
            String fileName = AppEncrypt.getName(""+i);
            fileList.add(fileName);

        }
        fileList.removeIf(fileName -> new File(voicePath, fileName).exists());
        if (!fileList.isEmpty()) {
            isDownloading = true;
            voiceAdapter.setVoiceDownloading(voiceItem.getValue());
            Data.Builder data = new Data.Builder();
            String jsonFilePath = voicePath + "/file_list.json";
            saveFileListToJson(fileList, jsonFilePath);
            data.putString("FILE_LIST_PATH", jsonFilePath);
            Log.d("DownloadVoiceWorker", "Size: " + fileList.size());
            data.putString("BASE_PATH", voicePath);
            data.putString("BASE_URL", ApiUtils.BASE_APP_URL + "voice/" + myPreference.getLanguage() + "/" + voiceItem.getValue() + "/");

            OneTimeWorkRequest syncWorkRequest =
                    new OneTimeWorkRequest.Builder(DownloadVoiceWorker.class)
                            .addTag("Sync")
                            .setInputData(data.build())
                            .build();

            WorkManager.getInstance(CoachOptionActivity.this).enqueue(syncWorkRequest);
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(syncWorkRequest.getId()).observe(this, workInfo -> {
                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                    if (!isFinishing() && !isDestroyed()) {
                        if (voiceAdapter != null) {
                            voiceAdapter.setVoiceDownloading("");
                            voiceAdapter.setSelectVoice(voiceItem.getValue());
                            voiceAdapter.setVoicePlay(voiceItem.getName());
                        }
                        if (myPreference != null) {
                            myPreference.setVoiceModel(myPreference.getLanguage(), voiceItem.getValue());
                            myPreference.setVoiceModelName(myPreference.getLanguage(), voiceItem.getName());
                        }
                        playTest(voiceItem);
                    }
                }
                if (workInfo.getState() == WorkInfo.State.FAILED){
                    if (!isFinishing() && !isDestroyed()) {
                        voiceAdapter.setVoiceDownloading("");
                        Toast.makeText(CoachOptionActivity.this, "Download Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
                isDownloading = false;
            });
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    @Override
    public void onItemClick(VoiceItem voiceItem) {
        voiceAdapter.setSelectVoice(voiceItem.getValue());
        myPreference.setVoiceModel(myPreference.getLanguage(),voiceItem.getValue());
        if (voiceItem.getValue().equals("default")){
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setVoiceAdapter("");
                }
            },2000);
            tts.speak(getString(R.string.test_voice),TextToSpeech.QUEUE_ADD,null,"");

        }else{
            String voicePath = getFilesDir()+"/voice/"+myPreference.getLanguage()+"/"+voiceItem.getValue()+"/";
            File dir = new File(voicePath);
            if (!dir.exists()) {
                dir.mkdirs();
                startDownLoad(voiceItem);
            }else {
                playTest(voiceItem);
                startDownLoad(voiceItem);
            }

        }
    }
    void setVoiceAdapter(String voice){
        voiceAdapter.setVoicePlay(voice);
    }

    @Override
    public void onItemChoose(VoiceItem voiceItem) {


        voiceAdapter.setVoicePlay(voiceItem.getName());
        if (voiceItem.getValue().equals("default")){
            voiceAdapter.setSelectVoice(voiceItem.getValue());
            myPreference.setVoiceModel(myPreference.getLanguage(),voiceItem.getValue());
            myPreference.setVoiceModelName(myPreference.getLanguage(),voiceItem.getName());
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
            voiceAdapter.setVoicePlay(voiceItem.getName());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setVoiceAdapter("");
                }
            },2000);
            tts.speak(getString(R.string.test_voice),TextToSpeech.QUEUE_ADD, null);

        }else{
            String voicePath = getFilesDir()+"/voice/"+myPreference.getLanguage()+"/"+voiceItem.getValue()+"/";
            File dir = new File(voicePath);

            if (!dir.exists()) {
                dir.mkdirs();
                if (!isDownloading) {
                    startDownLoad(voiceItem);
                }else {
                    Toast.makeText(CoachOptionActivity.this, "Downloading..,", Toast.LENGTH_SHORT).show();
                }
            }else {
                if (!(new File(voicePath, AppEncrypt.getName(getString(R.string.wp_ready_to_go))).exists())) {
                    if (!isDownloading) {
                        startDownLoad(voiceItem);
                    }else {
                        Toast.makeText(CoachOptionActivity.this, "Downloading..,", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    voiceAdapter.setSelectVoice(voiceItem.getValue());
                    myPreference.setVoiceModel(myPreference.getLanguage(),voiceItem.getValue());
                    myPreference.setVoiceModelName(myPreference.getLanguage(),voiceItem.getName());
                    playTest(voiceItem);
                }

            }

        }
    }

    @Override
    public void onItemPlay(VoiceItem voiceItem) {

        if (voiceItem.getValue().equals("default")){
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
            voiceAdapter.setVoicePlay(voiceItem.getName());
            new Handler().postDelayed(() -> setVoiceAdapter(""),2000);
            tts.speak(getString(R.string.test_voice),TextToSpeech.QUEUE_ADD, null);

        }else{

            String voicePath = getFilesDir()+"/voice/"+myPreference.getLanguage()+"/"+voiceItem.getValue()+"/";
            File dir = new File(voicePath);
            if (!dir.exists()) {
                dir.mkdirs();
                if (!isDownloading) {
                    startDownLoad(voiceItem);
                }else {
                    Toast.makeText(CoachOptionActivity.this, "Downloading..,", Toast.LENGTH_SHORT).show();
                }
            }else {
                if (!(new File(voicePath, AppEncrypt.getName(getString(R.string.wp_ready_to_go))).exists())) {
                    if (!isDownloading) {
                        startDownLoad(voiceItem);
                    }else {
                        Toast.makeText(CoachOptionActivity.this, "Downloading..,", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    playTest(voiceItem);
                }

            }

        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Home Button!
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void attachBaseContext(Context newBase) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String lang = preferences.getString("ST_LANGUAGE", "");
        if (!lang.isEmpty() ) {
            lang = lang.substring(0, 2);
        } else {
            lang = Locale.getDefault().getLanguage();
        }
        super.attachBaseContext(LanguageHelper.setLanguage(newBase, lang));

    }

    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            int uiMode = overrideConfiguration.uiMode;
            overrideConfiguration.setTo(getBaseContext().getResources().getConfiguration());
            overrideConfiguration.uiMode = uiMode;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }
}