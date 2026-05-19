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
    @Query("SELECT * FROM HistoryItem ORDER BY start_time DESC")
    List<HistoryItem> getAll();

    @Query("SELECT * FROM HistoryItem WHERE dateId = :dateId AND passed = 1 ORDER BY start_time ASC")
    List<HistoryItem> getPassedItemsByDate(long dateId);

    @Query("SELECT COALESCE(SUM(duration), 0) FROM HistoryItem WHERE dateId = :dateId AND passed = 1")
    int getTotalDurationByDate(long dateId);

    @Query("SELECT COUNT(*) FROM HistoryItem WHERE dateId = :dateId AND passed = 1")
    int getSessionCountByDate(long dateId);

    @Query("SELECT DISTINCT dateId FROM HistoryItem WHERE passed = 1 ORDER BY dateId ASC")
    List<Long> getAllPassedDateIds();

    @Query("SELECT DISTINCT dateId FROM HistoryItem WHERE passed = 1 AND dateId >= :fromDateId ORDER BY dateId ASC")
    List<Long> getPassedDateIdsFrom(long fromDateId);

    @Query("SELECT * FROM HistoryItem WHERE start_time >= :fromTime AND passed = 1 ORDER BY start_time DESC")
    List<HistoryItem> getPassedItemsFrom(long fromTime);
    @Query("SELECT * FROM HistoryItem WHERE passed = 1 ORDER BY start_time DESC LIMIT :limit")
    List<HistoryItem> getRecentPassedHistory(int limit);

    @Query("SELECT * FROM HistoryItem WHERE dateId = :dateId ORDER BY start_time ASC")
    List<HistoryItem> getHistoryByDateId(long dateId);

    @Query("SELECT DISTINCT dateId FROM HistoryItem WHERE dateId >= :startDateId AND dateId <= :endDateId ORDER BY dateId ASC")
    List<Long> getCompletedDateIdsInRange(long startDateId, long endDateId);

    @Query("SELECT * FROM HistoryItem WHERE dateId >= :startDateId AND dateId <= :endDateId ORDER BY start_time ASC")
    List<HistoryItem> getHistoryInRange(long startDateId, long endDateId);

    @Query("SELECT DISTINCT program_id FROM HistoryItem WHERE passed = 1 ORDER BY start_time DESC LIMIT 5")
    List<Integer> getRecentProgramIds();

    /**
     * ✅ Lấy tất cả history của một programId
     * @param programId ID của chương trình
     * @return List lịch sử tập của programId đó
     */
    @Query("SELECT * FROM HistoryItem WHERE program_id = :programId ORDER BY start_time ASC")
    List<HistoryItem> getHistoryByProgramId(int programId);

}
