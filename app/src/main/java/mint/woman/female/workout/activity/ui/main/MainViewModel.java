package mint.woman.female.workout.activity.ui.main;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import mint.woman.female.workout.utils.MyPreference;

public class MainViewModel extends AndroidViewModel {
    private final MutableLiveData<Boolean> mSyncOk;

    public MainViewModel(Application application) {
        super(application);
        mSyncOk = new MutableLiveData<>();
        mSyncOk.setValue(MyPreference.newInstance(application).isSyncGoogleFit());
    }
}
