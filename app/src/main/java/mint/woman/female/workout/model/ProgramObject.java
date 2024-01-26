package mint.woman.female.workout.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ProgramObject implements Parcelable {

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("type")
    @Expose
    private int type;
    @SerializedName("plan")
    @Expose
    private String plan;
    @SerializedName("level")
    @Expose
    private int level;
    @SerializedName("total")
    @Expose
    private int total;
    @SerializedName("premium")
    @Expose
    private int premium;
    @SerializedName("body")
    @Expose
    private String body;
    @SerializedName("focus")
    @Expose
    private String focus;
    @SerializedName("background")
    @Expose
    private String background;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("discover")
    @Expose
    private int discover;
    @SerializedName("description")
    @Expose
    private String description;
    public ProgramObject(){

    }

    protected ProgramObject(Parcel in) {
        id = in.readInt();
        name = in.readString();
        type = in.readInt();
        plan = in.readString();
        level = in.readInt();
        total = in.readInt();
        premium = in.readInt();
        body = in.readString();
        focus = in.readString();
        background = in.readString();
        image = in.readString();
        discover = in.readInt();
        description = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(type);
        dest.writeString(plan);
        dest.writeInt(level);
        dest.writeInt(total);
        dest.writeInt(premium);
        dest.writeString(body);
        dest.writeString(focus);
        dest.writeString(background);
        dest.writeString(image);
        dest.writeInt(discover);
        dest.writeString(description);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProgramObject> CREATOR = new Creator<ProgramObject>() {
        @Override
        public ProgramObject createFromParcel(Parcel in) {
            return new ProgramObject(in);
        }

        @Override
        public ProgramObject[] newArray(int size) {
            return new ProgramObject[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPremium() {
        return premium;
    }

    public void setPremium(int premium) {
        this.premium = premium;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getDiscover() {
        return discover;
    }

    public void setDiscover(int discover) {
        this.discover = discover;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}