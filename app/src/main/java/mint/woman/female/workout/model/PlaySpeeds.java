
package mint.woman.female.workout.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlaySpeeds {

    @SerializedName("man")
    @Expose
    private List<Speed> man;
    @SerializedName("woman")
    @Expose
    private List<Speed> woman;

    public List<Speed> getMan() {
        return man;
    }

    public void setMan(List<Speed> man) {
        this.man = man;
    }

    public List<Speed> getWoman() {
        return woman;
    }

    public void setWoman(List<Speed> woman) {
        this.woman = woman;
    }

}
