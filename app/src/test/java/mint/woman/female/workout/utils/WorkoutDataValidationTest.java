package mint.woman.female.workout.utils;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.PlanObject;
import static org.junit.Assert.assertEquals;

public class WorkoutDataValidationTest {
    private PlanObject.ActionObject action(int id) {
        PlanObject.ActionObject action = new PlanObject.ActionObject();
        action.actionId = id;
        return action;
    }

    @Test public void nonEmptyPlanStillFailsWhenLaterExerciseIsMissing() {
        Map<Integer, ExerciseObject> catalog = new HashMap<>();
        catalog.put(10, new ExerciseObject());
        assertEquals(1, WorkoutDataValidation.invalidActionIndex(Arrays.asList(action(10), action(20)), catalog));
    }

    @Test public void allExercisesPresentAllowsWorkout() {
        Map<Integer, ExerciseObject> catalog = new HashMap<>();
        catalog.put(10, new ExerciseObject());
        catalog.put(20, new ExerciseObject());
        assertEquals(-1, WorkoutDataValidation.invalidActionIndex(Arrays.asList(action(10), action(20)), catalog));
    }

    @Test public void nullCatalogOrNullCatalogEntryIsRejected() {
        assertEquals(0, WorkoutDataValidation.invalidActionIndex(Collections.singletonList(action(10)), null));
        Map<Integer, ExerciseObject> catalog = new HashMap<>();
        catalog.put(10, null);
        assertEquals(0, WorkoutDataValidation.invalidActionIndex(Collections.singletonList(action(10)), catalog));
    }

    @Test public void emptyOrNullActionsAreRejected() {
        assertEquals(0, WorkoutDataValidation.invalidActionIndex(null, Collections.emptyMap()));
        assertEquals(0, WorkoutDataValidation.invalidActionIndex(Collections.emptyList(), Collections.emptyMap()));
        assertEquals(0, WorkoutDataValidation.invalidActionIndex(Collections.singletonList(null), Collections.emptyMap()));
    }
}
