package mint.woman.female.workout.utils;

public class CaloriesTrendResult {
    public int totalCalories;             // Tổng calories trong khoảng thời gian
    public int caloriesTrend;             // Xu hướng calories so với khoảng trước (> 0 tăng, < 0 giảm, = 0 không đổi)
    public float caloriesTrendPercent;    // Phần trăm thay đổi calories (%)
    public String caloriesTrendText;      // String đại diện cho xu hướng: "UP", "DOWN", "STABLE"

    // Constructor cơ bản
    public CaloriesTrendResult(int totalCalories) {
        this.totalCalories = totalCalories;
        this.caloriesTrend = 0;
        this.caloriesTrendPercent = 0;
        this.caloriesTrendText = "STABLE";
    }

    // Constructor với trend data
    public CaloriesTrendResult(int totalCalories, int caloriesTrend, float caloriesTrendPercent) {
        this.totalCalories = totalCalories;
        this.caloriesTrend = caloriesTrend;
        this.caloriesTrendPercent = caloriesTrendPercent;
        this.caloriesTrendText = calculateTrendText(caloriesTrend);
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

    public int getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(int totalCalories) {
        this.totalCalories = totalCalories;
    }

    public int getCaloriesTrend() {
        return caloriesTrend;
    }

    public void setCaloriesTrend(int caloriesTrend) {
        this.caloriesTrend = caloriesTrend;
        this.caloriesTrendText = calculateTrendText(caloriesTrend);
    }

    public float getCaloriesTrendPercent() {
        return caloriesTrendPercent;
    }

    public void setCaloriesTrendPercent(float caloriesTrendPercent) {
        this.caloriesTrendPercent = caloriesTrendPercent;
    }

    public String getCaloriesTrendText() {
        return caloriesTrendText;
    }

    @Override
    public String toString() {
        return "CaloriesTrendResult{" +
                "totalCalories=" + totalCalories +
                ", caloriesTrend=" + caloriesTrend +
                ", caloriesTrendPercent=" + caloriesTrendPercent +
                ", caloriesTrendText='" + caloriesTrendText + '\'' +
                '}';
    }
}