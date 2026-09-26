package mint.woman.female.workout.activity.ui.onboarding.onboard

/** Tạo danh sách bước từ cùng một bộ component, không nhân bản Activity. */
object OnboardingStepFactory {

    fun create(
        variant: OnboardingVariant,
        answers: Map<String, String>
    ): List<OnboardStep> = when (variant) {
        OnboardingVariant.CONTROL_46 -> ONBOARD_STEPS
        OnboardingVariant.SHORT_28 -> shortSteps()
        OnboardingVariant.ADAPTIVE -> adaptiveSteps(answers)
    }

    private fun shortSteps(): List<OnboardStep> = ONBOARD_STEPS.filterNot { step ->
        step.design in SHORT_EXCLUDED_DESIGNS
    }

    /**
     * Bản adaptive đầu tiên chỉ rẽ nhánh trên dữ liệu hiện đã có trong UI.
     * Các safety branch mới sẽ được bổ sung bằng step riêng ở giai đoạn tiếp theo.
     */
    private fun adaptiveSteps(answers: Map<String, String>): List<OnboardStep> {
        val excluded = SHORT_EXCLUDED_DESIGNS.toMutableSet()
        val goal = answers[ObId.GOAL]
        if (goal != "lose_weight") {
            excluded += WEIGHT_LOSS_ONLY_DESIGNS
        }
        if (answers[ObId.WORKOUT_LEVEL] != "easy") {
            excluded += BEGINNER_ONLY_DESIGNS
        }
        return ONBOARD_STEPS.filterNot { it.design in excluded }
    }

    private val SHORT_EXCLUDED_DESIGNS = setOf(
        "step7",  // current body shape
        "step8",  // desired body shape
        "step11", // belly type
        "step12", // hip type
        "step13", // leg type
        "step18", // generic potential screen
        "step28", // second prediction
        "step30", // overlaps typical day/fitness level
        "step33", // flexibility does not yet alter the plan
        "step34", // aerobic overlaps fitness level
        "step35", // duplicate motivation statement
        "step36", // duplicate motivation statement
        "step37", // duplicate motivation statement
        "step38", // social proof already exists in testimonial
        "step39", // celebration prompt
        "step41", // generic appearance claim
        "step43", // duplicate full-screen commitment
        "step44"  // duplicate full-screen commitment
    )

    private val WEIGHT_LOSS_ONLY_DESIGNS = setOf("step5", "step14", "step42")
    private val BEGINNER_ONLY_DESIGNS = setOf("step25")
}

internal val OnboardStep.stableKey: String
    get() = "$design|${kind.name}|$section|$id|$title"
