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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserObject... userObjects);

    @Update
    void update(UserObject... userObjects);

    @Delete
    void delete(UserObject... userObjects);

}
