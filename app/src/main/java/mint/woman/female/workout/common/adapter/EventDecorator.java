package mint.woman.female.workout.common.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;

import mint.woman.female.workout.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.HashSet;
import java.util.List;

public  class EventDecorator implements DayViewDecorator {
    private final HashSet<CalendarDay> dates;

    public EventDecorator(List<CalendarDay> dates) {
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }


    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(20, Color.GREEN));
    }
}