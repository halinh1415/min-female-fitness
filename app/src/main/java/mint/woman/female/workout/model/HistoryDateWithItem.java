package mint.woman.female.workout.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class HistoryDateWithItem {
    @Embedded
    public HistoryDate date;
    @Relation(parentColumn = "date", entityColumn = "dateId", entity = HistoryItem.class)
    public List<HistoryItem> historyItems;

    public int totalCalories() {
        int cal = 0;
        for (HistoryItem item : historyItems
        ) {
            cal += item.getCalories();
        }
        return cal;
    }

    public int totalDuration() {
        int time = 0;
        for (HistoryItem item : historyItems
        ) {
            time += item.getRealDuration();
        }
        return time;
    }

    public int totalWorkout() {
        return historyItems.size();
    }
}
