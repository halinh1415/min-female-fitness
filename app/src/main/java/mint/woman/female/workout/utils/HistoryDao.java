package mint.woman.female.workout.utils;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import mint.woman.female.workout.model.HistoryDate;
import mint.woman.female.workout.model.HistoryDateWithItem;
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistory(HistoryItem... historyItems);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistory(HistoryItem history);

    @Delete
    void delete(HistoryItem... historyItems);

    @Update
    void update(HistoryItem... historyItems);

    @Transaction
    @Query("SELECT * FROM HistoryDate")
    LiveData<List<HistoryDateWithItem>> getAllHistoryByDate();

    @Transaction
    @Query("SELECT * FROM HistoryDate WHERE date BETWEEN :from AND :end")
    LiveData<List<HistoryDateWithItem>> findHistoryDateItemFromTo(long from, long end);

    @Transaction
    @Query("SELECT * FROM HistoryDate WHERE date =:day")
    LiveData<HistoryDateWithItem> findHistoryDateItemOnDay(long day);

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertHistoryDate(HistoryDate date);

    @Query("DELETE FROM HistoryItem")
    void deleteAllHistoryItem();

    @Query("DELETE FROM HistoryDate")
    void deleteAllHistoryDate();


}
