package mint.woman.female.workout.model;

public class NutritionObject {
    String name,unit,source,color1,color2,color3;
    float range1,range2,max,progress;

    public NutritionObject(String name, String unit, String source, String color1, String color2, String color3, float range1, float range2, float max) {
        this.name = name;
        this.unit = unit;
        this.source = source;
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
        this.range1 = range1;
        this.range2 = range2;
        this.max = max;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public String getSource() {
        return source;
    }

    public String getColor1() {
        return color1;
    }

    public String getColor2() {
        return color2;
    }

    public String getColor3() {
        return color3;
    }

    public float getRange1() {
        return range1;
    }

    public float getRange2() {
        return range2;
    }

    public float getMax() {
        return max;
    }

    public float getProgress() {
        return progress;
    }
}
