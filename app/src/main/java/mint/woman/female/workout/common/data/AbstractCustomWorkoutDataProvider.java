package mint.woman.female.workout.common.data;

import mint.woman.female.workout.model.PlanObject;

public abstract class AbstractCustomWorkoutDataProvider {


    public abstract int getGroupCount();

    public abstract int getChildCount(int groupPosition);

    public abstract PlanObject getGroupItem(int groupPosition);

    public abstract PlanObject.ActionObject getChildItem(int groupPosition, int childPosition);

    public abstract void moveChildItem(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition);

    public abstract void removeChildItem(int groupPosition, int childPosition);

    public abstract void updateItem(int groupPosition, int childPosition, PlanObject.ActionObject actionObject);

    public abstract void addChildItem(int groupPosition, PlanObject.ActionObject childData);

    public abstract int copyToAll();
}
