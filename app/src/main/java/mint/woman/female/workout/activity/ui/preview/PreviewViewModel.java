package mint.woman.female.workout.activity.ui.preview;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import mint.woman.female.workout.utils.MyPreference;

public class PreviewViewModel extends AndroidViewModel {

    private final MutableLiveData<DownloadStatus> mDownloadStatus;


    public PreviewViewModel(@NonNull Application application) {
        super(application);
        mDownloadStatus = new MutableLiveData<>();
        mDownloadStatus.setValue(DownloadStatus.DONE);
    }

    public void setDownloadStatus(DownloadStatus status){
        mDownloadStatus.setValue(status);
    }
    public LiveData<DownloadStatus> getDownloadStatus(){
        return mDownloadStatus;
    }
}
