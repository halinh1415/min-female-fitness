package mint.woman.female.workout.model;


import java.util.ArrayList;
import java.util.List;

public class Section{
    public String sectionName,subtitle;
    public List<Object> items = new ArrayList<>();
    public String background;
    public int viewType = 1;

    public Section(String sectionName, List<Object> items, int viewType) {
        this.sectionName = sectionName;
        this.items = items;
        this.viewType = viewType;
        this.background ="";
        this.subtitle ="";

    }
    public Section(String sectionName,String subtitle, List<Object> items, int viewType) {
        this.sectionName = sectionName;
        this.items = items;
        this.viewType = viewType;
        this.background ="";
        this.subtitle = subtitle;

    }


}
