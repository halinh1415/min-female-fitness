package mint.woman.female.workout.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Recipe {
    @PrimaryKey
    @ColumnInfo(name = "date")
    public long date;
    @ColumnInfo(name = "foodId")
    public long foodId;
    @ColumnInfo(name = "serving")
    public float serving;

    @ColumnInfo(name = "servingUnit")
    public String servingUnit;

    public Recipe(long date, long foodId, float serving, String servingUnit) {
        this.date = date;
        this.foodId = foodId;
        this.serving = serving;
        this.servingUnit = servingUnit;
    }
}
