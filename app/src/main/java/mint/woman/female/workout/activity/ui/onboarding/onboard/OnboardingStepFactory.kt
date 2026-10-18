package mint.woman.female.workout.activity.ui.onboarding.onboard

/** Tạo danh sách bước từ cùng một bộ component, không nhân bản Activity. */
object OnboardingStepFactory {

    fun create(variant: OnboardingVariant, all: List<OnboardStep>): List<OnboardStep> = when (variant) {
        OnboardingVariant.FULL -> all
        OnboardingVariant.STANDARD_26 -> all.filter { it.kind == StepKind.SECTION || it.design in STANDARD_DESIGNS }
        OnboardingVariant.QUICK_7 -> all.filter { it.design in QUICK_DESIGNS }
    }

    /**
     * 9 màn: chào → mục tiêu → vùng tập → chiều cao → cân nặng → cân mục tiêu → khó chịu khi tập (đau gối / lưng)
     * → tạo kế hoạch → kết quả. Màn khó chịu: chọn gối / lưng thì bật "Không nhảy" và plan tránh bài hại khớp
     * (như Home Workout). Giữ tên nhánh quick_7 (giá trị Remote Config / analytics) dù đã thêm màn.
     */
    private val QUICK_DESIGNS = setOf("step1", "step4", "step6", "step9", "step10", "step10b", "step27", "step45", "step46")

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
