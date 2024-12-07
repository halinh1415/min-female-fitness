package mint.woman.female.workout.activity.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import mint.woman.female.workout.utils.MyPreference

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val mSyncOk = MutableLiveData<Boolean>()

    init {
        mSyncOk.value = MyPreference.newInstance(application).isSyncGoogleFit
    }
}
