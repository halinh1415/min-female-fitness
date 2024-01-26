package mint.woman.female.workout.activity.ui.workout;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import mint.woman.female.workout.model.HistoryDate;
import mint.woman.female.workout.model.HistoryItem;
import mint.woman.female.workout.utils.HistoryRepository;
import mint.woman.female.workout.utils.MyPreference;

import java.util.List;

public class WorkoutViewModel extends AndroidViewModel {
    private HistoryRepository mRepository;
    private final MutableLiveData<String> mMusicSong;
    private final MutableLiveData<String> mTtsVoice;
    private final MutableLiveData<Float> mMusicVolume;
    private final MutableLiveData<Boolean> mMusicOn;
    private final MutableLiveData<Boolean> mPause;
    private int discover =0;
    private MyPreference myPreference;

    public WorkoutViewModel(@NonNull Application application) {
        super(application);
        this.mRepository = new HistoryRepository(application);
        myPreference = MyPreference.newInstance(application);
        mMusicSong = new MutableLiveData<>();
        mMusicSong.setValue(myPreference.getMusicName());
        mMusicVolume = new MutableLiveData<>();
        mMusicVolume.setValue(myPreference.getMusicVolume());

        mMusicOn = new MutableLiveData<>();
        mMusicOn.setValue(myPreference.isMusicOn());
        mPause = new MutableLiveData<>();
        mPause.setValue(false);
        mTtsVoice = new MutableLiveData<>();
        mTtsVoice.setValue(myPreference.getTTScode());

    }

    public int getDiscover() {
        return discover;
    }

    public void setDiscover(int discover) {
        this.discover = discover;
    }

    public void insertHistory(HistoryItem historyItem) {
        mRepository.insert(historyItem);
    }

    public void insertDate(long date) {
        mRepository.insertHistoryDate(new HistoryDate(date));
    }

    public LiveData<List<HistoryItem>> getAllHistory() {
        return mRepository.getAllHistory();
    }

    public LiveData<Boolean> isPause() {
        return mPause;
    }

    public void setPause(Boolean value) {
        mPause.setValue(value);
    }

    public void setMusicOn(Boolean value) {
        mMusicOn.setValue(value);
    }

    public LiveData<Boolean> isMusicOn() {
        return mMusicOn;
    }

    public LiveData<String> getMusicSong() {
        return mMusicSong;
    }

    public void setMusicSong(String musicSong) {
        mMusicSong.setValue(musicSong);
    }

    public void setMusicVolume(Float volume) {
        mMusicVolume.setValue(volume);
    }

    public LiveData<Float> getMusicVolume() {
        return mMusicVolume;
    }


    public LiveData<String> getTtsVoice() {
        return mTtsVoice;
    }

    public void setLanguage(String language) {
        mTtsVoice.setValue(language);
    }


}

