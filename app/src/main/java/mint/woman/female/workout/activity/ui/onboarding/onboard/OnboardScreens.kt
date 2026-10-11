package mint.woman.female.workout.activity.ui.onboarding.onboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.delay
import mint.woman.female.workout.R
import androidx.annotation.StringRes
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import java.util.Calendar
import androidx.compose.ui.text.withStyle
import kotlin.math.max
import kotlin.math.abs
import kotlin.math.roundToInt

private val ScreenPadding = 24.dp
private const val AUTO_ADVANCE_MS = 260L

/**
 * Vẽ một màn hình của luồng onboard dựa trên [OnboardStep.kind].
 *
 * Toàn bộ chrome (thanh tiến trình, nút back/skip) do [OnboardFlowScreen] xử lý;
 * ở đây chỉ dựng phần nội dung và nút CTA riêng của từng màn.
 */
@Composable
fun ObStepContent(
    step: OnboardStep,
    state: OnboardState,
    onNext: () -> Unit,
    onSignIn: (() -> Unit)? = null
) {
    when (step.kind) {
        StepKind.WELCOME -> WelcomeScreen(step, onNext, onSignIn)
        StepKind.COACH -> CoachScreen(step, onNext)
        StepKind.CHOICE -> ChoiceScreen(step, state, onNext)
        StepKind.CHOICE_IMAGE -> ChoiceImageScreen(step, state, onNext)
        StepKind.CHOICE_FEEDBACK -> ChoiceFeedbackScreen(step, state, onNext)
        StepKind.MULTI_CHOICE -> MultiChoiceScreen(step, state, onNext)
        StepKind.INFO -> InfoScreen(step, onNext)
        StepKind.INFO_POTENTIAL -> PotentialScreen(step, state, onNext)
        StepKind.TESTIMONIAL -> TestimonialScreen(step, onNext)
        StepKind.FOCUS_BODY -> FocusBodyScreen(step, state, onNext)
        StepKind.DESIRED_BODY -> DesiredBodyScreen(step, state, onNext)
        StepKind.HEIGHT -> HeightScreen(step, state, onNext)
        StepKind.WEIGHT -> WeightScreen(step, state, onNext)
        StepKind.TARGET_WEIGHT -> TargetWeightScreen(step, state, onNext)
        StepKind.AGE -> AgeScreen(step, state, onNext)
        StepKind.SECTION -> SectionScreen(step, onNext)
        StepKind.PREDICTION -> PredictionScreen(step, state, onNext)
        StepKind.SLIDER -> SliderScreen(step, state, onNext)
        StepKind.STATEMENT -> StatementScreen(step, state, onNext)
        StepKind.FULL_YES_NO -> FullYesNoScreen(step, state, onNext)
        StepKind.GENERATING -> GeneratingScreen(step, onNext)
        StepKind.PLAN_RESULT -> PlanResultScreen(step, state, onNext)
        StepKind.TRIAL_INTRO -> TrialIntroScreen(step, onNext)
    }
}

// ---------------------------------------------------------------- step1 / step2

@Composable
private fun WelcomeScreen(step: OnboardStep, onNext: () -> Unit, onSignIn: (() -> Unit)?) {
    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(0.58f)
                .padding(horizontal = ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ObArt(
                    name = step.art,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.42f),
                    placeholderEmoji = "🏋️‍♀️"
                )
                Spacer(Modifier.height(16.dp))
                ObTitle(step.title, fontSize = 32)
                Spacer(Modifier.height(24.dp))
                WelcomeStat(stringResource(R.string.obf_welcome_stat_minutes), "7+")
                WelcomeStat(stringResource(R.string.obf_welcome_stat_weeks), "4")
                WelcomeStat(stringResource(R.string.obf_welcome_stat_equipment), "0")
                Spacer(Modifier.height(8.dp))
            }
            ObPrimaryButton(step.ctaText(), onClick = onNext)
            if (onSignIn != null) {
                // Người dùng cũ: đăng nhập Google, lấy lại dữ liệu đã sao lưu và bỏ qua onboarding
                Spacer(Modifier.height(18.dp))
                Text(
                    stringResource(R.string.obf_sign_in),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ObColor.TextPrimary,
                    modifier = Modifier.clickable(onClick = onSignIn)
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WelcomeStat(label: String, value: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 18.sp, color = ObColor.TextPrimary)
        Text(value, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = ObColor.Pink)
    }
}

@Composable
private fun CoachScreen(step: OnboardStep, onNext: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ObArt(
            name = step.art,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.48f),
            placeholderEmoji = "👩‍🏫"
        )
        Column(
            modifier = Modifier
                .weight(0.52f)
                .padding(horizontal = ScreenPadding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(32.dp))
                ObTitle(step.title, fontSize = 34, textAlign = TextAlign.Start)
                Spacer(Modifier.height(20.dp))
                ObTitle(
                    step.body, accent = step.titleAccent, fontSize = 19,
                    textAlign = TextAlign.Start, color = ObColor.TextPrimary
                )
                Spacer(Modifier.height(8.dp))
            }
            ObPrimaryButton(step.ctaText(), onClick = onNext)
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ---------------------------------------------------------------- các màn hỏi

@Composable
private fun ChoiceScreen(step: OnboardStep, state: OnboardState, onNext: () -> Unit) {
    var pending by remember(step.design) { mutableStateOf<String?>(null) }
    LaunchedEffect(pending) {
        val key = pending ?: return@LaunchedEffect
        delay(AUTO_ADVANCE_MS)
        state.answers[step.id] = key
        onNext()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding)
    ) {
        Spacer(Modifier.height(20.dp))
        ObTitle(step.title, step.titleAccent)
        Spacer(Modifier.height(28.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
        ) {
            items(step.options) { option ->
                ObOptionRow(
                    option = option,
                    selected = pending == option.key || state.answers[step.id] == option.key
                ) { pending = option.key }
            }
        }
    }
}

@Composable
private fun ChoiceImageScreen(step: OnboardStep, state: OnboardState, onNext: () -> Unit) {
    var pending by remember(step.design) { mutableStateOf<String?>(null) }
    LaunchedEffect(pending) {
        val key = pending ?: return@LaunchedEffect
        delay(AUTO_ADVANCE_MS)
        state.answers[step.id] = key
        onNext()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding)
    ) {
        Spacer(Modifier.height(20.dp))
        ObTitle(step.title, step.titleAccent)
        Spacer(Modifier.height(24.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
        ) {
            items(step.options) { option ->
                ObImageOptionCard(
                    option = option,
                    selected = pending == option.key || state.answers[step.id] == option.key
                ) { pending = option.key }
            }
        }
    }
}

@Composable
private fun ChoiceFeedbackScreen(step: OnboardStep, state: OnboardState, onNext: () -> Unit) {
    val selected = state.answers[step.id]
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding)
    ) {
        Spacer(Modifier.height(20.dp))
        ObTitle(step.title, step.titleAccent)
        Spacer(Modifier.height(36.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            step.options.forEach { option ->
                ObOptionRow(option, selected == option.key) { state.answers[step.id] = option.key }
                if (selected == option.key && option.feedbackBody.isNotEmpty()) {
                    ObAssistantCard(option.feedbackTitle, option.feedbackBody)
                }
            }
        }
        ObPrimaryButton(step.ctaText(), enabled = selected != null, onClick = onNext)
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun MultiChoiceScreen(step: OnboardStep, state: OnboardState, onNext: () -> Unit) {
    val current = state.answers[step.id].orEmpty()
    val selected = remember(current) {
        current.split(',').filter { it.isNotBlank() }.toSet()
    }

    fun toggle(key: String) {
        val next = when {
            key == NONE_KEY -> setOf(NONE_KEY)
            selected.contains(key) -> selected - key
            else -> (selected - NONE_KEY) + key
        }
        state.answers[step.id] = next.joinToString(",")
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding)
    ) {
        Spacer(Modifier.height(20.dp))
        ObTitle(step.title, step.titleAccent)
        Spacer(Modifier.height(24.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 12.dp)
        ) {
            items(step.options) { option ->
                ObOptionRow(option, selected.contains(option.key)) { toggle(option.key) }
            }
        }
        ObPrimaryButton(step.ctaText(), enabled = selected.isNotEmpty(), onClick = onNext)
        Spacer(Modifier.height(20.dp))
    }
}

private const val NONE_KEY = "none"

// ---------------------------------------------------------------- màn thông tin

@Composable
private fun InfoScreen(step: OnboardStep, onNext: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            if (step.title.isNotEmpty() && step.art == "info_community") {
                ObTitle(step.title, step.titleAccent)
                Spacer(Modifier.height(24.dp))
            }
            ObArt(
                name = step.art,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(24.dp)),
                placeholderEmoji = step.hintEmoji.ifEmpty { "✨" },
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(28.dp))
            if (step.art != "info_community") {
                ObTitle(step.title, step.titleAccent)
                Spacer(Modifier.height(20.dp))
            } else if (step.subtitle.isNotEmpty()) {
                ObTitle(step.subtitle, step.subtitle.substringBefore(" of"), fontSize = 28)
                Spacer(Modifier.height(10.dp))
            }
            Text(
                step.body,
                fontSize = 19.sp,
                lineHeight = 26.sp,
                color = ObColor.TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(20.dp))
        ObPrimaryButton(step.ctaText(), onClick = onNext)
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun PotentialScreen(step: OnboardStep, state: OnboardState, onNext: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))
            ObTitle(step.title, step.titleAccent)
            Spacer(Modifier.height(30.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                ObPotentialChart(Modifier.fillMaxSize())
            }
            Spacer(Modifier.height(24.dp))
            val ageGroup = stringResource(R.string.obf_potential_age_group, (state.age / 10 * 10).coerceAtLeast(10))
            val note = stringResource(R.string.obf_potential_note, ageGroup)
            val bold = note.indexOf(ageGroup)
            Text(
                androidx.compose.ui.text.buildAnnotatedString {
                    if (bold < 0) {
                        append(note)
                    } else {
                        append(note.substring(0, bold))
                        withStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = ObColor.TextPrimary)) { append(ageGroup) }
                        append(note.substring(bold + ageGroup.length))
                    }
                },
                fontSize = 18.sp, lineHeight = 25.sp,
                color = ObColor.TextSecondary, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(24.dp))
        ObPrimaryButton(step.ctaText(), onClick = onNext)
        Spacer(Modifier.height(20.dp))
    }
}

private data class Testimonial(
    @StringRes val name: Int,
    @StringRes val quote: Int,
    @StringRes val highlight: Int,
    val art: String
)

private val TESTIMONIALS = listOf(
    Testimonial(R.string.obf_testi1_name, R.string.obf_testi1_quote, R.string.obf_testi1_accent, "testimonial_1"),
    Testimonial(R.string.obf_testi2_name, R.string.obf_testi2_quote, R.string.obf_testi2_accent, "testimonial_2"),
    Testimonial(R.string.obf_testi3_name, R.string.obf_testi3_quote, R.string.obf_testi3_accent, "testimonial_3")
)

@Composable
private fun TestimonialScreen(step: OnboardStep, onNext: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        ObTitle(step.title, step.titleAccent, modifier = Modifier.padding(horizontal = ScreenPadding))
        Spacer(Modifier.height(24.dp))
        LazyRow(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(TESTIMONIALS) { item ->
                Column(
                    Modifier
                        .width(320.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(ObColor.Surface)
                        .padding(14.dp)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BeforeAfterTile(stringResource(R.string.obf_start), item.art + "_before", Color(0xFF9E9E9E), Modifier.weight(1f))
                        BeforeAfterTile(stringResource(R.string.obf_goal), item.art + "_after", ObColor.Pink, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        ObIcon("✨", size = 18.dp, plain = true)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.obf_sample_plan), fontSize = 15.sp, modifier = Modifier.weight(1f))
                        Text(stringResource(item.name), fontSize = 16.sp, color = ObColor.TextSecondary)
                    }
                    Spacer(Modifier.height(8.dp))
                    ObTitle(
                        stringResource(item.quote), stringResource(item.highlight), fontSize = 17,
                        textAlign = TextAlign.Start, color = ObColor.TextPrimary
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        ObPrimaryButton(
            step.ctaText(),
            modifier = Modifier.padding(horizontal = ScreenPadding),
            onClick = onNext
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun BeforeAfterTile(label: String, art: String, badgeColor: Color, modifier: Modifier) {
    Box(modifier.fillMaxHeight()) {
        ObArt(
            name = art,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp)),
            placeholderEmoji = "🧍‍♀️"
        )
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                .background(badgeColor)
                .padding(horizontal = 16.dp, vertical = 5.dp)
        ) {
            Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------------------------------------------------------- step6 / step8

@Composable
private fun FocusBodyScreen(step: OnboardStep, state: OnboardState, onNext: () -> Unit) {
    val selected = state.answers[step.id]
    val onSelected: (String) -> Unit = { state.answers[step.id] = it }
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding)
    ) {
        Spacer(Modifier.height(20.dp))
        ObTitle(step.title, step.titleAccent)
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Giữ đúng tỉ lệ ảnh để chip rơi đúng đầu đường kẻ vẽ sẵn trong ảnh
            Box(
                Modifier
                    .fillMaxHeight()
                    .aspectRatio(FOCUS_BODY_ASPECT, matchHeightConstraintsFirst = true)
            ) {
                FocusBodyArt(selected = selected, onSelected = onSelected, modifier = Modifier.fillMaxSize())
                FocusBodyChips(
                    options = step.options,
                    selected = selected,
                    onSelected = onSelected,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        ObPrimaryButton(step.ctaText(), enabled = selected != null, onClick = onNext)
        Spacer(Modifier.height(20.dp))
    }
}

/** Kích thước drawable bg_problem_area (528 x 780). */
private const val FOCUS_BODY_ASPECT = 528f / 780f

/**
 * Đầu tự do của các đường kẻ cam vẽ sẵn trong bg_problem_area, chuẩn hoá theo ảnh. Chip đặt ngay trên
 * đầu đường kẻ như FocusFragment cũ (fragment_focus_area); "Full body" ở góc trên bên trái.
 */
private val FOCUS_CHIP_ANCHORS = mapOf(
    "arm" to Offset(0.932f, 0.087f),
    "belly" to Offset(0.063f, 0.528f),
    "butt" to Offset(0.915f, 0.676f),
    "leg" to Offset(0.081f, 0.921f)
)

@Composable
private fun FocusBodyChips(
    options: List<ObOption>,
    selected: String?,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Layout(
        modifier = modifier,
        content = {
            options.forEach { option ->
                FocusBodyChip(option.label, selected == option.key) { onSelected(option.key) }
            }
        }
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val gap = 4.dp.roundToPx()
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        layout(width, height) {
            placeables.forEachIndexed { index, placeable ->
                val anchor = FOCUS_CHIP_ANCHORS[options[index].key]
                if (anchor == null) {
                    placeable.place(0, 0)
                } else {
                    val x = (anchor.x * width - placeable.width / 2f).roundToInt()
                        .coerceIn(0, max(0, width - placeable.width))
                    val y = ((anchor.y * height).roundToInt() - placeable.height - gap).coerceAtLeast(0)
                    placeable.place(x, y)
                }
            }
        }
    }
}

@Composable
private fun FocusBodyChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) ObColor.Pink else Color.White)
            .border(1.5.dp, ObColor.Pink, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected) {
            Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
        }
        Text(
            label.uppercase(),
            color = if (selected) Color.White else ObColor.PinkText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class FocusBodyHotspot(
    val key: String,
    val centerX: Float,
    val centerY: Float,
    val radiusX: Float,
    val radiusY: Float
)

/**
 * Các toạ độ được chuẩn hoá theo drawable bg_problem_area (528 x 780).
 *
 * Một vùng có thể có nhiều hotspot (hai tay/hai chân). Vùng cụ thể luôn được kiểm tra
 * trước vùng full body để thao tác trên cơ thể cho kết quả ổn định và dễ đoán.
 */
private val FOCUS_BODY_HOTSPOTS = listOf(
    FocusBodyHotspot("arm", 0.31f, 0.27f, 0.20f, 0.18f),
    FocusBodyHotspot("arm", 0.76f, 0.25f, 0.20f, 0.18f),
    FocusBodyHotspot("belly", 0.43f, 0.60f, 0.20f, 0.13f),
    FocusBodyHotspot("butt", 0.66f, 0.75f, 0.22f, 0.14f),
    FocusBodyHotspot("leg", 0.38f, 0.88f, 0.22f, 0.16f),
    FocusBodyHotspot("leg", 0.65f, 0.88f, 0.20f, 0.16f)
)

private val FOCUS_BODY_FULL_HOTSPOTS = listOf(
    FocusBodyHotspot("full_body", 0.53f, 0.13f, 0.17f, 0.11f),
    FocusBodyHotspot("full_body", 0.50f, 0.40f, 0.27f, 0.20f)
)

private fun focusBodyRegionAt(position: Offset, width: Float, height: Float): String? {
    if (width <= 0f || height <= 0f) return null
    val x = position.x / width
    val y = position.y / height

    fun hit(hotspot: FocusBodyHotspot): Boolean {
        val dx = (x - hotspot.centerX) / hotspot.radiusX
        val dy = (y - hotspot.centerY) / hotspot.radiusY
        return dx * dx + dy * dy <= 1f
    }

    return FOCUS_BODY_HOTSPOTS.firstOrNull(::hit)?.key
        ?: FOCUS_BODY_FULL_HOTSPOTS.firstOrNull(::hit)?.key
}

@Composable
private fun FocusBodyArt(
    selected: String?,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val highlightAlpha by animateFloatAsState(
        targetValue = if (selected == null) 0f else 1f,
        animationSpec = tween(180),
        label = "focusBodyHighlight"
    )

    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.bg_problem_area),
            contentDescription = stringResource(R.string.obf_focus_tap_hint),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        // Đáy ảnh mờ dần về trắng → hoà về nền hồng nhạt của onboarding
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.12f)
                .background(Brush.verticalGradient(listOf(Color.Transparent, ObColor.Background)))
        )
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(onSelected) {
                    detectTapGestures { position ->
                        focusBodyRegionAt(
                            position = position,
                            width = size.width.toFloat(),
                            height = size.height.toFloat()
                        )?.let(onSelected)
                    }
                }
        ) {
            val active = when (selected) {
                "full_body" -> FOCUS_BODY_HOTSPOTS + FOCUS_BODY_FULL_HOTSPOTS
                null -> emptyList()
                else -> FOCUS_BODY_HOTSPOTS.filter { it.key == selected }
            }

            active.forEach { hotspot ->
                val center = Offset(size.width * hotspot.centerX, size.height * hotspot.centerY)
                val radiusX = size.width * hotspot.radiusX * 0.72f
                val radiusY = size.height * hotspot.radiusY * 0.72f
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ObColor.Pink.copy(alpha = 0.52f * highlightAlpha),
                            ObColor.Pink.copy(alpha = 0.18f * highlightAlpha),
                            Color.Transparent
                        ),
                        center = center,
                        radius = max(radiusX, radiusY)
                    ),
                    topLeft = Offset(center.x - radiusX, center.y - radiusY),
                    size = Size(radiusX * 2f, radiusY * 2f)
                )
            }

            val marker = when (selected) {
                // Chấm cam cuối đường kẻ trong ảnh
                "arm" -> Offset(size.width * 0.725f, size.height * 0.232f)
                "belly" -> Offset(size.width * 0.337f, size.height * 0.626f)
                "butt" -> Offset(size.width * 0.640f, size.height * 0.791f)
                "leg" -> Offset(size.width * 0.299f, size.height * 0.921f)
                "full_body" -> Offset(size.width * 0.50f, size.height * 0.40f)
                else -> null
            }
            marker?.let {
                val radius = 6.dp.toPx()
                drawCircle(
                    color = Color.White.copy(alpha = highlightAlpha),
                    radius = radius + 2.dp.toPx(),
                    center = it
                )
                drawCircle(
                    color = ObColor.Pink.copy(alpha = highlightAlpha),
                    radius = radius,
                    center = it
                )
                drawCircle(
                    color = Color.White.copy(alpha = highlightAlpha),
                    radius = radius + 1.dp.toPx(),
                    center = it,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }
    }
}

/** Các mốc tỉ lệ mỡ tương ứng từng nấc của thanh trượt step8. */
private val BODY_FAT_STOPS = listOf(
    Triple(R.string.obf_bf_range1, R.string.obf_bf_note1, ObColor.Warning),
    Triple(R.string.obf_bf_range2, R.string.obf_bf_note2, ObColor.Ok),
    Triple(R.string.obf_bf_range3, R.string.obf_bf_note3, ObColor.Ok),
    Triple(R.string.obf_bf_range4, R.string.obf_bf_note4, ObColor.Ok),
    Triple(R.string.obf_bf_range5, R.string.obf_bf_note5, ObColor.Warning),
    Triple(R.string.obf_bf_range6, R.string.obf_bf_note6, ObColor.Warning)
)

@Composable
private fun DesiredBodyScreen(step: OnboardStep, state: OnboardState, onNext: () -> Unit) {
    val stop = state.sliderStops[step.id] ?: step.defaultStop
    val (rangeRes, noteRes, color) = BODY_FAT_STOPS[stop.coerceIn(0, BODY_FAT_STOPS.lastIndex)]
    val range = stringResource(rangeRes)
    val note = stringResource(noteRes)

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(20.dp))
            ObTitle(step.title, step.titleAccent)
            Spacer(Modifier.height(16.dp))
            ObArt(
                name = "desired_body_$stop",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                placeholderEmoji = "🧍‍♀️",
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(12.dp))
            ObDiscreteSlider(
                stopCount = BODY_FAT_STOPS.size,
                value = stop,
                leftLabel = stringResource(R.string.obf_cut),
                rightLabel = stringResource(R.string.obf_extra)
            ) { state.sliderStops[step.id] = it }
            Spacer(Modifier.height(20.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(ObColor.PinkSoft)
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ObCoachAvatar()
                    Spacer(Modifier.width(10.dp))
                    Text(
                        stringResource(R.string.obf_body_fat_title), fontSize = 19.sp,
                        fontWeight = FontWeight.Bold, color = ObColor.TextPrimary
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(range, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = color)
                Spacer(Modifier.height(6.dp))
                Text(note, fontSize = 16.sp, color = ObColor.TextSecondary, lineHeight = 22.sp)
            }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(16.dp))
        ObPrimaryButton(step.ctaText()) {
            state.answers[step.id] = range
            onNext()
        }
        Spacer(Modifier.height(20.dp))
    }
}

// ---------------------------------------------------------------- số đo

@Composable
private fun HeightScreen(step: OnboardStep, state: OnboardState, onNext: () -> Unit) {
    val metric = state.heightMetric
    // giá trị hiển thị: cm khi ở hệ mét, inch khi ở hệ Anh
    val displayValue = if (metric) state.heightCm else (state.heightCm / 2.54f).roundToInt()

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding)
    ) {
        Spacer(Modifier.height(20.dp))
        ObTitle(step.title, step.titleAccent)
        Spacer(Modifier.height(22.dp))
        ObHintCard(step.hintEmoji, step.hint)
        Spacer(Modifier.height(22.dp))
        UnitToggle(
            leftLabel = "ft", rightLabel = "cm", rightActive = metric,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) { state.heightMetric = it }
        Spacer(Modifier.height(16.dp))

        Box(Modifier.weight(1f)) {
            // Thước tự vẽ vạch chỉ ở giữa; số đặt ngay trên vạch chỉ nên luôn khớp nhau.
            key(metric) {
                ObRuler(
                    min = if (metric) 120 else 47,
                    max = if (metric) 220 else 87,
                    value = displayValue,
                    vertical = true,
                    majorEvery = if (metric) 10 else 12,
                    midEvery = if (metric) 5 else 6,
                    label = { if (metric) it.toString() else "${it / 12}'" },
                    modifier = Modifier.fillMaxSize()
                ) { newValue ->
                    state.heightCm = if (metric) newValue else (newValue * 2.54f).roundToInt()
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(y = (-34).dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    if (metric) displayValue.toString() else "${displayValue / 12}' ${displayValue % 12}\"",
                    fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = ObColor.TextPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (metric) "cm" else "",
                    fontSize = 22.sp, color = ObColor.TextSecondary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        }
        ObPrimaryButton(step.ctaText(), onClick = onNext)
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun WeightScreen(step: OnboardStep, state: OnboardState, onNext: () -> Unit) {
    val metric = state.weightMetric
    val bmi = state.bmi

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))
            ObTitle(step.title, step.titleAccent)
            Spacer(Modifier.height(22.dp))
            UnitToggle(leftLabel = "lbs", rightLabel = "kg", rightActive = metric) { state.switchWeightUnit(it) }
            Spacer(Modifier.height(28.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    state.weightValue.toFloat().format1(),
                    fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = ObColor.TextPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (metric) "kg" else "lbs",
                    fontSize = 22.sp, color = ObColor.TextSecondary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            key(metric) {
                ObRuler(
                    min = if (metric) 30 else 66,
                    max = if (metric) 200 else 440,
                    value = state.weightValue,
                    vertical = false,
                    majorEvery = if (metric) 5 else 10,
                    midEvery = 0,
                    unitSpacing = if (metric) 12.dp else 7.dp,
                    label = { it.toString() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) { state.weightValue = it }
            }
            Spacer(Modifier.height(24.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFBEEF2))
                    .padding(18.dp)
            ) {
                Text(stringResource(R.string.obf_your_bmi), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = ObColor.TextPrimary)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        bmi.format1(),
                        fontSize = 30.sp, fontWeight = FontWeight.ExtraBold,
                        color = bmiColor(bmi), modifier = Modifier.width(92.dp)
                    )
                    Text(stringResource(bmiMessage(bmi)), fontSize = 17.sp, color = ObColor.TextSecondary, lineHeight = 23.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(16.dp))
        ObPrimaryButton(step.ctaText(), onClick = onNext)
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun TargetWeightScreen(step: OnboardStep, state: OnboardState, onNext: () -> Unit) {
    val metric = state.weightMetric
    // Lần đầu vào màn: gợi ý theo mục tiêu chính (người dùng kéo lại tuỳ ý).
    LaunchedEffect(Unit) {
        if (state.targetValue <= 0) state.targetValue = state.suggestedTargetValue()
    }
    val target = if (state.targetValue > 0) state.targetValue else state.suggestedTargetValue()
    val unit = if (metric) "kg" else "lbs"

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))
            ObTitle(step.title, step.titleAccent)
            Spacer(Modifier.height(22.dp))
            UnitToggle(leftLabel = "lbs", rightLabel = "kg", rightActive = metric) { state.switchWeightUnit(it) }
            Spacer(Modifier.height(28.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    target.toFloat().format1(),
                    fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = ObColor.TextPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text(unit, fontSize = 22.sp, color = ObColor.TextSecondary, modifier = Modifier.padding(bottom = 10.dp))
            }
            Text(
                stringResource(R.string.obf_now_value, "${state.weightValue.toFloat().format1()} $unit"),
                fontSize = 15.sp, color = ObColor.Pink, fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            key(metric) {
                ObRuler(
                    min = if (metric) 30 else 66,
                    max = if (metric) 200 else 440,
                    value = target,
                    vertical = false,
                    majorEvery = if (metric) 5 else 10,
                    unitSpacing = if (metric) 12.dp else 7.dp,
                    label = { it.toString() },
                    markValue = state.weightValue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) { state.targetValue = it }
            }
            Spacer(Modifier.height(24.dp))
            TargetFeedbackCard(state, target, unit)
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(16.dp))
        ObPrimaryButton(step.ctaText(), onClick = onNext)
        Spacer(Modifier.height(20.dp))
    }
}

/** Nhận xét mục tiêu: % cân giảm / tăng, cảnh báo khi BMI mục tiêu dưới mức khoẻ mạnh. */
@Composable
private fun TargetFeedbackCard(state: OnboardState, target: Int, unit: String) {
    val current = state.weightValue
    val percent = if (current > 0) abs(current - target) * 100f / current else 0f
    val pctText = "${percent.format1()}%"
    val healthyMin = 18.5f * state.heightMeters * state.heightMeters
    val healthyMinDisplay = if (state.weightMetric) healthyMin else healthyMin * OnboardState.LB_PER_KG
    val (title, body, warn) = when {
        state.targetBmi < 18.5f -> Triple(
            stringResource(R.string.obf_target_low_title),
            stringResource(R.string.obf_target_low_body, "${healthyMinDisplay.roundToInt()} $unit"),
            true
        )
        target == current -> Triple(
            stringResource(R.string.obf_target_keep_title),
            stringResource(R.string.obf_target_keep_body),
            false
        )
        target > current -> Triple(
            stringResource(R.string.obf_target_gain_title, pctText),
            stringResource(R.string.obf_target_gain_body),
            false
        )
        percent <= 10f -> Triple(
            stringResource(R.string.obf_target_realistic_title),
            stringResource(R.string.obf_target_realistic_body, pctText),
            false
        )
        percent <= 20f -> Triple(
            stringResource(R.string.obf_target_ambitious_title),
            stringResource(R.string.obf_target_ambitious_body, pctText),
            false
        )
        else -> Triple(
            stringResource(R.string.obf_target_long_title),
            stringResource(R.string.obf_target_long_body, pctText),
            false
        )
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (warn) Color(0xFFFFF3E0) else Color(0xFFFBEEF2))
            .padding(18.dp)
    ) {
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = if (warn) Color(0xFFB45309) else ObColor.TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(body, fontSize = 16.sp, lineHeight = 22.sp, color = ObColor.TextSecondary)
    }
}

private fun bmiColor(bmi: Float): Color = when {
    bmi < 18.5f -> ObColor.Purple
    bmi < 25f -> ObColor.Ok
    bmi < 30f -> ObColor.Warning
    else -> Color(0xFFE5484D)
}

@StringRes
private fun bmiMessage(bmi: Float): Int = when {
    bmi < 18.5f -> R.string.obf_bmi_slim
    bmi < 25f -> R.string.obf_bmi_healthy
    bmi < 30f -> R.string.obf_bmi_over
    else -> R.string.obf_bmi_obese
}

@Composable
private fun UnitToggle(
    leftLabel: String,
    rightLabel: String,
    rightActive: Boolean,
    modifier: Modifier = Modifier,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .width(200.dp)
            .height(58.dp)
            .clip(CircleShape)
            .background(ObColor.Surface)
            .border(2.dp, ObColor.Pink, CircleShape)
    ) {
        UnitToggleItem(leftLabel, !rightActive, Modifier.weight(1f)) { onChange(false) }
        UnitToggleItem(rightLabel, rightActive, Modifier.weight(1f)) { onChange(true) }
    }
}

@Composable
private fun UnitToggleItem(label: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .background(if (active) ObColor.Pink else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) Color.White else ObColor.TextSecondary
        )
    }
}

@Composable
private fun AgeScreen(step: OnboardStep, state: OnboardState, onNext: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding)
    ) {
        Spacer(Modifier.height(20.dp))
        ObTitle(step.title, step.titleAccent)
        Spacer(Modifier.height(22.dp))
        ObHintCard(step.hintEmoji, step.hint)
        Spacer(Modifier.height(12.dp))
        ObWheelPicker(
            min = 14, max = 90, value = state.age,
            modifier = Modifier.weight(1f)
        ) { state.age = it }
        ObPrimaryButton(step.ctaText(), onClick = onNext)
        Spacer(Modifier.height(20.dp))
    }
}

// ---------------------------------------------------------------- step16 chuyển phần

@Composable
private fun SectionScreen(step: OnboardStep, onNext: () -> Unit) {
    LaunchedEffect(step.design) {
        delay(1200)
        onNext()
    }
    Box(Modifier.fillMaxSize()) {
        Text(
            step.subtitle,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 0.dp),
            fontSize = 260.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ObColor.PinkSoft
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 32.dp)
        ) {
            Text(
                step.title,
                fontSize = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ObColor.TextPrimary
            )
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .width(54.dp)
                    .height(12.dp)
                    .background(ObColor.Cta)
            )
        }
    }
}

// ---------------------------------------------------------------- dự đoán

@Composable
private fun PredictionScreen(step: OnboardStep, state: OnboardState, onNext: () -> Unit) {
    // Dự đoán lần 1: màn "trước / sau" chạy ~2 giây rồi tự chuyển sang biểu đồ (chạm để bỏ qua).
    if (step.design != "step28") {
        var showChart by rememberSaveable(step.design) { mutableStateOf(false) }
        LaunchedEffect(showChart) {
            if (!showChart) {
                delay(2300)
                showChart = true
            }
        }
        androidx.compose.animation.Crossfade(targetState = showChart, animationSpec = tween(450), label = "predictionIntro") { chart ->
            if (chart) PredictionChartScreen(step, state, onNext)
            else BeforeAfterIntro(state) { showChart = true }
        }
    } else {
        PredictionChartScreen(step, state, onNext)
    }
}

/** "Hình dung bạn ở cân nặng mục tiêu": 2 thẻ hiện tại → mục tiêu, thẻ mục tiêu bật lên kèm pháo sáng. */
@Composable
private fun BeforeAfterIntro(state: OnboardState, onSkip: () -> Unit) {
    val unit = if (state.weightMetric) "kg" else "lbs"
    val target = if (state.weightMetric) state.targetWeightKg else state.targetWeightKg * OnboardState.LB_PER_KG
    val current = state.weightValue.toFloat()
    val goalIn = remember { Animatable(0f) }
    val burst = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(350)
        goalIn.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 260f))
        burst.animateTo(1f, tween(900))
    }
    Column(
        Modifier
            .fillMaxSize()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onSkip)
            .padding(horizontal = ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))
        ObTitle(stringResource(R.string.obf_change_title), accent = stringResource(R.string.obf_change_accent), fontSize = 30)
        Spacer(Modifier.height(40.dp))
        Box(Modifier.fillMaxWidth().height(320.dp), contentAlignment = Alignment.Center) {
            // pháo sáng: các tia toả ra quanh thẻ mục tiêu
            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                val c = Offset(size.width * 0.66f, size.height * 0.45f)
                val b = burst.value
                if (b > 0f && b < 1f) {
                    repeat(12) { i ->
                        val angle = Math.toRadians(i * 30.0)
                        val r1 = 60.dp.toPx() + 70.dp.toPx() * b
                        val r2 = r1 + 18.dp.toPx()
                        drawLine(
                            ObColor.Pink.copy(alpha = 1f - b),
                            Offset(c.x + (r1 * kotlin.math.cos(angle)).toFloat(), c.y + (r1 * kotlin.math.sin(angle)).toFloat()),
                            Offset(c.x + (r2 * kotlin.math.cos(angle)).toFloat(), c.y + (r2 * kotlin.math.sin(angle)).toFloat()),
                            strokeWidth = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                WeightCard(stringResource(R.string.obf_now), "${current.format1()} $unit", art = "change_before", highlight = false, modifier = Modifier.width(130.dp).height(200.dp))
                Text("➜", fontSize = 26.sp, color = ObColor.Pink, modifier = Modifier.padding(horizontal = 8.dp))
                WeightCard(
                    stringResource(R.string.obf_goal), "${target.format1()} $unit", art = "change_after", highlight = true,
                    modifier = Modifier
                        .width(150.dp)
                        .height(230.dp)
                        .graphicsLayer {
                            scaleX = 0.6f + 0.4f * goalIn.value
                            scaleY = 0.6f + 0.4f * goalIn.value
                            alpha = goalIn.value.coerceIn(0f, 1f)
                        }
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.obf_change_body),
            fontSize = 18.sp, lineHeight = 25.sp, color = ObColor.TextSecondary, textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WeightCard(caption: String, weight: String, art: String, highlight: Boolean, modifier: Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (highlight) Color.White else Color(0xFFEDE7EA))
            .border(if (highlight) 3.dp else 0.dp, if (highlight) ObColor.Pink else Color.Transparent, RoundedCornerShape(20.dp))
    ) {
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            if (rememberObArt(art) != null) {
                ObArt(name = art, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Text(if (highlight) "🙋‍♀️" else "🧍‍♀️", fontSize = if (highlight) 64.sp else 52.sp)
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .background(if (highlight) ObColor.Pink else Color(0xFFB9AFB4))
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(caption, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
            Text(weight, fontSize = if (highlight) 20.sp else 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

@Composable
private fun PredictionChartScreen(step: OnboardStep, state: OnboardState, onNext: () -> Unit) {
    val isSecond = step.design == "step28"
    val days = if (isSecond) state.fasterDays else state.baseDays
    val date = remember(days) { state.targetDate(days) }
    val baseDate = remember(state.baseDays) { state.targetDate(state.baseDays) }

    val unitLabel = if (state.weightMetric) "kg" else "lbs"
    val targetDisplay =
        if (state.weightMetric) state.targetWeightKg
        else state.targetWeightKg * OnboardState.LB_PER_KG
    val currentDisplay = state.weightValue.toFloat()

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))
            Text(
                step.subtitle,
                fontSize = 24.sp,
                color = ObColor.TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            val deltaDisplay = abs(targetDisplay - currentDisplay)
            val gain = targetDisplay >= currentDisplay
            if (isSecond) {
                ObTitle(
                    stringResource(R.string.obf_pred_by, "${targetDisplay.format1()} $unitLabel", OnboardState.formatDate(date)),
                    accent = "${targetDisplay.format1()} $unitLabel",
                    fontSize = 32
                )
            } else {
                ObTitle(
                    stringResource(
                        if (gain) R.string.obf_pred_gain_by else R.string.obf_pred_lose_by,
                        "${deltaDisplay.format1()} $unitLabel", OnboardState.formatDate(date)
                    ),
                    accent = "${deltaDisplay.format1()} $unitLabel",
                    fontSize = 32
                )
            }
            Spacer(Modifier.height(30.dp))
            ObWeightChart(
                currentWeight = currentDisplay,
                targetWeight = targetDisplay,
                unitLabel = unitLabel,
                days = days,
                endLabel = OnboardState.formatDateShort(date),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                ghostDays = if (isSecond && state.savedDays > 0) state.baseDays else 0,
                ghostLabel = if (isSecond) OnboardState.formatDateShort(baseDate) else "",
                shiftFromDays = if (isSecond && state.savedDays > 0) state.baseDays else 0
            )
            Spacer(Modifier.height(26.dp))
            if (isSecond) {
                if (state.savedDays > 0) {
                    // hiện sau khi mốc đích đã trượt về ngày mới (≈3.2 giây), số ngày đếm lên
                    var shown by remember { mutableIntStateOf(0) }
                    LaunchedEffect(state.savedDays) {
                        delay(3200)
                        for (d in 1..state.savedDays) { shown = d; delay(40) }
                    }
                    AnimatedVisibility(visible = shown > 0, enter = fadeIn() + expandVertically()) {
                        val res = LocalContext.current.resources
                        ObTitle(
                            res.getQuantityString(R.plurals.obf_pred_sooner, shown, shown),
                            accent = res.getQuantityString(R.plurals.obf_pred_sooner_accent, shown, shown), fontSize = 22
                        )
                    }
                }
                Text(
                    stringResource(R.string.obf_pred_estimate),
                    modifier = Modifier.padding(top = 10.dp),
                    fontSize = 15.sp, lineHeight = 21.sp,
                    color = ObColor.TextMuted, textAlign = TextAlign.Center
                )
            } else {
                Text(
                    step.body,
                    fontSize = 18.sp, lineHeight = 25.sp,
                    color = ObColor.TextSecondary, textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(26.dp))
        ObPrimaryButton(step.ctaText(), onClick = onNext)
        Spacer(Modifier.height(20.dp))
    }
}

// ---------------------------------------------------------------- thanh trượt

@Composable
private fun SliderScreen(step: OnboardStep, state: OnboardState, onNext: () -> Unit) {
    val stop = state.sliderStops[step.id] ?: step.defaultStop
    val current = step.stops[stop.coerceIn(0, step.stops.lastIndex)]

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))
            ObTitle(step.title, step.titleAccent)
            if (step.hint.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(step.hint, fontSize = 20.sp, color = ObColor.TextSecondary, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(20.dp))
            ObFigureArt(
                figure = step.figure,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                label = when (step.figure) {
                    ObFigure.CALENDAR -> OnboardState.FREQUENCY_PER_WEEK[stop.coerceIn(0, 3)].toString()
                    else -> current.title
                },
                fraction = if (step.stops.size > 1) stop.toFloat() / step.stops.lastIndex else 0f
            )
            Spacer(Modifier.height(20.dp))
            Text(
                current.title,
                fontSize = 27.sp, fontWeight = FontWeight.ExtraBold,
                color = ObColor.TextPrimary, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                current.description,
                fontSize = 18.sp, lineHeight = 25.sp,
                color = ObColor.TextSecondary, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(22.dp))
            ObDiscreteSlider(
                stopCount = step.stops.size,
                value = stop,
                leftLabel = step.subtitle,
                rightLabel = step.body
            ) { state.sliderStops[step.id] = it }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(18.dp))
        ObPrimaryButton(step.ctaText()) {
            state.answers[step.id] = current.title
            onNext()
        }
        Spacer(Modifier.height(20.dp))
    }
}

// ---------------------------------------------------------------- phát biểu / yes-no

@Composable
private fun StatementScreen(step: OnboardStep, state: OnboardState, onNext: () -> Unit) {
    fun answer(value: String) {
        state.answers[step.id] = value
        onNext()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding)
    ) {
        Spacer(Modifier.height(20.dp))
        ObTitle(step.title, step.titleAccent)
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            ObArt(
                name = step.art,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 42.dp)
                    .clip(RoundedCornerShape(24.dp)),
                placeholderEmoji = "🪞"
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(0.94f)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
                    .padding(horizontal = 22.dp, vertical = 20.dp)
            ) {
                Text(
                    step.body,
                    fontSize = 20.sp,
                    lineHeight = 27.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = ObColor.TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            YesNoButton(stringResource(R.string.obf_no), "✕", ObColor.Pink, Modifier.weight(1f)) { answer("no") }
            YesNoButton(stringResource(R.string.obf_yes), "✓", ObColor.Ok, Modifier.weight(1f)) { answer("yes") }
        }
        Spacer(Modifier.height(22.dp))
    }
}

@Composable
private fun YesNoButton(
    label: String,
    glyph: String,
    glyphColor: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(ObColor.Surface)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ObIcon(glyph, size = 28.dp, plain = true, tint = glyphColor)
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ObColor.TextPrimary)
    }
}

@Composable
private fun FullYesNoScreen(step: OnboardStep, state: OnboardState, onNext: () -> Unit) {
    fun answer(value: String) {
        state.answers[step.id] = value
        onNext()
    }

    Box(Modifier.fillMaxSize()) {
        ObArt(
            name = step.art,
            modifier = Modifier.fillMaxSize(),
            placeholderEmoji = "🧘‍♀️"
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0x59000000),
                        0.16f to Color.Transparent,
                        0.35f to Color.Transparent,
                        1f to Color(0xCC000000)
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = ScreenPadding)
        ) {
            Text(
                step.title,
                fontSize = 40.sp,
                lineHeight = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(Modifier.height(28.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .weight(0.4f)
                        .height(72.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .border(1.5.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(36.dp))
                        .clickable { answer("no") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.obf_no), fontSize = 22.sp, color = Color.White)
                }
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .height(72.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .background(Color.White)
                        .clickable { answer("yes") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.obf_yes), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ObColor.TextPrimary)
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

// ---------------------------------------------------------------- step45 / 46 / 47

private val GENERATING_MESSAGES = listOf(
    R.string.obf_gen_msg1,
    R.string.obf_gen_msg2,
    R.string.obf_gen_msg3,
    R.string.obf_gen_msg4
)

/**
 * Màn tạo kế hoạch: vòng % chạy ~8 giây, phần dưới đổi 3 pha theo % như video tham khảo
 * (cộng đồng → thư viện bài tập → đánh giá), mỗi pha một tiêu đề và một Lottie như Tai Chi Qigong
 * (xem ObGeneratingLottie.kt).
 */
@Composable
private fun GeneratingScreen(step: OnboardStep, onNext: () -> Unit) {
    var percent by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (percent < 100) {
            // chậm dần ở cuối cho cảm giác "đang hoàn thiện"
            delay(if (percent < 85) 70 else 130)
            percent += 1
        }
        delay(450)
        onNext()
    }
    val phase = when {
        percent < 35 -> 0
        percent < 85 -> 1
        else -> 2
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        Text(step.title, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = ObColor.TextPrimary)
        Spacer(Modifier.height(22.dp))
        Box(Modifier.size(170.dp), contentAlignment = Alignment.Center) {
            GeneratingRingLottie(Modifier.fillMaxSize())
            Text("$percent%", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = ObColor.TextPrimary)
        }
        Spacer(Modifier.height(14.dp))
        Text(
            stringResource(GENERATING_MESSAGES[(percent / 26).coerceIn(0, GENERATING_MESSAGES.lastIndex)]),
            fontSize = 16.sp, color = ObColor.TextSecondary
        )
        Spacer(Modifier.height(22.dp))
        androidx.compose.animation.Crossfade(
            targetState = phase,
            animationSpec = tween(400),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            label = "generatingPhase"
        ) { current ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                ObTitle(
                    when (current) {
                        0 -> stringResource(R.string.obf_gen_phase1)
                        1 -> stringResource(R.string.obf_gen_phase2)
                        else -> stringResource(R.string.obf_gen_phase3)
                    },
                    fontSize = 22, color = ObColor.Pink
                )
                Spacer(Modifier.height(12.dp))
                GeneratingStageLottie(
                    stage = GeneratingStage.entries[current],
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun PlanResultScreen(step: OnboardStep, state: OnboardState, onNext: () -> Unit) {
    val days = state.fasterDays
    val date = remember(days) { state.targetDate(days) }
    val unitLabel = if (state.weightMetric) "kg" else "lbs"
    val current =
        if (state.weightMetric) state.weightKg else state.weightKg * OnboardState.LB_PER_KG
    val target =
        if (state.weightMetric) state.targetWeightKg else state.targetWeightKg * OnboardState.LB_PER_KG
    val direction = if (target >= current) 1f else -1f
    // Mốc tuần 1 theo đúng tốc độ dự đoán (~0.6 kg/tuần), không phóng đại.
    val weeklyKg = OnboardState.KG_PER_DAY * 7f
    val weeklyInCurrentUnit =
        if (state.weightMetric) weeklyKg else weeklyKg * OnboardState.LB_PER_KG
    val weekOneChange = minOf(weeklyInCurrentUnit, abs(target - current))
    val weekOne = current + direction * weekOneChange
    val displayDelta = abs(target - current)

    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))
            Text(step.title, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = ObColor.TextPrimary)
            Spacer(Modifier.height(6.dp))
            ObTitle(
                stringResource(R.string.obf_plan_on, "${target.format1()}$unitLabel", OnboardState.formatDate(date)),
                accent = "${target.format1()}$unitLabel",
                fontSize = 28
            )
            Spacer(Modifier.height(18.dp))
            ObPlanWeightChart(
                currentWeight = current,
                weekOneWeight = weekOne,
                targetWeight = target,
                unitLabel = unitLabel,
                endLabel = OnboardState.formatDateShort(date),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            )
            Spacer(Modifier.height(16.dp))
            ObTitle(
                stringResource(R.string.obf_plan_stick),
                accent = stringResource(R.string.obf_plan_stick_accent), fontSize = 16, textAlign = TextAlign.Start,
                color = ObColor.TextSecondary
            )

            Spacer(Modifier.height(26.dp))
            SectionHeader(stringResource(R.string.obf_your_training_week))
            Spacer(Modifier.height(12.dp))
            RoutineCalendar(days, state.trainingDaysPerWeek)

            Spacer(Modifier.height(26.dp))
            SectionHeader(stringResource(R.string.obf_designed_for_you))
            Spacer(Modifier.height(12.dp))
            DesignedForYouCard(state)

            Spacer(Modifier.height(26.dp))
            SectionHeader(stringResource(R.string.obf_plan_preview))
            Spacer(Modifier.height(12.dp))
            PlanPreviewCard()
            Spacer(Modifier.height(24.dp))
        }
        Box(Modifier.padding(horizontal = ScreenPadding, vertical = 12.dp)) {
            ObPrimaryButton(step.ctaText(), onClick = onNext)
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        modifier = Modifier.fillMaxWidth(),
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        color = ObColor.TextPrimary
    )
}

@Composable
private fun RoutineCalendar(days: Int, perWeek: Int) {
    val calendar = remember { Calendar.getInstance() }
    val startWeekday = remember { calendar.get(Calendar.DAY_OF_WEEK) - 1 }
    // Buổi tập rải đều trong mỗi 7 ngày kể từ hôm bắt đầu, đúng số buổi/tuần đã chọn.
    val pattern = when (perWeek) {
        1 -> setOf(0)
        2 -> setOf(0, 3)
        3 -> setOf(0, 2, 4)
        4 -> setOf(0, 1, 3, 5)
        else -> setOf(0, 1, 2, 4, 5)
    }
    // Ngày trong tháng tính theo lịch thật (qua cuối tháng không bị sai số).
    fun dayOfMonth(offset: Int): Int = (calendar.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, offset)
    }.get(Calendar.DAY_OF_MONTH)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ObColor.Surface)
            .padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            // Chữ viết tắt thứ trong tuần theo ngôn ngữ app (CN/T2…, 日/一…), bắt đầu Chủ nhật như lưới bên dưới
            val weekdays = remember {
                android.icu.text.DateFormatSymbols.getInstance(java.util.Locale.getDefault())
                    .getWeekdays(android.icu.text.DateFormatSymbols.STANDALONE, android.icu.text.DateFormatSymbols.NARROW)
                    .drop(1)
            }
            weekdays.forEach {
                Text(
                    it,
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    color = ObColor.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        // lịch bắt đầu từ ô trống tới ngày hôm nay, chạy đủ số tuần của kế hoạch
        val totalCells = startWeekday + days + 1
        val weeks = (totalCells + 6) / 7
        for (week in 0 until weeks) {
            Row(Modifier.fillMaxWidth()) {
                for (weekday in 0 until 7) {
                    val cellIndex = week * 7 + weekday
                    val offset = cellIndex - startWeekday
                    Box(
                        Modifier
                            .weight(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            offset < 0 -> Spacer(Modifier.height(44.dp))
                            offset == 0 -> CalendarCell(stringResource(R.string.obf_start), true, ObColor.Pink, Color.White)
                            offset == days -> CalendarCell(stringResource(R.string.obf_success), true, Color.White, ObColor.Pink, bordered = true)
                            else -> {
                                val training = offset % 7 in pattern
                                CalendarCell(
                                    dayOfMonth(offset).toString(),
                                    training,
                                    if (training) Color(0xFFFDE7EF) else Color.Transparent,
                                    ObColor.TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarCell(
    label: String,
    active: Boolean,
    background: Color,
    textColor: Color,
    bordered: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .then(
                if (bordered) Modifier.border(2.dp, ObColor.Pink, RoundedCornerShape(10.dp))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            // nhãn dịch có thể dài hơn "Start"/"Success" → một dòng, chữ nhỏ dần, không tràn ô
            fontSize = when {
                label.length > 7 -> 9.sp
                label.length > 3 -> 11.sp
                else -> 15.sp
            },
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun DesignedForYouCard(state: OnboardState) {
    val focusLabel = state.optionLabel(
        id = ObId.FOCUS,
        key = state.answers[ObId.FOCUS],
        fallback = stringResource(R.string.obf_step6_opt_full_body)
    )
    val calories = (state.weightKg * 20).roundToInt()

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFDEEF3))
    ) {
        Column(
            Modifier
                .weight(1f)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PlanFact("🎯", stringResource(R.string.obf_focus_area), focusLabel)
            PlanFact("⏱️", stringResource(R.string.obf_duration), stringResource(R.string.obf_duration_value))
            PlanFact("🔥", stringResource(R.string.obf_calories), stringResource(R.string.obf_kcal_day, calories))
        }
        ObArt(
            name = "plan_body",
            modifier = Modifier
                .width(130.dp)
                .height(200.dp),
            placeholderEmoji = "🧍‍♀️",
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun PlanFact(emoji: String, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ObIcon(emoji, size = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 14.sp, color = ObColor.TextSecondary)
            Text(value, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = ObColor.TextPrimary)
        }
    }
}

private data class PlanWeek(@StringRes val title: Int, val emoji: String, val reps: List<String>)

private val PLAN_WEEKS = listOf(
    PlanWeek(R.string.obf_week1, "😊", listOf("20s", "×20", "×16", "×20")),
    PlanWeek(R.string.obf_week2, "🔥", listOf("30s", "×26", "×20", "×14")),
    PlanWeek(R.string.obf_week3, "💪", listOf("20s", "×30", "×26", "×20")),
    PlanWeek(R.string.obf_week4, "🤩", listOf("30s", "×30", "×16", "40s"))
)

@Composable
private fun PlanPreviewCard() {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ObColor.Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        PLAN_WEEKS.forEachIndexed { index, week ->
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(ObColor.Pink)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        stringResource(week.title), fontSize = 17.sp,
                        fontWeight = FontWeight.Bold, color = ObColor.TextPrimary
                    )
                    Spacer(Modifier.width(6.dp))
                    ObIcon(week.emoji, size = 20.dp, plain = true)
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    week.reps.forEachIndexed { repIndex, rep ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFDEEF3))
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ObArt(
                                name = "plan_w${index + 1}_$repIndex",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                placeholderEmoji = "🤸‍♀️",
                                contentScale = ContentScale.Fit
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(rep, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ObColor.TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrialIntroScreen(step: OnboardStep, onNext: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1600)
        onNext()
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFFFDE8EF), Color(0xFFFFF7FA)))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(ObColor.PinkLight, ObColor.Pink)))
            ) {
                ObIcon("🎁", size = 64.dp, plain = true, tint = Color.White)
            }
            Spacer(Modifier.height(16.dp))
            ObTitle(step.title, step.titleAccent, fontSize = 32)
        }
    }
}

/** Chữ nút chính của màn; rỗng → "Next" theo ngôn ngữ app. */
@Composable
internal fun OnboardStep.ctaText(): String = cta.ifEmpty { stringResource(R.string.obf_next) }
