package mint.woman.female.workout.recommend

import mint.woman.female.workout.recommend.ProgramRecommender.Area
import mint.woman.female.workout.recommend.ProgramRecommender.Candidate
import mint.woman.female.workout.recommend.ProgramRecommender.Context
import mint.woman.female.workout.recommend.ProgramRecommender.Goal
import mint.woman.female.workout.recommend.ProgramRecommender.Profile
import mint.woman.female.workout.recommend.ProgramRecommender.Reason
import mint.woman.female.workout.recommend.ProgramRecommender.Session
import mint.woman.female.workout.recommend.ProgramRecommender.Tag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Từng luật chấm điểm của ProgramRecommender. */
class ProgramRecommenderTest {

    private val recommender = ProgramRecommender()
    private val today = 20_000L

    private val absBeginner = Candidate(2, Area.BELLY, 1, 12, premium = false)
    private val absMid = Candidate(3, Area.BELLY, 2, 17, premium = false)
    private val absHard = Candidate(4, Area.BELLY, 3, 25, premium = false)
    private val buttBeginner = Candidate(6, Area.BUTT, 1, 11, premium = false)
    private val stretch = Candidate(959, null, 1, 5, premium = false, tags = setOf(Tag.LIGHT, Tag.GENERAL))
    private val hiit = Candidate(345, null, 1, 7, premium = false, tags = setOf(Tag.HIIT, Tag.GENERAL))
    private val all = listOf(absBeginner, absMid, absHard, buttBeginner, stretch, hiit)

    private fun ctx(streak: Int = 0, missed: Int = 0, didToday: Boolean = false, hour: Int = 9) =
        Context(today, hour, streak, missed, didToday)

    private fun session(id: Int, daysAgo: Long, minutes: Int = 12, completed: Boolean = true, hour: Int = 9) =
        Session(id, today - daysAgo, hour, minutes, completed)

    @Test
    fun emptyCandidatesGiveNothing() {
        assertNull(recommender.recommend(emptyList(), Profile(), emptyList(), ctx()))
    }

    @Test
    fun picksChosenFocusAreaAtUserLevel() {
        val rec = recommender.recommend(all, Profile(areas = setOf(Area.BELLY), level = 1), emptyList(), ctx())!!
        assertEquals(absBeginner.id, rec.programId)
        assertEquals(Reason.FOCUS, rec.reason)
        assertEquals(Area.BELLY, rec.reasonArea)
    }

    @Test
    fun matchesLevel() {
        val rec = recommender.recommend(all, Profile(areas = setOf(Area.BELLY), level = 3), emptyList(), ctx())!!
        assertEquals(absHard.id, rec.programId)
    }

    @Test
    fun levelsUpAfterEnoughSessionsAtCurrentLevel() {
        val history = listOf(session(absBeginner.id, 9), session(absBeginner.id, 7), session(buttBeginner.id, 5))
        assertEquals(2, recommender.effectiveLevel(Profile(level = 1), all, history, today))
        val rec = recommender.recommend(all, Profile(areas = setOf(Area.BELLY), level = 1), history, ctx())!!
        assertEquals(absMid.id, rec.programId)
        assertEquals(Reason.LEVEL_UP, rec.reason)
    }

    @Test
    fun avoidsWorkoutDoneInLastTwoDays() {
        val history = listOf(session(absBeginner.id, 1))
        val rec = recommender.recommend(all, Profile(areas = setOf(Area.BELLY), level = 1), history, ctx())!!
        assertNotEquals(absBeginner.id, rec.programId)
    }

    @Test
    fun longStreakSuggestsRecovery() {
        val rec = recommender.recommend(all, Profile(level = 1), emptyList(), ctx(streak = 7))!!
        assertEquals(stretch.id, rec.programId)
        assertEquals(Reason.RECOVERY, rec.reason)
    }

    @Test
    fun comebackPrefersShortEasyWorkout() {
        val rec = recommender.recommend(all, Profile(areas = setOf(Area.BELLY), level = 3), emptyList(), ctx(missed = 5))!!
        // Lâu không tập: không chọn bài khó nhất dù đúng trình độ, mà bài nhẹ hơn và ngắn hơn.
        val pick = all.first { it.id == rec.programId }
        assert(pick.level < 3 && pick.minutes < absHard.minutes)
        assertEquals(Reason.COMEBACK, rec.reason)
    }

    @Test
    fun firstWorkoutIsShortAndEasyWhateverTheChosenLevel() {
        val first = Context(today, 9, 0, 0, false, firstWorkout = true)
        val rec = recommender.recommend(all, Profile(areas = setOf(Area.BELLY), level = 3), emptyList(), first)!!
        val pick = all.first { it.id == rec.programId }
        assertEquals(1, pick.level)
        assert(pick.minutes <= 15)
        assertNotEquals(hiit.id, pick.id)
    }

    @Test
    fun weightLossGoalLikesHiit() {
        val rec = recommender.recommend(all, Profile(goal = Goal.LOSE_WEIGHT, level = 1), emptyList(), ctx())!!
        assertEquals(hiit.id, rec.programId)
    }

    @Test
    fun flexibilityGoalLikesStretching() {
        val rec = recommender.recommend(all, Profile(goal = Goal.FLEXIBILITY, level = 1), emptyList(), ctx())!!
        assertEquals(stretch.id, rec.programId)
        assertEquals(Reason.GOAL_FLEX, rec.reason)
    }

    @Test
    fun penalisesFrequentlyAbandonedWorkout() {
        val history = listOf(session(absBeginner.id, 10, completed = false), session(absBeginner.id, 8, completed = false))
        val rec = recommender.recommend(listOf(absBeginner, absMid), Profile(areas = setOf(Area.BELLY), level = 1), history, ctx())!!
        assertEquals(absMid.id, rec.programId)
    }

    @Test
    fun hidesPremiumForFreeUsersExceptTeaserDay() {
        val locked = Candidate(41, Area.BELLY, 1, 7, premium = true)
        val candidates = listOf(locked, buttBeginner)
        val profile = Profile(areas = setOf(Area.BELLY), level = 1)
        val normalDay = Context(today = 20_001, hour = 9, streakDays = 0, missedDays = 0, didToday = false)
        assertEquals(buttBeginner.id, recommender.recommend(candidates, profile, emptyList(), normalDay)!!.programId)
        val teaserDay = normalDay.copy(today = 20_000) // chia hết cho 5
        assertEquals(locked.id, recommender.recommend(candidates, profile, emptyList(), teaserDay)!!.programId)
        // Người dùng Premium: không bị trừ điểm.
        assertEquals(locked.id, recommender.recommend(candidates, profile.copy(isPremium = true), emptyList(), normalDay)!!.programId)
    }

    @Test
    fun afterTrainingTodaySuggestsSomethingLight() {
        val rec = recommender.recommend(all, Profile(areas = setOf(Area.BELLY), level = 2), listOf(session(absMid.id, 0)), ctx(didToday = true))!!
        assertEquals(Reason.EXTRA, rec.reason)
        val pick = all.first { it.id == rec.programId }
        assert(Tag.LIGHT in pick.tags || pick.minutes <= 7)
    }

    @Test
    fun sameInputsSameDayGiveSamePick() {
        val profile = Profile(level = 1)
        val first = recommender.recommend(all, profile, emptyList(), ctx())
        repeat(5) { assertEquals(first, recommender.recommend(all, profile, emptyList(), ctx())) }
    }

    @Test
    fun tooEasyFeedbackMovesToNextLevel() {
        val profile = Profile(areas = setOf(Area.BELLY), level = 1, feedback = mapOf(absBeginner.id to 4))
        val rec = recommender.recommend(all, profile, emptyList(), ctx())!!
        assertEquals(absMid.id, rec.programId)
        assertEquals(Reason.LEVEL_UP, rec.reason)
    }

    @Test
    fun tooHardFeedbackStepsBack() {
        val profile = Profile(areas = setOf(Area.BELLY), level = 2, feedback = mapOf(absMid.id to 0))
        val rec = recommender.recommend(all, profile, emptyList(), ctx())!!
        assertEquals(absBeginner.id, rec.programId)
    }

    @Test
    fun finishedSeriesLevelInvitesNextLevel() {
        val tummy1 = Candidate(1401, Area.BELLY, 1, 10, premium = false, series = "w1401.webp")
        val tummy2 = Candidate(1402, Area.BELLY, 2, 17, premium = false, series = "w1401.webp")
        val candidates = listOf(tummy1, tummy2, absBeginner)
        val history = listOf(session(tummy1.id, 5), session(tummy1.id, 3))
        val rec = recommender.recommend(candidates, Profile(areas = setOf(Area.BELLY), level = 1), history, ctx())!!
        assertEquals(tummy2.id, rec.programId)
        assertEquals(Reason.LEVEL_UP, rec.reason)
        // Chấm "quá khó" thì không mời lên cấp.
        val hard = Profile(areas = setOf(Area.BELLY), level = 1, feedback = mapOf(tummy1.id to 0))
        assertEquals(emptySet<Int>(), recommender.nextSeriesLevels(candidates.associateBy { it.id }, hard, history, today))
    }

    @Test
    fun lateEveningPrefersBedtimeStretchOverHiit() {
        val bedtime = Candidate(1503, null, 1, 5, premium = false, tags = setOf(Tag.LIGHT, Tag.STRETCH, Tag.BEDTIME, Tag.GENERAL))
        val rec = recommender.recommend(listOf(hiit, bedtime), Profile(level = 1), emptyList(), ctx(hour = 22))!!
        assertEquals(bedtime.id, rec.programId)
        assertEquals(Reason.BEDTIME, rec.reason)
        assertEquals(hiit.id, recommender.recommend(listOf(hiit, bedtime), Profile(level = 1), emptyList(), ctx(hour = 15))!!.programId)
    }

    @Test
    fun morningRoutineInTheMorning() {
        val wakeUp = Candidate(1500, null, 1, 4, premium = false, tags = setOf(Tag.LIGHT, Tag.MORNING, Tag.GENERAL))
        val plain = Candidate(1470, null, 1, 10, premium = false, tags = setOf(Tag.GENERAL))
        val rec = recommender.recommend(listOf(plain, wakeUp), Profile(goal = Goal.HEALTH, level = 1), emptyList(), ctx(hour = 7))!!
        assertEquals(wakeUp.id, rec.programId)
        assertEquals(Reason.MORNING, rec.reason)
    }

    @Test
    fun noJumpWeightLossPrefersLowImpact() {
        val lowImpact = Candidate(1461, null, 1, 12, premium = false, tags = setOf(Tag.LOW_IMPACT, Tag.GENERAL))
        val profile = Profile(goal = Goal.LOSE_WEIGHT, level = 1, noJump = true)
        assertEquals(lowImpact.id, recommender.recommend(listOf(hiit, lowImpact), profile, emptyList(), ctx())!!.programId)
        assertEquals(hiit.id, recommender.recommend(listOf(hiit, lowImpact), profile.copy(noJump = false), emptyList(), ctx())!!.programId)
    }

    @Test
    fun balancesChosenAreasOverTheWeek() {
        val history = listOf(session(absBeginner.id, 6), session(absBeginner.id, 4), session(buttBeginner.id, 5))
        val rec = recommender.recommend(listOf(absBeginner, buttBeginner), Profile(areas = setOf(Area.BELLY, Area.BUTT), level = 1), history, ctx())!!
        assertEquals(buttBeginner.id, rec.programId)
        assertEquals(Reason.FOCUS, rec.reason)
    }
}
