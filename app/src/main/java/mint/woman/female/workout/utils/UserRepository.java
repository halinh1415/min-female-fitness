package mint.woman.female.workout.utils;

import android.app.Application;

import androidx.lifecycle.LiveData;

import mint.woman.female.workout.model.UserObject;

import java.util.List;

public class UserRepository {
    private UserDao userDao;
    private LiveData<List<UserObject>> mAllUsers;

    public UserRepository(Application application) {
        UserDatabase database = UserDatabase.getDatabase(application);
        userDao = database.userDao();
        mAllUsers = userDao.getAllUser();
    }

    public LiveData<List<UserObject>> getAllUsers() {
        return mAllUsers;
    }

    public void insert(UserObject userObject) {
        UserDatabase.databaseWriteExecutor.execute(() -> {
            userDao.insert(userObject);
        });
    }

    /**
     * Xóa tất cả dữ liệu UserObject
     */
    public void deleteAllUsers() {
        UserDatabase.databaseWriteExecutor.execute(() -> {
            userDao.deleteAllUsers();
        });
    }

    /**
     * Xóa UserObject theo ngày cụ thể
     */
    public void deleteUserByDate(long date) {
        UserDatabase.databaseWriteExecutor.execute(() -> {
            userDao.deleteUserByDate(date);
        });
    }

    /**
     * Xóa UserObject trong khoảng thời gian
     */
    public void deleteUsersInRange(long startDate, long endDate) {
        UserDatabase.databaseWriteExecutor.execute(() -> {
            userDao.deleteUsersInRange(startDate, endDate);
        });
    }

    /**
     * Xóa UserObject cũ hơn N ngày
     */
    public void deleteUsersBefore(int days) {
        UserDatabase.databaseWriteExecutor.execute(() -> {
            long cutoffDate = System.currentTimeMillis() - (days * 24L * 60L * 60L * 1000L);
            userDao.deleteUsersBefore(cutoffDate);
        });
    }

    public void getWeightTrend(int days, Callback<WeightTrendResult> callback) {
        UserDatabase.databaseWriteExecutor.execute(() -> {
            long endDate = System.currentTimeMillis();
            long startDate = endDate - (days * 24L * 60L * 60L * 1000L);

            java.util.List<UserObject> users = userDao.getUsersInRange(startDate, endDate);
            WeightTrendResult result = calculateTrend(users, startDate, endDate);
            callback.onResult(result);
        });
    }

    private WeightTrendResult calculateTrend(java.util.List<UserObject> users, long startDate, long endDate) {
        if (users == null || users.isEmpty()) {
            return new WeightTrendResult(0, 0, "NO_DATA", startDate, endDate, 0, 0);
        }

        float startWeight = users.get(0).getWeight();
        float endWeight = users.get(users.size() - 1).getWeight();
        float weightChange = endWeight - startWeight;
        float percentChange = (startWeight != 0) ? (weightChange / startWeight) * 100 : 0;

        String trend;
        if (weightChange > 0.5f) {
            trend = "UP";
        } else if (weightChange < -0.5f) {
            trend = "DOWN";
        } else {
            trend = "STABLE";
        }

        return new WeightTrendResult(weightChange, percentChange, trend, startDate, endDate, startWeight, endWeight);
    }

    public interface Callback<T> {
        void onResult(T data);
    }
}
