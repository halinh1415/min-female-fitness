package mint.woman.female.workout.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ProductObject implements Parcelable {
    public static final Creator<ProductObject> CREATOR = new Creator<ProductObject>() {
        @Override
        public ProductObject createFromParcel(Parcel in) {
            return new ProductObject(in);
        }

        @Override
        public ProductObject[] newArray(int size) {
            return new ProductObject[size];
        }
    };
    public int id;
    public List<String> products;

    public ProductObject() {
        products = new ArrayList<>();
        id = 0;
    }

    public ProductObject(ProductObject productObject) {
        this.id = productObject.id;
        products = new ArrayList<>();
        products.addAll(productObject.products);
    }

    protected ProductObject(Parcel in) {
        id = in.readInt();
        products = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeStringList(products);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
