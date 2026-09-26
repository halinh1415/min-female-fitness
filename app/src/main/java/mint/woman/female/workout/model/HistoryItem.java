package mint.woman.female.workout.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Calendar;

@Entity
public class HistoryItem implements Parcelable {

    public static final Creator<HistoryItem> CREATOR = new Creator<HistoryItem>() {
        @Override
        public HistoryItem createFromParcel(Parcel in) {
            return new HistoryItem(in);
        }

        @Override
        public HistoryItem[] newArray(int size) {
            return new HistoryItem[size];
        }
    };
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;
    @ColumnInfo(name = "start_time")
    private long startTime;
    @ColumnInfo(name = "end_time")
    private long endTime;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "calories")
    private int calories;
    @ColumnInfo(name = "duration")
    private int duration;

    // --- ĐÃ CẬP NHẬT: Tên biến rõ ràng hơn nhưng vẫn giữ tên cột DB là "dateId" ---
    @ColumnInfo(name = "dateId")
    private long dateId;
    @ColumnInfo(name = "start")
    private String start;
    @ColumnInfo(name = "passed")
    private boolean passed;
    @ColumnInfo(name = "program_id")
    private int programId;
    @ColumnInfo(name = "day_index")
    private int dayIndex;
    @ColumnInfo(name =  "image")
    private String image;


    public HistoryItem(){
        duration = 0;
        calories = 0;
    }
    protected HistoryItem(Parcel in) {
        id = in.readInt();
        startTime = in.readLong();
        endTime = in.readLong();
        name = in.readString();
        calories = in.readInt();
        duration = in.readInt();
        // --- ĐÃ CẬP NHẬT: Đọc giá trị dateStartOfDayMillis từ Parcel ---
        dateId = in.readLong(); // Đọc giá trị cho biến mới
        start = in.readString();
        passed = in.readByte() != 0;
        programId = in.readInt();
        dayIndex = in.readInt();
        image = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeLong(startTime);
        dest.writeLong(endTime);
        dest.writeString(name);
        dest.writeInt(calories);
        dest.writeInt(duration);
        dest.writeLong(dateId);
        dest.writeString(start);
        dest.writeByte((byte) (passed ? 1 : 0));
        dest.writeInt(programId);
        dest.writeInt(dayIndex);
        dest.writeString(image);
    }

    @Override
    public int describeContents() {
        return 0;
    };

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCalories() {
        if (calories < 0) return 0;
        if (calories > 5000) return 5000;
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public int getDuration() {
        if (duration < 0) return 0;
        if (duration > 10000) return 10000;
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    // --- ĐÃ CẬP NHẬT: Getter cho cột "dateId" phải là getDateId() ---
    public long getDateId() { // Tên getter mới cho cột "dateId"
        return dateId;
    }

    /**
     * Set the date for this history item. It stores the milliseconds of 00:00:00
     * of the local calendar day to allow for easy and accurate queries by day.
     * @param millis A timestamp (in milliseconds) from which to derive the local calendar day.
     */
    // --- ĐÃ CẬP NHẬT: Setter cho cột "dateId" phải là setDateId() ---
    public void setDateId(long millis) { // Tên setter mới cho cột "dateId"
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        // Normalize to the beginning of the day in the local timezone
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        this.dateId = calendar.getTimeInMillis();
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public int getProgramId() {
        return programId;
    }

    public void setProgramId(int programId) {
        this.programId = programId;
    }

    public int getDayIndex() {
        return dayIndex;
    }

    public void setDayIndex(int dayIndex) {
        this.dayIndex = dayIndex;
    }

    public void addDurationTime(int time) {
        if (time < 0) return;
        if (time > 3000) {
            time = 3000;
        }
        this.duration += time;
    }

    public int getRealDuration() {
        return (int) ((endTime - startTime) / 1000);
    }
}