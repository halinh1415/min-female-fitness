package mint.woman.female.workout.activity.ui.onboarding.onboard

/** Một nhánh của thử nghiệm onboarding. */
enum class OnboardingVariant(val remoteValue: String) {
    CONTROL_46("control_46"),
    SHORT_28("short_28"),
    ADAPTIVE("adaptive");

    companion object {
        fun fromRemoteValue(value: String?): OnboardingVariant =
            entries.firstOrNull { it.remoteValue == value?.trim()?.lowercase() } ?: CONTROL_46
    }
}
