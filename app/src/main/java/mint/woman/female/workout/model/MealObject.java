package mint.woman.female.workout.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class MealObject implements Parcelable {
    public static final Creator<MealObject> CREATOR = new Creator<MealObject>() {
        @Override
        public MealObject createFromParcel(Parcel in) {
            return new MealObject(in);
        }

        @Override
        public MealObject[] newArray(int size) {
            return new MealObject[size];
        }
    };
    public String name;
    public List<String> recipes;

    public MealObject() {
        recipes = new ArrayList<>();
        name = "";
    }

    protected MealObject(Parcel in) {
        name = in.readString();
        recipes = in.createStringArrayList();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeStringList(recipes);
    }
}
