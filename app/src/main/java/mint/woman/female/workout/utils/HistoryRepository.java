package mint.woman.female.workout.utils;

import android.app.Application;

import androidx.lifecycle.LiveData;

import mint.woman.female.workout.model.HistoryDate;
import mint.woman.female.workout.model.HistoryDateWithItem;
import mint.woman.female.workout.model.HistoryItem;

import java.util.List;

public class HistoryRepository {

    private HistoryDao mHistoryDao;


    public HistoryRepository(Application application) {
        HistoryDatabase db = HistoryDatabase.getDatabase(application);
        mHistoryDao = db.historyDao();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<HistoryItem>> getAllHistory() {
        return mHistoryDao.getAllHistoryItem();
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insert(HistoryItem historyItem) {
        HistoryDatabase.databaseWriteExecutor.execute(() -> {
            mHistoryDao.insertHistory(historyItem);
        });

    }

    public LiveData<HistoryItem> getHistoryById(int id) {
        return mHistoryDao.getHistoryById(id);
    }

    public void update(HistoryItem historyItem) {
        HistoryDatabase.databaseWriteExecutor.execute(() -> {
            mHistoryDao.update(historyItem);
        });
    }

    public LiveData<List<HistoryItem>> findFromTo(long from, long to) {
        return mHistoryDao.findHistoryFromTo(from, to);
    }

    public void insertHistoryDate(HistoryDate date) {
        HistoryDatabase.databaseWriteExecutor.execute(() -> {
            mHistoryDao.insertHistoryDate(date);
        });
    }

    public LiveData<List<HistoryDateWithItem>> getAllHistoryByDate() {
        return mHistoryDao.getAllHistoryByDate();
    }

    public LiveData<List<HistoryDateWithItem>> findHistoryDateItemFromTo(long from, long end) {
        return mHistoryDao.findHistoryDateItemFromTo(from, end);
    }

    public LiveData<HistoryDateWithItem> findHistoryDateItemOnDay(long day) {
        return mHistoryDao.findHistoryDateItemOnDay(day);
    }

    public void deleteAllHistory() {
        HistoryDatabase.databaseWriteExecutor.execute(() -> {
            mHistoryDao.deleteAllHistoryItem();
            mHistoryDao.deleteAllHistoryDate();
        });

    }

}
