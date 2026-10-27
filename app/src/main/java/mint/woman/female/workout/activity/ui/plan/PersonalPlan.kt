package mint.woman.female.workout.activity.ui.plan

import android.content.Context
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import mint.woman.female.workout.FitnessApplication
import mint.woman.female.workout.R
import mint.woman.female.workout.model.ExerciseObject
import mint.woman.female.workout.model.ProgramObject
import mint.woman.female.workout.utils.MyPreference
import kotlin.random.Random

// ---------------------------------------------------------------- lựa chọn của người dùng

enum class PlanGoal { LOSE_WEIGHT, BUILD_MUSCLE, KEEP_FIT }
enum class PlanLevel { BEGINNER, INTERMEDIATE, ADVANCED }
enum class PlanFocus { FULL_BODY, ABS, ARM_CHEST, BACK, LEG, BUTT }
enum class PlanInjury { BACK, KNEE, ARM_SHOULDER, WRIST }
enum class PlanAerobic { LOW, MODERATE, HIGH }

/** Mức ngắn / vừa / dài, dùng cho thời gian nghỉ giữa bài và phần khởi động - giãn cơ. */
enum class PlanPace { SHORT, STANDARD, LONG }

/** Thời lượng mỗi buổi: <10, 10-20, 20-30, >30 phút -> số phút mục tiêu. */
enum class PlanDuration(val targetMinutes: Int) { UNDER_10(8), MIN_10_20(15), MIN_20_30(25), OVER_30(35) }

/** Tốc độ tăng độ khó qua từng tuần: giây cộng thêm (bài tính giờ) và số lần cộng thêm (bài đếm lần). */
enum class PlanProgression(val secondsPerWeek: Int, val repsPerWeek: Int) { GENTLE(3, 1), STANDARD(5, 2), FAST(8, 3) }

data class PlanSettings(
    val goal: PlanGoal = PlanGoal.LOSE_WEIGHT,
    val level: PlanLevel = PlanLevel.BEGINNER,
    val focus: Set<PlanFocus> = setOf(PlanFocus.FULL_BODY),
    val injuries: Set<PlanInjury> = emptySet(),
    val aerobic: PlanAerobic = PlanAerobic.MODERATE,
    val jumpFree: Boolean = false,
    val duration: PlanDuration = PlanDuration.MIN_10_20,
    /** Nghỉ giữa các bài: 10 / 15 / 25 giây. Plan cũ không có trường này -> STANDARD. */
    val restPace: PlanPace = PlanPace.STANDARD,
    /** Số bài khởi động và giãn cơ trong mỗi buổi. */
    val stretch: PlanPace = PlanPace.STANDARD,
    /** Số ngày tập mỗi tuần (3-6); các ngày còn lại là buổi giãn cơ nhẹ. Plan cũ không có trường này -> 6. */
    val trainingDays: Int = 6,
    /** Đánh giá cuối tuần chỉnh trường này; plan cũ không có -> STANDARD (đúng mức tăng trước đây). */
    val progression: PlanProgression = PlanProgression.STANDARD,
)

/** Một bài trong buổi tập (giống cấu trúc file plan của app). */
data class PlanAction(val actionId: Int, val time: Int, val unit: String, val rest: Int)

// ---------------------------------------------------------------- nhãn bài tập

/**
 * Nhãn gắn tay cho 146 bài tập tại nhà của app (id trong dữ liệu `i/language`).
 * Dữ liệu gốc có trường `jumped` nhưng luôn = false, nên tự phân loại ở đây.
 */
internal object ExerciseTags {
    /** Bài có bật nhảy / chạy tại chỗ (va chạm mạnh). */
    val JUMP = setOf(15, 26, 33, 37, 38, 40, 87, 140, 275)

    /** Bài dồn lực lên đầu gối (squat, lunge, quỳ gối...). */
    val KNEE = setOf(21, 22, 24, 26, 27, 31, 32, 33, 36, 37, 38, 40, 55, 88, 199, 200, 223, 282)

    /** Bài gây áp lực thắt lưng (nâng chân nằm ngửa, gập/ưỡn sâu...). */
    val BACK = setOf(1, 7, 26, 120, 125, 149, 168, 198, 228, 229, 237, 282)

    /** Bài chống tay / dồn lực lên tay-vai. */
    val ARM_SHOULDER = setOf(3, 12, 17, 18, 26, 34, 62, 65, 120, 164, 170, 243, 264, 282)

    /** Bài chống tay lên sàn (cổ tay chịu lực). */
    val WRIST = setOf(3, 10, 12, 17, 18, 26, 34, 47, 49, 50, 51, 91, 92, 120, 141, 164, 243, 282)

    fun blockedBy(injury: PlanInjury): Set<Int> = when (injury) {
        PlanInjury.BACK -> BACK
        PlanInjury.KNEE -> KNEE
        PlanInjury.ARM_SHOULDER -> ARM_SHOULDER
        PlanInjury.WRIST -> WRIST
    }

    /** Bài chính theo vùng tập. */
    val FOCUS: Map<PlanFocus, List<Int>> = mapOf(
        PlanFocus.ABS to listOf(0, 1, 3, 4, 7, 8, 13, 127, 164, 167, 169, 178, 228, 229, 230, 231, 237, 243),
        PlanFocus.ARM_CHEST to listOf(17, 18, 34, 65, 120, 301, 62, 264, 130, 133, 170, 192),
        PlanFocus.BACK to listOf(10, 168, 301, 300, 3, 243, 141, 164),
        PlanFocus.LEG to listOf(21, 22, 24, 31, 32, 36, 55, 88, 223, 70, 76, 78, 80, 82, 83, 125),
        PlanFocus.BUTT to listOf(11, 48, 47, 49, 50, 51, 91, 92, 93, 94, 261, 262, 276, 280, 281, 27, 223),
        PlanFocus.FULL_BODY to listOf(12, 26, 282, 120, 21, 24, 3, 17, 34, 48, 168, 8, 164, 223),
    )

    /** Cardio: bài nhảy chỉ dùng khi không chọn "Không nhảy"; boxing/đứng là cardio nhẹ. */
    val CARDIO = listOf(12, 15, 26, 33, 40, 87, 140, 275, 282, 62, 264, 167, 133, 170, 130)

    val WARM_UP = listOf(136, 258, 298, 299, 296, 297, 328, 62, 167, 130, 133)

    val COOL_DOWN = listOf(
        144, 154, 155, 203, 204, 196, 197, 239, 240, 259, 260, 330, 331, 332, 333, 334, 222,
        201, 202, 149, 198, 268, 269, 305, 306, 307, 308
    )

    /** Bài trái/phải: luôn tập cả hai bên liền nhau. */
    val PAIRS: Map<Int, Int> = listOf(
        32 to 31, 37 to 38, 49 to 47, 51 to 50, 82 to 70, 78 to 76, 80 to 83, 91 to 92, 93 to 94,
        197 to 196, 199 to 200, 201 to 202, 203 to 204, 218 to 219, 230 to 231, 239 to 240,
        259 to 260, 261 to 262, 268 to 269, 281 to 280, 292 to 293, 294 to 295, 303 to 304,
        305 to 306, 307 to 308, 326 to 327, 330 to 331, 332 to 333
    ).flatMap { (a, b) -> listOf(a to b, b to a) }.toMap()
}

// ---------------------------------------------------------------- sinh plan

/**
 * Sinh plan 4 tuần (28 ngày) bằng luật, không cần AI: lọc theo chấn thương / không nhảy / trình độ,
 * ưu tiên vùng tập, ghép buổi khởi động + bài chính + giãn cơ cho khớp thời lượng, tăng dần theo tuần.
 * Cùng một [PlanSettings] luôn cho ra cùng một plan.
 */
class PersonalPlanGenerator(private val exercises: Map<Int, ExerciseObject>) {

    fun generate(settings: PlanSettings): List<List<PlanAction>> {
        val blocked = settings.injuries.flatMap { ExerciseTags.blockedBy(it) }.toSet() +
            (if (settings.jumpFree) ExerciseTags.JUMP else emptySet())
        val maxLevel = when (settings.level) {
            PlanLevel.BEGINNER -> 1
            PlanLevel.INTERMEDIATE -> 2
            PlanLevel.ADVANCED -> 3
        }

        fun usable(ids: List<Int>, levelCap: Int = maxLevel): List<Int> =
            ids.filter { id ->
                val ex = exercises[id] ?: return@filter false
                id !in blocked && (ExerciseTags.PAIRS[id]?.let { it !in blocked } ?: true) &&
                    (ex.levelid?.toIntOrNull() ?: 1) <= levelCap
            }.distinct()

        /** Pool theo trình độ; nếu quá ít bài thì nới thêm một bậc độ khó. */
        fun pool(ids: List<Int>): List<Int> {
            val strict = usable(ids)
            return if (strict.size >= 4 || maxLevel >= 3) strict else usable(ids, maxLevel + 1)
        }

        val focusOrder = PlanFocus.values().filter { it in settings.focus }.ifEmpty { listOf(PlanFocus.FULL_BODY) }
        val cardioPool = pool(ExerciseTags.CARDIO)
        val warmPool = usable(ExerciseTags.WARM_UP, 3)
        val coolPool = usable(ExerciseTags.COOL_DOWN, 3)

        val cardioShare = (when (settings.goal) {
            PlanGoal.LOSE_WEIGHT -> 0.45
            PlanGoal.KEEP_FIT -> 0.3
            PlanGoal.BUILD_MUSCLE -> 0.15
        } + when (settings.aerobic) {
            PlanAerobic.LOW -> -0.15
            PlanAerobic.MODERATE -> 0.0
            PlanAerobic.HIGH -> 0.15
        }).coerceIn(0.0, 0.6)

        val seed = settings.hashCode()
        val restDays = restDaysOf(settings.trainingDays)
        var trainingIndex = 0
        val days = ArrayList<List<PlanAction>>(28)
        for (week in 0 until 4) {
            for (dayInWeek in 0 until 7) {
                val dayIndex = week * 7 + dayInWeek
                val random = Random(seed * 31 + dayIndex)
                days += if (dayInWeek in restDays) {
                    recoveryDay(warmPool, coolPool, random)
                } else {
                    // Đếm theo ngày tập (không theo ngày trong tuần) để vùng tập vẫn xoay vòng đều khi tập ít ngày.
                    val focus = focusOrder[trainingIndex++ % focusOrder.size]
                    // Ngoài vùng chính, trộn thêm một ít bài toàn thân để buổi tập cân bằng.
                    val mainPool = pool(ExerciseTags.FOCUS.getValue(focus))
                    val extraPool = if (focus == PlanFocus.FULL_BODY) emptyList() else pool(ExerciseTags.FOCUS.getValue(PlanFocus.FULL_BODY))
                    trainingDay(settings, week, mainPool, extraPool, cardioPool, warmPool, coolPool, cardioShare, random)
                }
            }
        }
        return days
    }

    /** Ngày nghỉ (giãn cơ) trong tuần, rải đều để không tập liền quá nhiều ngày. */
    private fun restDaysOf(trainingDays: Int): Set<Int> = when (trainingDays.coerceIn(3, 6)) {
        3 -> setOf(1, 3, 5, 6)
        4 -> setOf(2, 4, 6)
        5 -> setOf(2, 6)
        else -> setOf(6)
    }

    private fun actionFor(id: Int, settings: PlanSettings, week: Int, stretch: Boolean = false): PlanAction {
        val ex = exercises.getValue(id)
        val timed = ex.unit?.contains("s") == true
        val rest = when (settings.restPace) {
            PlanPace.SHORT -> 10
            PlanPace.STANDARD -> 15
            PlanPace.LONG -> 25
        }
        if (stretch) return PlanAction(id, 30, "s", 5)
        val levelIndex = settings.level.ordinal
        return if (timed) {
            val base = intArrayOf(20, 30, 40)[levelIndex] + (if (settings.goal == PlanGoal.BUILD_MUSCLE) 5 else 0)
            PlanAction(id, (base + week * settings.progression.secondsPerWeek).coerceAtMost(60), "s", rest)
        } else {
            val base = intArrayOf(8, 12, 16)[levelIndex] + (if (settings.goal == PlanGoal.BUILD_MUSCLE) 4 else 0)
            PlanAction(id, base + week * settings.progression.repsPerWeek, "", rest)
        }
    }

    /** Thời gian (giây) một bài chiếm, tính giống DemoDataProvider.getTotalTime(). */
    private fun cost(a: PlanAction): Int = a.rest + if (a.unit.isEmpty()) a.time * 2 else a.time

    /** Thêm bài (kèm bên còn lại nếu là bài trái/phải). */
    private fun MutableList<PlanAction>.addWithPair(id: Int, make: (Int) -> PlanAction) {
        add(make(id))
        ExerciseTags.PAIRS[id]?.takeIf { exercises.containsKey(it) }?.let { add(make(it)) }
    }

    private fun pickDistinct(pool: List<Int>, used: MutableSet<Int>, random: Random): Int? {
        val fresh = pool.filter { it !in used && ExerciseTags.PAIRS[it]?.let { p -> p !in used } != false }
        val id = (fresh.ifEmpty { pool }).randomOrNull(random) ?: return null
        used += id
        ExerciseTags.PAIRS[id]?.let { used += it }
        return id
    }

    private fun trainingDay(
        settings: PlanSettings,
        week: Int,
        mainPool: List<Int>,
        extraPool: List<Int>,
        cardioPool: List<Int>,
        warmPool: List<Int>,
        coolPool: List<Int>,
        cardioShare: Double,
        random: Random
    ): List<PlanAction> {
        val target = settings.duration.targetMinutes * 60
        val baseCount = if (settings.duration == PlanDuration.UNDER_10) 1 else 2
        val warmCount = when (settings.stretch) {
            PlanPace.SHORT -> 1
            PlanPace.STANDARD -> baseCount
            PlanPace.LONG -> baseCount + 2
        }
        val coolCount = warmCount
        val used = HashSet<Int>()
        val session = ArrayList<PlanAction>()

        repeat(warmCount) {
            pickDistinct(warmPool, used, random)?.let { id -> session.addWithPair(id) { PlanAction(it, 30, "s", 5) } }
        }
        val cool = ArrayList<PlanAction>()
        repeat(coolCount) {
            pickDistinct(coolPool, used, random)?.let { id -> cool.addWithPair(id) { actionFor(it, settings, week, stretch = true) } }
        }

        // Bài chính dạng vòng (circuit): chọn vài bài khác nhau rồi lặp nhiều vòng cho đủ thời lượng,
        // thay vì một danh sách dài lặp bài rải rác.
        val circuitSize = when (settings.duration) {
            PlanDuration.UNDER_10 -> 5
            PlanDuration.MIN_10_20 -> 7
            PlanDuration.MIN_20_30 -> 8
            PlanDuration.OVER_30 -> 9
        }
        val circuit = ArrayList<PlanAction>()
        var cardioDebt = 0.0
        var guard = 0
        while (circuit.size < circuitSize && guard++ < 40) {
            cardioDebt += cardioShare
            val source = when {
                cardioDebt >= 1.0 && cardioPool.isNotEmpty() -> {
                    cardioDebt -= 1.0
                    cardioPool
                }
                extraPool.isNotEmpty() && random.nextInt(5) == 0 -> extraPool
                mainPool.isNotEmpty() -> mainPool
                else -> extraPool.ifEmpty { cardioPool }
            }
            val fresh = source.filter { it !in used }
            if (fresh.isEmpty() && circuit.isNotEmpty()) continue
            val id = pickDistinct(source, used, random) ?: break
            circuit.addWithPair(id) { actionFor(it, settings, week) }
        }
        val fixed = session.sumOf { cost(it) } + cool.sumOf { cost(it) }
        val perRound = circuit.sumOf { cost(it) }.coerceAtLeast(1)
        val rounds = ((target - fixed).toDouble() / perRound).let { kotlin.math.round(it).toInt() }.coerceIn(1, 4)
        repeat(rounds) { session += circuit }
        session += cool
        return session
    }

    private fun recoveryDay(warmPool: List<Int>, coolPool: List<Int>, random: Random): List<PlanAction> {
        val used = HashSet<Int>()
        val session = ArrayList<PlanAction>()
        repeat(2) { pickDistinct(warmPool, used, random)?.let { id -> session.addWithPair(id) { PlanAction(it, 30, "s", 5) } } }
        repeat(6) { pickDistinct(coolPool, used, random)?.let { id -> session.addWithPair(id) { PlanAction(it, 40, "s", 5) } } }
        return session
    }
}

// ---------------------------------------------------------------- lưu trữ

/**
 * Plan cá nhân được lưu như một thử thách 28 ngày ([PROGRAM_ID]) để dùng lại màn Challenge, Preview,
 * tập luyện và Finished sẵn có. File plan nằm trong bộ nhớ app với tên bắt đầu bằng `custom_plan_new_`
 * nên JsonHelper.getListPlan() đọc trực tiếp.
 */
object PersonalPlanStore {
    const val PROGRAM_ID = 9000
    const val PLAN_FILE = "custom_plan_new_personal"
    private const val PREFS = "personal_plan"
    private const val KEY_SETTINGS = "settings"
    /** Tuần lớn nhất (1..3) người dùng đã đánh giá hoặc bỏ qua ở thẻ "Tuần x xong". */
    private const val KEY_REVIEWED_WEEK = "reviewed_week"

    private val gson = Gson()

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun settings(context: Context): PlanSettings? =
        prefs(context).getString(KEY_SETTINGS, null)?.let {
            runCatching { gson.fromJson(it, PlanSettings::class.java) }.getOrNull()
        }

    @JvmStatic
    fun hasPlan(context: Context): Boolean =
        settings(context) != null && context.getFileStreamPath(PLAN_FILE).exists()

    /** Gợi ý mặc định từ câu trả lời onboarding (mục tiêu, vùng tập, trình độ). */
    fun defaultsFromOnboarding(context: Context): PlanSettings {
        val pref = MyPreference(context)
        val goal = when (pref.userGoal) {
            "build_muscle" -> PlanGoal.BUILD_MUSCLE
            "keep_fit" -> PlanGoal.KEEP_FIT
            else -> PlanGoal.LOSE_WEIGHT
        }
        val level = when (pref.userExperience) {
            "intermediate" -> PlanLevel.INTERMEDIATE
            "advanced" -> PlanLevel.ADVANCED
            else -> PlanLevel.BEGINNER
        }
        val focus = pref.userFocusAreas.orEmpty().split(",").mapNotNull {
            when (it.trim()) {
                "belly" -> PlanFocus.ABS
                "arm" -> PlanFocus.ARM_CHEST
                "leg" -> PlanFocus.LEG
                "butt" -> PlanFocus.BUTT
                "full_body" -> PlanFocus.FULL_BODY
                else -> null
            }
        }.toSet().ifEmpty { setOf(PlanFocus.FULL_BODY) }
        // Câu hỏi đau khớp ở onboarding: 1 đầu gối, 2 lưng dưới, 3 cả hai.
        val injuries = when (pref.jointPain) {
            1 -> setOf(PlanInjury.KNEE)
            2 -> setOf(PlanInjury.BACK)
            3 -> setOf(PlanInjury.KNEE, PlanInjury.BACK)
            else -> emptySet()
        }
        // Người mới bắt đầu: buổi ngắn (~8 phút) cho tuần đầu dễ theo, các tuần sau tăng dần.
        val duration = if (level == PlanLevel.BEGINNER) PlanDuration.UNDER_10 else PlanDuration.MIN_10_20
        return PlanSettings(goal = goal, level = level, focus = focus, injuries = injuries, jumpFree = pref.isNoJump, duration = duration)
    }

    /** Sinh plan mới, ghi file, đặt lại tiến độ về ngày 1. */
    fun create(context: Context, settings: PlanSettings) {
        writePlan(context, settings, keepDays = 0)
        prefs(context).edit().putInt(KEY_REVIEWED_WEEK, 0).apply()
        MyPreference(context).setPlanProgress(PROGRAM_ID, 0)
    }

    /**
     * Ghi file plan theo [settings]. [keepDays] ngày đầu giữ nguyên nội dung cũ (đã tập), chỉ các ngày
     * sau được sinh lại; tiến độ không đổi.
     */
    private fun writePlan(context: Context, settings: PlanSettings, keepDays: Int) {
        val app = FitnessApplication.getApplication()
        val exercises = app.mJsonHelper.getHashMapExercise(0, 0)
        val days = PersonalPlanGenerator(exercises).generate(settings)
        val old = if (keepDays > 0) runCatching { JSONArray(app.mJsonHelper.readFileFromData(PLAN_FILE)) }.getOrNull() else null
        val json = JSONArray()
        days.forEachIndexed { index, actions ->
            if (old != null && index < keepDays && index < old.length()) {
                json.put(old.getJSONObject(index))
                return@forEachIndexed
            }
            json.put(JSONObject().apply {
                put("name", "Day ${index + 1}")
                put("exercise", JSONArray().apply {
                    actions.forEach { a ->
                        put(JSONObject().apply {
                            put("actionId", a.actionId)
                            put("time", a.time)
                            put("unit", a.unit)
                            put("rest", a.rest)
                        })
                    }
                })
            })
        }
        app.mJsonHelper.writeFileToData(PLAN_FILE, json.toString())
        prefs(context).edit().putString(KEY_SETTINGS, gson.toJson(settings)).apply()
        app.mJsonHelper.invalidatePrograms()
    }

    // ------------------------------------------------------------ đánh giá cuối tuần

    /** Tuần vừa xong (1..3) chưa được đánh giá, để Home mời "Tuần x xong, chỉnh tuần sau"; null nếu không có. */
    fun pendingReviewWeek(context: Context): Int? {
        if (!hasPlan(context)) return null
        val week = MyPreference(context).getPlanProgress(PROGRAM_ID) / 7
        if (week !in 1..3) return null
        return week.takeIf { it > prefs(context).getInt(KEY_REVIEWED_WEEK, 0) }
    }

    /**
     * Áp đánh giá tuần [week]: [feel] 1 quá dễ / 0 vừa / -1 quá khó; [length] 1 dài hơn / 0 giữ / -1 ngắn hơn.
     * Độ khó từng buổi đã có DifficultyStore lo (đánh giá cuối buổi), nên ở đây chỉnh "khung" plan: tốc độ
     * tăng qua tuần, rồi tới trình độ, và thời lượng buổi. Chỉ sinh lại các ngày chưa tập.
     * @return true nếu plan được sinh lại.
     */
    fun applyWeekReview(context: Context, week: Int, feel: Int, length: Int): Boolean {
        val current = settings(context) ?: return false
        var next = current
        val progressions = PlanProgression.values()
        val levels = PlanLevel.values()
        if (feel > 0) {
            next = if (next.progression != PlanProgression.FAST) next.copy(progression = progressions[next.progression.ordinal + 1])
            else if (next.level != PlanLevel.ADVANCED) next.copy(level = levels[next.level.ordinal + 1], progression = PlanProgression.STANDARD)
            else next
        } else if (feel < 0) {
            next = if (next.progression != PlanProgression.GENTLE) next.copy(progression = progressions[next.progression.ordinal - 1])
            else if (next.level != PlanLevel.BEGINNER) next.copy(level = levels[next.level.ordinal - 1], progression = PlanProgression.STANDARD)
            else next
        }
        if (length != 0) {
            val durations = PlanDuration.values()
            next = next.copy(duration = durations[(next.duration.ordinal + length).coerceIn(0, durations.size - 1)])
        }
        val changed = next != current
        if (changed) writePlan(context, next, keepDays = MyPreference(context).getPlanProgress(PROGRAM_ID))
        prefs(context).edit().putInt(KEY_REVIEWED_WEEK, week).apply()
        com.google.firebase.analytics.FirebaseAnalytics.getInstance(context).logEvent(
            "plan_week_review",
            android.os.Bundle().apply {
                putInt("week", week)
                putInt("feel", feel)
                putInt("length", length)
                putInt("changed", if (changed) 1 else 0)
            },
        )
        return changed
    }

    fun skipWeekReview(context: Context, week: Int) {
        prefs(context).edit().putInt(KEY_REVIEWED_WEEK, week).apply()
        com.google.firebase.analytics.FirebaseAnalytics.getInstance(context)
            .logEvent("plan_week_review_skip", android.os.Bundle().apply { putInt("week", week) })
    }

    /** ProgramObject của plan cá nhân (null nếu chưa tạo). JsonHelper thêm nó vào danh sách chương trình. */
    @JvmStatic
    fun program(context: Context): ProgramObject? {
        val settings = settings(context) ?: return null
        if (!context.getFileStreamPath(PLAN_FILE).exists()) return null
        val primary = settings.focus.minByOrNull { it.ordinal } ?: PlanFocus.FULL_BODY
        return ProgramObject().apply {
            id = PROGRAM_ID
            name = context.getString(R.string.pp_plan_name)
            subtitle = ""
            type = 1
            plan = PLAN_FILE
            total = 28
            level = settings.level.ordinal + 1
            premium = 0
            discover = 0
            version = 0
            background = "bg_1.webp"
            image = focusImage(primary)
            description = context.getString(R.string.pp_home_desc)
            focus = settings.focus.joinToString(", ") { context.getString(focusLabel(it)) }
        }
    }

    fun focusImage(focus: PlanFocus): String = when (focus) {
        PlanFocus.FULL_BODY -> "full_body.webp"
        PlanFocus.ABS -> "abs_1.webp"
        PlanFocus.ARM_CHEST -> "arm_1.webp"
        PlanFocus.BACK -> "back.webp"
        PlanFocus.LEG -> "thigh_1.webp"
        PlanFocus.BUTT -> "butt_1.webp"
    }

    fun focusLabel(focus: PlanFocus): Int = when (focus) {
        PlanFocus.FULL_BODY -> R.string.pp_full_body
        PlanFocus.ABS -> R.string.pp_abs
        PlanFocus.ARM_CHEST -> R.string.pp_arm_chest
        PlanFocus.BACK -> R.string.pp_back
        PlanFocus.LEG -> R.string.pp_leg
        PlanFocus.BUTT -> R.string.pp_butt
    }
}
