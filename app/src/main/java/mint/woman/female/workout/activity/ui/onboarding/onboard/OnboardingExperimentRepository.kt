package mint.woman.female.workout.activity.ui.onboarding.onboard

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import mint.woman.female.workout.BuildConfig
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
        // Bản debug: nhánh chọn tay bằng nút thử nghiệm ở màn chào (để test đủ 3 nhánh)
        if (BuildConfig.DEBUG) {
            preferences.getString(KEY_DEBUG_VARIANT, null)?.let { return OnboardingVariant.fromRemoteValue(it) }
        }
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

    /** Chỉ dùng ở bản debug: chọn tay nhánh kế tiếp để test. */
    fun setDebugVariant(variant: OnboardingVariant) {
        preferences.edit().putString(KEY_DEBUG_VARIANT, variant.remoteValue).apply()
    }

    companion object {
        /** Đổi id khi đổi bộ nhánh (7 / 26 / đầy đủ) để mọi máy được chia nhánh lại. */
        const val EXPERIMENT_ID = "onboarding_flow_v2"
        const val FLOW_VERSION = 2
        private const val KEY_DEBUG_VARIANT = "ONBOARDING_DEBUG_VARIANT"

        private const val KEY_EXPERIMENT_ID = "ONBOARDING_EXPERIMENT_ID"
        private const val KEY_VARIANT = "ONBOARDING_EXPERIMENT_VARIANT"
        private const val KEY_ASSIGNED_AT = "ONBOARDING_VARIANT_ASSIGNED_AT"
        private const val KEY_FLOW_VERSION = "ONBOARDING_FLOW_VERSION"
    }
}
