package mint.woman.female.workout.activity.ui.onboarding.onboard

import android.content.res.Resources
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingStepFactoryTest {

    /** Chữ trên màn không quan trọng ở đây: Resources giả trả "s<id>" để dựng danh sách màn ngoài thiết bị. */
    @Suppress("DEPRECATION")
    private class FakeResources : Resources(null, null, null) {
        override fun getString(id: Int): String = "s$id"
    }

    private val all = onboardSteps(FakeResources())

    @Test
    fun unknownRemoteValueFallsBackToQuick() {
        assertEquals(OnboardingVariant.QUICK_7, OnboardingVariant.fromRemoteValue("unknown"))
        assertEquals(OnboardingVariant.QUICK_7, OnboardingVariant.fromRemoteValue(null))
        assertEquals(OnboardingVariant.DEFAULT, OnboardingVariant.fromRemoteValue(null))
    }

    @Test
    fun legacyRemoteValuesStillMap() {
        assertEquals(OnboardingVariant.FULL, OnboardingVariant.fromRemoteValue("control_46"))
        assertEquals(OnboardingVariant.STANDARD_26, OnboardingVariant.fromRemoteValue("short_28"))
        assertEquals(OnboardingVariant.STANDARD_26, OnboardingVariant.fromRemoteValue("adaptive"))
        assertEquals(OnboardingVariant.QUICK_7, OnboardingVariant.fromRemoteValue(" Quick_7 "))
    }

    @Test
    fun fullUsesEveryScreen() {
        val steps = OnboardingStepFactory.create(OnboardingVariant.FULL, all)
        assertSame(all, steps)
        assertTrue("full should be 4x screens, was ${steps.size}", steps.size in 40..49)
        assertEquals(steps.size, steps.map { it.stableKey }.distinct().size)
    }

    @Test
    fun standardHasTwentySixScreensInOriginalOrder() {
        val steps = OnboardingStepFactory.create(OnboardingVariant.STANDARD_26, all)
        assertEquals(26, steps.size)
        assertOrderKept(steps)
        assertTrue(steps.any { it.kind == StepKind.TARGET_WEIGHT })
        assertTrue(steps.any { it.id == ObId.DISCOMFORT })
        assertEquals(StepKind.TRIAL_INTRO, steps.last().kind)
    }

    /** Nhánh quick_7 giữ tên cũ nhưng có 9 màn (thêm màn vùng tập và màn đau gối / lưng). */
    @Test
    fun quickHasNineScreensAndStillBuildsAPlan() {
        val steps = OnboardingStepFactory.create(OnboardingVariant.QUICK_7, all)
        assertEquals(9, steps.size)
        assertOrderKept(steps)
        assertEquals(StepKind.WELCOME, steps.first().kind)
        assertTrue(steps.any { it.id == ObId.FOCUS })
        assertTrue(steps.any { it.id == ObId.DISCOMFORT })
        assertTrue(steps.any { it.kind == StepKind.WEIGHT })
        assertTrue(steps.any { it.kind == StepKind.TARGET_WEIGHT })
        assertEquals(StepKind.PLAN_RESULT, steps.last().kind)
    }

    @Test
    fun targetWeightComesRightAfterCurrentWeight() {
        val weight = all.indexOfFirst { it.kind == StepKind.WEIGHT }
        assertEquals(StepKind.TARGET_WEIGHT, all[weight + 1].kind)
    }

    private fun assertOrderKept(steps: List<OnboardStep>) {
        val positions = steps.map { step -> all.indexOfFirst { it.stableKey == step.stableKey } }
        assertEquals(positions.sorted(), positions)
    }
}
