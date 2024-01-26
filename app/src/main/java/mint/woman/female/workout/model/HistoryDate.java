package mint.woman.female.workout.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


@Entity
public class HistoryDate {
    @PrimaryKey
    @ColumnInfo(name = "date")
    private long date;

    public HistoryDate(long date) {
        this.date = date;
    }

    @Ignore
    public HistoryDate() {
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
