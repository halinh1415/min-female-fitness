
package mint.woman.female.workout.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ExerciseObject {

    @SerializedName("unit")
    private String unit;
    @SerializedName("levelid")
    private String levelid;
    @SerializedName("videoUrl")
    private String videoUrl;
    @SerializedName("introduce")
    private String introduce;
//    @SerializedName("imagePath")
    private String imagePath;
    @SerializedName("name")
    private String name;
    @SerializedName("alternation")
    private Boolean alternation;
    @SerializedName("id")
    private Integer id;
    @SerializedName("speed")
    private Integer speed;
    @SerializedName("wmspeed")
    private Integer wmspeed;
    @SerializedName("categorynums")
    private String categorynums;
    @SerializedName("classid")
    private Integer classid;
    @SerializedName("muscle")
    private String muscle;
    @SerializedName("womenset")
    private Boolean womenset;
    @SerializedName("jumped")
    private Boolean jumped;
    @SerializedName("kaluli")
    private String kaluli;
    @SerializedName("etype")
    private Integer etype;
    @SerializedName("strength")
    private Integer strength;
    @SerializedName("qicai")
    private List<String> qicai;
  //  @SerializedName("customfields")
//  @SerializedName("xxx")
    private String customfields;
    @SerializedName("tips")
    private List<Tip> tips;
    @SerializedName("groupactions")
    @Expose
    private List<Integer> groupactions;
    @SerializedName("playspeeds")
    private PlaySpeeds playspeeds;
//    @SerializedName("imgs")
    private List<String> imgs;
//    @SerializedName("wmimgs")
    private List<String> wmimgs;
//    @SerializedName("mankaluli")
    private float mankaluli;
//    @SerializedName("womankaluli")
    private float womankaluli;



    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getLevelid() {
        return levelid;
    }

    public void setLevelid(String levelid) {
        this.levelid = levelid;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getAlternation() {
        return alternation;
    }

    public void setAlternation(Boolean alternation) {
        this.alternation = alternation;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public Integer getWmspeed() {
        return wmspeed;
    }

    public void setWmspeed(Integer wmspeed) {
        this.wmspeed = wmspeed;
    }

    public String getCategorynums() {
        return categorynums;
    }

    public void setCategorynums(String categorynums) {
        this.categorynums = categorynums;
    }

    public Integer getClassid() {
        return classid;
    }

    public void setClassid(Integer classid) {
        this.classid = classid;
    }

    public String getMuscle() {
        return muscle;
    }

    public void setMuscle(String muscle) {
        this.muscle = muscle;
    }

    public Boolean getWomenset() {
        return womenset;
    }

    public void setWomenset(Boolean womenset) {
        this.womenset = womenset;
    }

    public Boolean getJumped() {
        return jumped;
    }

    public void setJumped(Boolean jumped) {
        this.jumped = jumped;
    }

    public String getKaluli() {
        return kaluli;
    }

    public void setKaluli(String kaluli) {
        this.kaluli = kaluli;
    }

    public Integer getEtype() {
        return etype;
    }

    public void setEtype(Integer etype) {
        this.etype = etype;
    }

    public Integer getStrength() {
        return strength;
    }

    public void setStrength(Integer strength) {
        this.strength = strength;
    }

    public List<String> getQicai() {
        return qicai;
    }

    public void setQicai(List<String> qicai) {
        this.qicai = qicai;
    }

    public String getCustomfields() {
        return customfields;
    }

    public void setCustomfields(String customfields) {
        this.customfields = customfields;
    }

    public List<Tip> getTips() {
        return tips;
    }

    public void setTips(List<Tip> tips) {
        this.tips = tips;
    }

    public PlaySpeeds getPlayspeeds() {
        return playspeeds;
    }

    public void setPlayspeeds(PlaySpeeds playspeeds) {
        this.playspeeds = playspeeds;
    }

    public List<String> getImgs() {
        return imgs;
    }

    public void setImgs(List<String> imgs) {
        this.imgs = imgs;
    }

    public List<String> getWmimgs() {
        return wmimgs;
    }

    public void setWmimgs(List<String> wmimgs) {
        this.wmimgs = wmimgs;
    }

    public float getMankaluli() {
        return mankaluli;
    }

    public void setMankaluli(float mankaluli) {
        this.mankaluli = mankaluli;
    }

    public float getWomankaluli() {
        return womankaluli;
    }

    public void setWomankaluli(float womankaluli) {
        this.womankaluli = womankaluli;
    }

    public List<Integer> getGroupactions() {
        return groupactions;
    }

    public void setGroupactions(List<Integer> groupactions) {
        this.groupactions = groupactions;
    }
//    public String toTitleCase() {
//        final String ACTIONABLE_DELIMITERS = " '-/"; // these cause the character following
//        // to be capitalized
//
//        StringBuilder sb = new StringBuilder();
//        boolean capNext = true;
//
//        for (char c : name.toCharArray()) {
//            c = (capNext)
//                    ? Character.toUpperCase(c)
//                    : Character.toLowerCase(c);
//            sb.append(c);
//            capNext = (ACTIONABLE_DELIMITERS.indexOf((int) c) >= 0); // explicit cast not needed
//        }
//        Log.d("HAHA",sb.toString());
//        return sb.toString();
//    }
}
