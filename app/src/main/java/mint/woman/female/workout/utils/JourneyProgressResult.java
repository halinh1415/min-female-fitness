package mint.woman.female.workout.utils;

/**
 * Kết quả tiến độ hành trình của một chương trình
 * Theo dõi: thời gian tập, calories, số ngày hoàn thành
 */
public class JourneyProgressResult {
    public int programId;                    // ID của chương trình
    public String programName;               // Tên chương trình
    public int totalDaysCompleted;          // Tổng số ngày đã hoàn thành
    public int totalDaysInProgram;          // Tổng số ngày của chương trình
    public long totalDuration;              // Tổng thời gian tập (seconds)
    public int totalCalories;               // Tổng calories đốt
    public float averageDuration;           // Thời gian tập trung bình (seconds)
    public float averageCalories;           // Calories trung bình
    public long firstCompletedDate;         // Ngày bắt đầu hoàn thành
    public long lastCompletedDate;          // Ngày hoàn thành gần nhất
    public float completionPercentage;      // Phần trăm hoàn thành

    public JourneyProgressResult() {
    }

    public JourneyProgressResult(int programId, String programName, int totalDaysCompleted,
                                int totalDaysInProgram, long totalDuration, int totalCalories,
                                float averageDuration, float averageCalories,
                                long firstCompletedDate, long lastCompletedDate,
                                float completionPercentage) {
        this.programId = programId;
        this.programName = programName;
        this.totalDaysCompleted = totalDaysCompleted;
        this.totalDaysInProgram = totalDaysInProgram;
        this.totalDuration = totalDuration;
        this.totalCalories = totalCalories;
        this.averageDuration = averageDuration;
        this.averageCalories = averageCalories;
        this.firstCompletedDate = firstCompletedDate;
        this.lastCompletedDate = lastCompletedDate;
        this.completionPercentage = completionPercentage;
    }

    @Override
    public String toString() {
        return "JourneyProgressResult{" +
                "programId=" + programId +
                ", programName='" + programName + '\'' +
                ", totalDaysCompleted=" + totalDaysCompleted +
                ", totalDaysInProgram=" + totalDaysInProgram +
                ", totalDuration=" + totalDuration +
                ", totalCalories=" + totalCalories +
                ", averageDuration=" + averageDuration +
                ", averageCalories=" + averageCalories +
                ", firstCompletedDate=" + firstCompletedDate +
                ", lastCompletedDate=" + lastCompletedDate +
                ", completionPercentage=" + completionPercentage +
                '}';
    }
}

