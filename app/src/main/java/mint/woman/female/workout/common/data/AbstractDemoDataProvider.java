package mint.woman.female.workout.common.data;

import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.PlanObject;

public abstract class AbstractDemoDataProvider {
    public abstract int getItemCount();

    public abstract PlanObject.ActionObject getItem(int position);

    public abstract ExerciseObject getExerciseItem(int actionId);

    public abstract void moveItem(int fromPosition, int toPosition);

    public abstract void removeItem(int position);

    public abstract void addItem(int action);

    public abstract void updateItem(int position, PlanObject.ActionObject actionObject);
    public abstract void updateTime(int position, int time);
    public abstract void replaceAction(int position,PlanObject.ActionObject actionObject);

    public abstract PlanObject getPlanObject();
}
