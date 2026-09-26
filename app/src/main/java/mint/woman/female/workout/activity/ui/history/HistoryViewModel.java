package mint.woman.female.workout.activity.ui.history;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import mint.woman.female.workout.model.HistoryItem;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.model.UserObject;
import mint.woman.female.workout.utils.HistoryRepository;
import mint.woman.female.workout.utils.JourneyProgressResult;
import mint.woman.female.workout.utils.TodayWorkoutResult;
import mint.woman.female.workout.utils.UserRepository;
import mint.woman.female.workout.utils.UserStreakResult;
import mint.woman.female.workout.utils.WeeklyProgressResult;
import mint.woman.female.workout.utils.WeightTrendResult;
import mint.woman.female.workout.utils.CaloriesTrendResult;

import java.util.List;
import java.util.Map;

public class HistoryViewModel extends AndroidViewModel {
    private final HistoryRepository mHistoryRepository;
    private final UserRepository mUserRepository;
    private final MutableLiveData<UserStreakResult> mUserStreak = new MutableLiveData<>();
    private final MutableLiveData<Integer> mAvgDuration = new MutableLiveData<>(0);
    private final MutableLiveData<WeightTrendResult> mWeightTrend7Days = new MutableLiveData<>();
    private final MutableLiveData<WeightTrendResult> mWeightTrend30Days = new MutableLiveData<>();
    private final MutableLiveData<WeightTrendResult> mWeightTrend90Days = new MutableLiveData<>();
    private final MutableLiveData<CaloriesTrendResult> mCaloriesTrend7Days = new MutableLiveData<>();
    private final MutableLiveData<CaloriesTrendResult> mCaloriesTrend30Days = new MutableLiveData<>();
    private final MutableLiveData<CaloriesTrendResult> mCaloriesTrend90Days = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> mCaloriesChartData7Days = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> mCaloriesChartData30Days = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> mCaloriesChartData90Days = new MutableLiveData<>();
    private final MutableLiveData<TodayWorkoutResult> mTodayWorkout = new MutableLiveData<>();
    private final MutableLiveData<ProgramObject> suggestProgram = new MutableLiveData<>();
    private final MutableLiveData<WeeklyProgressResult> mWeeklyProgress = new MutableLiveData<>();
    private final MutableLiveData<List<Integer>> mRecentProgramIds = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mWeightChanged = new MutableLiveData<>();
    private final MutableLiveData<JourneyProgressResult> mJourneyProgress = new MutableLiveData<>();

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        mHistoryRepository = new HistoryRepository(application);
        mUserRepository = new UserRepository(application);

        refreshUserStreak();
        refreshAvgDuration(); //
        refreshRecentProgramIds();
    }

    public LiveData<List<HistoryItem>> getAllHistory() {
        return mHistoryRepository.getAllHistory();
    }
    public LiveData<List<HistoryItem>> findFromTo(long from, long end) {
        return mHistoryRepository.findFromTo(from, end);
    }

    public LiveData<List<HistoryItem>> findInDay(long day){
        return mHistoryRepository.findInDay(day);
    }
    public void insertHistory(HistoryItem historyItem) {
        mHistoryRepository.insert(historyItem);

        refreshUserStreak();
        refreshAvgDuration(); //
        refreshRecentProgramIds();
    }
    public void setSuggestProgram(ProgramObject programObject){
        Log.d("🎯 SuggestProgram", "setSuggestProgram() called with: " +
              (programObject != null ? "programId=" + programObject.getId() + ", name=" + programObject.getName() : "NULL"));
        suggestProgram.postValue(programObject);
    }

    // ...existing code...

    public LiveData<ProgramObject> getSuggestProgram(){
        Log.d("🎯 SuggestProgram", "getSuggestProgram() called - Current value: " +
              (suggestProgram.getValue() != null ? suggestProgram.getValue().getName() : "NULL"));
        return suggestProgram;
    }


    public void deleteAllHistory() {
        mHistoryRepository.deleteAllHistory();
    }

    public LiveData<List<UserObject>> getAllUsers() {
        return mUserRepository.getAllUsers();
    }

    public void insertUser(UserObject userObject) {
        mUserRepository.insert(userObject);
    }

    /**
     * Xóa tất cả dữ liệu UserObject
     */
    public void deleteAllUsers() {
        mUserRepository.deleteAllUsers();
    }

    /**
     * Xóa UserObject theo ngày cụ thể
     * @param date ngày cần xóa (milliseconds)
     */
    public void deleteUserByDate(long date) {
        mUserRepository.deleteUserByDate(date);
    }

    /**
     * Xóa UserObject trong khoảng thời gian
     * @param startDate ngày bắt đầu
     * @param endDate ngày kết thúc
     */
    public void deleteUsersInRange(long startDate, long endDate) {
        mUserRepository.deleteUsersInRange(startDate, endDate);
    }

    /**
     * Xóa UserObject cũ hơn N ngày
     * @param days số ngày từ hôm nay
     */
    public void deleteUsersBefore(int days) {
        mUserRepository.deleteUsersBefore(days);
    }

    public LiveData<Integer> getAvgDuration() {
        return mAvgDuration;
    }

    public LiveData<UserStreakResult> getUserStreak() {
        return mUserStreak;
    }

    public void refreshUserStreak() {
        mHistoryRepository.getUserStreak(result -> {
            mUserStreak.postValue(result);
        });
    }

    public void refreshAvgDuration() {
        mHistoryRepository.getAvgDuration(10, result -> {
            mAvgDuration.postValue(result);
        });
    }

    public LiveData<WeightTrendResult> getWeightTrend7Days() {
        return mWeightTrend7Days;
    }

    public LiveData<WeightTrendResult> getWeightTrend30Days() {
        return mWeightTrend30Days;
    }

    public LiveData<WeightTrendResult> getWeightTrend90Days() {
        return mWeightTrend90Days;
    }

    public LiveData<CaloriesTrendResult> getCaloriesTrend7Days() {
        return mCaloriesTrend7Days;
    }

    public LiveData<CaloriesTrendResult> getCaloriesTrend30Days() {
        return mCaloriesTrend30Days;
    }

    public LiveData<CaloriesTrendResult> getCaloriesTrend90Days() {
        return mCaloriesTrend90Days;
    }

    public LiveData<Map<String, Integer>> getCaloriesChartData7Days() {
        return mCaloriesChartData7Days;
    }

    public LiveData<Map<String, Integer>> getCaloriesChartData30Days() {
        return mCaloriesChartData30Days;
    }

    public LiveData<Map<String, Integer>> getCaloriesChartData90Days() {
        return mCaloriesChartData90Days;
    }

    public void refreshWeightTrends() {
        mUserRepository.getWeightTrend(7, result -> {
            mWeightTrend7Days.postValue(result);
        });
        mUserRepository.getWeightTrend(30, result -> {
            mWeightTrend30Days.postValue(result);
        });
        mUserRepository.getWeightTrend(90, result -> {
            mWeightTrend90Days.postValue(result);
        });
    }

    public void refreshCaloriesTrends() {
        long sevenDaysMillis = java.util.concurrent.TimeUnit.DAYS.toMillis(7);
        long thirtyDaysMillis = java.util.concurrent.TimeUnit.DAYS.toMillis(30);
        long ninetyDaysMillis = java.util.concurrent.TimeUnit.DAYS.toMillis(90);

        mHistoryRepository.getCaloriesTrend(sevenDaysMillis, result -> {
            mCaloriesTrend7Days.postValue(result);
        });
        mHistoryRepository.getCaloriesTrend(thirtyDaysMillis, result -> {
            mCaloriesTrend30Days.postValue(result);
        });
        mHistoryRepository.getCaloriesTrend(ninetyDaysMillis, result -> {
            mCaloriesTrend90Days.postValue(result);
        });
    }

    public void refreshCaloriesChartData() {
        long sevenDaysMillis = java.util.concurrent.TimeUnit.DAYS.toMillis(7);
        long thirtyDaysMillis = java.util.concurrent.TimeUnit.DAYS.toMillis(30);
        long ninetyDaysMillis = java.util.concurrent.TimeUnit.DAYS.toMillis(90);

        mHistoryRepository.getCaloriesChartData(sevenDaysMillis, result -> {
            mCaloriesChartData7Days.postValue(result);
        });
        mHistoryRepository.getCaloriesChartData(thirtyDaysMillis, result -> {
            mCaloriesChartData30Days.postValue(result);
        });
        mHistoryRepository.getCaloriesChartData(ninetyDaysMillis, result -> {
            mCaloriesChartData90Days.postValue(result);
        });
    }

    public LiveData<TodayWorkoutResult> getTodayWorkout() {
        return mTodayWorkout;
    }

    public void refreshTodayWorkout() {
        mHistoryRepository.getTodayWorkoutStats(result -> {
            mTodayWorkout.postValue(result);
        });
    }

    public LiveData<WeeklyProgressResult> getWeeklyProgress() {
        return mWeeklyProgress;
    }

    public void refreshWeeklyProgress(int goalDays, int firstDayOfWeek) {
        mHistoryRepository.getWeeklyProgress(goalDays, firstDayOfWeek, result -> {
            mWeeklyProgress.postValue(result);
        });
    }

    public LiveData<List<Integer>> getRecentProgramIds() {
        return mRecentProgramIds;
    }

    public void refreshRecentProgramIds() {
        mHistoryRepository.getRecentProgramIds(result -> {
            mRecentProgramIds.postValue(result);
        });
    }

    public LiveData<Boolean> getWeightChanged() {
        return mWeightChanged;
    }

    public void notifyWeightChanged() {
        mWeightChanged.setValue(true);
    }

    public LiveData<JourneyProgressResult> getJourneyProgress() {
        return mJourneyProgress;
    }

    /**
     * ✅ Refresh journey progress cho một programId
     * @param programId ID của chương trình
     */
    public void refreshJourneyProgress(int programId) {
        mHistoryRepository.getJourneyProgress(programId, result -> {
            mJourneyProgress.postValue(result);
        });
    }
}
