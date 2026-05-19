package mint.woman.female.workout.utils;

import java.util.Calendar;

public class DateIdUtils {

    public static long todayDateId() {
        return toDateId(System.currentTimeMillis());
    }

    public static long toDateId(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long minusDays(long dateId, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateId);
        calendar.add(Calendar.DAY_OF_YEAR, -days);
        return calendar.getTimeInMillis();
    }

    public static long plusDays(long dateId, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateId);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTimeInMillis();
    }
}