package mint.woman.female.workout.model;

import android.os.Parcel;
import android.os.Parcelable;

public class TipObject implements Parcelable {
    public static final Creator<TipObject> CREATOR = new Creator<TipObject>() {
        @Override
        public TipObject createFromParcel(Parcel in) {
            return new TipObject(in);
        }

        @Override
        public TipObject[] newArray(int size) {
            return new TipObject[size];
        }
    };
    public int id;
    public String mTitle, mContent;

    public TipObject() {

    }

    protected TipObject(Parcel in) {
        id = in.readInt();
        mTitle = in.readString();
        mContent = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(mTitle);
        dest.writeString(mContent);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
