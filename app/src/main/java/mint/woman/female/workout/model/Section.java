package mint.woman.female.workout.model;

import java.util.List;

public class Section {
    public String sectionName;
    public List<ProgramObject> programObjectList;
    public int layout;

    public Section(String sectionName, List<ProgramObject> programObjectList,int layout) {
        this.sectionName = sectionName;
        this.programObjectList = programObjectList;
        this.layout = layout;
    }
}
