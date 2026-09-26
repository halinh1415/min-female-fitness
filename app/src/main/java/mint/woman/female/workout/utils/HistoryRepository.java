package mint.woman.female.workout.utils;

import android.app.Application;

import androidx.lifecycle.LiveData;

//import homeworkout.noequipment.workout.athome.model.HistoryDate;
//import homeworkout.noequipment.workout.athome.model.HistoryDateWithItem;
import mint.woman.female.workout.model.HistoryItem;

import java.util.List;

public class HistoryRepository {

    private HistoryDao mHistoryDao;


    public HistoryRepository(Application application) {
        HistoryDatabase db = HistoryDatabase.getDatabase(application);
        mHistoryDao = db.historyDao();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<HistoryItem>> getAllHistory() {
        return mHistoryDao.getAllHistoryItem();
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insert(HistoryItem historyItem) {
        HistoryDatabase.databaseWriteExecutor.execute(() -> {
            mHistoryDao.insertHistory(historyItem);
        });

    }

    public LiveData<HistoryItem> getHistoryById(int id) {
        return mHistoryDao.getHistoryById(id);
    }

    public void update(HistoryItem historyItem) {
        HistoryDatabase.databaseWriteExecutor.execute(() -> {
            mHistoryDao.update(historyItem);
        });
    }

    public LiveData<List<HistoryItem>> findFromTo(long from, long to) {
        return mHistoryDao.findHistoryFromTo(from, to);
    }
    public LiveData<List<HistoryItem>> findInDay(long day) {
        return mHistoryDao.findHistoryInDate(day);
    }

    public void getAvgDuration(int limit, Callback<Integer> callback) {
        HistoryDatabase.databaseWriteExecutor.execute(() -> {
            List<HistoryItem> history = mHistoryDao.getRecentPassedHistory(limit);

            int avgDuration = 0;
            if (history != null && !history.isEmpty()) {
                int sumSeconds = 0;
                int count = 0;

                for (HistoryItem item : history) {
                    if (item == null) continue;

                    int duration = item.getDuration();
                    if (duration <= 0) {
                        duration = item.getRealDuration();
                    }

                    if (duration < 60) continue;

                    sumSeconds += duration;
                    count++;
                }

                if (count > 0) {
                    avgDuration = (sumSeconds / count) / 60; // đổi sang phút
                }
            }

            callback.onResult(avgDuration);
        });
    }

    public void deleteAllHistory() {
        HistoryDatabase.databaseWriteExecutor.execute(() -> {
            mHistoryDao.deleteAllHistoryItem();
        });

    }

    public interface Callback<T> {
        void onResult(T data);
    }

    public void getUserStreak(Callback<UserStreakResult> callback) {
        HistoryDatabase.databaseWriteExecutor.execute(() -> {
            StreakCalculator calculator = new StreakCalculator(mHistoryDao);
            UserStreakResult result = calculator.getUserStreakResult();
            callback.onResult(result);
        });
    }

    public void getTodayWorkoutStats(Callback<TodayWorkoutResult> callback) {
        HistoryDatabase.databaseWriteExecutor.execute(() -> {
            // Lấy dateId của hôm nay
            long today = System.currentTimeMillis();
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTimeInMillis(today);
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calendar.set(java.util.Calendar.MINUTE, 0);
            calendar.set(java.util.Calendar.SECOND, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            long todayDateId = calendar.getTimeInMillis();

            // Lấy dateId của hôm qua
            calendar.add(java.util.Calendar.DAY_OF_MONTH, -1);
            long yesterdayDateId = calendar.getTimeInMillis();

            // Lấy dữ liệu hôm nay
            List<HistoryItem> todayHistory = mHistoryDao.getHistoryByDateId(todayDateId);

            int workoutCount = 0;
            int totalCalories = 0;
            int totalDuration = 0;

            if (todayHistory != null) {
                workoutCount = todayHistory.size();
                for (HistoryItem item : todayHistory) {
                    if (item != null) {
                        totalCalories += item.getCalories();
                        totalDuration += item.getDuration();
                    }
                }
            }

            // Lấy dữ liệu hôm qua để tính xu hướng
            List<HistoryItem> yesterdayHistory = mHistoryDao.getHistoryByDateId(yesterdayDateId);

            int yesterdayWorkoutCount = 0;
            int yesterdayTotalCalories = 0;
            int yesterdayTotalDuration = 0;

            if (yesterdayHistory != null) {
                yesterdayWorkoutCount = yesterdayHistory.size();
                for (HistoryItem item : yesterdayHistory) {
                    if (item != null) {
                        yesterdayTotalCalories += item.getCalories();
                        yesterdayTotalDuration += item.getDuration();
                    }
                }
            }

            // Tính xu hướng (trend)
            int durationTrend = totalDuration - yesterdayTotalDuration;
            int caloriesTrend = totalCalories - yesterdayTotalCalories;
            int workoutCountTrend = workoutCount - yesterdayWorkoutCount;

            // Tính phần trăm thay đổi
            float durationTrendPercent = (yesterdayTotalDuration != 0)
                    ? (durationTrend * 100.0f / yesterdayTotalDuration)
                    : 0;
            float caloriesTrendPercent = (yesterdayTotalCalories != 0)
                    ? (caloriesTrend * 100.0f / yesterdayTotalCalories)
                    : 0;
            float workoutCountTrendPercent = (yesterdayWorkoutCount != 0)
                    ? (workoutCountTrend * 100.0f / yesterdayWorkoutCount)
                    : 0;

            TodayWorkoutResult result = new TodayWorkoutResult(workoutCount, totalCalories, totalDuration,
                    durationTrend, caloriesTrend, workoutCountTrend,
                    durationTrendPercent, caloriesTrendPercent, workoutCountTrendPercent);
            callback.onResult(result);
        });
    }    public void getWeeklyProgress(int goalDays, int firstDayOfWeek, Callback<WeeklyProgressResult> callback) {
        HistoryDatabase.databaseWriteExecutor.execute(() -> {
            // Lấy các ngày trong tuần hiện tại dựa trên firstDayOfWeek dưới dạng dateId
            java.util.Calendar calendar = java.util.Calendar.getInstance();

            // Tìm ngày đầu tuần hiện tại theo cài đặt
            calendar.set(java.util.Calendar.DAY_OF_WEEK, firstDayOfWeek);
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calendar.set(java.util.Calendar.MINUTE, 0);
            calendar.set(java.util.Calendar.SECOND, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            long weekStartDateId = calendar.getTimeInMillis();

            // Tìm ngày cuối tuần (6 ngày sau ngày đầu)
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 6);
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calendar.set(java.util.Calendar.MINUTE, 0);
            calendar.set(java.util.Calendar.SECOND, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            long weekEndDateId = calendar.getTimeInMillis();

            // Tính tuần trước
            long lastWeekStartDateId = weekStartDateId - (7 * 24 * 60 * 60 * 1000L);
            long lastWeekEndDateId = weekEndDateId - (7 * 24 * 60 * 60 * 1000L);

            // Lấy dữ liệu tuần hiện tại
            List<Long> completedDateIds = mHistoryDao.getCompletedDateIdsInRange(weekStartDateId, weekEndDateId);
            int completedDays = completedDateIds != null ? completedDateIds.size() : 0;

            List<HistoryItem> weeklyItems = mHistoryDao.getHistoryInRange(weekStartDateId, weekEndDateId);

            int completedWorkouts = 0;
            int totalDuration = 0;
            int totalCalories = 0;

            if (weeklyItems != null) {
                completedWorkouts = weeklyItems.size();
                for (HistoryItem item : weeklyItems) {
                    if (item != null) {
                        totalDuration += item.getDuration();
                        totalCalories += item.getCalories();
                    }
                }
            }

            // Lấy dữ liệu tuần trước để tính xu hướng
            List<HistoryItem> lastWeekItems = mHistoryDao.getHistoryInRange(lastWeekStartDateId, lastWeekEndDateId);

            int lastWeekDuration = 0;
            int lastWeekCalories = 0;
            int lastWeekWorkouts = 0;

            if (lastWeekItems != null) {
                lastWeekWorkouts = lastWeekItems.size();
                for (HistoryItem item : lastWeekItems) {
                    if (item != null) {
                        lastWeekDuration += item.getDuration();
                        lastWeekCalories += item.getCalories();
                    }
                }
            }

            // Tính xu hướng (trend)
            int durationTrend = totalDuration - lastWeekDuration;
            int caloriesTrend = totalCalories - lastWeekCalories;
            int workoutCountTrend = completedWorkouts - lastWeekWorkouts;

            // Tính phần trăm thay đổi
            float durationTrendPercent = (lastWeekDuration != 0)
                    ? (durationTrend * 100.0f / lastWeekDuration)
                    : 0;
            float caloriesTrendPercent = (lastWeekCalories != 0)
                    ? (caloriesTrend * 100.0f / lastWeekCalories)
                    : 0;
            float workoutCountTrendPercent = (lastWeekWorkouts != 0)
                    ? (workoutCountTrend * 100.0f / lastWeekWorkouts)
                    : 0;

            WeeklyProgressResult result = new WeeklyProgressResult(completedDays, goalDays, completedWorkouts,
                    totalDuration, totalCalories, completedDateIds,
                    durationTrend, caloriesTrend, workoutCountTrend,
                    durationTrendPercent, caloriesTrendPercent, workoutCountTrendPercent);
            callback.onResult(result);
        });
    }

    public void getRecentProgramIds(Callback<List<Integer>> callback) {
        HistoryDatabase.databaseWriteExecutor.execute(() -> {
            List<Integer> recentProgramIds = mHistoryDao.getRecentProgramIds();
            callback.onResult(recentProgramIds);
        });
    }

    /**
     * ✅ Lấy tiến độ hành trình của một programId
     * Tính: thời gian tập, calories, số ngày hoàn thành
     */
    public void getJourneyProgress(int programId, Callback<JourneyProgressResult> callback) {
        HistoryDatabase.databaseWriteExecutor.execute(() -> {
            // Lấy tất cả history của programId
            List<HistoryItem> programHistory = mHistoryDao.getHistoryByProgramId(programId);

            int totalDaysCompleted = 0;
            long totalDuration = 0;
            int totalCalories = 0;
            long firstCompletedDate = 0;
            long lastCompletedDate = 0;
            String programName = "";

            if (programHistory != null && !programHistory.isEmpty()) {
                totalDaysCompleted = programHistory.size();

                for (HistoryItem item : programHistory) {
                    if (item != null) {
                        totalDuration += item.getDuration();
                        totalCalories += item.getCalories();
                        programName = item.getName();

                        // ✅ Sửa: Dùng getStartTime() để lấy timestamp chính xác
                        long itemStartTime = item.getStartTime();

                        // Lấy ngày đầu và cuối
                        if (firstCompletedDate == 0 || itemStartTime < firstCompletedDate) {
                            firstCompletedDate = itemStartTime;
                        }
                        if (itemStartTime > lastCompletedDate) {
                            lastCompletedDate = itemStartTime;
                        }
                    }
                }
            }

            // Tính trung bình
            float averageDuration = totalDaysCompleted > 0 ? (float) totalDuration / totalDaysCompleted : 0;
            float averageCalories = totalDaysCompleted > 0 ? (float) totalCalories / totalDaysCompleted : 0;

            // Giả sử mỗi chương trình có khoảng 30 ngày (có thể lấy từ ProgramObject)
            int totalDaysInProgram = 30;
            float completionPercentage = (totalDaysInProgram > 0)
                    ? (totalDaysCompleted * 100.0f / totalDaysInProgram)
                    : 0;

            JourneyProgressResult result = new JourneyProgressResult(
                    programId,
                    programName,
                    totalDaysCompleted,
                    totalDaysInProgram,
                    totalDuration,
                    totalCalories,
                    averageDuration,
                    averageCalories,
                    firstCompletedDate,
                    lastCompletedDate,
                    completionPercentage
            );

            callback.onResult(result);
        });
    }
    /**
     * ✅ Lấy xu hướng calories trong một khoảng thời gian
     * So sánh calories của khoảng thời gian hiện tại với khoảng thời gian trước đó
     *
     * @param timeRangeMillis Khoảng thời gian (ms) để tính trend (ví dụ: 7 ngày, 30 ngày)
     * @param callback Callback trả về CaloriesTrendResult
     */
    public void getCaloriesTrend(long timeRangeMillis, Callback<CaloriesTrendResult> callback) {
        HistoryDatabase.databaseWriteExecutor.execute(() -> {
            long now = System.currentTimeMillis();

            // Lấy dữ liệu khoảng thời gian hiện tại
            long currentStartTime = now - timeRangeMillis;
            List<HistoryItem> currentItems = mHistoryDao.getHistoryInRange(currentStartTime, now);

            int currentTotalCalories = 0;
            if (currentItems != null) {
                for (HistoryItem item : currentItems) {
                    if (item != null) {
                        currentTotalCalories += item.getCalories();
                    }
                }
            }

            // Lấy dữ liệu khoảng thời gian trước đó
            long previousStartTime = currentStartTime - timeRangeMillis;
            List<HistoryItem> previousItems = mHistoryDao.getHistoryInRange(previousStartTime, currentStartTime);

            int previousTotalCalories = 0;
            if (previousItems != null) {
                for (HistoryItem item : previousItems) {
                    if (item != null) {
                        previousTotalCalories += item.getCalories();
                    }
                }
            }

            // Tính xu hướng (trend)
            int caloriesTrend = currentTotalCalories - previousTotalCalories;

            // Tính phần trăm thay đổi
            float caloriesTrendPercent = (previousTotalCalories != 0)
                    ? (caloriesTrend * 100.0f / previousTotalCalories)
                    : 0;

            CaloriesTrendResult result = new CaloriesTrendResult(currentTotalCalories, caloriesTrend, caloriesTrendPercent);
            callback.onResult(result);
        });
    }

    /**
     * ✅ Lấy dữ liệu calories hàng ngày cho biểu đồ trong khoảng thời gian
     * Nhóm calories theo từng ngày
     *
     * @param timeRangeMillis Khoảng thời gian (ms) để lấy dữ liệu (ví dụ: 7 ngày, 30 ngày)
     * @param callback Callback trả về một Map với key là ngày (yyyy-MM-dd) và value là tổng calories
     */
    public void getCaloriesChartData(long timeRangeMillis, Callback<java.util.Map<String, Integer>> callback) {
        HistoryDatabase.databaseWriteExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            long startTime = now - timeRangeMillis;

            List<HistoryItem> items = mHistoryDao.getHistoryInRange(startTime, now);

            java.util.Map<String, Integer> caloriesByDay = new java.util.LinkedHashMap<>();

            if (items != null) {
                for (HistoryItem item : items) {
                    if (item == null) continue;

                    // Lấy ngày từ startTime của item
                    long itemDate = item.getStartTime();
                    String dayString = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            .format(new java.util.Date(itemDate));

                    // Cộng calories vào từng ngày
                    int currentCalories = caloriesByDay.getOrDefault(dayString, 0);
                    caloriesByDay.put(dayString, currentCalories + item.getCalories());
                }
            }

            callback.onResult(caloriesByDay);
        });
    }
}
