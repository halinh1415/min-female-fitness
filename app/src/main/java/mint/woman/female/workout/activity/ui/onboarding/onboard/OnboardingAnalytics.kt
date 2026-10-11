package mint.woman.female.workout.activity.ui.onboarding.onboard

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/** Analytics không chứa câu trả lời sức khỏe hoặc số đo thô. */
class OnboardingAnalytics(
    private val firebase: FirebaseAnalytics,
    private val variant: OnboardingVariant
) {
    init {
        firebase.setUserProperty(PARAM_VARIANT, variant.remoteValue)
    }

    fun flowStarted(visibleStepCount: Int) {
        firebase.logEvent(EVENT_STARTED, commonBundle(visibleStepCount))
    }

    fun stepViewed(step: OnboardStep, index: Int, visibleStepCount: Int) {
        firebase.logEvent(EVENT_STEP_VIEWED, commonBundle(visibleStepCount).apply {
            putString(PARAM_STEP_ID, step.stableKey.take(MAX_PARAM_LENGTH))
            putString(PARAM_STEP_KIND, step.kind.name.lowercase())
            putLong(PARAM_STEP_INDEX, index.toLong())
            putLong(PARAM_SECTION, step.section.toLong())
        })
    }

    fun backClicked(step: OnboardStep, index: Int, visibleStepCount: Int) {
        firebase.logEvent(EVENT_BACK_CLICKED, commonBundle(visibleStepCount).apply {
            putString(PARAM_STEP_ID, step.stableKey.take(MAX_PARAM_LENGTH))
            putLong(PARAM_STEP_INDEX, index.toLong())
        })
    }

    fun sectionSkipped(step: OnboardStep, index: Int, visibleStepCount: Int) {
        firebase.logEvent(EVENT_SECTION_SKIPPED, commonBundle(visibleStepCount).apply {
            putString(PARAM_STEP_ID, step.stableKey.take(MAX_PARAM_LENGTH))
            putLong(PARAM_STEP_INDEX, index.toLong())
            putLong(PARAM_SECTION, step.section.toLong())
        })
    }

    fun flowFinished(completed: Boolean, step: OnboardStep, visibleStepCount: Int) {
        firebase.logEvent(
            if (completed) EVENT_COMPLETED else EVENT_EXITED,
            commonBundle(visibleStepCount).apply {
                putString(PARAM_STEP_ID, step.stableKey.take(MAX_PARAM_LENGTH))
            }
        )
    }

    /** Người dùng cũ bấm SIGN IN ở màn chào và đăng nhập thành công. */
    fun signedIn(step: OnboardStep, visibleStepCount: Int) {
        firebase.logEvent(EVENT_SIGNED_IN, commonBundle(visibleStepCount).apply {
            putString(PARAM_STEP_ID, step.stableKey.take(MAX_PARAM_LENGTH))
        })
    }

    private fun commonBundle(visibleStepCount: Int) = Bundle().apply {
        putString(PARAM_EXPERIMENT_ID, OnboardingExperimentRepository.EXPERIMENT_ID)
        putString(PARAM_VARIANT, variant.remoteValue)
        putLong(PARAM_FLOW_VERSION, OnboardingExperimentRepository.FLOW_VERSION.toLong())
        putLong(PARAM_VISIBLE_STEP_COUNT, visibleStepCount.toLong())
    }

    private companion object {
        const val EVENT_STARTED = "onboarding_started"
        const val EVENT_STEP_VIEWED = "onboarding_step_viewed"
        const val EVENT_BACK_CLICKED = "onboarding_back_clicked"
        const val EVENT_SECTION_SKIPPED = "onboarding_section_skipped"
        const val EVENT_COMPLETED = "onboarding_completed"
        const val EVENT_EXITED = "onboarding_exited"
        const val EVENT_SIGNED_IN = "onboarding_signed_in"

        const val PARAM_EXPERIMENT_ID = "experiment_id"
        const val PARAM_VARIANT = "onboarding_variant"
        const val PARAM_FLOW_VERSION = "flow_version"
        const val PARAM_STEP_ID = "step_id"
        const val PARAM_STEP_KIND = "step_kind"
        const val PARAM_STEP_INDEX = "step_index"
        const val PARAM_VISIBLE_STEP_COUNT = "visible_step_count"
        const val PARAM_SECTION = "section"
        const val MAX_PARAM_LENGTH = 100
    }
}
