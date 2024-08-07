package mint.woman.female.workout.utils;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import mint.woman.female.workout.model.HistoryItem;

import java.util.List;

@Dao
public interface HistoryDao {
    @Query("SELECT * FROM HistoryItem")
    LiveData<List<HistoryItem>> getAllHistoryItem();


    @Query("SELECT * FROM HistoryItem WHERE id =:id")
    LiveData<HistoryItem> getHistoryById(int id);

    @Query("SELECT * FROM HistoryItem WHERE dateId BETWEEN :from AND :to")
    LiveData<List<HistoryItem>> findHistoryFromTo(long from, long to);
    @Query("SELECT * FROM HistoryItem WHERE dateId = :date")
    LiveData<List<HistoryItem>> findHistoryInDate(long date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistory(HistoryItem... historyItems);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistory(HistoryItem history);

    @Delete
    void delete(HistoryItem... historyItems);

    @Update
    void update(HistoryItem... historyItems);

    @Query("DELETE FROM HistoryItem")
    void deleteAllHistoryItem();


}
