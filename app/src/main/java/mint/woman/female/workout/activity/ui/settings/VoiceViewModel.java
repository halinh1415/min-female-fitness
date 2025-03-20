package mint.woman.female.workout.activity.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class VoiceViewModel extends ViewModel {
    private final MutableLiveData<String> selectModel = new MutableLiveData<>("default");

    public LiveData<String> getSelectModel() {
        return selectModel;
    }

    public void setSelectModel(String newModel) {
        selectModel.setValue(newModel);
    }
}