package mint.woman.female.workout.activity.ui.onboarding.onboard

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ProvideTextStyle
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import mint.woman.female.workout.BuildConfig
import mint.woman.female.workout.R
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import mint.woman.female.workout.activity.ui.main.YogaActivity
import mint.woman.female.workout.activity.ui.premium.PaywallActivity
import mint.woman.female.workout.utils.EdgeToEdgeHelper
import mint.woman.female.workout.utils.LanguageHelper
import mint.woman.female.workout.activity.ui.plan.PersonalPlanStore
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.lifecycleScope
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
    private lateinit var onboardState: OnboardState

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result -> onSignInResult(result) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        preference = MyPreference(this)

        val state = ViewModelProvider(this)[OnboardState::class.java]
        onboardState = state
        val experiment = OnboardingExperimentRepository(this)
        val variant = experiment.resolveVariant()
        state.configure(variant, onboardSteps(resources))
        analytics = OnboardingAnalytics(FirebaseAnalytics.getInstance(this), variant)
        if (savedInstanceState == null) {
            analytics.flowStarted(state.steps.size)
        }

        setContent {
            MaterialTheme {
                ProvideTextStyle(LocalTextStyle.current.copy(fontFamily = ObFontFamily)) {
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
                    onComplete = { completed -> finishFlow(state, completed) },
                    onSignIn = if (canSignIn()) ({ startSignIn() }) else null,
                    debugLabel = if (BuildConfig.DEBUG) "TEST: ${variant.remoteValue} · ${state.steps.size} màn  ↻" else null,
                    onDebugCycle = {
                        // Bản debug: đổi sang nhánh kế tiếp rồi chạy lại onboarding từ đầu
                        val next = OnboardingVariant.entries[(variant.ordinal + 1) % OnboardingVariant.entries.size]
                        experiment.setDebugVariant(next)
                        finish()
                        startActivity(Intent(this, OnboardFlowActivity::class.java))
                    }
                )
                }
            }
        }
    }

    private fun finishFlow(state: OnboardState, completed: Boolean) {
        if (finished) return
        finished = true

        state.persist(preference)
        analytics.flowFinished(completed, state.step, state.steps.size)
        // Tạo luôn plan 28 ngày từ câu trả lời (trước đây người dùng phải tự vào "Kế hoạch cá nhân → Tạo"), xong mới
        // mở Home để thẻ plan / popup buổi đầu có ngay. Chạy nền vì phải giải mã / mã hoá dữ liệu bài tập.
        val context = applicationContext
        lifecycleScope.launch {
            if (!PersonalPlanStore.hasPlan(context)) {
                withContext(Dispatchers.IO) {
                    runCatching { PersonalPlanStore.create(context, PersonalPlanStore.defaultsFromOnboarding(context)) }
                }
            }
            openMain()
        }
    }

    /** Đăng nhập dùng chung FirebaseUI + Realtime DB với mục Back up & Restore trong Cài đặt. */
    private fun canSignIn(): Boolean =
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS &&
            FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.ENABLE_SYNC)

    private fun startSignIn() {
        val intent = AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(listOf(AuthUI.IdpConfig.GoogleBuilder().build()))
            .build()
        signInLauncher.launch(intent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode != RESULT_OK || FirebaseAuth.getInstance().currentUser == null) {
            // Bấm huỷ (idpResponse null) thì ở lại màn chào, không báo lỗi
            if (result.idpResponse != null) {
                Toast.makeText(this, getString(R.string.oops_sync_failed), Toast.LENGTH_SHORT).show()
            }
            return
        }
        if (finished) return
        finished = true

        // Dữ liệu sao lưu (tiến độ, diet, lịch sử) được YogaActivity tải về khi mở;
        // hồ sơ onboarding chưa có trên server nên chỉ đánh dấu đã onboard, giữ giá trị mặc định.
        preference.setOnboarded(true)
        preference.setFirstOpen(false)
        analytics.signedIn(onboardState.step, onboardState.steps.size)
        openMain()
    }

    private fun openMain() {
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
        val lang = when {
            saved.length < 2 -> Locale.getDefault().toLanguageTag()
            saved == "nlL" -> "nl" // Legacy language-picker code.
            else -> saved // Keep region codes for Simplified/Traditional Chinese.
        }
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
    onComplete: (Boolean) -> Unit,
    onSignIn: (() -> Unit)? = null,
    debugLabel: String? = null,
    onDebugCycle: () -> Unit = {}
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
            .background(ObColor.Background)
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
            if (current.isImmersive()) {
                // Ảnh nền tràn lên dưới status bar, thanh tiến trình nổi phía trên.
                Box(Modifier.fillMaxSize()) {
                    ObStepContent(step = current, state = state, onNext = ::goNext, onSignIn = onSignIn)
                    if (current.hasProgress) {
                        Box(Modifier.statusBarsPadding()) {
                            ObTopBar(
                                section = current.section,
                                fraction = state.sectionFraction(),
                                onBack = ::goBack,
                                onSkip = ::skipSection,
                                light = true
                            )
                        }
                    }
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
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
                        ObStepContent(step = current, state = state, onNext = ::goNext, onSignIn = onSignIn)
                    }
                }
            }
        }
        if (debugLabel != null && state.index == 0) {
            Text(
                debugLabel,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xCC1F1A1D))
                    .clickable(onClick = onDebugCycle)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                color = Color.White,
                fontSize = 13.sp
            )
        }
    }
}
