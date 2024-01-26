package mint.woman.female.workout.utils;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.ads.nativead.NativeAd;

public class StorageCommon {
    public MutableLiveData<NativeAd> nativeAdsDone = new MutableLiveData<>();
    public MutableLiveData<NativeAd> nativeAdLanguage = new MutableLiveData<>();
    public MutableLiveData<NativeAd> nativeAdOnBoard = new MutableLiveData<>();
    public MutableLiveData<NativeAd> nativeSetup = new MutableLiveData<>();
}
