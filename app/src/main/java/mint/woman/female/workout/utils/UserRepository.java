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
}
