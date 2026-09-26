package mint.woman.female.workout.activity.ui.onboarding.onboard

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import mint.woman.female.workout.activity.ui.main.YogaActivity
import mint.woman.female.workout.activity.ui.premium.PaywallActivity
import mint.woman.female.workout.utils.EdgeToEdgeHelper
import mint.woman.female.workout.utils.LanguageHelper
import mint.woman.female.workout.utils.MyPreference
import mint.woman.female.workout.utils.RemoteConfigKeys
import java.util.Locale

/**
 * Màn Onboard mới – dựng lại đầy đủ luồng theo bộ ảnh thiết kế `task/step1..step47`.
 *
 * Thứ tự màn hình do [OnboardingStepFactory] tạo theo variant; activity chỉ lo phần khung
 * (thanh tiến trình, back/skip, chuyển động), analytics và việc kết thúc flow.
 */
class OnboardFlowActivity : ComponentActivity(), EdgeToEdgeHelper.HandlesInsets {

    private lateinit var preference: MyPreference
    private lateinit var analytics: OnboardingAnalytics
    private var finished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        preference = MyPreference(this)

        val state = ViewModelProvider(this)[OnboardState::class.java]
        val variant = OnboardingExperimentRepository(this).resolveVariant()
        state.configure(variant)
        analytics = OnboardingAnalytics(FirebaseAnalytics.getInstance(this), variant)
        if (savedInstanceState == null) {
            analytics.flowStarted(state.steps.size)
        }

        setContent {
            MaterialTheme {
                OnboardFlowScreen(
                    state = state,
                    onStepShown = { step ->
                        analytics.stepViewed(step, state.index, state.steps.size)
                    },
                    onBackClicked = { step ->
                        analytics.backClicked(step, state.index, state.steps.size)
                    },
                    onSectionSkipped = { step ->
                        analytics.sectionSkipped(step, state.index, state.steps.size)
                    },
                    onExit = {
                        analytics.flowFinished(false, state.step, state.steps.size)
                        finish()
                    },
                    onComplete = { completed -> finishFlow(state, completed) }
                )
            }
        }
    }

    private fun finishFlow(state: OnboardState, completed: Boolean) {
        if (finished) return
        finished = true

        state.persist(preference)
        analytics.flowFinished(completed, state.step, state.steps.size)

        val main = Intent(this, YogaActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(main)
        if (!preference.isPremiumMember && FirebaseRemoteConfig.getInstance()
                .getBoolean(RemoteConfigKeys.PAYWALL_AFTER_ONBOARDING)) {
            startActivity(Intent(this, PaywallActivity::class.java).apply {
                putExtra(PaywallActivity.EXTRA_IS_FIRST, true)
                putExtra(PaywallActivity.EXTRA_SOURCE, "onboarding")
            })
        }
        finish()
    }

    override fun attachBaseContext(newBase: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(newBase)
        val saved = preferences.getString("ST_LANGUAGE", "").orEmpty()
        val lang = if (saved.length > 1) saved.substring(0, 2) else Locale.getDefault().language
        super.attachBaseContext(LanguageHelper.setLanguage(newBase, lang))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (overrideConfiguration != null) {
            val uiMode = overrideConfiguration.uiMode
            overrideConfiguration.setTo(baseContext.resources.configuration)
            overrideConfiguration.uiMode = uiMode
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }

}

/** Các màn vẽ tràn cạnh (ảnh nền toàn màn hình) dùng chrome màu trắng. */
private fun OnboardStep.isLightChrome(): Boolean = kind == StepKind.FULL_YES_NO

/** Các màn chiếm trọn màn hình, không chừa lề dưới cho thanh điều hướng.
 *  Chỉ FULL_YES_NO mới tràn cạnh thật sự (ảnh nền phủ hết + gradient tối ở dưới);
 *  WELCOME/COACH vẫn có nút Next ở cuối Column nên cần navigationBarsPadding
 *  để không bị thanh điều hướng hệ thống che mất. */
private fun OnboardStep.isImmersive(): Boolean =
    kind == StepKind.FULL_YES_NO

@Composable
private fun OnboardFlowScreen(
    state: OnboardState,
    onStepShown: (OnboardStep) -> Unit,
    onBackClicked: (OnboardStep) -> Unit,
    onSectionSkipped: (OnboardStep) -> Unit,
    onExit: () -> Unit,
    onComplete: (Boolean) -> Unit
) {
    val step = state.step

    LaunchedEffect(step.stableKey) { onStepShown(step) }

    fun goNext() {
        if (state.advance()) onComplete(true)
    }

    fun goBack() {
        // không cho quay lại khi kế hoạch đã bắt đầu được tạo
        if (step.kind == StepKind.GENERATING ||
            step.kind == StepKind.PLAN_RESULT ||
            step.kind == StepKind.TRIAL_INTRO
        ) return
        onBackClicked(step)
        if (!state.moveBack()) onExit()
    }

    fun skipSection() {
        onSectionSkipped(step)
        state.index = state.nextSectionIndex()
    }

    BackHandler { goBack() }

    Box(
        Modifier
            .fillMaxSize()
            .background(if (step.isLightChrome()) Color.Black else ObColor.Background)
    ) {
        AnimatedContent(
            targetState = state.index,
            transitionSpec = {
                val forward = targetState >= initialState
                val width = 260
                (slideInHorizontally { if (forward) width else -width } + fadeIn(tween(220)))
                    .togetherWith(
                        slideOutHorizontally { if (forward) -width else width } + fadeOut(tween(180))
                    )
                    .using(SizeTransform(clip = false))
            },
            label = "onboardStep"
        ) { index ->
            val current = state.steps[index.coerceIn(0, state.steps.lastIndex)]
            Column(
                Modifier
                    .fillMaxSize()
                    .then(if (current.isImmersive()) Modifier else Modifier.navigationBarsPadding())
                    .statusBarsPadding()
            ) {
                if (current.hasProgress) {
                    ObTopBar(
                        section = current.section,
                        fraction = state.sectionFraction(),
                        onBack = ::goBack,
                        onSkip = ::skipSection,
                        light = current.isLightChrome()
                    )
                }
                Box(Modifier.weight(1f)) {
                    ObStepContent(step = current, state = state, onNext = ::goNext)
                }
            }
        }
    }
}
