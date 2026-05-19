package mint.woman.female.workout.utils;

public class UserStreakResult {
    public final int streakDays;
    public final int streakScore;
    public final boolean didPracticeToday;
    public final StreakMessageType messageType;

    public UserStreakResult(int streakDays, int streakScore, boolean didPracticeToday, StreakMessageType messageType) {
        this.streakDays = streakDays;
        this.streakScore = streakScore;
        this.didPracticeToday = didPracticeToday;
        this.messageType = messageType;
    }
}