package mint.woman.female.workout.activity.ui.language;

public class Language {
    private String code;
    private String name;
    private int idIcon;
    boolean isChoose;


    public Language(String code, String name, int idIcon, boolean isChoose) {
        this.code = code;
        this.name = name;
        this.idIcon = idIcon;
        this.isChoose = isChoose;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIdIcon() {
        return idIcon;
    }

    public void setIdIcon(int idIcon) {
        this.idIcon = idIcon;
    }

    public boolean isChoose() {
        return isChoose;
    }

    public void setChoose(boolean choose) {
        isChoose = choose;
    }
}

