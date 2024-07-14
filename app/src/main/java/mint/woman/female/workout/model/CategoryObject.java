package mint.woman.female.workout.model;

import java.util.ArrayList;
import java.util.List;

public class CategoryObject {
    private String name;

    private List<ProgramObject> programObjectList = new ArrayList<>();

    public CategoryObject(List<ProgramObject> programObjectList) {
        ProgramObject programObject = programObjectList.get(0);
        this.programObjectList.addAll(programObjectList);
        this.name = programObject.getName();

    }

    public String getName() {
        return name;
    }



    public ProgramObject getFirstProgramObject(){
        return programObjectList.get(0);
    }
    public List<ProgramObject> getProgramObjectList(){
        return programObjectList;
    }


}
