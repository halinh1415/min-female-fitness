package mint.woman.female.workout.activity.ui.onboarding.onboard

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import mint.woman.female.workout.R
import java.util.Calendar
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
    onNext: () -> Unit
) {
    when (step.kind) {
        StepKind.WELCOME -> WelcomeScreen(step, onNext)
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
private fun WelcomeScreen(step: OnboardStep, onNext: () -> Unit) {
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
                WelcomeStat("Minutes a day to get started", "7+")
                WelcomeStat("Weeks in your first personal plan", "4")
                WelcomeStat("Equipment needed", "0")
                Spacer(Modifier.height(8.dp))
            }
            ObPrimaryButton(step.cta, onClick = onNext)
            Spacer(Modifier.height(18.dp))
            Text(
                "Already have an account? SIGN IN",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ObColor.TextPrimary,
                modifier = Modifier.clickable(onClick = onNext)
            )
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
            ObPrimaryButton(step.cta, onClick = onNext)
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
        ObPrimaryButton(step.cta, enabled = selected != null, onClick = onNext)
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
        ObPrimaryButton(step.cta, enabled = selected.isNotEmpty(), onClick = onNext)
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
        ObPrimaryButton(step.cta, onClick = onNext)
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
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(start = 32.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Day 1", "Day 7", "Day 30").forEach {
                        Text(it, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = ObColor.TextPrimary)
                    }
                }
                Text(
                    "Fitness\nResults",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 32.dp),
                    fontSize = 17.sp, fontWeight = FontWeight.Bold, color = ObColor.TextPrimary
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "For women in their ${state.age / 10 * 10}s, " +
                        "progress is often slow in the first week, but stay consistent " +
                        "and your body will start to respond.",
                fontSize = 18.sp, lineHeight = 25.sp,
                color = ObColor.TextSecondary, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(24.dp))
        ObPrimaryButton(step.cta, onClick = onNext)
        Spacer(Modifier.height(20.dp))
    }
}

private data class Testimonial(
    val name: String,
    val quote: String,
    val highlight: String,
    val art: String
)

private val TESTIMONIALS = listOf(
    Testimonial(
        "Full body · 4 weeks",
        "Gentle full-body sessions that build up week by week, no equipment needed.",
        "no equipment needed", "testimonial_1"
    ),
    Testimonial(
        "Belly & core · 20 min",
        "20 minutes a day of core work, mixed with stretching to keep it fun.",
        "20 minutes", "testimonial_2"
    ),
    Testimonial(
        "Legs & butt · Beginner",
        "Low-impact, jump-free moves that are easy on the knees.",
        "jump-free", "testimonial_3"
    )
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
                        BeforeAfterTile("Start", item.art + "_before", Color(0xFF9E9E9E), Modifier.weight(1f))
                        BeforeAfterTile("Goal", item.art + "_after", ObColor.Pink, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("✨ Sample plan", fontSize = 15.sp, modifier = Modifier.weight(1f))
                        Text(item.name, fontSize = 16.sp, color = ObColor.TextSecondary)
                    }
                    Spacer(Modifier.height(8.dp))
                    ObTitle(
                        item.quote, item.highlight, fontSize = 17,
                        textAlign = TextAlign.Start, color = ObColor.TextPrimary
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        ObPrimaryButton(
            step.cta,
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
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding)
    ) {
        Spacer(Modifier.height(20.dp))
        ObTitle(step.title, step.titleAccent)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .weight(0.46f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                step.options.forEach { option ->
                    val isSelected = selected == option.key
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isSelected) ObColor.PinkSoft else ObColor.Surface)
                            .then(
                                if (isSelected) Modifier.border(2.dp, ObColor.Pink, RoundedCornerShape(24.dp))
                                else Modifier
                            )
                            .clickable { state.answers[step.id] = option.key }
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            option.label,
                            modifier = Modifier.weight(1f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) ObColor.PinkText else ObColor.TextPrimary
                        )
                        ObCheckCircle(isSelected)
                    }
                }
            }
            FocusBodyArt(
                selected = selected,
                onSelected = { state.answers[step.id] = it },
                modifier = Modifier
                    .weight(0.54f)
                    .fillMaxHeight()
            )
        }
        ObPrimaryButton(step.cta, enabled = selected != null, onClick = onNext)
        Spacer(Modifier.height(20.dp))
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
 * Các toạ độ được chuẩn hoá theo drawable bg_problem_area (500 x 780).
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
            contentDescription = "Tap a body area to select it",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
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
                "arm" -> Offset(size.width * 0.76f, size.height * 0.25f)
                "belly" -> Offset(size.width * 0.43f, size.height * 0.60f)
                "butt" -> Offset(size.width * 0.66f, size.height * 0.75f)
                "leg" -> Offset(size.width * 0.38f, size.height * 0.88f)
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
    Triple("10%~15%", "This is an athletic body fat level - it takes serious training to hold.", ObColor.Warning),
    Triple("16%~20%", "A lean and toned level that most users are happy with.", ObColor.Ok),
    Triple("21%~25%", "A healthy level for women - great for long term results.", ObColor.Ok),
    Triple("26%~30%", "Still within a healthy range, with some room to tone up.", ObColor.Ok),
    Triple("31%~35%", "A bit above the healthy range, we'll help you bring it down.", ObColor.Warning),
    Triple("36%~45% (Consult a doctor)", "This body fat level seems too high for you, which might cause some health issues...", ObColor.Warning)
)

@Composable
private fun DesiredBodyScreen(step: OnboardStep, state: OnboardState, onNext: () -> Unit) {
    val stop = state.sliderStops[step.id] ?: step.defaultStop
    val (range, note, color) = BODY_FAT_STOPS[stop.coerceIn(0, BODY_FAT_STOPS.lastIndex)]

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
                leftLabel = "Cut",
                rightLabel = "Extra"
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
                        "Your Target Body Fat", fontSize = 19.sp,
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
        ObPrimaryButton(step.cta) {
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
            // Tạo lại state của ruler khi đổi hệ đo. Nếu giữ LazyListState cũ,
            // vị trí index của cm sẽ bị tái sử dụng cho inch và làm thước nhảy sai.
            key(metric) {
                ObVerticalRuler(
                    min = if (metric) 120 else 47,
                    max = if (metric) 220 else 87,
                    value = displayValue,
                    labelStep = if (metric) 10 else 12,
                    feetAndInches = !metric,
                    modifier = Modifier.fillMaxSize()
                ) { newValue ->
                    state.heightCm =
                        if (metric) newValue else (newValue * 2.54f).roundToInt()
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    if (metric) state.heightCm.toString() else "${displayValue / 12}' ${displayValue % 12}\"",
                    fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = ObColor.TextPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (metric) "cm" else "",
                    fontSize = 22.sp, color = ObColor.TextSecondary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .offset(y = 44.dp)
                    .fillMaxWidth(0.72f)
                    .height(3.dp)
                    .background(ObColor.Pink)
            )
        }
        ObPrimaryButton(step.cta, onClick = onNext)
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
            UnitToggle(leftLabel = "lbs", rightLabel = "kg", rightActive = metric) { newMetric ->
                if (newMetric != metric) {
                    state.weightValue = if (newMetric) {
                        (state.weightValue / OnboardState.LB_PER_KG).roundToInt()
                    } else {
                        (state.weightValue * OnboardState.LB_PER_KG).roundToInt()
                    }
                    state.weightMetric = newMetric
                }
            }
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
            ObHorizontalRuler(
                min = if (metric) 30 else 66,
                max = if (metric) 200 else 440,
                value = state.weightValue,
                majorStep = if (metric) 5 else 10,
                labelStep = if (metric) 5 else 10,
                pixelsPerUnitDp = if (metric) 14 else 7,
                feetAndInches = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) { state.weightValue = it }
            Spacer(Modifier.height(24.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFBEEF2))
                    .padding(18.dp)
            ) {
                Text("CURRENT BMI", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = ObColor.TextPrimary)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        bmi.format1(),
                        fontSize = 30.sp, fontWeight = FontWeight.ExtraBold,
                        color = bmiColor(bmi), modifier = Modifier.width(92.dp)
                    )
                    Text(bmiMessage(bmi), fontSize = 17.sp, color = ObColor.TextSecondary, lineHeight = 23.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(16.dp))
        ObPrimaryButton(step.cta, onClick = onNext)
        Spacer(Modifier.height(20.dp))
    }
}

private fun bmiColor(bmi: Float): Color = when {
    bmi < 18.5f -> ObColor.Purple
    bmi < 25f -> ObColor.Ok
    bmi < 30f -> ObColor.Warning
    else -> Color(0xFFE5484D)
}

private fun bmiMessage(bmi: Float): String = when {
    bmi < 18.5f -> "You are on the slim side - let's build strength together!"
    bmi < 25f -> "Your BMI is in a healthy range. Let's keep it that way!"
    bmi < 30f -> "You only need a bit more sweat exercises to see a fitter you!"
    else -> "A steady plan will help you get back to a healthy range."
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
        ObPrimaryButton(step.cta, onClick = onNext)
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
            val verb = if (targetDisplay >= currentDisplay) "Gain" else "Lose"
            if (isSecond) {
                ObTitle(
                    "${targetDisplay.format1()} $unitLabel by ${OnboardState.formatDate(date)}",
                    accent = "${targetDisplay.format1()} $unitLabel",
                    fontSize = 32
                )
            } else {
                ObTitle(
                    "$verb ${deltaDisplay.format1()} $unitLabel by ${OnboardState.formatDate(date)}",
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
                ghostLabel = if (isSecond) OnboardState.formatDateShort(baseDate) else ""
            )
            Spacer(Modifier.height(26.dp))
            if (isSecond) {
                if (state.savedDays > 0) {
                    ObTitle(
                        "Your routine gets you there ${state.savedDays} days sooner",
                        accent = "${state.savedDays} days sooner", fontSize = 22
                    )
                }
                Text(
                    "Estimated at a safe pace of under 0.7 kg (1.5 lb) per week. Results vary.",
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
        ObPrimaryButton(step.cta, onClick = onNext)
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
        ObPrimaryButton(step.cta) {
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
            YesNoButton("No", "✕", ObColor.Pink, Modifier.weight(1f)) { answer("no") }
            YesNoButton("Yes", "✓", ObColor.Ok, Modifier.weight(1f)) { answer("yes") }
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
        Text(glyph, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = glyphColor)
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
                    Text("No", fontSize = 22.sp, color = Color.White)
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
                    Text("Yes", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ObColor.TextPrimary)
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

// ---------------------------------------------------------------- step45 / 46 / 47

private val GENERATING_MESSAGES = listOf(
    "Analyzing your answers...",
    "Calculating your fitness level...",
    "Picking the right exercises...",
    "Finalizing your personal plan..."
)

@Composable
private fun GeneratingScreen(step: OnboardStep, onNext: () -> Unit) {
    var percent by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (percent < 100) {
            delay(45)
            percent += 1
        }
        delay(320)
        onNext()
    }
    val animated by animateFloatAsState(percent / 100f, tween(220), label = "generating")

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))
        Text(step.title, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = ObColor.TextPrimary)
        Spacer(Modifier.height(30.dp))
        Box(Modifier.size(170.dp), contentAlignment = Alignment.Center) {
            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                val stroke = size.minDimension * 0.08f
                drawArc(
                    color = ObColor.Track,
                    startAngle = -90f, sweepAngle = 360f, useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
                )
                drawArc(
                    color = ObColor.Pink,
                    startAngle = -90f, sweepAngle = 360f * animated, useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = stroke,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )
            }
            Text("$percent%", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = ObColor.TextPrimary)
        }
        Spacer(Modifier.height(26.dp))
        Text(
            GENERATING_MESSAGES[(percent / 26).coerceIn(0, GENERATING_MESSAGES.lastIndex)],
            fontSize = 18.sp, color = ObColor.TextSecondary
        )
        Spacer(Modifier.height(26.dp))
        ObTitle(step.subtitle, fontSize = 26, color = ObColor.Pink)
        Spacer(Modifier.height(20.dp))
        if (rememberObArt("generating_users") != null) {
            ObArt(
                name = "generating_users",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Fit
            )
        } else {
            // Chưa có ảnh thì không để khung trống chiếm nửa màn hình.
            Spacer(Modifier.weight(1f))
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
                "${target.format1()}$unitLabel on ${OnboardState.formatDate(date)}",
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
                "Stick with your plan and you could reach this goal at a safe, steady pace. Results vary.",
                accent = "safe, steady pace", fontSize = 16, textAlign = TextAlign.Start,
                color = ObColor.TextSecondary
            )

            Spacer(Modifier.height(26.dp))
            SectionHeader("FITNESS ROUTINE")
            Spacer(Modifier.height(12.dp))
            RoutineCalendar(days)

            Spacer(Modifier.height(26.dp))
            SectionHeader("DESIGNED FOR YOU")
            Spacer(Modifier.height(12.dp))
            DesignedForYouCard(state)

            Spacer(Modifier.height(26.dp))
            SectionHeader("PLAN PREVIEW")
            Spacer(Modifier.height(12.dp))
            PlanPreviewCard()
            Spacer(Modifier.height(24.dp))
        }
        Box(Modifier.padding(horizontal = ScreenPadding, vertical = 12.dp)) {
            ObPrimaryButton(step.cta, onClick = onNext)
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
private fun RoutineCalendar(days: Int) {
    val calendar = remember { Calendar.getInstance() }
    val startDay = remember { calendar.get(Calendar.DAY_OF_MONTH) }
    val startWeekday = remember { calendar.get(Calendar.DAY_OF_WEEK) - 1 }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ObColor.Surface)
            .padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
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
                            offset == 0 -> CalendarCell("Start", true, ObColor.Pink, Color.White)
                            offset == days -> CalendarCell("Success", true, Color.White, ObColor.Pink, bordered = true)
                            else -> CalendarCell(
                                ((startDay + offset - 1) % 31 + 1).toString(),
                                offset % 3 != 0,
                                if (offset % 3 != 0) Color(0xFFFDE7EF) else Color.Transparent,
                                ObColor.TextPrimary
                            )
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
            fontSize = if (label.length > 3) 11.sp else 15.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
private fun DesignedForYouCard(state: OnboardState) {
    val focusLabel = state.optionLabel(
        id = ObId.FOCUS,
        key = state.answers[ObId.FOCUS],
        fallback = "Full body"
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
            PlanFact("🎯", "Focus Area", focusLabel)
            PlanFact("⏱️", "Duration", "7-14 min")
            PlanFact("🔥", "Calories", "$calories kcal/day")
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
        Text(emoji, fontSize = 24.sp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 14.sp, color = ObColor.TextSecondary)
            Text(value, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = ObColor.TextPrimary)
        }
    }
}

private data class PlanWeek(val title: String, val emoji: String, val reps: List<String>)

private val PLAN_WEEKS = listOf(
    PlanWeek("Week 1: Wake up your body", "😊", listOf("20s", "×20", "×16", "×20")),
    PlanWeek("Week 2: Burn fat", "🔥", listOf("30s", "×26", "×20", "×14")),
    PlanWeek("Week 3: Build muscle", "💪", listOf("20s", "×30", "×26", "×20")),
    PlanWeek("Week 4: Sculpt your body", "🤩", listOf("30s", "×30", "×16", "40s"))
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
                        week.title, fontSize = 17.sp,
                        fontWeight = FontWeight.Bold, color = ObColor.TextPrimary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(week.emoji, fontSize = 17.sp)
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
            Text("🎁", fontSize = 72.sp)
            Spacer(Modifier.height(16.dp))
            ObTitle(step.title, step.titleAccent, fontSize = 32)
        }
    }
}
