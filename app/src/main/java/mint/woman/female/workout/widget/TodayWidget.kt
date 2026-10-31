package mint.woman.female.workout.widget

import android.os.Build
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.google.firebase.analytics.FirebaseAnalytics
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.main.YogaActivity
import mint.woman.female.workout.activity.ui.plan.PersonalPlanStore
import mint.woman.female.workout.model.ProgramObject
import mint.woman.female.workout.FitnessApplication
import mint.woman.female.workout.utils.MyPreference
import mint.woman.female.workout.utils.HistoryDatabase
import mint.woman.female.workout.utils.StreakCalculator
import java.util.concurrent.Executors

/** Widget màn hình chính: chuỗi ngày tập + bài hôm nay, bấm "Bắt đầu" vào thẳng bài. */
class TodayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        TodayWidget.refresh(context.applicationContext, goAsync())
    }

    override fun onEnabled(context: Context) {
        FirebaseAnalytics.getInstance(context).logEvent("widget_added", null)
    }

    override fun onDisabled(context: Context) {
        FirebaseAnalytics.getInstance(context).logEvent("widget_removed", null)
    }
}

object TodayWidget {
    /** YogaActivity đọc extra này để mở thẳng bài (plan cá nhân hoặc bài gợi ý). */
    const val EXTRA_PROGRAM_ID = "widget_program_id"
    private const val TAG = "TodayWidget"

    private val executor = Executors.newSingleThreadExecutor()

    /** Gọi khi dữ liệu đổi (tập xong, mở app, tạo plan). Không có widget nào thì bỏ qua. */
    @JvmStatic
    fun update(context: Context) = refresh(context.applicationContext, null)

    /** Đã có widget của app trên màn hình chính chưa. */
    @JvmStatic
    fun isAdded(context: Context): Boolean = AppWidgetManager.getInstance(context)
        .getAppWidgetIds(ComponentName(context, TodayWidgetProvider::class.java)).isNotEmpty()

    /** Launcher cho ghim widget ngay từ trong app (Android 8+, tuỳ launcher). */
    @JvmStatic
    fun canPin(context: Context): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported

    /** Mở hộp thoại thêm widget của launcher (nút ở Cài đặt và màn Hoàn thành); false = launcher không hỗ trợ. */
    @JvmStatic
    fun requestPin(context: Context): Boolean {
        if (!canPin(context)) return false
        return try {
            AppWidgetManager.getInstance(context)
                .requestPinAppWidget(ComponentName(context, TodayWidgetProvider::class.java), null, null)
        } catch (e: IllegalStateException) {
            // Gọi khi app không ở foreground.
            Log.w(TAG, "requestPin failed", e)
            false
        }
    }

    internal fun refresh(context: Context, pending: BroadcastReceiver.PendingResult?) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, TodayWidgetProvider::class.java))
        if (ids.isEmpty()) {
            pending?.finish()
            return
        }
        // Đọc lịch sử (Room) và gợi ý bài: phải chạy ngoài main thread.
        executor.execute {
            try {
                manager.updateAppWidget(ids, build(context))
            } catch (e: Exception) {
                Log.w(TAG, "update failed", e)
            } finally {
                pending?.finish()
            }
        }
    }

    private fun build(context: Context): RemoteViews {
        val streak = StreakCalculator(HistoryDatabase.getDatabase(context).historyDao()).userStreakResult
        val plan = PersonalPlanStore.program(context)
        val program: ProgramObject?
        val subtitle: String
        if (plan != null) {
            val total = plan.total.coerceAtLeast(1)
            val done = MyPreference(context).getPlanProgress(plan.id)
            program = plan
            subtitle = context.getString(R.string.pp_home_title) + " · " +
                context.getString(R.string.rest_day_progress, (done + 1).coerceAtMost(total), total)
        } else {
            // Widget dùng bài Home vừa gợi ý gần nhất.
            program = suggestedProgram(context)
            subtitle = context.getString(R.string.widget_suggested)
        }

        val views = RemoteViews(context.packageName, R.layout.widget_today)
        views.setTextViewText(R.id.widget_streak_count, streak.streakDays.toString())
        // Chữ trong layout do launcher nạp theo ngôn ngữ máy; gán lại từ context của app để khớp ngôn ngữ đã chọn trong app.
        views.setTextViewText(R.id.widget_streak_label, context.getString(R.string.widget_streak_label))
        views.setTextViewText(
            R.id.widget_status,
            if (streak.didPracticeToday) context.getString(R.string.widget_done_today) else subtitle,
        )
        views.setTextViewText(R.id.widget_title, program?.name ?: context.getString(R.string.widget_name))
        views.setTextViewText(
            R.id.widget_button,
            context.getString(if (streak.didPracticeToday) R.string.widget_train_more else R.string.widget_start),
        )
        views.setViewVisibility(R.id.widget_button, if (program != null) View.VISIBLE else View.GONE)

        views.setOnClickPendingIntent(R.id.widget_root, openApp(context, null))
        program?.let { views.setOnClickPendingIntent(R.id.widget_button, openApp(context, it.id)) }
        return views
    }

    private fun openApp(context: Context, programId: Int?): PendingIntent {
        val intent = Intent(context, YogaActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        programId?.let { intent.putExtra(EXTRA_PROGRAM_ID, it) }
        return PendingIntent.getActivity(
            context,
            programId ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private const val PREFS = "today_widget"
    private const val KEY_SUGGEST = "suggest_program_id"

    /** Home gọi khi vừa tính xong bài gợi ý hôm nay (Home gợi ý bằng RecommendationService). */
    @JvmStatic
    fun saveSuggestion(context: Context, programId: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getInt(KEY_SUGGEST, -1) == programId) return
        prefs.edit().putInt(KEY_SUGGEST, programId).apply()
        update(context)
    }

    private fun suggestedProgram(context: Context): ProgramObject? {
        val id = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_SUGGEST, -1)
        if (id < 0) return null
        return runCatching { FitnessApplication.getApplication().mJsonHelper.allCategoryHashMap[id] }.getOrNull()
    }

    /** YogaActivity gọi khi mở từ nút widget, để đo tỉ lệ dùng widget. */
    @JvmStatic
    fun logOpened(context: Context, programId: Int) {
        FirebaseAnalytics.getInstance(context).logEvent("widget_start", Bundle().apply { putInt("program_id", programId) })
    }
}
