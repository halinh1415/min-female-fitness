package mint.woman.female.workout.notification;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class DailyNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AppNotificationHelper.createChannel(context);

        PracticeStats stats = PracticeStatsStore.get(context);

        // Nếu user đã tập hôm nay -> skip
        if (stats != null && stats.isDidCompleteToday()) {
            DailyNotificationScheduler.scheduleAt19(context);
            return;
        }

        NotificationContent content = buildContent(context, stats);

        // schedule lại cho ngày mai
        DailyNotificationScheduler.scheduleAt19(context);

        // Dựng thông báo (ảnh, DB) ở thread nền.
        PendingResult pendingResult = goAsync();
        AppNotificationHelper.showNotificationAsync(context, content, pendingResult::finish);
    }

    private NotificationContent buildContent(Context context, PracticeStats stats) {
        if (stats == null) {
            return new NotificationContent(
                    NotificationType.DAILY_REMINDER,
                    "Đến giờ luyện tập rồi",
                    "Hôm nay mình tiếp tục một bài ngắn nhé.",
                    "app://home/discover"
            );
        }

        // streak cao -> message riêng
        if (stats.getStreakCount() >= 7) {
            return new NotificationContent(
                    NotificationType.STREAK_PROTECT,
                    "Giữ chuỗi " + stats.getStreakCount() + " ngày hôm nay",
                    "Bạn đang duy trì rất tốt. Chỉ cần 1 bài ngắn để giữ chuỗi.",
                    "app://practice/quick"
            );
        }

        // missed 1 ngày
        if (stats.getMissedDays() == 1) {
            return new NotificationContent(
                    NotificationType.GENTLE_COMEBACK,
                    "Quay lại nhẹ nhàng hôm nay nhé",
                    "Không sao nếu lỡ 1 ngày. Mình đã chuẩn bị một bài ngắn cho bạn.",
                    "app://practice/comeback"
            );
        }

        // missed 2-4 ngày
        if (stats.getMissedDays() >= 2 && stats.getMissedDays() <= 4) {
            return new NotificationContent(
                    NotificationType.REENGAGE_LIGHT,
                    "Bắt đầu lại thật nhẹ",
                    "Hôm nay thử 7 phút thôi, không cần quá sức.",
                    "app://practice/comeback"
            );
        }

        // active nhiều ngày -> recovery
        if (stats.getConsecutiveActiveDays() >= 6) {
            return new NotificationContent(
                    NotificationType.RECOVERY_DAY,
                    "Hôm nay là ngày phục hồi",
                    "Bạn đã duy trì rất tốt. Thử 5 phút thở nhẹ hoặc bài thư giãn nhé.",
                    "app://practice/recovery"
            );
        }

        return new NotificationContent(
                NotificationType.DAILY_REMINDER,
                "Đến giờ luyện tập rồi",
                "Hôm nay mình tiếp tục một bài ngắn nhé.",
                "app://home/discover"
        );
    }
}