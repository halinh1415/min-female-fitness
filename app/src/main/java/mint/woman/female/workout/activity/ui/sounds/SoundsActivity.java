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
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.io.File;
import java.util.List;
import java.util.Locale;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.premium.PremiumActivity;
import mint.woman.female.workout.activity.ui.preview.DownloadStatus;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.custom.LoadingDialogFragment;
import mint.woman.female.workout.databinding.ActivitySoundsBinding;
import mint.woman.female.workout.retrofit.ApiUtils;
import mint.woman.female.workout.retrofit.MusicItem;
import mint.woman.female.workout.retrofit.SOService;
import mint.woman.female.workout.utils.DownloadAndUnzipWorker;
import mint.woman.female.workout.utils.DownloadFileWorker;
import mint.woman.female.workout.utils.DownloadVideoWorker;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SoundsActivity extends AppCompatActivity implements MusicClickListener{
    private static final String TAG = "SoundsActivity";
    private ActivitySoundsBinding binding;
    SOService mService;
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
        mService = ApiUtils.getSOService(SoundsActivity.this);

        showDialog();
        mService.getMusic().enqueue(new Callback<List<MusicItem>>() {
            @Override
            public void onResponse(Call<List<MusicItem>> call, Response<List<MusicItem>> response) {
                if (response.isSuccessful()){
                    Log.d("HAHA","music list: "+response.body().toString());
                    hideDialog();
                    musicAdapter.setMusicItemList(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<MusicItem>> call, Throwable throwable) {

            }
        });
       playMusic(myPreference.getMusicFile());

    }
    LoadingDialogFragment loadingDialog;

    void showDialog() {
        try {
            if (loadingDialog == null) {
                loadingDialog = new LoadingDialogFragment();
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            loadingDialog.show(fragmentManager, "loading_dialog");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void hideDialog() {
        try {
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }
        } catch (Exception e) {
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
        if (!lang.isEmpty() && lang.length() > 2) {
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