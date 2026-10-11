package mint.woman.female.workout.activity.ui.onboarding.onboard

/**
 * Một nhánh của thử nghiệm onboarding, đặt tên theo tổng số màn người dùng đi qua
 * (đếm cả màn chào, màn chuyển phần, màn tạo kế hoạch và kết quả).
 */
enum class OnboardingVariant(val remoteValue: String) {
    QUICK_7("quick_7"),
    STANDARD_26("standard_26"),
    FULL("full_4x");

    companion object {
        /** Giá trị cũ trên Remote Config vẫn đọc được: control_46 → đầy đủ, short_28 / adaptive → 26 màn. */
        private val LEGACY = mapOf(
            "control_46" to FULL,
            "short_28" to STANDARD_26,
            "adaptive" to STANDARD_26
        )

        fun fromRemoteValue(value: String?): OnboardingVariant {
            val key = value?.trim()?.lowercase()
            return entries.firstOrNull { it.remoteValue == key } ?: LEGACY[key] ?: FULL
        }
    }
}
