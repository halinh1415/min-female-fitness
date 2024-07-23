package mint.woman.female.workout.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Customfields {

    @SerializedName("type_tag")
    @Expose
    private String typeTag;
    @SerializedName("muscle_major")
    @Expose
    private List<String> muscleMajor;
    @SerializedName("muscle_minor")
    @Expose
    private List<String> muscleMinor;
    @SerializedName("musclepart_major")
    @Expose
    private List<String> musclepartMajor;
    @SerializedName("musclepart_minor")
    @Expose
    private List<String> musclepartMinor;
    @SerializedName("work_part")
    @Expose
    private List<String> workPart;
    @SerializedName("muscle_tag")
    @Expose
    private List<String> muscleTag;
    @SerializedName("classic")
    @Expose
    private Integer classic;
    @SerializedName("move_popular")
    @Expose
    private Integer movePopular;
    @SerializedName("oneside")
    @Expose
    private Integer oneside;

    public String getTypeTag() {
        return typeTag;
    }

    public void setTypeTag(String typeTag) {
        this.typeTag = typeTag;
    }

    public List<String> getMuscleMajor() {
        return muscleMajor;
    }

    public void setMuscleMajor(List<String> muscleMajor) {
        this.muscleMajor = muscleMajor;
    }

    public List<String> getMuscleMinor() {
        return muscleMinor;
    }

    public void setMuscleMinor(List<String> muscleMinor) {
        this.muscleMinor = muscleMinor;
    }

    public List<String> getMusclepartMajor() {
        return musclepartMajor;
    }

    public void setMusclepartMajor(List<String> musclepartMajor) {
        this.musclepartMajor = musclepartMajor;
    }

    public List<String> getMusclepartMinor() {
        return musclepartMinor;
    }

    public void setMusclepartMinor(List<String> musclepartMinor) {
        this.musclepartMinor = musclepartMinor;
    }

    public List<String> getWorkPart() {
        return workPart;
    }

    public void setWorkPart(List<String> workPart) {
        this.workPart = workPart;
    }

    public List<String> getMuscleTag() {
        return muscleTag;
    }

    public void setMuscleTag(List<String> muscleTag) {
        this.muscleTag = muscleTag;
    }

    public Integer getClassic() {
        return classic;
    }

    public void setClassic(Integer classic) {
        this.classic = classic;
    }

    public Integer getMovePopular() {
        return movePopular;
    }

    public void setMovePopular(Integer movePopular) {
        this.movePopular = movePopular;
    }

    public Integer getOneside() {
        return oneside;
    }

    public void setOneside(Integer oneside) {
        this.oneside = oneside;
    }

}