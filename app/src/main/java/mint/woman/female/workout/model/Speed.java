
package mint.woman.female.workout.model;



import com.google.gson.annotations.SerializedName;

public class Speed {

    @SerializedName("name")
    private String name;
    @SerializedName("speed")
    private int speed;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

}
