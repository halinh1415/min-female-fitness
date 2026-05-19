package mint.woman.female.workout.notification;


public class PracticeStats {
    private int streakCount;
    private int consecutiveActiveDays;
    private int missedDays;
    private boolean didCompleteToday;
    private int preferredHour;
    private int preferredMinute;
    private long lastCompletedAt;

    public PracticeStats() {
        this.streakCount = 0;
        this.consecutiveActiveDays = 0;
        this.missedDays = 0;
        this.didCompleteToday = false;
        this.preferredHour = 19;
        this.preferredMinute = 0;
        this.lastCompletedAt = 0L;
    }

    public PracticeStats(int streakCount,
                         int consecutiveActiveDays,
                         int missedDays,
                         boolean didCompleteToday,
                         int preferredHour,
                         int preferredMinute,
                         long lastCompletedAt) {
        this.streakCount = streakCount;
        this.consecutiveActiveDays = consecutiveActiveDays;
        this.missedDays = missedDays;
        this.didCompleteToday = didCompleteToday;
        this.preferredHour = preferredHour;
        this.preferredMinute = preferredMinute;
        this.lastCompletedAt = lastCompletedAt;
    }

    public int getStreakCount() {
        return streakCount;
    }

    public void setStreakCount(int streakCount) {
        this.streakCount = streakCount;
    }

    public int getConsecutiveActiveDays() {
        return consecutiveActiveDays;
    }

    public void setConsecutiveActiveDays(int consecutiveActiveDays) {
        this.consecutiveActiveDays = consecutiveActiveDays;
    }

    public int getMissedDays() {
        return missedDays;
    }

    public void setMissedDays(int missedDays) {
        this.missedDays = missedDays;
    }

    public boolean isDidCompleteToday() {
        return didCompleteToday;
    }

    public void setDidCompleteToday(boolean didCompleteToday) {
        this.didCompleteToday = didCompleteToday;
    }

    public int getPreferredHour() {
        return preferredHour;
    }

    public void setPreferredHour(int preferredHour) {
        this.preferredHour = preferredHour;
    }

    public int getPreferredMinute() {
        return preferredMinute;
    }

    public void setPreferredMinute(int preferredMinute) {
        this.preferredMinute = preferredMinute;
    }

    public long getLastCompletedAt() {
        return lastCompletedAt;
    }

    public void setLastCompletedAt(long lastCompletedAt) {
        this.lastCompletedAt = lastCompletedAt;
    }
}
