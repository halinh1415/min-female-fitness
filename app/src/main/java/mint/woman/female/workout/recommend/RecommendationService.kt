package mint.woman.female.workout.recommend

import android.content.Context
import android.os.Bundle
import androidx.annotation.WorkerThread
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import mint.woman.female.workout.R
import mint.woman.female.workout.model.ProgramObject
import mint.woman.female.workout.FitnessApplication
import mint.woman.female.workout.recommend.ProgramRecommender.Area
import mint.woman.female.workout.recommend.ProgramRecommender.Reason
import mint.woman.female.workout.recommend.ProgramRecommender.Tag
import mint.woman.female.workout.utils.HistoryDatabase
import mint.woman.female.workout.utils.DateIdUtils
import mint.woman.female.workout.utils.MyPreference
import mint.woman.female.workout.utils.RemoteConfigKeys
import mint.woman.female.workout.utils.StreakCalculator
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale

/**
 * Nối [ProgramRecommender] với dữ liệu thật của app: danh sách chương trình (JsonHelper), câu trả lời
 * onboarding (MyPreference), lịch sử 60 ngày (Room) và streak. Dùng chung cho Home, nút "Tập ngay"
 * trên thông báo và màn tóm tắt onboarding. Gọi ngoài main thread.
 */
object RecommendationService {

    data class Result(val program: ProgramObject, val reasonText: String, val recommendation: ProgramRecommender.Recommendation)

    private const val KEY_ID = "REC_PROGRAM_ID"
    private const val KEY_DAY = "REC_DAY"
    private const val KEY_SHOWN_DAY = "REC_SHOWN_DAY"
    private const val KEY_SKIPPED = "REC_SKIPPED"
    private const val HISTORY_DAYS = 60L

    /** Không hợp để gợi ý hằng ngày: mặt / cằm (10, 11), tăng chiều cao (975), yoga trẻ em (979), tạo hình mũi (1304). */
    private val EXCLUDED_IDS = setOf(10, 11, 975, 979, 1304)

    /** Streak / số ngày vắng / đã tập hôm nay - Min không có PracticeStatsStore.loadForNotification như Women. */
    private class Stats(val streakCount: Int, val missedDays: Int, val isDidCompleteToday: Boolean)

    private fun practiceStats(context: Context): Stats {
        val dao = HistoryDatabase.getDatabase(context).historyDao()
        val streak = StreakCalculator(dao).userStreakResult
        val last = dao.getAllPassedDateIds().orEmpty().lastOrNull()
        val today = DateIdUtils.todayDateId()
        val missed = if (streak.streakDays > 0 || last == null) 0 else maxOf(0, (today - last - 1).toInt())
        return Stats(streak.streakDays, missed, streak.didPracticeToday)
    }

    /** Gợi ý hôm nay và các bài thay thế (Home: thẻ "Bài tập hôm nay", nút "Đổi bài", mục "Gợi ý thêm"). */
    data class Today(val pick: Result, val alternatives: List<Result>)

    @WorkerThread
    fun recommend(context: Context): Result? = today(context, 0)?.pick

    /**
     * Bài hôm nay = bài điểm cao nhất chưa bị "Đổi bài" trong ngày; [alternatives] bài kế tiếp, mỗi chuỗi
     * (các cấp của cùng một bài) chỉ lấy một. Ghi lại bài hôm nay để log recommend_complete.
     */
    @WorkerThread
    fun today(context: Context, alternatives: Int): Today? {
        val ranked = distinctSeries(rankToday(context))
        if (ranked.isEmpty()) return null
        val today = DateIdUtils.todayDateId()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val skipped = skippedToday(prefs, today)
        // Đã đổi hết các bài: quay lại từ đầu.
        val pick = ranked.firstOrNull { it.program.id !in skipped } ?: ranked.first()
        prefs.edit().putInt(KEY_ID, pick.program.id).putLong(KEY_DAY, today).apply()
        return Today(pick, ranked.filter { it.program.id != pick.program.id && it.program.id !in skipped }.take(alternatives))
    }

    /** "Đổi bài": bỏ qua bài hôm nay [current] trong ngày, lần tính sau ra bài kế tiếp. */
    fun skipToday(context: Context, current: Result) {
        val today = DateIdUtils.todayDateId()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val skipped = skippedToday(prefs, today) + current.program.id
        prefs.edit().putString(KEY_SKIPPED, "$today:" + skipped.joinToString(",")).apply()
        log(context, "recommend_swap", current.program.id, current.recommendation.reason.name)
    }

    private fun skippedToday(prefs: android.content.SharedPreferences, today: Long): Set<Int> {
        val raw = prefs.getString(KEY_SKIPPED, null) ?: return emptySet()
        val day = raw.substringBefore(':').toLongOrNull()
        if (day != today) return emptySet()
        return raw.substringAfter(':').split(',').mapNotNull { it.toIntOrNull() }.toSet()
    }

    private fun distinctSeries(results: List<Result>): List<Result> {
        val seen = HashSet<String>()
        return results.filter { r -> r.program.seriesKey().let { it == null || seen.add(it) } }
    }

    /** Mọi bài lẻ xếp theo điểm cho hôm nay. */
    @WorkerThread
    private fun rankToday(context: Context): List<Result> {
        val programs = runCatching { FitnessApplication.getApplication().mJsonHelper.allCategoryHashMap }.getOrNull().orEmpty()
        val pref = MyPreference(context)
        val stats = practiceStats(context)
        val today = DateIdUtils.todayDateId()
        val ranked = ProgramRecommender(weights()).rank(
            programs.values.mapNotNull(::toCandidate),
            profile(pref, context),
            loadHistory(context, today),
            ProgramRecommender.Context(
                today = today,
                hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                streakDays = stats.streakCount,
                missedDays = stats.missedDays,
                didToday = stats.isDidCompleteToday,
                firstWorkout = isFirstWorkout(context),
            ),
        )
        return ranked.mapNotNull { rec -> programs[rec.programId]?.let { Result(it, reasonText(context, rec, stats.streakCount), rec) } }
    }

    /**
     * Bài gợi ý cho buổi tới (màn Finished): chấm điểm như thể đã sang ngày mai, buổi vừa tập thành "hôm qua"
     * nên vùng tập vừa xong bị trừ điểm (đổi vùng cơ). Bỏ qua bài vừa tập. Không ghi lại làm "bài hôm nay".
     */
    @WorkerThread
    fun recommendNext(context: Context, excludeId: Int): Result? {
        val programs = runCatching { FitnessApplication.getApplication().mJsonHelper.allCategoryHashMap }.getOrNull().orEmpty()
        val byId = programs.values.associateBy { it.id }
        val pref = MyPreference(context)
        val stats = practiceStats(context)
        val tomorrow = DateIdUtils.todayDateId() + 1
        val ranked = ProgramRecommender(weights()).rank(
            // Chỉ bài mở được ngay: người dùng thường không được gợi ý bài Premium (bấm Bắt đầu lại gặp màn khoá).
            programs.values.mapNotNull(::toCandidate).filter { it.id != excludeId && (pref.isPremiumMember || !it.premium) },
            profile(pref, context),
            loadHistory(context, tomorrow),
            ProgramRecommender.Context(tomorrow, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), stats.streakCount + 1, 0, false),
        )
        val rec = ranked.firstOrNull() ?: return null
        val program = byId[rec.programId] ?: return null
        return Result(program, reasonText(context, rec, stats.streakCount + 1), rec)
    }

    /** Top [count] bài gợi ý (Discover "Dành cho bạn"), mỗi chuỗi một bài. Không ghi lại làm "bài hôm nay". */
    @WorkerThread
    fun recommendTop(context: Context, count: Int): List<Result> = distinctSeries(rankToday(context)).take(count)

    /** Chưa có buổi tập nào trong lịch sử (người mới): buổi đầu gợi ý bài dễ, ngắn. */
    private fun isFirstWorkout(context: Context): Boolean =
        runCatching { HistoryDatabase.getDatabase(context).historyDao().countHistory() == 0 }.getOrDefault(false)

    private fun loadHistory(context: Context, today: Long): List<ProgramRecommender.Session> =
        HistoryDatabase.getDatabase(context).historyDao()
            .getHistoryInRange(today - HISTORY_DAYS, today).orEmpty()
            .map { item ->
                val seconds = if (item.duration > 0) item.duration else maxOf(0, item.realDuration)
                val hour = Calendar.getInstance().apply { timeInMillis = item.startTime }.get(Calendar.HOUR_OF_DAY)
                ProgramRecommender.Session(item.programId, item.dateId, hour, seconds / 60, item.isPassed)
            }
            .sortedBy { it.dateId }

    /** Gợi ý từ hồ sơ chưa lưu (màn tóm tắt onboarding: chưa có lịch sử). */
    @WorkerThread
    fun recommendFor(context: Context, profile: ProgramRecommender.Profile): Result? {
        val programs = runCatching { FitnessApplication.getApplication().mJsonHelper.allCategoryHashMap }.getOrNull().orEmpty()
        return recommend(
            context,
            programs.values.toList(),
            profile,
            emptyList(),
            ProgramRecommender.Context(DateIdUtils.todayDateId(), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), 0, 0, false, firstWorkout = true),
            0,
        )
    }

    private fun recommend(
        context: Context,
        programs: List<ProgramObject>,
        profile: ProgramRecommender.Profile,
        history: List<ProgramRecommender.Session>,
        ctx: ProgramRecommender.Context,
        streakDays: Int,
    ): Result? {
        val candidates = programs.mapNotNull(::toCandidate)
        val rec = ProgramRecommender(weights()).recommend(candidates, profile, history, ctx) ?: return null
        val program = programs.firstOrNull { it.id == rec.programId } ?: return null
        return Result(program, reasonText(context, rec, streakDays), rec)
    }

    fun profile(pref: MyPreference, context: Context? = null): ProgramRecommender.Profile = ProgramRecommender.Profile(
        // Onboarding Min: lose_weight / build_muscle (săn chắc) / keep_fit.
        goal = when (pref.userGoal) {
            "lose_weight" -> ProgramRecommender.Goal.LOSE_WEIGHT
            "build_muscle" -> ProgramRecommender.Goal.TONE_UP
            "keep_fit" -> ProgramRecommender.Goal.HEALTH
            else -> null
        },
        areas = pref.userFocusAreas.orEmpty().split(",").mapNotNull { areaOfFocusName(it.trim()) }.toSet(),
        level = when (pref.userExperience) {
            "advanced" -> 3
            "intermediate" -> 2
            else -> 1
        },
        isPremium = pref.isPremiumMember,
        feedback = context?.let(::feedback).orEmpty(),
        noJump = pref.isNoJump,
    )

    /** Đánh giá độ khó lưu ở màn kết thúc (FEEL_<programId>). */
    private fun feedback(context: Context): Map<Int, Int> =
        PreferenceManager.getDefaultSharedPreferences(context).all
            .filterKeys { it.startsWith("FEEL_") }
            .mapNotNull { (key, value) -> key.removePrefix("FEEL_").toIntOrNull()?.let { id -> (value as? Int)?.let { id to it } } }
            .toMap()

    /** Tên vùng lưu từ onboarding (OnboardingFocus.name viết thường). Toàn thân = không giới hạn vùng. */
    private fun areaOfFocusName(name: String): Area? = when (name.lowercase(Locale.US)) {
        "belly", "abs" -> Area.BELLY
        "arm", "arms" -> Area.ARMS
        "butt" -> Area.BUTT
        "leg", "legs", "thigh" -> Area.LEGS
        else -> null
    }

    /** Vùng tập của chương trình theo trường focus (null = toàn thân / chung). */
    @JvmStatic
    fun areaOf(p: ProgramObject): Area? = when (p.focus.orEmpty().ifBlank { focusFromTags(p) }.lowercase(Locale.US)) {
        "abs" -> Area.BELLY
        "arm" -> Area.ARMS
        "butt" -> Area.BUTT
        "thigh", "leg" -> Area.LEGS
        "breast" -> Area.CHEST
        else -> null
    }

    /** Bài lẻ (type 2) có thể gợi ý, tag lấy từ tag_category (chuẩn hoá bằng tools/normalize_tags.py). */
    @JvmStatic
    fun toCandidate(p: ProgramObject): ProgramRecommender.Candidate? {
        if (p.type != 2 || p.id in EXCLUDED_IDS) return null
        if (p.focus.orEmpty().lowercase(Locale.US) == "face" || hasTag(p, "face_firming_yoga")) return null
        val area = areaOf(p)
        val tags = tagsOf(p)
        return ProgramRecommender.Candidate(
            p.id, area, levelOf(p, (p.name.orEmpty() + " " + p.subtitle.orEmpty()).lowercase(Locale.US)), p.total, p.premium == 1, tags,
            // Các cấp của một bài dùng chung ảnh (flat_belly.webp, w1401.webp...).
            series = p.seriesKey(),
        )
    }

    /** Vài bài Min để trống focus nhưng có vùng trong tag ("..., beginner, thigh"). */
    private fun focusFromTags(p: ProgramObject): String =
        listOf("abs", "arm", "butt", "thigh", "leg", "breast").firstOrNull { hasTag(p, it) }.orEmpty()

    /** Nhãn trong trường "tag" của Min ("longer_than_7_minute, beginner, abs"), so khớp nguyên từ. */
    private fun hasTag(p: ProgramObject, tag: String): Boolean =
        p.tags.orEmpty().split(",").any { it.trim().equals(tag, ignoreCase = true) }

    /** Các cấp của một bài dùng chung tên (abs_1/2/3.webp cùng tên "Cơ bụng"); bài lẻ không có cấp -> null. */
    private fun ProgramObject.seriesKey(): String? =
        if (subtitle.isNullOrBlank()) null else name?.trim()?.lowercase(Locale.US)

    @JvmStatic
    fun tagsOf(p: ProgramObject): Set<Tag> = buildSet {
        if (hasTag(p, "hiit")) add(Tag.HIIT)
        if (hasTag(p, "low_impact")) add(Tag.LOW_IMPACT)
        if (hasTag(p, "yoga") || hasTag(p, "pilates")) add(Tag.YOGA)
        if (hasTag(p, "stretch")) add(Tag.STRETCH)
        if (hasTag(p, "pain_relief")) add(Tag.PAIN)
        if (hasTag(p, "morning")) add(Tag.MORNING)
        if (hasTag(p, "bedtime")) add(Tag.BEDTIME)
        // Nhẹ: giãn cơ, giảm đau, yoga nhẹ, bài thường ngày ngắn. Không xét độ khó, hợp ngày phục hồi.
        val gentleYoga = hasTag(p, "yoga") && p.level <= 1
        val shortRoutine = hasTag(p, "routine") && p.total <= 6
        if (Tag.STRETCH in this || Tag.PAIN in this || Tag.BEDTIME in this || gentleYoga || shortRoutine) add(Tag.LIGHT)
        if (areaOf(p) == null) add(Tag.GENERAL)
    }

    /**
     * Cấp độ thật của bài: nhiều bài để level = 1 cho mọi cấp, cấp nằm trong tên ("... Advanced"), nên đọc tên
     * trước (trước đây "Upper body Advanced" bị coi là bài cấp 1 và được gợi ý cho người mới).
     */
    private fun levelOf(p: ProgramObject, lowerName: String): Int = when {
        "advanced" in lowerName -> 3
        "intermediate" in lowerName -> 2
        "beginner" in lowerName -> 1
        else -> p.level.coerceIn(1, 3)
    }

    fun reasonText(context: Context, rec: ProgramRecommender.Recommendation, streakDays: Int): String = when (rec.reason) {
        Reason.FOCUS -> rec.reasonArea?.let { context.getString(R.string.rec_reason_focus, context.getString(areaLabel(it))) }
            ?: context.getString(R.string.rec_reason_default)
        Reason.GOAL_LOSE -> context.getString(R.string.rec_reason_lose)
        Reason.GOAL_FLEX -> context.getString(R.string.rec_reason_flex)
        Reason.GOAL_HEALTH -> context.getString(R.string.rec_reason_health)
        Reason.LEVEL_UP -> context.getString(R.string.rec_reason_level_up)
        Reason.HABIT -> context.getString(R.string.rec_reason_habit)
        Reason.RECOVERY -> context.getString(R.string.rec_reason_recovery, streakDays)
        Reason.COMEBACK -> context.getString(R.string.rec_reason_comeback)
        Reason.NEW -> context.getString(R.string.rec_reason_new)
        Reason.EXTRA -> context.getString(R.string.home_done_sub)
        Reason.MORNING -> context.getString(R.string.rec_reason_morning)
        Reason.BEDTIME -> context.getString(R.string.rec_reason_bedtime)
        Reason.DEFAULT -> context.getString(R.string.rec_reason_default)
    }

    private fun areaLabel(area: Area): Int = when (area) {
        Area.BELLY -> R.string.belly
        Area.ARMS -> R.string.arm
        Area.BUTT -> R.string.ass
        Area.LEGS -> R.string.thigh
        Area.CHEST -> R.string.chest
    }

    /** Trọng số mặc định, ghi đè từng khoá bằng JSON trong Remote Config recommend_weights. */
    private fun weights(): ProgramRecommender.Weights {
        val raw = runCatching { FirebaseRemoteConfig.getInstance().getString(RemoteConfigKeys.RECOMMEND_WEIGHTS) }.getOrNull()
        if (raw.isNullOrBlank()) return ProgramRecommender.Weights()
        return runCatching {
            val json = JSONObject(raw)
            val d = ProgramRecommender.Weights()
            fun i(key: String, default: Int) = if (json.has(key)) json.getInt(key) else default
            d.copy(
                focusMatch = i("focusMatch", d.focusMatch), generalForFullBody = i("generalForFullBody", d.generalForFullBody),
                goal = i("goal", d.goal), goalFlex = i("goalFlex", d.goalFlex),
                levelMatch = i("levelMatch", d.levelMatch), levelFar = i("levelFar", d.levelFar),
                habitDuration = i("habitDuration", d.habitDuration), habitDurationFar = i("habitDurationFar", d.habitDurationFar),
                eveningShort = i("eveningShort", d.eveningShort), recoveryLight = i("recoveryLight", d.recoveryLight),
                recoveryHiit = i("recoveryHiit", d.recoveryHiit), heavyYesterdayLight = i("heavyYesterdayLight", d.heavyYesterdayLight),
                comebackShort = i("comebackShort", d.comebackShort), comebackEasy = i("comebackEasy", d.comebackEasy),
                recentRepeat = i("recentRepeat", d.recentRepeat), sameAreaYesterday = i("sameAreaYesterday", d.sameAreaYesterday),
                neverTried = i("neverTried", d.neverTried), abandoned = i("abandoned", d.abandoned),
                premiumLocked = i("premiumLocked", d.premiumLocked), premiumTeaserEvery = i("premiumTeaserEvery", d.premiumTeaserEvery),
                extraLight = i("extraLight", d.extraLight), levelUpSessions = i("levelUpSessions", d.levelUpSessions),
                seriesNext = i("seriesNext", d.seriesNext), seriesDoneSessions = i("seriesDoneSessions", d.seriesDoneSessions),
                morning = i("morning", d.morning), bedtime = i("bedtime", d.bedtime), focusBalance = i("focusBalance", d.focusBalance),
                lateHiit = i("lateHiit", d.lateHiit),
                noJumpLowImpact = i("noJumpLowImpact", d.noJumpLowImpact),
            )
        }.getOrDefault(ProgramRecommender.Weights())
    }

    // ------------------------------------------------------------------ analytics

    /** Home vừa hiện gợi ý: log một lần mỗi ngày. */
    fun logShown(context: Context, result: Result) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val today = DateIdUtils.todayDateId()
        if (prefs.getLong(KEY_SHOWN_DAY, -1L) == today) return
        prefs.edit().putLong(KEY_SHOWN_DAY, today).apply()
        log(context, "recommend_shown", result.program.id, result.recommendation.reason.name)
    }

    fun logStart(context: Context, result: Result) =
        log(context, "recommend_start", result.program.id, result.recommendation.reason.name)

    /** Gọi ở màn kết thúc buổi tập: nếu đó là bài được gợi ý hôm nay thì log hoàn thành. */
    @JvmStatic
    fun logCompleteIfRecommended(context: Context, programId: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (prefs.getInt(KEY_ID, -1) == programId && prefs.getLong(KEY_DAY, -1L) == DateIdUtils.todayDateId()) {
            // Chỉ log một lần cho mỗi gợi ý (màn kết thúc có thể được tạo lại khi xoay / quay lại).
            prefs.edit().remove(KEY_ID).apply()
            log(context, "recommend_complete", programId, null)
        }
    }

    private fun log(context: Context, event: String, programId: Int, reason: String?) {
        FirebaseAnalytics.getInstance(context).logEvent(
            event,
            Bundle().apply {
                putInt("program_id", programId)
                reason?.let { putString("reason", it.lowercase(Locale.US)) }
            },
        )
    }
}
