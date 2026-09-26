package mint.woman.female.workout.utils;

public class TodayWorkoutResult {
    public int workoutCount;              // Số bài tập hôm nay
    public int totalCalories;             // Tổng calories hôm nay
    public int totalDuration;             // Tổng thời gian (giây)

    // Xu hướng so với hôm qua (> 0 tăng, < 0 giảm, = 0 không đổi)
    public int durationTrend;             // Xu hướng thời gian (giây)
    public int caloriesTrend;             // Xu hướng calories
    public int workoutCountTrend;         // Xu hướng số bài tập

    // Phần trăm thay đổi
    public float durationTrendPercent;    // Phần trăm thay đổi thời gian (%)
    public float caloriesTrendPercent;    // Phần trăm thay đổi calories (%)
    public float workoutCountTrendPercent; // Phần trăm thay đổi số bài tập (%)

    // String đại diện cho xu hướng: "UP", "DOWN", "STABLE"
    public String durationTrendText;      // Xu hướng thời gian
    public String caloriesTrendText;      // Xu hướng calories
    public String workoutCountTrendText;  // Xu hướng số bài tập

    public TodayWorkoutResult(int workoutCount, int totalCalories, int totalDuration) {
        this.workoutCount = workoutCount;
        this.totalCalories = totalCalories;
        this.totalDuration = totalDuration;
    }

    // Constructor với trend data
    public TodayWorkoutResult(int workoutCount, int totalCalories, int totalDuration,
                             int durationTrend, int caloriesTrend, int workoutCountTrend,
                             float durationTrendPercent, float caloriesTrendPercent, float workoutCountTrendPercent) {
        this.workoutCount = workoutCount;
        this.totalCalories = totalCalories;
        this.totalDuration = totalDuration;
        this.durationTrend = durationTrend;
        this.caloriesTrend = caloriesTrend;
        this.workoutCountTrend = workoutCountTrend;
        this.durationTrendPercent = durationTrendPercent;
        this.caloriesTrendPercent = caloriesTrendPercent;
        this.workoutCountTrendPercent = workoutCountTrendPercent;

        // Tính text trend
        this.durationTrendText = calculateTrendText(durationTrend);
        this.caloriesTrendText = calculateTrendText(caloriesTrend);
        this.workoutCountTrendText = calculateTrendText(workoutCountTrend);
    }

    private static String calculateTrendText(int trend) {
        if (trend > 0) {
            return "UP";
        } else if (trend < 0) {
            return "DOWN";
        } else {
            return "STABLE";
        }
    }

    @Override
    public String toString() {
        return "TodayWorkoutResult{" +
                "workoutCount=" + workoutCount +
                ", totalCalories=" + totalCalories +
                ", totalDuration=" + totalDuration +
                ", durationTrend=" + durationTrend +
                ", caloriesTrend=" + caloriesTrend +
                ", workoutCountTrend=" + workoutCountTrend +
                ", durationTrendPercent=" + durationTrendPercent +
                ", caloriesTrendPercent=" + caloriesTrendPercent +
                ", workoutCountTrendPercent=" + workoutCountTrendPercent +
                ", durationTrendText='" + durationTrendText + '\'' +
                ", caloriesTrendText='" + caloriesTrendText + '\'' +
                ", workoutCountTrendText='" + workoutCountTrendText + '\'' +
                '}';
    }
}
