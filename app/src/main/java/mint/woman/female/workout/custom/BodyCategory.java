package mint.woman.female.workout.custom;

class BodyCategory {
    protected int bodyCategory;
    protected int color;
    protected String text;
    private float valueMale, valueFemale;

    BodyCategory(int bodyCategory, int color, String text, float valueMale, float valueFemale) {
        this.bodyCategory = bodyCategory;
        this.color = color;
        this.text = text;
        this.valueMale = valueMale;
        this.valueFemale = valueFemale;
    }

    protected float getLimit(int gender) {
        if (gender == 0) {
            return valueMale;
        } else if (gender == 1) {
            return valueFemale;
        }
        return 0;
    }
}
