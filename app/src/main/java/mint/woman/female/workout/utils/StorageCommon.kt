package mint.woman.female.workout.utils

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.nativead.NativeAd
import mint.woman.female.workout.model.CategoryObject
import mint.woman.female.workout.model.ProgramObject

class StorageCommon {
    @JvmField
    var nativeAdLanguage: MutableLiveData<NativeAd> = MutableLiveData()
    @JvmField
    var nativeAdOnBoard: MutableLiveData<NativeAd> = MutableLiveData()
    @JvmField
    var nativeSetup: MutableLiveData<NativeAd> = MutableLiveData()
    @JvmField
    var category: MutableLiveData<CategoryObject> = MutableLiveData()
    var programObject: MutableLiveData<ProgramObject> = MutableLiveData()
}
