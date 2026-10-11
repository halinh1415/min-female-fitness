package mint.woman.female.workout.activity.ui.onboarding.onboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingStepFactoryTest {

    @Test
    fun unknownRemoteValueFallsBackToFull() {
        assertEquals(OnboardingVariant.FULL, OnboardingVariant.fromRemoteValue("unknown"))
        assertEquals(OnboardingVariant.FULL, OnboardingVariant.fromRemoteValue(null))
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
        val steps = OnboardingStepFactory.create(OnboardingVariant.FULL)
        assertSame(ONBOARD_STEPS, steps)
        assertTrue("full should be 4x screens, was ${steps.size}", steps.size in 40..49)
        assertEquals(steps.size, steps.map { it.stableKey }.distinct().size)
    }

    @Test
    fun standardHasTwentySixScreensInOriginalOrder() {
        val steps = OnboardingStepFactory.create(OnboardingVariant.STANDARD_26)
        assertEquals(26, steps.size)
        assertOrderKept(steps)
        assertTrue(steps.any { it.kind == StepKind.TARGET_WEIGHT })
        assertTrue(steps.any { it.id == ObId.DISCOMFORT })
        assertEquals(StepKind.TRIAL_INTRO, steps.last().kind)
    }

    @Test
    fun quickHasSevenScreensAndStillBuildsAPlan() {
        val steps = OnboardingStepFactory.create(OnboardingVariant.QUICK_7)
        assertEquals(7, steps.size)
        assertOrderKept(steps)
        assertEquals(StepKind.WELCOME, steps.first().kind)
        assertTrue(steps.any { it.kind == StepKind.WEIGHT })
        assertTrue(steps.any { it.kind == StepKind.TARGET_WEIGHT })
        assertEquals(StepKind.PLAN_RESULT, steps.last().kind)
    }

    @Test
    fun targetWeightComesRightAfterCurrentWeight() {
        val weight = ONBOARD_STEPS.indexOfFirst { it.kind == StepKind.WEIGHT }
        assertEquals(StepKind.TARGET_WEIGHT, ONBOARD_STEPS[weight + 1].kind)
    }

    private fun assertOrderKept(steps: List<OnboardStep>) {
        val positions = steps.map { step -> ONBOARD_STEPS.indexOfFirst { it.stableKey == step.stableKey } }
        assertEquals(positions.sorted(), positions)
    }
}
