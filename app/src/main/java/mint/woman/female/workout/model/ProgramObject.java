package mint.woman.female.workout.model;



import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ProgramObject  {

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
    @SerializedName("subtitle")
    @Expose
    private String subtitle;
    @SerializedName("tag")
    @Expose
    private String tags;
    @SerializedName("version")
    @Expose
    private int version;
    @SerializedName("duration")
    @Expose
    private int duration;

    public int progress =0;

    public String getTags() {
        return tags;
    }

    public void setTags(String tag) {
        this.tags = tag;
    }

    public ProgramObject(){

    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}