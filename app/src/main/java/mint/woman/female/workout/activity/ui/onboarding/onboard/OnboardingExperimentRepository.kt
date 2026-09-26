package mint.woman.female.workout.activity.ui.onboarding.onboard

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import mint.woman.female.workout.utils.RemoteConfigKeys

/**
 * Gán một installation vào đúng một nhánh trong suốt vòng đời experiment.
 *
 * Remote Config quyết định nhánh lần đầu; assignment sau đó được giữ tại local để việc
 * fetch config hoặc mở lại app không làm người dùng đổi flow khi đang trả lời.
 */
class OnboardingExperimentRepository(
    context: Context,
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
) {
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    fun resolveVariant(): OnboardingVariant {
        val storedExperiment = preferences.getString(KEY_EXPERIMENT_ID, null)
        val storedVariant = preferences.getString(KEY_VARIANT, null)
        if (storedExperiment == EXPERIMENT_ID && storedVariant != null) {
            return OnboardingVariant.fromRemoteValue(storedVariant)
        }

        val variant = OnboardingVariant.fromRemoteValue(
            remoteConfig.getString(RemoteConfigKeys.ONBOARDING_EXPERIMENT_VARIANT)
        )
        preferences.edit()
            .putString(KEY_EXPERIMENT_ID, EXPERIMENT_ID)
            .putString(KEY_VARIANT, variant.remoteValue)
            .putLong(KEY_ASSIGNED_AT, System.currentTimeMillis())
            .putInt(KEY_FLOW_VERSION, FLOW_VERSION)
            .apply()
        return variant
    }

    companion object {
        const val EXPERIMENT_ID = "onboarding_flow_v1"
        const val FLOW_VERSION = 1

        private const val KEY_EXPERIMENT_ID = "ONBOARDING_EXPERIMENT_ID"
        private const val KEY_VARIANT = "ONBOARDING_EXPERIMENT_VARIANT"
        private const val KEY_ASSIGNED_AT = "ONBOARDING_VARIANT_ASSIGNED_AT"
        private const val KEY_FLOW_VERSION = "ONBOARDING_FLOW_VERSION"
    }
}
