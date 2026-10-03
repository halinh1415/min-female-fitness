package mint.woman.female.workout.activity.ui.report

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.badge.BadgeActivity
import mint.woman.female.workout.activity.ui.compose.MinTheme
import mint.woman.female.workout.activity.ui.history.HistoryViewModel
import mint.woman.female.workout.badge.BadgeBoard
import mint.woman.female.workout.badge.BadgeFormatter
import mint.woman.female.workout.badge.BadgeRepository
import mint.woman.female.workout.custom.DialogWeight
import mint.woman.female.workout.model.ProgramObject
import mint.woman.female.workout.notification.AppNotificationHelper
import mint.woman.female.workout.notification.NotificationPermissionDialog
import mint.woman.female.workout.utils.CaloriesTrendResult
import mint.woman.female.workout.utils.MyPreference
import mint.woman.female.workout.utils.WeightTrendResult
import kotlin.math.abs

/**
 * Màn Report viết bằng Jetpack Compose.
 *
 * Dữ liệu vẫn lấy từ [HistoryViewModel] như bản cũ; biểu đồ cân nặng/calo vẽ bằng Canvas
 * (có hiệu ứng) thay cho MPAndroidChart.
 */
class ReportFragment : Fragment(), ReportActions {

    private lateinit var pref: MyPreference
    private lateinit var historyViewModel: HistoryViewModel
    private val state = ReportUiState()
    private var suggestion: ProgramObject? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { MinTheme { ReportScreen(state, this@ReportFragment) } }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pref = MyPreference.newInstance(requireContext())
        historyViewModel = ViewModelProvider(requireActivity())[HistoryViewModel::class.java]

        historyViewModel.refreshWeightTrends()
        historyViewModel.refreshCaloriesTrends()
        historyViewModel.refreshCaloriesChartData()
        historyViewModel.refreshTodayWorkout()
        historyViewModel.refreshWeeklyProgress(pref.goalOfWeek, pref.firstDayOfWeek)

        val owner = viewLifecycleOwner
        historyViewModel.userStreak.observe(owner) { streak ->
            if (streak == null) return@observe
            state.streakDays = streak.streakDays
            state.streakTargetText = getString(R.string.streak_x_day_target, streak.streakDays + 1)
        }
        historyViewModel.todayWorkout.observe(owner) { today ->
            if (today == null) return@observe
            state.todayWorkouts = today.workoutCount
            state.todayCalories = today.totalCalories
            state.todayMinutes = today.totalDuration / 60
            state.todayWorkoutsTrend = today.workoutCountTrendPercent
            state.todayCaloriesTrend = today.caloriesTrendPercent
            state.todayMinutesTrend = today.durationTrendPercent
        }
        historyViewModel.weeklyProgress.observe(owner) { week ->
            if (week == null) return@observe
            state.weekDone = week.completedDays
            state.weekGoal = week.goalDays
            state.weekCalories = week.totalCalories
            state.weekMinutes = week.totalDuration / 60
            state.weekCaloriesTrend = week.caloriesTrendPercent
            state.weekMinutesTrend = week.durationTrendPercent
        }

        historyViewModel.allUsers.observe(owner) { users ->
            state.weights = users.orEmpty()
                .map { WeightPoint(it.date, if (pref.isKg) it.weight else it.lbs) }
                .sortedBy { it.time }
        }
        historyViewModel.weightTrend7Days.observe(owner) { putWeightTrend(7, it) }
        historyViewModel.weightTrend30Days.observe(owner) { putWeightTrend(30, it) }
        historyViewModel.weightTrend90Days.observe(owner) { putWeightTrend(90, it) }

        historyViewModel.caloriesChartData7Days.observe(owner) { putCalories(7, it) }
        historyViewModel.caloriesChartData30Days.observe(owner) { putCalories(30, it) }
        historyViewModel.caloriesChartData90Days.observe(owner) { putCalories(90, it) }
        historyViewModel.caloriesTrend7Days.observe(owner) { putCaloriesTrend(7, it) }
        historyViewModel.caloriesTrend30Days.observe(owner) { putCaloriesTrend(30, it) }
        historyViewModel.caloriesTrend90Days.observe(owner) { putCaloriesTrend(90, it) }

        historyViewModel.suggestProgram.observe(owner) { program ->
            suggestion = program
            state.suggestionText = program?.let {
                it.name + " - " + getString(R.string.x_minutes, it.total)
            }.orEmpty()
        }
        historyViewModel.weightChanged.observe(owner) { changed ->
            if (changed == true) {
                updateBody()
                historyViewModel.refreshWeightTrends()
            }
        }
        updateBody()
    }

    override fun onResume() {
        super.onResume()
        // Tính lại huy hiệu mỗi lần quay lại màn – người dùng có thể vừa tập xong.
        BadgeRepository.load(requireContext()) { board -> bindBadges(board) }
    }

    // ------------------------------------------------------------ dữ liệu

    private fun updateBody() {
        state.weightUnit = pref.weightUnit
        state.currentWeight = pref.currentWeight
        val heightM = pref.currentHeightM
        var target = heightM * heightM * 22.9f
        if (!pref.isKg) target *= 2.20462f
        state.targetWeight = target

        val bmi = if (heightM > 0f) pref.currentWeightKG / (heightM * heightM) else 0f
        state.bmi = bmi
        state.bmiLabel = getString(
            when {
                bmi < 18.5f -> R.string.txt_underweight
                bmi < 25f -> R.string.txt_normal
                bmi < 30f -> R.string.txt_overweight
                bmi < 35f -> R.string.txt_obese_class_1
                bmi < 40f -> R.string.txt_obese_class_2
                else -> R.string.txt_obese_class_3
            }
        )
    }

    private fun putWeightTrend(days: Int, result: WeightTrendResult?) {
        if (result == null) return
        val unit = pref.weightUnit
        val text = when (result.trend) {
            // chuỗi đã có sẵn dấu +/-, nên truyền giá trị tuyệt đối
            "UP" -> getString(R.string.up_weight_trend, abs(result.weightChange), unit, days)
            "DOWN" -> getString(R.string.down_weight_trend, abs(result.weightChange), unit, days)
            else -> stableText(days)
        }
        state.weightTrendText = state.weightTrendText + (days to text)
    }

    private fun putCalories(days: Int, data: Map<String, Int>?) {
        state.caloriesData = state.caloriesData + (days to data.orEmpty().map { it.key to it.value })
    }

    private fun putCaloriesTrend(days: Int, result: CaloriesTrendResult?) {
        if (result == null) return
        val text = when {
            result.caloriesTrend > 0 ->
                getString(R.string.up_calories_trend, result.caloriesTrend, days, result.caloriesTrendPercent)
            result.caloriesTrend < 0 ->
                getString(R.string.down_calories_trend, abs(result.caloriesTrend), days, abs(result.caloriesTrendPercent))
            else -> stableText(days)
        }
        state.caloriesTrendText = state.caloriesTrendText + (days to text)
    }

    private fun stableText(days: Int): String = getString(
        when (days) {
            30 -> R.string.stable_over_past_30d
            90 -> R.string.stable_over_past_90d
            else -> R.string.stable_over_past_7d
        }
    )

    private fun bindBadges(board: BadgeBoard) {
        if (!isAdded) return
        state.badgeTotal = board.totalCount
        state.badgeUnlocked = board.unlockedCount
        state.badgePercent = board.percent
        val next = board.closestLockedBadge
        state.nextBadge = next
        state.nextBadgeRemaining = next?.let { BadgeFormatter.remaining(requireContext(), it) }.orEmpty()
    }

    // ------------------------------------------------------------ hành động

    override fun onEditWeight() {
        DialogWeight().show(parentFragmentManager, "enter_weight")
    }

    override fun onOpenBadges() {
        startActivity(BadgeActivity.newIntent(requireContext()))
    }

    /** Lên lịch nhắc tập buổi được gợi ý vào ngày mai (chỉ tạo nhắc nhở). */
    override fun onScheduleNext() {
        val program = suggestion ?: return
        if (!hasNotificationPermission()) return
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.schedule_reminder_title))
            .setMessage(
                getString(R.string.schedule_reminder_message, program.name, program.total, state.streakDays)
            )
            .setPositiveButton(getString(R.string.schedule_pick_time)) { dialog, _ ->
                dialog.dismiss()
                AppNotificationHelper.scheduleNotificationWithTimePicker(
                    requireContext(), program.name, program.total
                ) { hour, minute ->
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.schedule_time_selected_toast, hour, minute),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(getString(R.string.schedule_default_time_button)) { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(requireContext(), getString(R.string.schedule_default_time_toast), Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            NotificationPermissionDialog.showPracticeReminderDialog(requireActivity())
            return false
        }
        return true
    }
}
