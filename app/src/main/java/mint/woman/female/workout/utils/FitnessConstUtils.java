package mint.woman.female.workout.utils;

import mint.woman.female.workout.R;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.Calendar;

public class FitnessConstUtils {
    public final static int CALORIES_CONST = 800;
    public final static int FREQUENCY_AD = 10;

    public final static CalendarDay convertFromCalendar(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        return CalendarDay.from(year, month + 1, dayOfMonth);
    }

    public static String toTime(int time) {
        int minutes = (time) / 60;
        int seconds = time % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static int getMuscle(int group, boolean z10) {
        if (z10) {
            switch (group) {
                case 1:
                case 13:
                case 14:
                default:
                    return R.drawable.img_abs_1;
                case 2:
                    return R.drawable.img_back_1;
                case 3:
                    return R.drawable.img_triceps_1;
                case 4:
                    return R.drawable.img_biceps_1;
                case 5:
                    return R.drawable.img_forearms_1;
                case 6:
                    return R.drawable.img_chest_1;
                case 7:
                    return R.drawable.img_shoulders_1;
                case 8:
                    return R.drawable.img_glutes_1;
                case 9:
                    return R.drawable.img_quadriceps_1;
                case 10:
                    return R.drawable.img_hamstrings_1;
                case 11:
                    return R.drawable.img_calves_1;
                case 12:
                    return R.drawable.img_traps_1;
                case 15:
                case 16:
                    return R.drawable.img_adductors_1;
                case 17:
                    return R.drawable.img_neck_1;
            }
        }
        switch (group) {
            case 1:
            case 13:
            case 14:
            default:
                return R.drawable.img_abs_2;
            case 2:
                return R.drawable.img_back_2;
            case 3:
                return R.drawable.img_triceps_2;
            case 4:
                return R.drawable.img_biceps_2;
            case 5:
                return R.drawable.img_forearms_2;
            case 6:
                return R.drawable.img_chest_2;
            case 7:
                return R.drawable.img_shoulders_2;
            case 8:
                return R.drawable.img_glutes_2;
            case 9:
                return R.drawable.img_quadriceps_2;
            case 10:
                return R.drawable.img_hamstrings_2;
            case 11:
                return R.drawable.img_calves_2;
            case 12:
                return R.drawable.img_traps_2;
            case 15:
            case 16:
                return R.drawable.img_adductors_2;
            case 17:
                return R.drawable.img_neck_2;
        }
    }

    public static int getMuscleId(int group) {
        switch (group) {
            case 1:
            case 13:
            case 14:
            default:
                return R.string.abs;
            case 2:
                return R.string.back;
            case 3:
                return R.string.triceps;
            case 4:
                return R.string.biceps;
            case 5:
                return R.string.forearms;
            case 6:
                return R.string.chest;
            case 7:
                return R.string.shoulders;
            case 8:
                return R.string.glutes;
            case 9:
                return R.string.quadriceps;
            case 10:
                return R.string.hamstrings;
            case 11:
                return R.string.calves;
            case 12:
                return R.string.traps;
            case 15:
            case 16:
                return R.string.adductors;
            case 17:
                return R.string.neck;
        }
    }

}
