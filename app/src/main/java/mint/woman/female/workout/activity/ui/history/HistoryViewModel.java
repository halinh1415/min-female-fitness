package mint.woman.female.workout.activity.ui.history;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import mint.woman.female.workout.model.HistoryItem;
import mint.woman.female.workout.model.UserObject;
import mint.woman.female.workout.utils.HistoryRepository;
import mint.woman.female.workout.utils.UserRepository;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {
    private final HistoryRepository mHistoryRepository;
    private final UserRepository mUserRepository;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        mHistoryRepository = new HistoryRepository(application);
        mUserRepository = new UserRepository(application);
    }

    public LiveData<List<HistoryItem>> getAllHistory() {
        return mHistoryRepository.getAllHistory();
    }

    public void insertHistory(HistoryItem historyItem) {
        mHistoryRepository.insert(historyItem);
    }
    public LiveData<List<HistoryItem>> findFromTo(long from, long end) {
        return mHistoryRepository.findFromTo(from, end);
    }

    public LiveData<List<HistoryItem>> findInDay(long day){
        return mHistoryRepository.findInDay(day);
    }


    public void deleteAllHistory() {
        mHistoryRepository.deleteAllHistory();
    }

    public LiveData<List<UserObject>> getAllUsers() {
        return mUserRepository.getAllUsers();
    }

    public void insertUser(UserObject userObject) {
        mUserRepository.insert(userObject);
    }
}
