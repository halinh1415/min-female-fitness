package mint.woman.female.workout.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class DietObject implements Parcelable {
    public static final Creator<DietObject> CREATOR = new Creator<DietObject>() {
        @Override
        public DietObject createFromParcel(Parcel in) {
            return new DietObject(in);
        }

        @Override
        public DietObject[] newArray(int size) {
            return new DietObject[size];
        }
    };
    public String day;
    public List<MealObject> meals;

    public DietObject() {
        meals = new ArrayList<>();
        day = "";
    }

    protected DietObject(Parcel in) {
        day = in.readString();
        meals = in.createTypedArrayList(MealObject.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(day);
        dest.writeTypedList(meals);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
