package mint.woman.female.workout.utils;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import mint.woman.female.workout.model.UserObject;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM UserObject")
    LiveData<List<UserObject>> getAllUser();

    @Query("SELECT * FROM UserObject WHERE date = :date LIMIT 1")
    LiveData<UserObject> getUserByDay(int date);

    @Query("SELECT * FROM UserObject WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    List<UserObject> getUsersInRange(long startDate, long endDate);

    @Query("SELECT * FROM UserObject ORDER BY date DESC LIMIT 1")
    UserObject getLatest();

    @Query("SELECT * FROM UserObject WHERE date < :beforeDate ORDER BY date DESC LIMIT 1")
    UserObject getLatestBefore(long beforeDate);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserObject... userObjects);

    @Update
    void update(UserObject... userObjects);

    @Delete
    void delete(UserObject... userObjects);

    /**
     * Xóa tất cả UserObject
     */
    @Query("DELETE FROM UserObject")
    void deleteAllUsers();

    /**
     * Xóa UserObject theo ngày cụ thể
     */
    @Query("DELETE FROM UserObject WHERE date = :date")
    void deleteUserByDate(long date);

    /**
     * Xóa UserObject trong khoảng thời gian
     */
    @Query("DELETE FROM UserObject WHERE date >= :startDate AND date <= :endDate")
    void deleteUsersInRange(long startDate, long endDate);

    /**
     * Xóa UserObject cũ hơn ngày cụ thể
     */
    @Query("DELETE FROM UserObject WHERE date < :cutoffDate")
    void deleteUsersBefore(long cutoffDate);

}
