package mint.woman.female.workout.activity.ui.firstsetup;

import androidx.lifecycle.ViewModel;

public class SetupViewModel extends ViewModel {
    private float weight = 68.0f;
    private float height = 170;
    private boolean isKg = true;
    private int hour = 20;
    private int minute = 0;
    public SetupViewModel(){

    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public float getWeight() {
        return weight;
    }
    public float getWeightCm(){
        return isKg? weight: weight/2.20462262f;
    }
    public float getHeightCm(){
        return isKg? height: height *30.48f;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public boolean isKg() {
        return isKg;
    }

    public void setKg(boolean kg) {
        isKg = kg;
    }
}
