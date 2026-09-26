package mint.woman.female.workout.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Rule {

    private int type;
    private int value;

    public Rule(int type, int value) {
        this.type = type;
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}