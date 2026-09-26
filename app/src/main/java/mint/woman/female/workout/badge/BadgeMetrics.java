package mint.woman.female.workout.badge;

/**
 * Cac chi so da tinh xong tu lich su tap. Mot lan tinh dung cho toan bo danh sach huy hieu.
 */
public class BadgeMetrics {

    int totalSessions;
    int totalDays;
    int totalMinutes;
    int totalCalories;
    int currentStreak;
    int bestStreak;
    int dawnSessions;
    int eveningSessions;
    int weekendSessions;
    int longSessions;
    int singleDayMinutes;
    int distinctPrograms;
    int activeWeeks;
    int activeMonths;

    public int valueOf(BadgeMetric metric) {
        if (metric == null) return 0;
        switch (metric) {
            case TOTAL_SESSIONS: return totalSessions;
            case TOTAL_DAYS: return totalDays;
            case TOTAL_MINUTES: return totalMinutes;
            case TOTAL_CALORIES: return totalCalories;
            case CURRENT_STREAK: return currentStreak;
            case BEST_STREAK: return bestStreak;
            case DAWN_SESSIONS: return dawnSessions;
            case EVENING_SESSIONS: return eveningSessions;
            case WEEKEND_SESSIONS: return weekendSessions;
            case LONG_SESSIONS: return longSessions;
            case SINGLE_DAY_MINUTES: return singleDayMinutes;
            case DISTINCT_PROGRAMS: return distinctPrograms;
            case ACTIVE_WEEKS: return activeWeeks;
            case ACTIVE_MONTHS: return activeMonths;
            default: return 0;
        }
    }

    public int getTotalSessions() {
        return totalSessions;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public int getTotalMinutes() {
        return totalMinutes;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getBestStreak() {
        return bestStreak;
    }
}
