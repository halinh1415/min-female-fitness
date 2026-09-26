package mint.woman.female.workout.common.adapter;

import mint.woman.female.workout.model.CategoryObject;
import mint.woman.female.workout.model.ProgramObject;

import java.util.List;

public interface ProgramClickListener {
    void onItemRootViewClicked(ProgramObject programObject);
    void onGroupClickListener(CategoryObject categoryObject);

    void onFooterRootViewClicked(String title, List<ProgramObject> programObjects);
}
