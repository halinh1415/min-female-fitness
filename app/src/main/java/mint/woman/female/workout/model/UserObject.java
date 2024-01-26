package mint.woman.female.workout.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity
public class UserObject {


    @PrimaryKey
    @ColumnInfo(name = "date")
    private long date;
    @ColumnInfo(name = "weight")
    private float weight;
    @ColumnInfo(name = "height")
    private float height;
    @ColumnInfo(name = "waist")
    private float waist;
    public UserObject() {
        this.weight = 65.0f;
        this.height = 175.0f;
        this.waist = 75.0f;
    }

    public UserObject(long date, float weight, float height,float waist) {
        this.date = date;
        this.weight = weight;
        this.height = height;
        this.waist = waist;
    }

    public float getLbs() {
        return weight * 2.20462f;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getHeight() {
        return height;
    }


    public void setHeight(float height) {
        this.height = height;
    }

    public float getWaist() {
        return waist;
    }

    public void setWaist(float waist) {
        this.waist = waist;
    }
}
