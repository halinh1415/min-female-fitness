package mint.woman.female.workout.utils;


import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import mint.woman.female.workout.model.HistoryItem;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


// --- ĐÃ CẬP NHẬT: Tăng phiên bản database lên 4 ---
@Database(entities = {HistoryItem.class}, version = 4, exportSchema = false)
public abstract class HistoryDatabase extends RoomDatabase {

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE HistoryItem ADD program TEXT DEFAULT 'null'");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Migration 2-3 không làm gì, có thể vì bạn đã thêm cột 'program' ở 1-2
            // nhưng quên tăng version hoặc thay đổi Entity.
            // Nếu bạn đã thực sự thêm cột 'program' ở phiên bản 2,
            // và bây giờ bạn muốn thêm 'programObject' ở version 3,
            // thì migration này cần tạo cột 'programObject' và di chuyển dữ liệu nếu cần.
            // Tuy nhiên, vì mục tiêu hiện tại là sửa 'dateId', chúng ta cứ giữ nguyên như bạn đã định nghĩa.
        }
    };

    // --- ĐÃ THÊM: Migration mới để xử lý cột dateId ---
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Lấy tất cả các bản ghi hiện có từ bảng HistoryItem
            // và cập nhật cột 'dateId' của chúng
            Cursor cursor = database.query("SELECT id, dateId FROM HistoryItem");

            if (cursor != null) {
                try {
                    int idColumnIndex = cursor.getColumnIndexOrThrow("id");
                    int dateIdColumnIndex = cursor.getColumnIndexOrThrow("dateId");

                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(idColumnIndex);
                        long oldDateId = cursor.getLong(dateIdColumnIndex); // oldDateId là số ngày từ Epoch UTC

                        // Chuyển đổi oldDateId sang mili giây của 00:00:00 theo múi giờ địa phương
                        long newDateStartOfDayMillis = convertOldDateIdToNewFormat(oldDateId);

                        // Cập nhật cột 'dateId' trong bảng với giá trị mới
                        database.execSQL("UPDATE HistoryItem SET dateId = ? WHERE id = ?",
                                new Object[]{newDateStartOfDayMillis, id});
                    }
                } finally {
                    cursor.close();
                }
            }
        }

        /**
         * Chuyển đổi giá trị 'dateId' cũ (số ngày từ Epoch UTC)
         * sang mili giây của 00:00:00 của ngày đó theo múi giờ địa phương.
         * @param oldDateId Số ngày từ Epoch UTC.
         * @return Mili giây của 00:00:00 của ngày địa phương tương ứng.
         */
        private long convertOldDateIdToNewFormat(long oldDateId) {
            // 1. Chuyển số ngày từ Epoch UTC thành mili giây từ Epoch UTC
            long millisFromEpochUtc = TimeUnit.DAYS.toMillis(oldDateId);

            // 2. Tạo một đối tượng Calendar sử dụng múi giờ mặc định của thiết bị.
            Calendar localCalendar = Calendar.getInstance();

            // 3. Đặt thời gian của Calendar này bằng mili giây từ Epoch UTC.
            //    Calendar sẽ tự động điều chỉnh để hiển thị ngày và giờ chính xác
            //    trong múi giờ địa phương của thiết bị.
            localCalendar.setTimeInMillis(millisFromEpochUtc);

            // 4. Chuẩn hóa Calendar về 00:00:00 của ngày đó trong múi giờ địa phương.
            localCalendar.set(Calendar.HOUR_OF_DAY, 0);
            localCalendar.set(Calendar.MINUTE, 0);
            localCalendar.set(Calendar.SECOND, 0);
            localCalendar.set(Calendar.MILLISECOND, 0);

            // 5. Trả về mili giây của 00:00:00 của ngày địa phương đã chuẩn hóa.
            return localCalendar.getTimeInMillis();
        }
    };

    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static volatile HistoryDatabase INSTANCE;

    public static HistoryDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (HistoryDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    HistoryDatabase.class, "history_database")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4) // --- ĐÃ CẬP NHẬT: Thêm MIGRATION_3_4 ---
                            // .fallbackToDestructiveMigration() // KHÔNG DÙNG DÒNG NÀY khi bạn có migration
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract HistoryDao historyDao();

}
