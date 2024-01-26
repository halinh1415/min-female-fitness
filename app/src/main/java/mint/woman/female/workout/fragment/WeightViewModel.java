package mint.woman.female.workout.fragment;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import mint.woman.female.workout.model.UserObject;
import mint.woman.female.workout.utils.MyPreference;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class WeightViewModel extends AndroidViewModel {
    private final MutableLiveData<UserObject> mUser;
    private final MutableLiveData<Boolean> isKg;
    private MyPreference myPreference;

    public WeightViewModel(@NonNull Application application) {
        super(application);
        myPreference = new MyPreference(application);
        mUser = new MutableLiveData<>();

        Long nowDay  = TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTimeInMillis());
        mUser.setValue(new UserObject(nowDay,myPreference.getCurrentWeightKG(),myPreference.getCurrentHeightCm(),myPreference.getCurrentWaistCm()));
        isKg = new MutableLiveData<>();
    }

    public LiveData<UserObject> getUser() {
        return mUser;
    }

    public void setUser(UserObject userObject) {
        this.mUser.setValue(userObject);
    }

    public LiveData<Boolean> getIsKg() {
        return isKg;
    }

    public void setIsKg(boolean isKg) {
        this.isKg.setValue(isKg);
    }
}
