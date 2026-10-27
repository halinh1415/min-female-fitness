package mint.woman.female.workout.activity.ui.home

import mint.woman.female.workout.recommend.RecommendationService
import mint.woman.female.workout.widget.TodayWidget
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import mint.woman.female.workout.FitnessApplication
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ExploreDetailActivity
import mint.woman.female.workout.activity.ui.compose.MinTheme
import mint.woman.female.workout.activity.ui.history.HistoryActivity
import mint.woman.female.workout.activity.ui.plan.PersonalPlanActivity
import mint.woman.female.workout.activity.ui.plan.PersonalPlanStore
import mint.woman.female.workout.activity.ui.history.HistoryViewModel
import mint.woman.female.workout.activity.ui.premium.DiscountDialogFragment
import mint.woman.female.workout.activity.ui.premium.PaywallPromo
import mint.woman.female.workout.activity.ui.premium.PremiumViewModel
import mint.woman.female.workout.activity.ui.premium.PremiumActivity
import mint.woman.female.workout.activity.ui.preview.PreviewActivity
import mint.woman.female.workout.activity.ui.weekgoal.SetGoalActivity
import mint.woman.female.workout.activity.ui.workout.ChallengeActivity
import mint.woman.female.workout.model.HistoryItem
import mint.woman.female.workout.model.ProgramObject
import mint.woman.female.workout.utils.HistoryDatabase
import mint.woman.female.workout.utils.MyPreference
import mint.woman.female.workout.utils.StreakMessageType
import mint.woman.female.workout.utils.UserStreakResult
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Màn Home viết bằng Jetpack Compose.
 *
 * Giữ nguyên dữ liệu và luồng của bản cũ (HomeFragment + WeekGoalNewFragment):
 * gợi ý bài tập từ [RecommendationService], mục tiêu tuần từ [HistoryViewModel],
 * các nhóm chương trình cố định theo id.
 */
class HomeFragment : Fragment(), HomeActions {

    interface OnHomeFragmentInteractionListener {
        fun onGotoPlan(programObject: ProgramObject)
    }

    private var listener: OnHomeFragmentInteractionListener? = null
    private lateinit var pref: MyPreference
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var premiumViewModel: PremiumViewModel
    private val state = HomeUiState()

    private val recentIds = LinkedHashSet<Int>()
    private var todayRecommendation: RecommendationService.Result? = null
    private var avgDuration = 0
    private var firstDayOfWeekMillis = 0L
    private var lastDayOfWeekMillis = 0L
    private var weekLabels: List<Pair<String, String>> = emptyList()
    private var todayIndex = 0
    private var weekHistory: LiveData<List<HistoryItem>>? = null
    private var weekHistoryObserver: Observer<List<HistoryItem>>? = null
    private var suggestJob: Job? = null
    private var programLaunchInFlight = false

    private val setGoalLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) setupWeek()
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnHomeFragmentInteractionListener
            ?: throw RuntimeException("$context must implement OnHomeFragmentInteractionListener")
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { MinTheme { HomeScreen(state, this@HomeFragment) } }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pref = MyPreference(requireContext())
        historyViewModel = ViewModelProvider(requireActivity())[HistoryViewModel::class.java]
        // Billing cho nút "Nhận ngay" của thẻ Flash Sale (mua thẳng gói 50%): tạo sớm để giá có sẵn khi bấm.
        premiumViewModel = ViewModelProvider(requireActivity())[PremiumViewModel::class.java]
        premiumViewModel.isPremium.observe(viewLifecycleOwner) { premium ->
            if (premium == true) {
                state.isPremium = true
                state.promoEndsAt = null
            }
        }

        state.dateText = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
            .replaceFirstChar { it.titlecase(Locale.getDefault()) }
        // Đọc danh sách chương trình (giải mã + parse JSON lần đầu) ở luồng nền, không chặn khung hình.
        viewLifecycleOwner.lifecycleScope.launch {
            val sections = withContext(Dispatchers.IO) { buildSections() }
            state.sections = sections
            refreshPlanProgress()
        }

        historyViewModel.avgDuration.observe(viewLifecycleOwner) { avg ->
            if (avg != null) avgDuration = avg
        }
        historyViewModel.recentProgramIds.observe(viewLifecycleOwner) { ids ->
            if (ids != null) {
                recentIds.clear()
                recentIds.addAll(ids)
            }
        }
        historyViewModel.userStreak.observe(viewLifecycleOwner) { result ->
            if (result == null) return@observe
            state.streakDays = result.streakDays
            state.coachMessage = streakMessage(result)
            suggestProgram(result.streakDays)
        }
        historyViewModel.weeklyProgress.observe(viewLifecycleOwner) { weekly ->
            if (weekly == null) return@observe
            val done = BooleanArray(7)
            weekly.completedDaysList?.forEach { dateId ->
                val index = TimeUnit.MILLISECONDS.toDays(dateId - firstDayOfWeekMillis).toInt()
                if (index in 0..6) done[index] = true
            }
            state.weekDays = weekLabels.mapIndexed { i, (label, day) ->
                WeekDay(label, day, done[i], i == todayIndex)
            }
            state.goalDone = weekly.completedDays
            state.goalTotal = weekly.goalDays
        }
        setupWeek()
    }

    override fun onResume() {
        super.onResume()
        programLaunchInFlight = false
        state.isPremium = pref.isPremiumMember
        state.promoEndsAt = if (pref.isPremiumMember) null else PaywallPromo.activeEndTime(requireContext())
        state.personalPlan = PersonalPlanStore.program(requireContext())
        state.weekReview = PersonalPlanStore.pendingReviewWeek(requireContext())
        refreshPlanProgress()
        refreshFirstWorkoutNudge()
    }

    /** Popup "Bắt đầu buổi tập đầu tiên": chỉ khi chưa có buổi tập nào và chưa từng mời. */
    private fun refreshFirstWorkoutNudge() {
        val context = requireContext().applicationContext
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        if (prefs.getBoolean(PREF_FIRST_WORKOUT_NUDGE, false)) {
            state.firstWorkoutNudge = false
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val noHistory = withContext(Dispatchers.IO) {
                runCatching { HistoryDatabase.getDatabase(context).historyDao().countHistory() == 0 }.getOrDefault(false)
            }
            state.firstWorkoutNudge = noHistory
        }
    }

    private fun markFirstWorkoutNudgeSeen() {
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
            .edit().putBoolean(PREF_FIRST_WORKOUT_NUDGE, true).apply()
        state.firstWorkoutNudge = false
    }

    // ------------------------------------------------------------ dữ liệu

    private fun buildSections(): List<HomeSection> {
        val all = FitnessApplication.getApplication().mJsonHelper.allCategoryHashMap
        fun pick(ids: IntArray) = ids.toList().mapNotNull { all[it] }
        return listOf(
            HomeSection(getString(R.string.challenge), pick(CHALLENGES), challenge = true),
            HomeSection(getString(R.string.breast_training), pick(BREAST), challenge = false),
            HomeSection(getString(R.string.txt_abs), pick(ABS), challenge = false),
            HomeSection(getString(R.string.txt_arm), pick(ARM), challenge = false),
            HomeSection(getString(R.string.txt_butt), pick(BUTT), challenge = false),
            HomeSection(getString(R.string.txt_thigh), pick(THIGH), challenge = false)
        ).filter { it.programs.isNotEmpty() }
    }

    private fun refreshPlanProgress() {
        val ids = (state.sections.flatMap { it.programs } + state.recentPrograms + listOfNotNull(state.personalPlan))
            .map { it.id }.toSet()
        state.planProgress = ids.associateWith { pref.getPlanProgress(it) }
    }

    /** Tính 7 ngày của tuần hiện tại theo ngày bắt đầu tuần người dùng chọn. */
    private fun setupWeek() {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val cal = Calendar.getInstance().apply {
            firstDayOfWeek = pref.firstDayOfWeek
            set(Calendar.DAY_OF_WEEK, pref.firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis > now.timeInMillis) cal.add(Calendar.DAY_OF_MONTH, -7)
        firstDayOfWeekMillis = cal.timeInMillis

        val weekFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
        val labels = ArrayList<Pair<String, String>>(7)
        for (i in 0 until 7) {
            labels += weekFormat.format(cal.time) to dayFormat.format(cal.time)
            if (cal.timeInMillis == now.timeInMillis) todayIndex = i
            cal.add(Calendar.DATE, 1)
        }
        lastDayOfWeekMillis = cal.timeInMillis
        weekLabels = labels
        state.weekDays = labels.mapIndexed { i, (label, day) -> WeekDay(label, day, false, i == todayIndex) }
        state.goalTotal = pref.goalOfWeek

        historyViewModel.refreshWeeklyProgress(pref.goalOfWeek, pref.firstDayOfWeek)
        observeWeekHistory()
    }

    /** Các chương trình đã tập trong tuần (tối đa 3 cái gần nhất). */
    private fun observeWeekHistory() {
        weekHistoryObserver?.let { weekHistory?.removeObserver(it) }
        val observer = Observer<List<HistoryItem>> { items ->
            val ids = LinkedHashSet<Int>()
            items?.forEach { item ->
                if (recentIds.size < 2) recentIds.add(item.programId)
                ids.add(item.programId)
            }
            // Lần đầu mở app danh sách bài có thể chưa giải mã xong: chờ ở luồng nền, không chặn main.
            viewLifecycleOwner.lifecycleScope.launch {
                val all = withContext(Dispatchers.IO) { FitnessApplication.getApplication().mJsonHelper.allCategoryHashMap }
                state.recentPrograms = ids.toList().takeLast(3).mapNotNull { all[it] }
                refreshPlanProgress()
            }
        }
        weekHistory = historyViewModel.findFromTo(firstDayOfWeekMillis, lastDayOfWeekMillis).also {
            it.observe(viewLifecycleOwner, observer)
        }
        weekHistoryObserver = observer
    }

    /**
     * Danh sách bài đã cache trong JsonHelper (giải mã sẵn lúc mở app); vẫn chạy ở luồng nền vì
     * lần đầu có thể phải chờ giải mã xong, và chấm điểm 170+ bài không cần chiếm main thread.
     */
    private fun suggestProgram(@Suppress("UNUSED_PARAMETER") streak: Int) {
        val appContext = requireContext().applicationContext
        suggestJob?.cancel()
        suggestJob = viewLifecycleOwner.lifecycleScope.launch {
            // RecommendationService (chép từ Women): chấm điểm theo onboarding, trình độ, thói quen, lịch sử 60 ngày,
            // đánh giá độ khó; thay cho ProgramRecommenderV3 cũ (bộ chấm của app Tai Chi).
            val today = withContext(Dispatchers.IO) { RecommendationService.today(appContext, 1) } ?: return@launch
            todayRecommendation = today.pick
            val suggested = today.pick.program
            state.suggestion = suggested
            state.suggestionInfo = programInfo(suggested)
            state.suggestionReason = today.pick.reasonText
            state.canSwap = today.alternatives.isNotEmpty()
            historyViewModel.setSuggestProgram(suggested)
            RecommendationService.logShown(appContext, today.pick)
            TodayWidget.saveSuggestion(appContext, suggested.id)
        }
    }

    private fun programInfo(program: ProgramObject): String {
        val premium = if (program.premium == 1) "👑 " else ""
        val level = when (program.level) {
            1 -> getString(R.string.beginner)
            2 -> getString(R.string.intermediate)
            3 -> getString(R.string.advanced)
            else -> getString(R.string.any_level)
        }
        val focus = program.focus?.takeIf { it.isNotBlank() }?.let { "  •  🧘 $it" } ?: ""
        return "$premium⏱ ${getString(R.string.x_minutes, program.total)}  •  $level$focus"
    }

    private fun timeOfDay(): String {
        val hour = LocalTime.now().hour
        return when {
            hour in 5..11 -> mint.woman.female.workout.utils.ProgramRecommenderV3.TIME_MORNING
            hour in 12..16 -> mint.woman.female.workout.utils.ProgramRecommenderV3.TIME_AFTERNOON
            else -> mint.woman.female.workout.utils.ProgramRecommenderV3.TIME_EVENING
        }
    }

    private fun dayOff(lastWorkout: Long): Int {
        if (lastWorkout <= 0) return 999
        val last = Instant.ofEpochMilli(lastWorkout).atZone(ZoneId.systemDefault()).toLocalDate()
        return ChronoUnit.DAYS.between(last, LocalDate.now()).toInt()
    }

    private fun streakMessage(result: UserStreakResult): String = when (result.messageType) {
        StreakMessageType.START_NEW -> getString(R.string.streak_start_new)
        StreakMessageType.KEEP_TODAY -> getString(R.string.streak_kept_today)
        StreakMessageType.NEED_TODAY -> getString(R.string.streak_need_today)
        StreakMessageType.BROKEN_RECENTLY -> getString(R.string.streak_broken_recently)
        StreakMessageType.RESUMED -> getString(R.string.streak_resumed)
        StreakMessageType.MILESTONE_ACHIEVED -> getString(R.string.streak_milestone, result.streakDays)
        StreakMessageType.AT_RISK -> getString(R.string.streak_at_risk)
        StreakMessageType.PAUSED -> getString(R.string.streak_paused)
        else -> getString(R.string.streak_unknown)
    }

    // ------------------------------------------------------------ hành động

    private fun openProgram(program: ProgramObject) {
        val host = activity ?: return
        val owner = viewLifecycleOwnerLiveData.value ?: return
        if (!isAdded || host.isFinishing || host.isDestroyed || programLaunchInFlight
            || !owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return
        val target = when (program.type) {
            1 -> Intent(host, ChallengeActivity::class.java)
                .putExtras(Bundle().apply { putInt("PROGRAM_ID", program.id) })
            2 -> Intent(host, PreviewActivity::class.java)
                .putExtras(Bundle().apply {
                    putInt("PROGRAM_ID", program.id)
                    putInt("DAY_NUMBER", 0)
                })
            else -> null
        }
        if (target == null) return
        programLaunchInFlight = true
        try {
            startActivity(target)
        } catch (error: NullPointerException) {
            // Some Android builds return an NPE from ActivityTaskManager through Parcel.
            // Limit recovery to the launch call; do not swallow errors while building the intent.
            programLaunchInFlight = false
            val crashlytics = FirebaseCrashlytics.getInstance()
            val destination = target.component?.className.orEmpty()
            crashlytics.setCustomKey("PROGRAM_ID", program.id)
            crashlytics.setCustomKey("HOME_PROGRAM_TYPE", program.type)
            crashlytics.setCustomKey("HOME_PROGRAM_DISCOVER", program.discover)
            crashlytics.setCustomKey("HOME_PROGRAM_VERSION", program.version)
            crashlytics.setCustomKey("HOME_LAUNCH_TARGET", destination)
            crashlytics.log("Home program launch failed: programId=${program.id}, type=${program.type}, "
                + "discover=${program.discover}, version=${program.version}, target=$destination")
            crashlytics.recordException(error)
            FirebaseAnalytics.getInstance(host).logEvent("home_program_launch_error", Bundle().apply {
                putInt("program_id", program.id)
                putInt("program_type", program.type)
                putInt("discover", program.discover)
                putInt("data_version", program.version)
                putString("target", destination)
            })
            Toast.makeText(host, R.string.retry, Toast.LENGTH_SHORT).show()
        }
    }

    private fun log(event: String) {
        FirebaseAnalytics.getInstance(requireContext()).logEvent(event, Bundle())
    }

    override fun onStartFirstWorkout(program: ProgramObject) {
        log("click_first_workout_start_scr_home")
        markFirstWorkoutNudgeSeen()
        openProgram(program)
    }

    override fun onFirstWorkoutDismissed() {
        log("click_first_workout_close_scr_home")
        markFirstWorkoutNudgeSeen()
    }

    override fun onStartSuggestion() {
        val program = state.suggestion ?: return
        todayRecommendation?.takeIf { it.program.id == program.id }?.let { RecommendationService.logStart(requireContext(), it) }
        openProgram(program)
    }

    /** "Đổi bài": bỏ qua bài hôm nay, lấy bài gợi ý kế tiếp. */
    override fun onSwapSuggestion() {
        val current = todayRecommendation ?: return
        RecommendationService.skipToday(requireContext(), current)
        suggestProgram(0)
    }

    override fun onProgramClick(program: ProgramObject) {
        listener?.onGotoPlan(program)
    }

    override fun onRecentProgramClick(program: ProgramObject) = openProgram(program)

    override fun onSeeMore(section: HomeSection) {
        startActivity(ExploreDetailActivity.intent(requireContext(), section.title, section.programs))
    }

    override fun onEditGoal() {
        log("click_txt_set_goal_scr_home")
        setGoalLauncher.launch(Intent(requireActivity(), SetGoalActivity::class.java))
    }

    override fun onOpenHistory() {
        log("click_layout_this_week_scr_home")
        startActivity(Intent(requireActivity(), HistoryActivity::class.java))
    }

    override fun onOpenPersonalPlan() {
        val plan = state.personalPlan
        if (plan == null) {
            log("click_personal_plan_create_scr_home")
            startActivity(Intent(requireActivity(), PersonalPlanActivity::class.java))
        } else {
            openProgram(plan)
        }
    }

    override fun onWeekReview(week: Int, feel: Int, length: Int) {
        state.weekReview = null
        val appContext = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch {
            val changed = withContext(Dispatchers.IO) { PersonalPlanStore.applyWeekReview(appContext, week, feel, length) }
            if (changed) {
                android.widget.Toast.makeText(appContext, getString(R.string.pp_review_done, week + 1), android.widget.Toast.LENGTH_SHORT).show()
            }
            state.personalPlan = PersonalPlanStore.program(appContext)
            refreshPlanProgress()
        }
    }

    override fun onWeekReviewSkip(week: Int) {
        state.weekReview = null
        PersonalPlanStore.skipWeekReview(requireContext(), week)
    }

    override fun onAdjustPersonalPlan() {
        log("click_personal_plan_adjust_scr_home")
        startActivity(Intent(requireActivity(), PersonalPlanActivity::class.java))
    }

    override fun onPremium() {
        startActivity(Intent(requireActivity(), PremiumActivity::class.java))
    }

    /**
     * Thẻ Flash Sale: mở vé ưu đãi (có ghi giá, chu kỳ trừ tiền, tự gia hạn – chính sách Subscriptions của Google Play),
     * không mở thẳng hộp thanh toán. Hết đợt thì vào paywall.
     */
    override fun onFlashSale() {
        log("click_flash_sale_scr_home")
        val activity = requireActivity()
        if (!DiscountDialogFragment.showIfPromoActive(activity)) {
            startActivity(
                Intent(activity, PremiumActivity::class.java)
                    .putExtra(PremiumActivity.EXTRA_SOURCE, "flash_sale"),
            )
        }
    }

    override fun onPromoExpired() {
        state.promoEndsAt = null
    }

    override fun onMyWorkouts() {
        findNavController().navigate(R.id.action_nav_home_to_nav_my_workout)
    }

    companion object {
        private const val PREF_FIRST_WORKOUT_NUDGE = "HOME_FIRST_WORKOUT_NUDGE_SEEN"
        private val CHALLENGES = intArrayOf(1, 879, 880, 881, 882, 1178)
        private val ABS = intArrayOf(879, 2, 3, 4)
        private val ARM = intArrayOf(880, 5)
        private val BUTT = intArrayOf(881, 6, 7, 8)
        private val THIGH = intArrayOf(882, 13, 14, 15)
        private val BREAST = intArrayOf(1178, 1179, 1180, 1181)
    }
}
