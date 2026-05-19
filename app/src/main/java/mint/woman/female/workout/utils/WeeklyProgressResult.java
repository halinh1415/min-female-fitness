package mint.woman.female.workout.utils;

import java.util.List;

public class WeeklyProgressResult {
    public int completedDays;           // Số ngày đã hoàn thành bài tập trong tuần
    public int goalDays;                // Mục tiêu ngày trong tuần
    public float progressPercent;       // Phần trăm hoàn thành
    public int completedWorkouts;       // Số lượng bài tập hoàn thành trong tuần
    public int totalDuration;           // Tổng thời gian tập luyện trong tuần (giây)
    public int totalCalories;           // Tổng calories đốt trong tuần
    public List<Long> completedDaysList; // Danh sách các ngày hoàn thành bài tập (dateId)

    // Xu hướng so với tuần trước (> 0 tăng, < 0 giảm, = 0 không đổi)
    public int durationTrend;           // Xu hướng thời gian (giây)
    public int caloriesTrend;           // Xu hướng calories
    public int workoutCountTrend;       // Xu hướng số bài tập

    // Phần trăm thay đổi
    public float durationTrendPercent;  // Phần trăm thay đổi thời gian (%)
    public float caloriesTrendPercent;  // Phần trăm thay đổi calories (%)
    public float workoutCountTrendPercent; // Phần trăm thay đổi số bài tập (%)

    // String đại diện cho xu hướng: "UP", "DOWN", "STABLE"
    public String durationTrendText;    // Xu hướng thời gian
    public String caloriesTrendText;    // Xu hướng calories
    public String workoutCountTrendText; // Xu hướng số bài tập

    public WeeklyProgressResult(int completedDays, int goalDays, int completedWorkouts,
                               int totalDuration, int totalCalories, List<Long> completedDaysList) {
        this.completedDays = completedDays;
        this.goalDays = goalDays;
        this.progressPercent = (float) (completedDays * 100.0 / goalDays);
        this.completedWorkouts = completedWorkouts;
        this.totalDuration = totalDuration;
        this.totalCalories = totalCalories;
        this.completedDaysList = completedDaysList;
    }

    // Constructor với trend data
    public WeeklyProgressResult(int completedDays, int goalDays, int completedWorkouts,
                               int totalDuration, int totalCalories, List<Long> completedDaysList,
                               int durationTrend, int caloriesTrend, int workoutCountTrend,
                               float durationTrendPercent, float caloriesTrendPercent, float workoutCountTrendPercent) {
        this.completedDays = completedDays;
        this.goalDays = goalDays;
        this.progressPercent = (float) (completedDays * 100.0 / goalDays);
        this.completedWorkouts = completedWorkouts;
        this.totalDuration = totalDuration;
        this.totalCalories = totalCalories;
        this.completedDaysList = completedDaysList;
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
        return "WeeklyProgressResult{" +
                "completedDays=" + completedDays +
                ", goalDays=" + goalDays +
                ", progressPercent=" + progressPercent +
                ", completedWorkouts=" + completedWorkouts +
                ", totalDuration=" + totalDuration +
                ", totalCalories=" + totalCalories +
                ", completedDaysList=" + completedDaysList +
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
