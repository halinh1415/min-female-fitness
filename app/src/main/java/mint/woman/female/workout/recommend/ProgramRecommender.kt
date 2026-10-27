package mint.woman.female.workout.recommend

import kotlin.math.abs

/**
 * Gợi ý "Bài tập hôm nay" bằng cách chấm điểm theo luật (không dùng ML: dữ liệu mỗi người còn ít).
 * Lớp thuần Kotlin, không phụ thuộc Android - xem [RecommendationService] cho phần đọc dữ liệu thật,
 * và ProgramRecommenderTest cho từng luật.
 *
 * Mỗi bài ứng viên được cộng/trừ điểm theo: sở thích (vùng tập, mục tiêu), trình độ (tự nâng bậc, lên cấp
 * trong cùng chuỗi bài), thói quen (thời lượng, giờ tập), buổi sáng / trước giờ ngủ, cân bằng các vùng đã
 * chọn trong tuần, streak/phục hồi, đa dạng, tỉ lệ bỏ dở và premium. Bài điểm cao
 * nhất thắng; hoà điểm thì xáo trộn cố định theo ngày nên trong một ngày luôn ra cùng một bài.
 */
class ProgramRecommender(private val weights: Weights = Weights()) {

    /** CHEST không có trong onboarding: chỉ để bài ngực không bị coi là bài toàn thân. */
    enum class Area { BELLY, ARMS, BUTT, LEGS, CHEST }
    enum class Goal { LOSE_WEIGHT, TONE_UP, FLEXIBILITY, HEALTH }
    /** Lấy từ tag_category của chương trình (xem RecommendationService.toCandidate). */
    enum class Tag { HIIT, LIGHT, GENERAL, LOW_IMPACT, YOGA, STRETCH, PAIN, MORNING, BEDTIME }

    /** Một bài tập có thể gợi ý (đã chuẩn hoá từ ProgramObject). */
    data class Candidate(
        val id: Int,
        /** null = toàn thân / chung. */
        val area: Area?,
        val level: Int,
        val minutes: Int,
        val premium: Boolean,
        val tags: Set<Tag> = emptySet(),
        /** Các cấp của cùng một bài (Beginner/Intermediate/Advanced) có chung khoá; null = bài lẻ. */
        val series: String? = null,
    )

    data class Profile(
        val goal: Goal? = null,
        val areas: Set<Area> = emptySet(),
        /** Trình độ 1..3 (onboarding: mới bắt đầu / trung bình / nâng cao). */
        val level: Int = 1,
        val isPremium: Boolean = false,
        /** Đánh giá độ khó sau buổi tập (màn kết thúc): programId -> 0 quá khó .. 4 quá dễ. */
        val feedback: Map<Int, Int> = emptyMap(),
        /** Đau gối / lưng dưới: không gợi ý bài HIIT (nhiều động tác bật nhảy). */
        val noJump: Boolean = false,
    )

    /** Một buổi tập trong lịch sử. [dateId] cùng cách đánh số ngày với phần còn lại của app. */
    data class Session(
        val programId: Int,
        val dateId: Long,
        val startHour: Int,
        val minutes: Int,
        val completed: Boolean,
    )

    data class Context(
        val today: Long,
        val hour: Int,
        val streakDays: Int,
        /** Số ngày nghỉ liền trước hôm nay (0 = hôm qua có tập). */
        val missedDays: Int,
        /** Hôm nay đã tập xong: gợi ý bài nhẹ để "tập thêm chút". */
        val didToday: Boolean,
        /** Chưa từng tập buổi nào: buổi đầu luôn là bài dễ, ngắn (bất kể trình độ chọn ở onboarding). */
        val firstWorkout: Boolean = false,
    )

    /** Lý do chính hiện cho người dùng (lấy từ luật cộng điểm nhiều nhất). */
    enum class Reason { FOCUS, GOAL_LOSE, GOAL_FLEX, GOAL_HEALTH, LEVEL_UP, HABIT, RECOVERY, COMEBACK, NEW, EXTRA, MORNING, BEDTIME, DEFAULT }

    data class Recommendation(val programId: Int, val score: Int, val reason: Reason, val reasonArea: Area? = null)

    /** Trọng số từng luật; có thể ghi đè qua Remote Config (xem [RecommendationService]). */
    data class Weights(
        val focusMatch: Int = 40,
        val generalForFullBody: Int = 20,
        val goal: Int = 15,
        val goalFlex: Int = 30,
        val levelMatch: Int = 20,
        val levelFar: Int = -20,
        val habitDuration: Int = 10,
        val habitDurationFar: Int = -5,
        val eveningShort: Int = 5,
        val recoveryLight: Int = 25,
        val recoveryHiit: Int = -15,
        val heavyYesterdayLight: Int = 10,
        val comebackShort: Int = 20,
        val comebackEasy: Int = 10,
        val recentRepeat: Int = -25,
        val sameAreaYesterday: Int = -10,
        val neverTried: Int = 8,
        val abandoned: Int = -15,
        val premiumLocked: Int = -60,
        /** Cứ mỗi N ngày có một "ngày giới thiệu" bài premium cho người dùng thường (0 = tắt). */
        val premiumTeaserEvery: Int = 5,
        val extraLight: Int = 60,
        val levelUpSessions: Int = 3,
        /** Bài bị chấm "khó/quá khó": trừ chính bài đó và các bài cùng vùng khó hơn hoặc bằng. */
        val feedbackHard: Int = -25,
        val feedbackHardSameArea: Int = -10,
        /** Bài bị chấm "dễ/quá dễ": ưu tiên bậc kế tiếp cùng vùng, bài đó thì giảm nhẹ. */
        val feedbackEasyNext: Int = 25,
        val feedbackEasySame: Int = -5,
        /** Buổi tập đầu tiên: cộng cho bài cấp 1 và bài ngắn, trừ mạnh bài dài / HIIT. */
        val firstEasy: Int = 25,
        val firstShort: Int = 30,
        val firstLong: Int = -50,
        val firstHiit: Int = -20,
        val noJumpHiit: Int = -40,
        val noJumpLowImpact: Int = 10,
        /** Đã tập xong cấp hiện tại của một chuỗi đủ [seriesDoneSessions] lần (không chấm khó): mời cấp kế tiếp. */
        val seriesNext: Int = 35,
        val seriesDoneSessions: Int = 2,
        /** 5h–10h: bài buổi sáng; từ 21h: bài thư giãn trước khi ngủ. */
        val morning: Int = 25,
        val bedtime: Int = 25,
        /** Từ 21h: không mời HIIT. */
        val lateHiit: Int = -10,
        /** Chọn nhiều vùng: vùng ít được tập nhất trong 7 ngày được cộng thêm. */
        val focusBalance: Int = 10,
    )

    fun recommend(
        candidates: List<Candidate>,
        profile: Profile,
        history: List<Session>,
        context: Context,
    ): Recommendation? = rank(candidates, profile, history, context).firstOrNull()

    /** Toàn bộ ứng viên xếp theo điểm giảm dần (Discover dùng top N cho "Dành cho bạn"). */
    fun rank(
        candidates: List<Candidate>,
        profile: Profile,
        history: List<Session>,
        context: Context,
    ): List<Recommendation> {
        if (candidates.isEmpty()) return emptyList()
        val baseLevel = effectiveLevel(profile, candidates, history, context.today)
        val levelUp = baseLevel > profile.level
        // Quay lại sau nhiều ngày nghỉ: hạ một bậc độ khó cho dễ bắt đầu lại.
        val comeback = context.missedDays >= 3
        val effectiveLevel = when {
            context.firstWorkout -> 1
            comeback -> (baseLevel - 1).coerceAtLeast(1)
            else -> baseLevel
        }
        val typicalMinutes = median(history.filter { it.completed }.takeLast(20).map { it.minutes })
        val eveningHabit = median(history.takeLast(20).map { it.startHour })?.let { it >= 19 } ?: false
        val byId = candidates.associateBy { it.id }
        val yesterday = history.filter { it.dateId == context.today - 1 }
        val yesterdayAreas = yesterday.mapNotNull { byId[it.programId]?.area }.toSet()
        val heavyYesterday = yesterday.sumOf { it.minutes } >= 25
        val recent = history.filter { context.today - it.dateId in 0..2 }.map { it.programId }.toSet()
        val tried = history.map { it.programId }.toSet()
        val teaserDay = weights.premiumTeaserEvery > 0 && context.today % weights.premiumTeaserEvery == 0L
        val nextInSeries = nextSeriesLevels(byId, profile, history, context.today)
        val morningTime = context.hour in 5..10
        val bedTime = context.hour >= 21 || context.hour < 2
        // Vùng (trong các vùng đã chọn) ít được tập nhất 7 ngày qua: cộng điểm để các vùng đều nhau.
        val weakAreas = if (profile.areas.size < 2) emptySet() else {
            val counts = profile.areas.associateWith { area ->
                history.count { it.completed && context.today - it.dateId in 0..6 && byId[it.programId]?.area == area }
            }
            val min = counts.values.minOrNull() ?: 0
            if (counts.values.all { it == min }) emptySet() else counts.filterValues { it == min }.keys
        }

        val scored = candidates.map { c ->
            val parts = mutableMapOf<Reason, Int>()
            fun add(reason: Reason, points: Int) {
                parts[reason] = (parts[reason] ?: 0) + points
            }

            // 1. Sở thích: vùng tập + mục tiêu.
            when {
                c.area != null && c.area in profile.areas -> add(Reason.FOCUS, weights.focusMatch)
                c.area == null && profile.areas.isEmpty() -> add(Reason.FOCUS, weights.generalForFullBody)
            }
            if (c.area != null && c.area in weakAreas) add(Reason.FOCUS, weights.focusBalance)
            when (profile.goal) {
                Goal.LOSE_WEIGHT -> when {
                    Tag.HIIT in c.tags -> add(Reason.GOAL_LOSE, weights.goal)
                    // Không nhảy được: bài đốt mỡ ít tác động thay cho HIIT.
                    Tag.LOW_IMPACT in c.tags -> add(Reason.GOAL_LOSE, if (profile.noJump) weights.goal else weights.goal * 2 / 3)
                    c.area == null && Tag.LIGHT !in c.tags -> add(Reason.GOAL_LOSE, weights.goal / 2)
                }
                Goal.FLEXIBILITY -> if (Tag.LIGHT in c.tags || Tag.STRETCH in c.tags || Tag.YOGA in c.tags) add(Reason.GOAL_FLEX, weights.goalFlex)
                Goal.HEALTH -> if (c.area == null || Tag.LIGHT in c.tags || Tag.LOW_IMPACT in c.tags) add(Reason.GOAL_HEALTH, weights.goal / 2)
                Goal.TONE_UP -> if (c.area != null || (Tag.YOGA in c.tags && Tag.LIGHT !in c.tags)) add(Reason.FOCUS, weights.goal / 2)
                null -> Unit
            }

            // 2. Trình độ (bài nhẹ/giãn cơ không xét độ khó).
            if (Tag.LIGHT !in c.tags) {
                when (abs(c.level - effectiveLevel)) {
                    0 -> add(if (levelUp && !comeback) Reason.LEVEL_UP else Reason.DEFAULT, weights.levelMatch)
                    1 -> Unit
                    else -> add(Reason.DEFAULT, weights.levelFar)
                }
            }

            // 2b. Cảm nhận độ khó sau buổi tập.
            profile.feedback.forEach { (programId, feel) ->
                val rated = byId[programId] ?: return@forEach
                when {
                    feel <= 1 && c.id == programId -> add(Reason.DEFAULT, weights.feedbackHard)
                    feel <= 1 && c.area != null && c.area == rated.area && c.level >= rated.level ->
                        add(Reason.DEFAULT, weights.feedbackHardSameArea)
                    feel >= 3 && c.id == programId -> add(Reason.DEFAULT, weights.feedbackEasySame)
                    feel >= 3 && c.area != null && c.area == rated.area && c.level == rated.level + 1 ->
                        add(Reason.LEVEL_UP, weights.feedbackEasyNext)
                }
            }

            // 2c. Lên cấp trong cùng chuỗi bài đã tập quen.
            if (c.id in nextInSeries) add(Reason.LEVEL_UP, weights.seriesNext)

            // 3. Thói quen.
            if (typicalMinutes != null) {
                val diff = abs(c.minutes - typicalMinutes)
                if (diff <= 3) add(Reason.HABIT, weights.habitDuration) else if (diff >= 10) add(Reason.DEFAULT, weights.habitDurationFar)
            }
            if (eveningHabit && context.hour >= 19 && c.minutes <= 10) add(Reason.HABIT, weights.eveningShort)
            if (morningTime && Tag.MORNING in c.tags) add(Reason.MORNING, weights.morning)
            if (bedTime && Tag.BEDTIME in c.tags) add(Reason.BEDTIME, weights.bedtime)
            if (bedTime && Tag.HIIT in c.tags) add(Reason.DEFAULT, weights.lateHiit)

            // 4. Streak & phục hồi.
            if (context.streakDays >= 6) {
                if (Tag.LIGHT in c.tags) add(Reason.RECOVERY, weights.recoveryLight)
                if (Tag.HIIT in c.tags) add(Reason.DEFAULT, weights.recoveryHiit)
            }
            if (heavyYesterday && Tag.LIGHT in c.tags) add(Reason.RECOVERY, weights.heavyYesterdayLight)
            if (comeback) {
                if (c.minutes <= 12) add(Reason.COMEBACK, weights.comebackShort)
                if (c.level == 1) add(Reason.COMEBACK, weights.comebackEasy)
            }
            if (context.didToday && (Tag.LIGHT in c.tags || c.minutes <= 7)) add(Reason.EXTRA, weights.extraLight)
            if (profile.noJump && Tag.HIIT in c.tags) add(Reason.DEFAULT, weights.noJumpHiit)
            if (profile.noJump && Tag.LOW_IMPACT in c.tags) add(Reason.DEFAULT, weights.noJumpLowImpact)
            if (context.firstWorkout) {
                if (c.level == 1) add(Reason.DEFAULT, weights.firstEasy)
                when {
                    c.minutes <= FIRST_MAX_MINUTES -> add(Reason.DEFAULT, weights.firstShort)
                    c.minutes > FIRST_LONG_MINUTES -> add(Reason.DEFAULT, weights.firstLong)
                }
                if (Tag.HIIT in c.tags) add(Reason.DEFAULT, weights.firstHiit)
            }

            // 5. Đa dạng.
            if (c.id in recent) add(Reason.DEFAULT, weights.recentRepeat)
            if (c.area != null && c.area in yesterdayAreas) add(Reason.DEFAULT, weights.sameAreaYesterday)
            if (c.id !in tried && history.isNotEmpty()) add(Reason.NEW, weights.neverTried)

            // 6. Bài hay bỏ dở.
            val attempts = history.filter { it.programId == c.id }
            if (attempts.size >= 2 && attempts.count { !it.completed } * 2 >= attempts.size) add(Reason.DEFAULT, weights.abandoned)

            // 7. Premium.
            if (c.premium && !profile.isPremium && !teaserDay) add(Reason.DEFAULT, weights.premiumLocked)

            val total = parts.values.sum()
            // Lý do hiện cho người dùng: ưu tiên tình huống hôm nay (phục hồi, quay lại...) hơn sở thích chung.
            val reason = REASON_PRIORITY.firstOrNull { (parts[it] ?: 0) >= MIN_REASON_POINTS } ?: Reason.DEFAULT
            Triple(c, total, reason)
        }

        // Hoà điểm: xáo trộn cố định theo ngày để mỗi ngày một bài khác, nhưng trong ngày không đổi.
        return scored
            .sortedWith(compareByDescending<Triple<Candidate, Int, Reason>> { it.second }.thenByDescending { jitter(it.first.id, context.today) })
            .map { (candidate, score, reason) ->
                Recommendation(
                    programId = candidate.id,
                    score = score,
                    reason = if (context.didToday) Reason.EXTRA else reason,
                    reasonArea = candidate.area?.takeIf { it in profile.areas },
                )
            }
    }

    /**
     * Bài cấp kế tiếp của các chuỗi đã hoàn thành ít nhất [Weights.seriesDoneSessions] buổi ở cấp cao nhất đã tập
     * (30 ngày), trừ khi bài đó bị chấm khó.
     */
    internal fun nextSeriesLevels(byId: Map<Int, Candidate>, profile: Profile, history: List<Session>, today: Long): Set<Int> {
        val done = history.filter { it.completed && today - it.dateId <= 30 }.mapNotNull { byId[it.programId] }.filter { it.series != null }
        return done.groupBy { it.series!! }.mapNotNull { (series, list) ->
            val top = list.maxOf { it.level }
            val atTop = list.filter { it.level == top }
            if (atTop.size < weights.seriesDoneSessions || atTop.any { (profile.feedback[it.id] ?: 2) <= 1 }) return@mapNotNull null
            byId.values.firstOrNull { it.series == series && it.level == top + 1 }?.id
        }.toSet()
    }

    /** Trình độ hiệu lực: tự nâng một bậc khi đã hoàn thành đủ buổi ở bậc hiện tại trong 30 ngày. */
    internal fun effectiveLevel(profile: Profile, candidates: List<Candidate>, history: List<Session>, today: Long): Int {
        val levelOf = candidates.associate { it.id to it.level }
        val doneAtLevel = history.count { it.completed && today - it.dateId <= 30 && levelOf[it.programId] == profile.level }
        return if (doneAtLevel >= weights.levelUpSessions) (profile.level + 1).coerceAtMost(3) else profile.level.coerceIn(1, 3)
    }

    private fun median(values: List<Int>): Int? {
        if (values.isEmpty()) return null
        val sorted = values.sorted()
        return sorted[sorted.size / 2]
    }

    private companion object {
        val REASON_PRIORITY = listOf(
            Reason.EXTRA, Reason.RECOVERY, Reason.COMEBACK, Reason.BEDTIME, Reason.MORNING, Reason.LEVEL_UP,
            Reason.GOAL_FLEX, Reason.GOAL_LOSE, Reason.GOAL_HEALTH, Reason.FOCUS, Reason.HABIT, Reason.NEW,
        )
        const val MIN_REASON_POINTS = 8
        const val FIRST_MAX_MINUTES = 15
        const val FIRST_LONG_MINUTES = 20
    }

    private fun jitter(id: Int, day: Long): Int = ((id * 31L + day * 17L) % 97).toInt()
}
