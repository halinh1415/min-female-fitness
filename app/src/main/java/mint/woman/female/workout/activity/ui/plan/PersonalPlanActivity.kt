package mint.woman.female.workout.activity.ui.plan

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.compose.GlideImage
import mint.woman.female.workout.activity.ui.compose.MinButton
import mint.woman.female.workout.activity.ui.compose.MinColor
import mint.woman.female.workout.activity.ui.compose.MinTheme
import mint.woman.female.workout.activity.ui.workout.ChallengeActivity
import mint.woman.female.workout.utils.LanguageHelper
import java.util.Locale

/**
 * Màn "Plan Settings" (bản riêng của Min): mục tiêu, thời lượng, trình độ, vùng tập, thời gian nghỉ
 * giữa bài, khởi động - giãn cơ và các hạn chế (chấn thương, không nhảy); bấm tạo thì sinh plan 28 ngày ([PersonalPlanStore]) và mở màn Challenge.
 */
class PersonalPlanActivity : AppCompatActivity() {

    private var settings by mutableStateOf(PlanSettings())
    private var generating by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Bản Min không có mục cường độ aerobic (nghỉ giữa bài chọn riêng): plan cũ từng chọn thì đưa về mặc định.
        settings = (PersonalPlanStore.settings(this) ?: PersonalPlanStore.defaultsFromOnboarding(this))
            .copy(aerobic = PlanAerobic.MODERATE)
        setContent {
            MinTheme {
                PlanSettingsScreen(
                    settings = settings,
                    generating = generating,
                    onChange = { settings = it },
                    onBack = ::finish,
                    onCreate = ::createPlan
                )
            }
        }
    }

    private fun createPlan() {
        if (generating) return
        generating = true
        val chosen = settings
        FirebaseAnalytics.getInstance(this).logEvent("personal_plan_created", Bundle().apply {
            putString("goal", chosen.goal.name)
            putString("level", chosen.level.name)
            putString("focus", chosen.focus.joinToString(",") { it.name })
            putString("injuries", chosen.injuries.joinToString(",") { it.name })
            putBoolean("jump_free", chosen.jumpFree)
            putString("rest_pace", chosen.restPace.name)
            putString("stretch", chosen.stretch.name)
            putInt("training_days", chosen.trainingDays)
            putString("duration", chosen.duration.name)
        })
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { PersonalPlanStore.create(applicationContext, chosen) }
            // Cho hiệu ứng "đang tạo plan" chạy hết để người dùng thấy plan được tính riêng cho mình.
            delay(1400)
            startActivity(
                Intent(this@PersonalPlanActivity, ChallengeActivity::class.java)
                    .putExtras(Bundle().apply { putInt("PROGRAM_ID", PersonalPlanStore.PROGRAM_ID) })
            )
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = PreferenceManager.getDefaultSharedPreferences(newBase).getString("ST_LANGUAGE", "").orEmpty()
        super.attachBaseContext(
            LanguageHelper.setLanguage(newBase, if (lang.isNotEmpty()) lang.substring(0, 2) else Locale.getDefault().language)
        )
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

// ---------------------------------------------------------------- giao diện

@Composable
fun PlanSettingsScreen(
    settings: PlanSettings,
    generating: Boolean,
    onChange: (PlanSettings) -> Unit,
    onBack: () -> Unit,
    onCreate: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MinColor.Background)
    ) {
        Column(Modifier.fillMaxSize()) {
            // thanh trên: quay lại + tiêu đề
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) { Text("←", fontSize = 24.sp) }
                Text(
                    "✦ " + stringResource(R.string.pp_settings),
                    color = MinColor.Pink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 12.dp)
            ) {
                Section(stringResource(R.string.pp_goal)) {
                    ChipRow(
                        options = PlanGoal.values().toList(),
                        label = {
                            stringResource(
                                when (it) {
                                    PlanGoal.LOSE_WEIGHT -> R.string.pp_goal_lose
                                    PlanGoal.BUILD_MUSCLE -> R.string.pp_goal_muscle
                                    PlanGoal.KEEP_FIT -> R.string.pp_goal_fit
                                }
                            )
                        },
                        selected = { it == settings.goal },
                        onClick = { onChange(settings.copy(goal = it)) }
                    )
                }
                Section(stringResource(R.string.pp_duration)) {
                    val min = stringResource(R.string.pp_min)
                    ChipRow(
                        options = PlanDuration.values().toList(),
                        label = {
                            when (it) {
                                PlanDuration.UNDER_10 -> "<10 $min"
                                PlanDuration.MIN_10_20 -> "10-20 $min"
                                PlanDuration.MIN_20_30 -> "20-30 $min"
                                PlanDuration.OVER_30 -> ">30 $min"
                            }
                        },
                        selected = { it == settings.duration },
                        onClick = { onChange(settings.copy(duration = it)) }
                    )
                }
                Section(stringResource(R.string.pp_level)) {
                    ChipRow(
                        options = PlanLevel.values().toList(),
                        label = {
                            stringResource(
                                when (it) {
                                    PlanLevel.BEGINNER -> R.string.beginner
                                    PlanLevel.INTERMEDIATE -> R.string.intermediate
                                    PlanLevel.ADVANCED -> R.string.advanced
                                }
                            )
                        },
                        selected = { it == settings.level },
                        onClick = { onChange(settings.copy(level = it)) }
                    )
                }
                Section(stringResource(R.string.pp_days_week)) {
                    ChipRow(
                        options = (3..6).toList(),
                        label = { it.toString() },
                        selected = { it == settings.trainingDays.coerceIn(3, 6) },
                        onClick = { onChange(settings.copy(trainingDays = it)) }
                    )
                    Text(
                        "ⓘ " + stringResource(R.string.pp_days_hint),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MinColor.TextSecondary,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp)
                    )
                }
                Section(stringResource(R.string.pp_focus), stringResource(R.string.pp_multiple)) {
                    FocusGrid(settings, onChange)
                }
                Section(stringResource(R.string.pp_rest_between)) {
                    ChipRow(
                        options = PlanPace.values().toList(),
                        label = {
                            val name = stringResource(
                                when (it) {
                                    PlanPace.SHORT -> R.string.pp_short
                                    PlanPace.STANDARD -> R.string.pp_standard
                                    PlanPace.LONG -> R.string.pp_long
                                }
                            )
                            when (it) {
                                PlanPace.SHORT -> "$name · 10s"
                                PlanPace.STANDARD -> "$name · 15s"
                                PlanPace.LONG -> "$name · 25s"
                            }
                        },
                        selected = { it == settings.restPace },
                        onClick = { onChange(settings.copy(restPace = it)) }
                    )
                }
                Section(stringResource(R.string.pp_warmup_stretch)) {
                    ChipRow(
                        options = PlanPace.values().toList(),
                        label = {
                            val name = stringResource(
                                when (it) {
                                    PlanPace.SHORT -> R.string.pp_short
                                    PlanPace.STANDARD -> R.string.pp_standard
                                    PlanPace.LONG -> R.string.pp_long
                                }
                            )
                            name
                        },
                        selected = { it == settings.stretch },
                        onClick = { onChange(settings.copy(stretch = it)) }
                    )
                }
                // Gộp chấn thương và "không nhảy" thành một mục hạn chế (chọn nhiều).
                Section(stringResource(R.string.pp_limits), stringResource(R.string.pp_multiple)) {
                    val none = "none"
                    val noJump = "no_jump"
                    ChipRow(
                        options = listOf<Any>(none) + PlanInjury.values().toList() + noJump,
                        label = {
                            stringResource(
                                when (it) {
                                    PlanInjury.BACK -> R.string.pp_back
                                    PlanInjury.KNEE -> R.string.pp_knee
                                    PlanInjury.ARM_SHOULDER -> R.string.pp_shoulder
                                    PlanInjury.WRIST -> R.string.pp_wrist
                                    noJump -> R.string.pp_jump_free
                                    else -> R.string.pp_none
                                }
                            )
                        },
                        selected = {
                            when (it) {
                                none -> settings.injuries.isEmpty() && !settings.jumpFree
                                noJump -> settings.jumpFree
                                else -> it in settings.injuries
                            }
                        },
                        onClick = {
                            onChange(
                                when (it) {
                                    is PlanInjury -> settings.copy(injuries = if (it in settings.injuries) settings.injuries - it else settings.injuries + it)
                                    noJump -> settings.copy(jumpFree = !settings.jumpFree)
                                    else -> settings.copy(injuries = emptySet(), jumpFree = false)
                                }
                            )
                        }
                    )
                    if (settings.injuries.isNotEmpty()) {
                        Text(
                            "ⓘ " + stringResource(R.string.pp_injury_note),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MinColor.TextSecondary,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp)
                        )
                    }
                }
            }
            MinButton(
                stringResource(R.string.pp_create),
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                height = 54.dp,
                onClick = onCreate
            )
        }
        if (generating) GeneratingOverlay()
    }
}

@Composable
private fun Section(title: String, hint: String? = null, content: @Composable () -> Unit) {
    Column(Modifier.padding(top = 18.dp)) {
        Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.Bottom) {
            Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            if (hint != null) {
                Spacer(Modifier.width(6.dp))
                Text(hint, fontSize = 13.sp, color = MinColor.TextSecondary, modifier = Modifier.padding(bottom = 2.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun <T> ChipRow(
    options: List<T>,
    label: @Composable (T) -> String,
    selected: (T) -> Boolean,
    onClick: (T) -> Unit
) {
    Row(
        Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEach { option ->
            val active = selected(option)
            Text(
                label(option),
                fontSize = 16.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                color = if (active) MinColor.Pink else MinColor.TextPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (active) MinColor.PinkSoft else MinColor.Surface)
                    .border(2.dp, if (active) MinColor.Pink else Color.Transparent, RoundedCornerShape(16.dp))
                    .clickable { onClick(option) }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun FocusGrid(settings: PlanSettings, onChange: (PlanSettings) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PlanFocus.values().toList().chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { focus ->
                    val active = focus in settings.focus
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                val next = if (active) settings.focus - focus else settings.focus + focus
                                // Luôn giữ ít nhất một vùng tập.
                                if (next.isNotEmpty()) onChange(settings.copy(focus = next))
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(0.85f)
                                .aspectRatio(1f)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(3.dp, if (active) MinColor.Pink else Color.Transparent, CircleShape)
                                    .clipToBounds()
                            ) {
                                GlideImage(
                                    "file:///android_asset/images/" + PersonalPlanStore.focusImage(focus),
                                    Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    scaleType = ImageView.ScaleType.CENTER_CROP
                                )
                            }
                            if (active) {
                                Box(
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(MinColor.Pink),
                                    contentAlignment = Alignment.Center
                                ) { Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(PersonalPlanStore.focusLabel(focus)),
                            fontSize = 14.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            color = if (active) MinColor.Pink else MinColor.TextPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneratingOverlay() {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val progress by animateFloatAsState(if (started) 1f else 0f, tween(1400, easing = LinearEasing), label = "gen")
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(enabled = true, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MinColor.Surface)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("✨", fontSize = 40.sp)
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.pp_creating), fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            mint.woman.female.workout.activity.ui.compose.MinProgressBar(progress, Modifier.width(200.dp))
            Spacer(Modifier.height(8.dp))
            Text("${(progress * 100).toInt()}%", fontSize = 14.sp, color = MinColor.Pink, fontWeight = FontWeight.Bold)
        }
    }
}
