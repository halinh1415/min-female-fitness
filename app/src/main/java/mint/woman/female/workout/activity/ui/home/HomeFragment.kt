package mint.woman.female.workout.activity.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import mint.woman.female.workout.FitnessApplication
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ExploreDetailActivity
import mint.woman.female.workout.activity.ui.compose.MinTheme
import mint.woman.female.workout.activity.ui.history.HistoryActivity
import mint.woman.female.workout.activity.ui.plan.PersonalPlanActivity
import mint.woman.female.workout.activity.ui.plan.PersonalPlanStore
import mint.woman.female.workout.activity.ui.history.HistoryViewModel
import mint.woman.female.workout.activity.ui.premium.PremiumActivity
import mint.woman.female.workout.activity.ui.preview.PreviewActivity
import mint.woman.female.workout.activity.ui.weekgoal.SetGoalActivity
import mint.woman.female.workout.activity.ui.workout.ChallengeActivity
import mint.woman.female.workout.model.HistoryItem
import mint.woman.female.workout.model.ProgramObject
import mint.woman.female.workout.utils.MyPreference
import mint.woman.female.workout.utils.ProgramRecommenderV3
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
 * gợi ý bài tập từ [ProgramRecommenderV3], mục tiêu tuần từ [HistoryViewModel],
 * các nhóm chương trình cố định theo id.
 */
class HomeFragment : Fragment(), HomeActions {

    interface OnHomeFragmentInteractionListener {
        fun onGotoPlan(programObject: ProgramObject)
    }

    private var listener: OnHomeFragmentInteractionListener? = null
    private lateinit var pref: MyPreference
    private lateinit var historyViewModel: HistoryViewModel
    private val state = HomeUiState()

    private val recentIds = LinkedHashSet<Int>()
    private var avgDuration = 0
    private var firstDayOfWeekMillis = 0L
    private var lastDayOfWeekMillis = 0L
    private var weekLabels: List<Pair<String, String>> = emptyList()
    private var todayIndex = 0
    private var weekHistory: LiveData<List<HistoryItem>>? = null
    private var weekHistoryObserver: Observer<List<HistoryItem>>? = null
    private var suggestJob: Job? = null

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
        state.isPremium = pref.isPremiumMember
        state.personalPlan = PersonalPlanStore.program(requireContext())
        refreshPlanProgress()
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
    private fun suggestProgram(streak: Int) {
        val timeOfDay = timeOfDay()
        val recent = LinkedHashSet(recentIds)
        val dayOff = dayOff(pref.lastWorkout)
        val avg = avgDuration
        val focus = pref.userTargetFocus
        suggestJob?.cancel()
        suggestJob = viewLifecycleOwner.lifecycleScope.launch {
            val suggested = withContext(Dispatchers.IO) {
                val programs = FitnessApplication.getApplication().mJsonHelper.allCategory
                ProgramRecommenderV3().suggest(programs, streak, true, timeOfDay, recent, dayOff, avg, focus, false)
            } ?: return@launch
            state.suggestion = suggested
            state.suggestionInfo = programInfo(suggested)
            historyViewModel.setSuggestProgram(suggested)
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
            hour in 5..11 -> ProgramRecommenderV3.TIME_MORNING
            hour in 12..16 -> ProgramRecommenderV3.TIME_AFTERNOON
            else -> ProgramRecommenderV3.TIME_EVENING
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
        val target = when (program.type) {
            1 -> Intent(requireActivity(), ChallengeActivity::class.java)
                .putExtras(Bundle().apply { putInt("PROGRAM_ID", program.id) })
            2 -> Intent(requireActivity(), PreviewActivity::class.java)
                .putExtras(Bundle().apply {
                    putInt("PROGRAM_ID", program.id)
                    putInt("DAY_NUMBER", 0)
                })
            else -> null
        }
        target?.let(::startActivity)
    }

    private fun log(event: String) {
        FirebaseAnalytics.getInstance(requireContext()).logEvent(event, Bundle())
    }

    override fun onStartSuggestion() {
        state.suggestion?.let(::openProgram)
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

    override fun onAdjustPersonalPlan() {
        log("click_personal_plan_adjust_scr_home")
        startActivity(Intent(requireActivity(), PersonalPlanActivity::class.java))
    }

    override fun onPremium() {
        startActivity(Intent(requireActivity(), PremiumActivity::class.java))
    }

    override fun onMyWorkouts() {
        findNavController().navigate(R.id.action_nav_home_to_nav_my_workout)
    }

    companion object {
        private val CHALLENGES = intArrayOf(1, 879, 880, 881, 882, 1178)
        private val ABS = intArrayOf(879, 2, 3, 4)
        private val ARM = intArrayOf(880, 5)
        private val BUTT = intArrayOf(881, 6, 7, 8)
        private val THIGH = intArrayOf(882, 13, 14, 15)
        private val BREAST = intArrayOf(1178, 1179, 1180, 1181)
    }
}
