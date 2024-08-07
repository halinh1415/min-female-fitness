package mint.woman.female.workout.activity.ui.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import mint.woman.female.workout.model.HistoryItem;
import mint.woman.female.workout.utils.HistoryRepository;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final HistoryRepository mHistoryRepository;

    public HomeViewModel(Application application) {
        super(application);
        mHistoryRepository = new HistoryRepository(application);
    }

    public LiveData<List<HistoryItem>> findHistoryFromTo(long from, long end) {
        return mHistoryRepository.findFromTo(from, end);
    }
}