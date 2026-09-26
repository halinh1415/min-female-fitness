package mint.woman.female.workout.badge;

import mint.woman.female.workout.model.HistoryItem;
import mint.woman.female.workout.utils.HistoryDao;
import mint.woman.female.workout.utils.StreakCalculator;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Tinh toan bo chi so huy hieu tu lich su tap. Chay tren thread nen (xem {@link BadgeRepository}).
 */
public final class BadgeMetricsCalculator {

    /** Buoi "dai" tinh tu 20 phut tro len. */
    private static final int LONG_SESSION_SECONDS = 20 * 60;
    /** Truoc 08:00 duoc tinh la buoi tap binh minh. */
    private static final int DAWN_BEFORE_HOUR = 8;
    /** Tu 18:00 tro di duoc tinh la buoi tap toi. */
    private static final int EVENING_FROM_HOUR = 18;

    private BadgeMetricsCalculator() {
    }

    public static BadgeMetrics calculate(HistoryDao dao) {
        BadgeMetrics metrics = new BadgeMetrics();
        if (dao == null) return metrics;

        // Dung chung bo dem streak voi phan con lai cua app de so lieu khong lech nhau.
        StreakCalculator streaks = new StreakCalculator(dao);
        metrics.currentStreak = streaks.getCurrentStreak();
        metrics.bestStreak = streaks.getBestStreak();

        List<HistoryItem> history = dao.getPassedItemsFrom(0L);
        if (history == null || history.isEmpty()) {
            return metrics;
        }

        Map<Long, Integer> secondsPerDay = new HashMap<>();
        Set<Integer> programs = new HashSet<>();
        Set<String> weeks = new HashSet<>();
        Set<String> months = new HashSet<>();

        long totalSeconds = 0;
        Calendar calendar = Calendar.getInstance();

        for (HistoryItem item : history) {
            if (item == null) continue;

            int seconds = item.getDuration();
            if (seconds <= 0) {
                seconds = Math.max(0, item.getRealDuration());
            }

            metrics.totalSessions++;
            totalSeconds += seconds;
            metrics.totalCalories += item.getCalories();
            if (seconds >= LONG_SESSION_SECONDS) {
                metrics.longSessions++;
            }
            if (item.getProgramId() > 0) {
                programs.add(item.getProgramId());
            }

            long dateId = item.getDateId();
            Integer daySeconds = secondsPerDay.get(dateId);
            secondsPerDay.put(dateId, (daySeconds == null ? 0 : daySeconds) + seconds);

            long startTime = item.getStartTime() > 0 ? item.getStartTime() : dateId;
            calendar.setTimeInMillis(startTime);

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (hour < DAWN_BEFORE_HOUR) {
                metrics.dawnSessions++;
            } else if (hour >= EVENING_FROM_HOUR) {
                metrics.eveningSessions++;
            }

            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                metrics.weekendSessions++;
            }

            weeks.add(String.format(Locale.US, "%d-%d",
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.WEEK_OF_YEAR)));
            months.add(String.format(Locale.US, "%d-%d",
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)));
        }

        int busiestDaySeconds = 0;
        for (Integer daySeconds : secondsPerDay.values()) {
            busiestDaySeconds = Math.max(busiestDaySeconds, daySeconds);
        }

        metrics.totalMinutes = (int) (totalSeconds / 60);
        metrics.totalDays = secondsPerDay.size();
        metrics.distinctPrograms = programs.size();
        metrics.activeWeeks = weeks.size();
        metrics.activeMonths = months.size();
        metrics.singleDayMinutes = busiestDaySeconds / 60;

        return metrics;
    }
}
