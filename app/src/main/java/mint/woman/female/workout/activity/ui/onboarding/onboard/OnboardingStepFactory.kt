package mint.woman.female.workout.activity.ui.onboarding.onboard

/** Tạo danh sách bước từ cùng một bộ component, không nhân bản Activity. */
object OnboardingStepFactory {

    fun create(variant: OnboardingVariant): List<OnboardStep> = when (variant) {
        OnboardingVariant.FULL -> ONBOARD_STEPS
        OnboardingVariant.STANDARD_26 -> ONBOARD_STEPS.filter { it.kind == StepKind.SECTION || it.design in STANDARD_DESIGNS }
        OnboardingVariant.QUICK_7 -> ONBOARD_STEPS.filter { it.design in QUICK_DESIGNS }
    }

    /** 7 màn: chào → mục tiêu → chiều cao → cân nặng → cân mục tiêu → tạo kế hoạch → kết quả. */
    private val QUICK_DESIGNS = setOf("step1", "step4", "step9", "step10", "step10b", "step45", "step46")

    /** 26 màn = 22 màn dưới đây + 4 màn chuyển phần. */
    private val STANDARD_DESIGNS = setOf(
        "step1", "step2",
        "step3", "step4", "step6",                      // mục tiêu
        "step9", "step10", "step10b", "step14",         // số đo + dự đoán lần 1
        "step17", "step18", "step19", "step22", "step26", "step27", "step28", // về bạn + dự đoán lần 2
        "step29", "step31", "step32",                   // lối sống
        "step45", "step46", "step47"                    // tạo kế hoạch, kết quả, mời dùng thử
    )
}

internal val OnboardStep.stableKey: String
    get() = "$design|${kind.name}|$section|$id|$title"
