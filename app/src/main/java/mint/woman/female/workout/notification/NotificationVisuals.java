package mint.woman.female.workout.notification;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.activity.ui.main.YogaActivity;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.activity.ui.workout.ChallengeActivity;
import mint.woman.female.workout.model.HistoryItem;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.utils.HistoryDatabase;
import mint.woman.female.workout.utils.MyPreference;

/**
 * Phần "trang trí" của thông báo nhắc tập (giống app WomenWorkout): ảnh minh hoạ theo loại
 * thông báo, tiến độ tuần và bài tập để mở thẳng từ nút "Tập ngay".
 * Các hàm đọc DB / assets phải gọi ngoài main thread.
 */
final class NotificationVisuals {

    /** Nền hồng nhạt sau ảnh minh hoạ để ảnh trong suốt không lẫn vào nền tối của thông báo. */
    private static final int PICTURE_BG = Color.parseColor("#FDE7EF");

    private NotificationVisuals() {
    }

    /** Ảnh minh hoạ onboarding hợp với từng loại thông báo. */
    static String imageFor(NotificationType type) {
        switch (type) {
            case STREAK_PROTECT:
                return "onboards/onboard_intro_5.png";
            case RECOVERY_DAY:
                return "onboards/onboard_intro_2.png";
            case GENTLE_COMEBACK:
                return "onboards/onboard_intro_3.png";
            case REENGAGE_LIGHT:
                return "onboards/onboard_intro_4.png";
            default:
                return "onboards/onboard_intro_1.png";
        }
    }

    @Nullable
    private static Bitmap loadAsset(Context context, String path) {
        try (InputStream input = context.getAssets().open(path)) {
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            return null;
        }
    }

    /** Ảnh lớn 2:1 (BigPicture): minh hoạ đặt giữa trên nền hồng nhạt. */
    @Nullable
    static Bitmap bigPicture(Context context, NotificationType type) {
        Bitmap source = loadAsset(context, imageFor(type));
        if (source == null) return null;
        Bitmap out = Bitmap.createBitmap(1024, 512, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        canvas.drawColor(PICTURE_BG);
        drawFit(canvas, source, new RectF(0, 24, 1024, 512 - 24));
        source.recycle();
        return out;
    }

    /** Icon lớn vuông bo góc (hiện bên phải khi thông báo thu gọn). */
    @Nullable
    static Bitmap largeIcon(Context context, NotificationType type) {
        Bitmap source = loadAsset(context, imageFor(type));
        if (source == null) return null;
        int size = 256;
        Bitmap out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setColor(PICTURE_BG);
        canvas.drawRoundRect(new RectF(0, 0, size, size), 48, 48, bg);
        drawFit(canvas, source, new RectF(16, 16, size - 16, size - 16));
        source.recycle();
        return out;
    }

    private static void drawFit(Canvas canvas, Bitmap source, RectF box) {
        float scale = Math.min(box.width() / source.getWidth(), box.height() / source.getHeight());
        float w = source.getWidth() * scale;
        float h = source.getHeight() * scale;
        float left = box.centerX() - w / 2f;
        float top = box.centerY() - h / 2f;
        canvas.drawBitmap(source, new Rect(0, 0, source.getWidth(), source.getHeight()),
                new RectF(left, top, left + w, top + h), new Paint(Paint.FILTER_BITMAP_FLAG));
    }

    private static long startOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /** Số ngày đã tập trong tuần hiện tại (theo ngày bắt đầu tuần người dùng chọn). */
    @WorkerThread
    static int daysTrainedThisWeek(Context context) {
        Calendar calendar = Calendar.getInstance();
        long today = startOfDay(calendar);
        int firstDay = new MyPreference(context).getFirstDayOfWeek();
        int sinceWeekStart = (calendar.get(Calendar.DAY_OF_WEEK) - firstDay + 7) % 7;
        calendar.add(Calendar.DAY_OF_MONTH, -sinceWeekStart);
        long weekStart = startOfDay(calendar);
        try {
            List<Long> days = HistoryDatabase.getDatabase(context).historyDao().getPassedDateIdsFrom(weekStart);
            int count = 0;
            if (days != null) {
                for (Long day : days) {
                    if (day != null && day >= weekStart && day < today + 24L * 60 * 60 * 1000) count++;
                }
            }
            return count;
        } catch (RuntimeException e) {
            return 0;
        }
    }

    static int weeklyGoal(Context context) {
        return Math.max(1, new MyPreference(context).getGoalOfWeek());
    }

    /**
     * Intent mở thẳng plan cá nhân / bài tập gần nhất (bỏ qua Splash và quảng cáo); người mới thì vào màn chính.
     * Phần tử đầu luôn là màn chính để bấm back quay về app.
     */
    @WorkerThread
    static Intent[] startWorkoutIntents(Context context, NotificationType type) {
        Intent main = new Intent(context, YogaActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra(AppNotificationHelper.EXTRA_NOTIFICATION_TYPE, type.name());
        // Có plan cá nhân 28 ngày: mở thẳng plan (ngày đang tới), không thì bài tập gần nhất.
        ProgramObject program = mint.woman.female.workout.activity.ui.plan.PersonalPlanStore.program(context);
        if (program == null) program = lastProgram(context);
        if (program == null) return new Intent[]{main};

        Bundle bundle = new Bundle();
        bundle.putInt("PROGRAM_ID", program.getId());
        Intent target;
        if (program.getType() == 1) {
            target = new Intent(context, ChallengeActivity.class);
        } else if (program.getType() == 2) {
            target = new Intent(context, PreviewActivity.class);
            bundle.putInt("DAY_NUMBER", 0);
        } else {
            return new Intent[]{main};
        }
        target.putExtras(bundle);
        return new Intent[]{main, target};
    }

    @Nullable
    private static ProgramObject lastProgram(Context context) {
        try {
            List<HistoryItem> history = HistoryDatabase.getDatabase(context).historyDao().getRecentPassedHistory(1);
            if (history == null || history.isEmpty() || history.get(0).getProgramId() <= 0) return null;
            return FitnessApplication.getApplication().mJsonHelper.getAllCategoryHashMap()
                    .get(history.get(0).getProgramId());
        } catch (RuntimeException e) {
            return null;
        }
    }
}
