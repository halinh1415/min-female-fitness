package mint.woman.female.workout.activity.ui.settings;

public class VoiceItem {
    private int iconRes; // Resource ID của icon (R.drawable.ic_voice)
    private String name;
    private String value;

    public VoiceItem(int iconRes, String name,String value) {
        this.iconRes = iconRes;
        this.name = name;
        this.value = value;
    }

    public int getIconRes() {
        return iconRes;
    }

    public String getName() {
        return name;
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}