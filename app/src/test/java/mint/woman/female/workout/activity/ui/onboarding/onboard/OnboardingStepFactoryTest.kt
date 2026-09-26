package mint.woman.female.workout.activity.ui.onboarding.onboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingStepFactoryTest {

    @Test
    fun unknownRemoteValueFallsBackToControl() {
        assertEquals(
            OnboardingVariant.CONTROL_46,
            OnboardingVariant.fromRemoteValue("unknown")
        )
    }

    @Test
    fun controlUsesTheUnmodifiedProductionDefinition() {
        val steps = OnboardingStepFactory.create(OnboardingVariant.CONTROL_46, emptyMap())

        assertSame(ONBOARD_STEPS, steps)
        assertEquals(steps.size, steps.map { it.stableKey }.distinct().size)
    }

    @Test
    fun shortFlowIsSmallerAndKeepsCriticalScreens() {
        val steps = OnboardingStepFactory.create(OnboardingVariant.SHORT_28, emptyMap())

        assertEquals(28, steps.size)
        assertTrue(steps.any { it.id == ObId.DISCOMFORT })
        assertTrue(steps.any { it.kind == StepKind.GENERATING })
        assertTrue(steps.any { it.kind == StepKind.PLAN_RESULT })
        assertTrue(steps.any { it.kind == StepKind.TRIAL_INTRO })
    }

    @Test
    fun adaptiveOmitsWeightLossPitchForKeepFitGoal() {
        val steps = OnboardingStepFactory.create(
            OnboardingVariant.ADAPTIVE,
            mapOf(ObId.GOAL to "keep_fit")
        )

        assertFalse(steps.any { it.design in setOf("step5", "step14", "step42") })
    }

    @Test
    fun adaptiveKeepsWeightLossPitchForLoseWeightGoal() {
        val steps = OnboardingStepFactory.create(
            OnboardingVariant.ADAPTIVE,
            mapOf(ObId.GOAL to "lose_weight", ObId.WORKOUT_LEVEL to "easy")
        )

        assertTrue(steps.any { it.design == "step5" })
        assertTrue(steps.any { it.design == "step14" })
        assertTrue(steps.any { it.design == "step42" })
    }

    @Test
    fun adaptiveShowsBeginnerEducationOnlyForEasyLevel() {
        val easy = OnboardingStepFactory.create(
            OnboardingVariant.ADAPTIVE,
            mapOf(ObId.WORKOUT_LEVEL to "easy")
        )
        val challenging = OnboardingStepFactory.create(
            OnboardingVariant.ADAPTIVE,
            mapOf(ObId.WORKOUT_LEVEL to "challenging")
        )

        assertTrue(easy.any { it.design == "step25" })
        assertFalse(challenging.any { it.design == "step25" })
    }
}
