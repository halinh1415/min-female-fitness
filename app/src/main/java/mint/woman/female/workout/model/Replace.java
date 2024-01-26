package mint.woman.female.workout.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Replace implements Parcelable {

    @SerializedName("id")
    private int id;
    @SerializedName("maxValue")
    private int maxValue;
    @SerializedName("minValue")
    private int minValue;
    private int time;

    public Replace(int id, int maxValue, int minValue) {
        this.id = id;
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.time = minValue;
    }

    protected Replace(Parcel in) {
        id = in.readInt();
        maxValue = in.readInt();
        minValue = in.readInt();
        time = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(maxValue);
        dest.writeInt(minValue);
        dest.writeInt(time);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Replace> CREATOR = new Creator<Replace>() {
        @Override
        public Replace createFromParcel(Parcel in) {
            return new Replace(in);
        }

        @Override
        public Replace[] newArray(int size) {
            return new Replace[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}