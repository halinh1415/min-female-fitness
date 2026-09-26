package mint.woman.female.workout.activity.ui.sounds;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.premium.PremiumActivity;
import mint.woman.female.workout.databinding.ActivitySoundsBinding;
import mint.woman.female.workout.retrofit.MusicItem;

import mint.woman.female.workout.utils.DownloadFileWorker;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.RemoteConfigKeys;

public class SoundsActivity extends AppCompatActivity implements MusicClickListener{
    private static final String TAG = "SoundsActivity";
    private ActivitySoundsBinding binding;
    MusicAdapter musicAdapter;
    MediaPlayer musicPlayer;
    MyPreference myPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySoundsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        File file = new File(getFilesDir(), "music");
        if (!file.exists()) {
            file.mkdirs();
        }
        initView();

    }

    private void initView() {
        myPreference = new MyPreference(this);
        musicAdapter = new MusicAdapter(this,myPreference.getMusicName(),!myPreference.isPremiumMember());
        binding.rcMusic.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));


        binding.rcMusic.setAdapter(musicAdapter);

        binding.ltLoading.setVisibility(View.GONE);
        binding.ltLoading.setAnimation("loading.json");
        binding.ltLoading.playAnimation();
        setTitle(getString(R.string.txt_select_audio));
        loadMusic();
        playMusic(myPreference.getMusicFile());

    }

    private void loadMusic() {
        try {
            Gson gson = new Gson();
            List<MusicItem> musicItemList = gson.fromJson(FirebaseRemoteConfig.getInstance().getString(RemoteConfigKeys.BACKGROUND_MUSIC), new TypeToken<List<MusicItem>>() {
            }.getType());
            if (musicItemList != null) {
                Collections.sort(musicItemList, new Comparator<MusicItem>() {
                    @Override
                    public int compare(MusicItem o1, MusicItem o2) {
                        return Integer.compare(o1.getPremium(), o2.getPremium());
                    }
                });
                musicAdapter.setMusicItemList(musicItemList);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    @Override
    public void onMusicClick(MusicItem musicItem,int position) {
        if ( musicItem.getPremium() ==1 && !myPreference.isPremiumMember()){
            Toast.makeText(this, "Need join Premium member to download this song!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SoundsActivity.this, PremiumActivity.class));
            return;
        }
        String musicPath = getFilesDir()+"/music/"+musicItem.getFile();
        if (!existsData(musicPath)){
            musicAdapter.setSelectPosition(position);
            musicAdapter.setDownloadStatus(0);
            if (musicPlayer != null) {
                musicPlayer.release();
            }
            startDownLoadMusic(musicItem);
        }else{
            musicAdapter.setDownloadStatus(1);
            musicAdapter.setSelectPosition(position);
            playMusic(musicItem.getFile());
        }

        myPreference.setMusicName(musicItem.getName());
        myPreference.setMusicFile(musicItem.getFile());
    }

    @Override
    protected void onDestroy() {
        if (musicPlayer != null) {
            musicPlayer.release();
        }
        super.onDestroy();

    }

    void startDownLoadMusic(MusicItem musicItem){
        String musicPath = getFilesDir()+"/music/"+musicItem.getFile();
        Data.Builder data = new Data.Builder();
        data.putString("URL", "https://min-fitness.uk/min-fitness/female_workout/music/"+musicItem.getFile());
        data.putString("FILE_PATH", musicPath);

        OneTimeWorkRequest syncWorkRequest =
                new OneTimeWorkRequest.Builder(DownloadFileWorker.class)
                        .addTag("Sync")
                        .setInputData(data.build())
                        .build();

        WorkManager.getInstance(SoundsActivity.this).enqueue(syncWorkRequest);
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(syncWorkRequest.getId()).observe(this, workInfo -> {

            if (workInfo.getState() == WorkInfo.State.SUCCEEDED){
                Log.d("HAHA","download Done!");
                playMusic(musicItem.getFile());
                musicAdapter.setDownloadStatus(1);
            }

        });
    }
    void playMusic(String file){

        String musicPath =  getFilesDir()+"/music/"+file;

        if (existsData(musicPath)) {
            if (musicPlayer != null) {
                musicPlayer.release();
            }
            musicPlayer = MediaPlayer.create(SoundsActivity.this, Uri.parse(musicPath));
            if (musicPlayer == null){
                return;
            }
            musicPlayer.setLooping(true);
            musicPlayer.start();
        }else {
            Toast.makeText(this, "File not found!", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean existsData(String fileName) {
        File file = new File(fileName);
        return file.exists();
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