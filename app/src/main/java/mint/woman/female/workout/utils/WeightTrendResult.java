package mint.woman.female.workout.utils;

public class WeightTrendResult {
    public float weightChange;      // Thay đổi cân nặng (kg)
    public float percentChange;     // Phần trăm thay đổi (%)
    public String trend;            // "UP", "DOWN", "STABLE", "NO_DATA"
    public long startDate;
    public long endDate;
    public float startWeight;
    public float endWeight;

    public WeightTrendResult(float weightChange, float percentChange, String trend,
                           long startDate, long endDate, float startWeight, float endWeight) {
        this.weightChange = weightChange;
        this.percentChange = percentChange;
        this.trend = trend;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startWeight = startWeight;
        this.endWeight = endWeight;
    }

    @Override
    public String toString() {
        return "WeightTrendResult{" +
                "weightChange=" + weightChange +
                ", percentChange=" + percentChange +
                ", trend='" + trend + '\'' +
                ", startWeight=" + startWeight +
                ", endWeight=" + endWeight +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }

    public float getWeightChange(boolean isKg) {
        return isKg?weightChange:weightChange*2.20462f;
    }
}

