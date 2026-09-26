package mint.woman.female.workout.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StreakCalculator {

    private static final int MIN_SECONDS_FOR_STREAK = 3 * 60;

    private final HistoryDao historyDao;

    public StreakCalculator(HistoryDao historyDao) {
        this.historyDao = historyDao;
    }

    public UserStreakResult getUserStreakResult() {
        long today = DateIdUtils.todayDateId();
        boolean didPracticeToday = isQualifiedDay(today);
        boolean hasAnyActivityToday = hasAnyActivity(today);

        int streakDays = getCurrentStreak();
        int streakScore = mapStreakDaysToScore(streakDays);

        StreakMessageType type = StreakMessageType.UNKNOWN;

        // Inspect recent history to decide more granular message types
        List<mint.woman.female.workout.model.HistoryItem> recent = historyDao.getRecentPassedHistory(5);

        if (recent == null || recent.isEmpty()) {
            // No passed history yet. If user is training today but chưa đủ điều kiện streak,
            // show encouragement to complete today instead of "broken"-style messaging.
            type = hasAnyActivityToday && !didPracticeToday
                    ? StreakMessageType.NEED_TODAY
                    : StreakMessageType.START_NEW;
        } else {
            long latestDateId = recent.get(0).getDateId();
            int daysSinceLast = daysBetween(latestDateId, today);

            if (didPracticeToday) {
                // latestDateId should be today (daysSinceLast == 0)
                // Check milestone
                if (isMilestone(streakDays)) {
                    type = StreakMessageType.MILESTONE_ACHIEVED;
                } else {
                    // Check if resumed after a gap: compare second most recent date
                    if (recent.size() > 1) {
                        long secondDateId = recent.get(1).getDateId();
                        int gap = daysBetween(secondDateId, latestDateId);
                        if (gap > 1) {
                            type = StreakMessageType.RESUMED;
                        } else {
                            type = StreakMessageType.KEEP_TODAY;
                        }
                    } else {
                        type = StreakMessageType.KEEP_TODAY;
                    }
                }
            } else {
                // user did NOT qualify streak today
                if (hasAnyActivityToday) {
                    // They already trained today but chưa đạt ngưỡng streak (>= MIN_SECONDS_FOR_STREAK)
                    type = StreakMessageType.NEED_TODAY;
                } else if (streakDays > 0) {
                    // They practiced yesterday (streak includes yesterday) — still can keep by practicing today
                    type = StreakMessageType.NEED_TODAY;
                } else {
                    // No current streak (didn't practice today nor yesterday)
                    // If last practice was recently (within 7 days) treat as broken recently
                    if (daysSinceLast <= 7) {
                        type = StreakMessageType.BROKEN_RECENTLY;
                    } else {
                        type = StreakMessageType.START_NEW;
                    }
                }
            }
        }

        return new UserStreakResult(streakDays, streakScore, didPracticeToday, type);
    }

    private boolean hasAnyActivity(long dateId) {
        List<mint.woman.female.workout.model.HistoryItem> items = historyDao.getHistoryByDateId(dateId);
        return items != null && !items.isEmpty();
    }

    private int daysBetween(long fromDateId, long toDateId) {
        return (int) (toDateId - fromDateId);
    }

    private boolean isMilestone(int streakDays) {
        // define milestone days
        if (streakDays <= 0) return false;
        int[] milestones = new int[]{7, 14, 21, 30};
        for (int m : milestones) if (streakDays == m) return true;
        return false;
    }

    public int getCurrentStreak() {
        long today = DateIdUtils.todayDateId();

        if (isQualifiedDay(today)) {
            return countBackward(today);
        }

        long yesterday = DateIdUtils.minusDays(today, 1);
        if (isQualifiedDay(yesterday)) {
            return countBackward(yesterday);
        }

        return 0;
    }

    public int getBestStreak() {
        List<Long> days = historyDao.getAllPassedDateIds();
        if (days == null || days.isEmpty()) return 0;

        int best = 0;
        int current = 0;
        Long prev = null;

        for (Long dateId : days) {
            if (!isQualifiedDay(dateId)) continue;

            if (prev == null) {
                current = 1;
            } else {
                long expectedNext = DateIdUtils.plusDays(prev, 1);
                if (expectedNext == dateId) {
                    current++;
                } else {
                    current = 1;
                }
            }

            best = Math.max(best, current);
            prev = dateId;
        }

        return best;
    }

    public boolean isQualifiedDay(long dateId) {
        int totalDuration = historyDao.getTotalDurationByDate(dateId);
        return totalDuration >= MIN_SECONDS_FOR_STREAK;
    }

    private int countBackward(long startDateId) {
        int streak = 0;
        long cursor = startDateId;
        Set<Long> countedDays = new HashSet<>();

        while (isQualifiedDay(cursor)) {
            if (!countedDays.contains(cursor)) {
                streak++;
                countedDays.add(cursor);
            }
            cursor = DateIdUtils.minusDays(cursor, 1);
        }
        return streak;
    }

    private int mapStreakDaysToScore(int streakDays) {
        if (streakDays == 0) return 0;
        if (streakDays == 1) return 30;
        if (streakDays == 2) return 45;
        if (streakDays == 3) return 60;
        if (streakDays == 4) return 72;
        if (streakDays == 5) return 84;
        if (streakDays == 6) return 92;
        return 100;
    }
}

