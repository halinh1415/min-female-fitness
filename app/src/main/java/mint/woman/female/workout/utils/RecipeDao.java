package mint.woman.female.workout.utils;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import mint.woman.female.workout.model.Recipe;

import java.util.List;

@Dao
public interface RecipeDao {

    @Query("select * from Recipe where date =:date")
    LiveData<List<Recipe>> getRecipe(long date);

    @Insert
    void insertRecipe(Recipe...recipes);

    @Delete
    void deleteRecipe(Recipe...recipes);

}
