package mint.woman.female.workout.utils;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import mint.woman.female.workout.model.UserObject;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Database(entities = {UserObject.class}, version = 2, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static volatile UserDatabase INSTANCE;

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Lấy tất cả các bản ghi hiện có từ bảng UserObject
            // Chúng ta cần cột 'date' vì nó là khóa chính VÀ là cột cần được cập nhật
            Cursor cursor = database.query("SELECT date FROM UserObject"); // Chỉ lấy cột 'date'

            if (cursor != null) {
                try {
                    int dateColumnIndex = cursor.getColumnIndexOrThrow("date");

                    while (cursor.moveToNext()) {
                        long oldDateValue = cursor.getLong(dateColumnIndex); // oldDateValue là số ngày từ Epoch UTC

                        // Chuyển đổi oldDateValue sang mili giây của 00:00:00 theo múi giờ địa phương
                        long newDateStartOfDayMillis = convertOldDateValueToNewFormat(oldDateValue);

                        // Do đó, cách UPDATE trực tiếp này khả thi:
                        database.execSQL("UPDATE UserObject SET date = ? WHERE date = ?",
                                new Object[]{newDateStartOfDayMillis, oldDateValue});
                    }
                } finally {
                    cursor.close();
                }
            }
        }

        private long convertOldDateValueToNewFormat(long oldDateValue) {
            long millisFromEpochUtc = TimeUnit.DAYS.toMillis(oldDateValue);
            Calendar localCalendar = Calendar.getInstance();
            localCalendar.setTimeInMillis(millisFromEpochUtc);
            localCalendar.set(Calendar.HOUR_OF_DAY, 0);
            localCalendar.set(Calendar.MINUTE, 0);
            localCalendar.set(Calendar.SECOND, 0);
            localCalendar.set(Calendar.MILLISECOND, 0);
            return localCalendar.getTimeInMillis();
        }
    };

    public static UserDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (UserDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    UserDatabase.class, "user_database")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract UserDao userDao();
}
