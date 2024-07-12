package mint.woman.female.workout.utils;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.ads.nativead.NativeAd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mint.woman.female.workout.model.CategoryObject;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.model.ProgramObject;

public class StorageCommon {
    public MutableLiveData<NativeAd> nativeAdLanguage = new MutableLiveData<>();
    public MutableLiveData<NativeAd> nativeAdOnBoard = new MutableLiveData<>();
    public MutableLiveData<NativeAd> nativeSetup = new MutableLiveData<>();
    public MutableLiveData<CategoryObject> category = new MutableLiveData<>();
    public MutableLiveData<ProgramObject> programObject = new MutableLiveData<>();

}
