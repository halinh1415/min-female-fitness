package mint.woman.female.workout.activity.ui.report

import android.widget.ImageView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.compose.MinButton
import mint.woman.female.workout.activity.ui.compose.MinCard
import mint.woman.female.workout.activity.ui.compose.MinColor
import mint.woman.female.workout.activity.ui.compose.MinLargeHeader
import mint.woman.female.workout.activity.ui.compose.MinProgressBar
import mint.woman.female.workout.badge.BadgeIconBinder
import mint.woman.female.workout.badge.BadgeProgress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// ---------------------------------------------------------------- state

data class WeightPoint(val time: Long, val value: Float)

/** Trạng thái màn Report – Fragment cập nhật từ HistoryViewModel/MyPreference. */
class ReportUiState {
    var streakDays by mutableIntStateOf(0)

    // hôm nay
    var todayWorkouts by mutableIntStateOf(0)
    var todayCalories by mutableIntStateOf(0)
    var todayMinutes by mutableIntStateOf(0)
    var todayWorkoutsTrend by mutableFloatStateOf(0f)
    var todayCaloriesTrend by mutableFloatStateOf(0f)
    var todayMinutesTrend by mutableFloatStateOf(0f)

    // tuần
    var weekDone by mutableIntStateOf(0)
    var weekGoal by mutableIntStateOf(0)
    var weekCalories by mutableIntStateOf(0)
    var weekMinutes by mutableIntStateOf(0)
    var weekCaloriesTrend by mutableFloatStateOf(0f)
    var weekMinutesTrend by mutableFloatStateOf(0f)

    // cân nặng
    var weightUnit by mutableStateOf("kg")
    var currentWeight by mutableFloatStateOf(0f)
    var targetWeight by mutableFloatStateOf(0f)
    var weights by mutableStateOf<List<WeightPoint>>(emptyList())
    var weightRange by mutableIntStateOf(7)
    var weightTrendText by mutableStateOf<Map<Int, String>>(emptyMap())

    // calo
    var caloriesRange by mutableIntStateOf(7)
    var caloriesData by mutableStateOf<Map<Int, List<Pair<String, Int>>>>(emptyMap())
    var caloriesTrendText by mutableStateOf<Map<Int, String>>(emptyMap())
    /** Phút tập từng ngày theo khoảng 7/30/90 (cùng khoá ngày "yyyy-MM-dd" như calo); [showMinutes] = đang xem phút. */
    var minutesData by mutableStateOf<Map<Int, List<Pair<String, Int>>>>(emptyMap())
    var showMinutes by mutableStateOf(false)

    // kỷ lục & tổng cộng (toàn bộ lịch sử)
    var totalWorkouts by mutableIntStateOf(0)
    var totalMinutes by mutableIntStateOf(0)
    var totalCalories by mutableIntStateOf(0)
    var longestSec by mutableIntStateOf(0)
    var longestName by mutableStateOf("")
    var bestWeekWorkouts by mutableIntStateOf(0)

    // BMI
    var bmi by mutableFloatStateOf(0f)
    var bmiLabel by mutableStateOf("")

    // huy hiệu
    var badgeTotal by mutableIntStateOf(0)
    var badgeUnlocked by mutableIntStateOf(0)
    var badgePercent by mutableIntStateOf(0)
    var nextBadge by mutableStateOf<BadgeProgress?>(null)
    var nextBadgeRemaining by mutableStateOf("")

    // gợi ý buổi sau
    var suggestionText by mutableStateOf("")
    var streakTargetText by mutableStateOf("")
}

interface ReportActions {
    fun onEditWeight()
    fun onOpenBadges()
    fun onScheduleNext()
}

// ---------------------------------------------------------------- screen

@Composable
fun ReportScreen(state: ReportUiState, actions: ReportActions) {
    // Column cuộn thường thay vì LazyColumn (như Home / Tai Chi): Report chỉ ~9 thẻ, dựng sẵn hết thì lúc
    // cuộn không phải dựng lại thẻ biểu đồ.
    Column(
        Modifier
            .fillMaxSize()
            .background(MinColor.Background)
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MinLargeHeader(stringResource(R.string.report_center_title), horizontalPadding = 0.dp)
        StreakHero(state)
        TodayCard(state)
        WeekCard(state)
        WeightCard(state, actions)
        CaloriesCard(state)
        if (state.totalWorkouts > 0) RecordsCard(state)
        BmiCard(state, actions)
        if (state.badgeTotal > 0) BadgeCard(state, actions)
        if (state.suggestionText.isNotEmpty()) NextCard(state, actions)
    }
}

// ---------------------------------------------------------------- streak

@Composable
private fun StreakHero(state: ReportUiState) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(MinColor.PinkLight, MinColor.Pink, MinColor.Plum)))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.streak_days_in_a_row),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 15.sp
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${state.streakDays}", color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.txt_days).lowercase(),
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            if (state.streakTargetText.isNotEmpty()) {
                Text(state.streakTargetText, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
            }
        }
        Box(
            Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) { Text("🔥", fontSize = 44.sp) }
    }
}

// ---------------------------------------------------------------- hôm nay

@Composable
private fun TodayCard(state: ReportUiState) {
    Column {
        Text(
            stringResource(R.string.report_today),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp, start = 2.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("🏋️", "${state.todayWorkouts}", stringResource(R.string.txt_exercise), state.todayWorkoutsTrend, Modifier.weight(1f))
            StatTile("🔥", "${state.todayCalories}", "kcal", state.todayCaloriesTrend, Modifier.weight(1f))
            StatTile("⏱", "${state.todayMinutes}", stringResource(R.string.minutes).lowercase(), state.todayMinutesTrend, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(emoji: String, value: String, unit: String, trend: Float, modifier: Modifier) {
    MinCard(modifier, padding = 14.dp) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(8.dp))
        Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(unit, fontSize = 13.sp, color = MinColor.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(8.dp))
        TrendChip(trend)
    }
}

@Composable
private fun TrendChip(percent: Float) {
    val (text, color) = when {
        percent > 0.05f -> "▲ ${"%.0f".format(percent)}%" to MinColor.Ok
        percent < -0.05f -> "▼ ${"%.0f".format(-percent)}%" to MinColor.Danger
        else -> "— ${stringResource(R.string.stable)}" to MinColor.TextMuted
    }
    Text(
        text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

// ---------------------------------------------------------------- tuần này

@Composable
private fun WeekCard(state: ReportUiState) {
    MinCard(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.progress_this_week), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val fraction = if (state.weekGoal > 0) state.weekDone / state.weekGoal.toFloat() else 0f
            ProgressRing(fraction, "${state.weekDone}/${state.weekGoal}", stringResource(R.string.txt_completed))
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                WeekStat("🔥", "${state.weekCalories} kcal", state.weekCaloriesTrend)
                WeekStat("⏱", stringResource(R.string.x_minutes, state.weekMinutes), state.weekMinutesTrend)
            }
        }
    }
}

@Composable
private fun WeekStat(emoji: String, value: String, trend: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MinColor.PinkSoft),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 18.sp) }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            TrendChip(trend)
        }
    }
}

@Composable
private fun ProgressRing(fraction: Float, center: String, caption: String) {
    val animated by animateFloatAsState(fraction.coerceIn(0f, 1f), tween(900, easing = FastOutSlowInEasing), label = "ring")
    Box(Modifier.size(112.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 12.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(MinColor.Track, -90f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            drawArc(
                Brush.sweepGradient(listOf(MinColor.PinkLight, MinColor.Pink, MinColor.PinkLight)),
                -90f, 360f * animated, false, Offset(inset, inset), arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(center, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MinColor.Pink)
            Text(caption, fontSize = 12.sp, color = MinColor.TextSecondary, maxLines = 1)
        }
    }
}

// ---------------------------------------------------------------- chọn khoảng thời gian

@Composable
private fun RangeSelector(selected: Int, onSelect: (Int) -> Unit) {
    val options = listOf(
        7 to stringResource(R.string.day_7s),
        30 to stringResource(R.string.day_30s),
        90 to stringResource(R.string.month_3s)
    )
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MinColor.Track)
            .padding(4.dp)
    ) {
        options.forEach { (days, label) ->
            val active = days == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (active) MinColor.Surface else Color.Transparent)
                    .clickable { onSelect(days) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 14.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    color = if (active) MinColor.Pink else MinColor.TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

// ---------------------------------------------------------------- cân nặng

@Composable
private fun WeightCard(state: ReportUiState, actions: ReportActions) {
    MinCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.txt_weight), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${stringResource(R.string.txt_target)}: ${"%.1f".format(state.targetWeight)} ${state.weightUnit}",
                    fontSize = 13.sp,
                    color = MinColor.TextSecondary
                )
            }
            Row(
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MinColor.PinkSoft)
                    .clickable(onClick = actions::onEditWeight)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${"%.1f".format(state.currentWeight)} ${state.weightUnit}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MinColor.Pink
                )
                Spacer(Modifier.width(6.dp))
                Text("✎", fontSize = 15.sp, color = MinColor.Pink)
            }
        }
        Spacer(Modifier.height(14.dp))
        RangeSelector(state.weightRange) { state.weightRange = it }
        Spacer(Modifier.height(12.dp))
        val cutoff = System.currentTimeMillis() - state.weightRange * DAY_MILLIS
        val points = state.weights.filter { it.time >= cutoff }.ifEmpty {
            listOf(WeightPoint(System.currentTimeMillis(), state.currentWeight))
        }
        WeightLineChart(
            points = points,
            target = state.targetWeight,
            unit = state.weightUnit,
            animationKey = state.weightRange,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
        )
        state.weightTrendText[state.weightRange]?.takeIf { it.isNotEmpty() }?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MinColor.TextSecondary)
        }
    }
}

/**
 * Tiến độ hiệu ứng vẽ biểu đồ. Chỉ chạy khi [key] đổi (đổi khoảng thời gian / có dữ liệu mới):
 * thẻ bị LazyColumn huỷ khi cuộn khỏi màn hình rồi dựng lại thì vẽ ngay, không chạy lại hiệu ứng.
 */
@Composable
private fun rememberChartProgress(key: String, durationMillis: Int): Animatable<Float, *> {
    var playedKey by rememberSaveable { mutableStateOf<String?>(null) }
    val progress = remember { Animatable(if (playedKey == key) 1f else 0f) }
    LaunchedEffect(key) {
        if (playedKey == key) {
            progress.snapTo(1f)
        } else {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(durationMillis, easing = FastOutSlowInEasing))
            playedKey = key
        }
    }
    return progress
}

private const val DAY_MILLIS = 24L * 60 * 60 * 1000

@Composable
private fun WeightLineChart(
    points: List<WeightPoint>,
    target: Float,
    unit: String,
    animationKey: Any,
    modifier: Modifier
) {
    val progress = rememberChartProgress("$animationKey/${points.size}", 1100)
    val dateFormat = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }
    val minTime = points.minOf { it.time }
    val maxTime = points.maxOf { it.time }
    // Mục tiêu ở quá xa dữ liệu sẽ ép đường cân nặng thành đường thẳng – khi đó không vẽ.
    val dataSpan = points.maxOf { it.value } - points.minOf { it.value }
    val showTarget = target > 0f && abs(target - points.last().value) <= max(dataSpan * 2f, 3f)
    val values = points.map { it.value } + if (showTarget) listOf(target) else emptyList()
    val minV = values.min()
    val maxV = values.max()
    val pad = max((maxV - minV) * 0.2f, 0.5f)
    val low = minV - pad
    val high = maxV + pad

    Column(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val left = 6.dp.toPx()
                val right = size.width - 6.dp.toPx()
                val top = 26.dp.toPx()
                val bottom = size.height - 6.dp.toPx()
                fun x(t: Long) = if (maxTime == minTime) (left + right) / 2f
                else left + (right - left) * ((t - minTime).toFloat() / (maxTime - minTime))
                fun y(v: Float) = top + (bottom - top) * ((high - v) / (high - low))

                // lưới
                for (i in 0..3) {
                    val gy = top + (bottom - top) * i / 3f
                    drawLine(MinColor.Track, Offset(left, gy), Offset(right, gy), strokeWidth = 1.dp.toPx())
                }
                // đường mục tiêu
                if (showTarget) {
                    val ty = y(target)
                    drawLine(
                        MinColor.Ok.copy(alpha = 0.7f), Offset(left, ty), Offset(right, ty),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))
                    )
                }

                val offsets = points.map { Offset(x(it.time), y(it.value)) }
                val line = Path()
                offsets.forEachIndexed { i, p ->
                    if (i == 0) line.moveTo(p.x, p.y) else {
                        val prev = offsets[i - 1]
                        val midX = (prev.x + p.x) / 2f
                        line.cubicTo(midX, prev.y, midX, p.y, p.x, p.y)
                    }
                }
                val fill = Path().apply {
                    addPath(line)
                    lineTo(offsets.last().x, bottom)
                    lineTo(offsets.first().x, bottom)
                    close()
                }
                val reveal = left + (right - left) * progress.value
                clipRect(right = reveal + 12.dp.toPx()) {
                    if (offsets.size > 1) {
                        drawPath(
                            fill,
                            Brush.verticalGradient(
                                listOf(MinColor.Pink.copy(alpha = 0.25f), MinColor.Pink.copy(alpha = 0.02f)),
                                startY = top, endY = bottom
                            )
                        )
                        drawPath(
                            line,
                            Brush.horizontalGradient(listOf(MinColor.PinkLight, MinColor.Pink, MinColor.Plum)),
                            style = Stroke(3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    val dotEvery = max(1, offsets.size / 12)
                    offsets.forEachIndexed { i, p ->
                        if (i % dotEvery == 0 || i == offsets.lastIndex) {
                            drawCircle(Color.White, 5.dp.toPx(), p)
                            drawCircle(MinColor.Pink, 5.dp.toPx(), p, style = Stroke(2.5.dp.toPx()))
                        }
                    }
                }
            }
            // giá trị mới nhất
            Text(
                "${"%.1f".format(points.last().value)} $unit",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MinColor.Pink)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(dateFormat.format(Date(minTime)), fontSize = 12.sp, color = MinColor.TextMuted)
            if (maxTime != minTime) {
                Text(dateFormat.format(Date((minTime + maxTime) / 2)), fontSize = 12.sp, color = MinColor.TextMuted)
                Text(dateFormat.format(Date(maxTime)), fontSize = 12.sp, color = MinColor.TextMuted)
            }
        }
    }
}

// ---------------------------------------------------------------- calo

@Composable
private fun CaloriesCard(state: ReportUiState) {
    MinCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(if (state.showMinutes) R.string.rec_minutes_title else R.string.txt_calories_chart),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            // Chuyển biểu đồ giữa kcal và phút tập.
            UnitToggle(state.showMinutes) { state.showMinutes = it }
        }
        Spacer(Modifier.height(14.dp))
        RangeSelector(state.caloriesRange) { state.caloriesRange = it }
        Spacer(Modifier.height(12.dp))
        val data = (if (state.showMinutes) state.minutesData else state.caloriesData)[state.caloriesRange].orEmpty()
        CaloriesBarChart(
            data,
            animationKey = "${state.caloriesRange}/${state.showMinutes}",
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        )
        state.caloriesTrendText[state.caloriesRange]?.takeIf { it.isNotEmpty() && !state.showMinutes }?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MinColor.TextSecondary)
        }
    }
}

@Composable
private fun UnitToggle(minutes: Boolean, onSelect: (Boolean) -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MinColor.Track)
            .padding(3.dp)
    ) {
        listOf(false to "kcal", true to stringResource(R.string.rec_minutes_short)).forEach { (value, label) ->
            val active = value == minutes
            Box(
                Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (active) MinColor.Surface else Color.Transparent)
                    .clickable { onSelect(value) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    color = if (active) MinColor.Pink else MinColor.TextSecondary
                )
            }
        }
    }
}

/** Tổng cộng từ trước đến nay + kỷ lục cá nhân, lưới 2 cột giản dị. */
@Composable
private fun RecordsCard(state: ReportUiState) {
    val minutesUnit = stringResource(R.string.rec_minutes_short)
    MinCard(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.rec_title), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row {
            RecordCell(state.totalWorkouts.toString(), stringResource(R.string.rec_total_workouts), Modifier.weight(1f))
            RecordCell(
                if (state.totalMinutes < 60) "${state.totalMinutes} $minutesUnit"
                else stringResource(R.string.rec_hours, state.totalMinutes / 60f),
                stringResource(R.string.rec_total_time),
                Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row {
            RecordCell("${state.totalCalories} kcal", stringResource(R.string.rec_total_kcal), Modifier.weight(1f))
            RecordCell(
                stringResource(R.string.rec_workouts_value, state.bestWeekWorkouts),
                stringResource(R.string.rec_best_week),
                Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        RecordCell(
            String.format(java.util.Locale.US, "%d:%02d", state.longestSec / 60, state.longestSec % 60),
            stringResource(R.string.rec_longest) + if (state.longestName.isNotBlank()) " · " + state.longestName else "",
            Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RecordCell(value: String, label: String, modifier: Modifier) {
    Column(modifier) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MinColor.TextPrimary, maxLines = 1)
        Text(label, fontSize = 13.sp, color = MinColor.TextSecondary, maxLines = 2)
    }
}

@Composable
private fun CaloriesBarChart(data: List<Pair<String, Int>>, animationKey: Any, modifier: Modifier) {
    val progress = rememberChartProgress("$animationKey/${data.size}", 900)
    val maxValue = max(data.maxOfOrNull { it.second } ?: 0, 1)
    val density = androidx.compose.ui.platform.LocalDensity.current
    val valuePaint = remember(density) {
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(233, 30, 99)
            textSize = with(density) { 11.sp.toPx() }
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }
    }
    fun label(key: String): String {
        val parts = key.split("-")
        return if (parts.size >= 3) "${parts[2]}/${parts[1]}" else key
    }
    Column(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (data.isEmpty() || data.all { it.second == 0 }) {
                Text(
                    stringResource(R.string.txt_no_workout),
                    fontSize = 14.sp,
                    color = MinColor.TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Canvas(Modifier.fillMaxSize()) {
                val top = 18.dp.toPx()
                val bottom = size.height
                val count = max(data.size, 1)
                val slot = size.width / count
                val barWidth = min(slot * 0.62f, 26.dp.toPx())
                for (i in 0..3) {
                    val gy = top + (bottom - top) * i / 3f
                    drawLine(MinColor.Track, Offset(0f, gy), Offset(size.width, gy), strokeWidth = 1.dp.toPx())
                }
                data.forEachIndexed { i, (_, value) ->
                    val h = (bottom - top) * (value / maxValue.toFloat()) * progress.value
                    if (h <= 0f) return@forEachIndexed
                    val x = slot * i + (slot - barWidth) / 2f
                    drawRoundRect(
                        Brush.verticalGradient(listOf(MinColor.PinkLight, MinColor.Pink), startY = bottom - h, endY = bottom),
                        topLeft = Offset(x, bottom - h),
                        size = Size(barWidth, h),
                        cornerRadius = CornerRadius(min(barWidth / 2f, 8.dp.toPx()))
                    )
                    // ít cột thì ghi số trên từng cột, nhiều cột chỉ ghi cột cao nhất
                    val isPeak = value == maxValue
                    if (progress.value > 0.95f && (data.size <= 10 || isPeak)) {
                        drawContext.canvas.nativeCanvas.drawText(
                            value.toString(), x + barWidth / 2f, bottom - h - 4.dp.toPx(), valuePaint
                        )
                    }
                }
            }
        }
        if (data.isNotEmpty()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label(data.first().first), fontSize = 12.sp, color = MinColor.TextMuted)
                if (data.size > 2) Text(label(data[data.size / 2].first), fontSize = 12.sp, color = MinColor.TextMuted)
                if (data.size > 1) Text(label(data.last().first), fontSize = 12.sp, color = MinColor.TextMuted)
            }
        }
    }
}

// ---------------------------------------------------------------- BMI

@Composable
private fun BmiCard(state: ReportUiState, actions: ReportActions) {
    MinCard(Modifier.fillMaxWidth(), onClick = actions::onEditWeight) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("BMI", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("✎", fontSize = 16.sp, color = MinColor.Pink)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("%.1f".format(state.bmi), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = bmiColor(state.bmi))
            Spacer(Modifier.width(10.dp))
            Text(
                state.bmiLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = bmiColor(state.bmi),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        BmiScale(state.bmi)
    }
}

private fun bmiColor(bmi: Float): Color = when {
    bmi < 18.5f -> MinColor.Purple
    bmi < 25f -> MinColor.Ok
    bmi < 30f -> MinColor.Warning
    else -> MinColor.Danger
}

private val BMI_MIN = 15f
private val BMI_MAX = 40f

@Composable
private fun BmiScale(bmi: Float) {
    val target = ((bmi.coerceIn(BMI_MIN, BMI_MAX) - BMI_MIN) / (BMI_MAX - BMI_MIN))
    val animated by animateFloatAsState(target, tween(900, easing = FastOutSlowInEasing), label = "bmi")
    Column {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(26.dp)
        ) {
            val barTop = 12.dp.toPx()
            val barHeight = 10.dp.toPx()
            val segments = listOf(
                18.5f to MinColor.Purple,
                25f to MinColor.Ok,
                30f to MinColor.Warning,
                BMI_MAX to MinColor.Danger
            )
            var start = BMI_MIN
            val gap = 3.dp.toPx()
            segments.forEach { (end, color) ->
                val x0 = size.width * (start - BMI_MIN) / (BMI_MAX - BMI_MIN)
                val x1 = size.width * (end - BMI_MIN) / (BMI_MAX - BMI_MIN)
                drawRoundRect(
                    color,
                    topLeft = Offset(x0 + gap / 2, barTop),
                    size = Size(x1 - x0 - gap, barHeight),
                    cornerRadius = CornerRadius(barHeight / 2)
                )
                start = end
            }
            val mx = size.width * animated
            val marker = Path().apply {
                moveTo(mx - 7.dp.toPx(), 0f)
                lineTo(mx + 7.dp.toPx(), 0f)
                lineTo(mx, barTop - 1.dp.toPx())
                close()
            }
            drawPath(marker, MinColor.TextPrimary)
        }
        BoxWithConstraints(Modifier.fillMaxWidth().height(16.dp)) {
            val labelWidth = 32.dp
            listOf(15f, 18.5f, 25f, 30f, 40f).forEach { v ->
                val center = maxWidth * ((v - BMI_MIN) / (BMI_MAX - BMI_MIN))
                val x = (center - labelWidth / 2).coerceIn(0.dp, maxWidth - labelWidth)
                Text(
                    if (v % 1f == 0f) v.toInt().toString() else v.toString(),
                    fontSize = 11.sp,
                    color = MinColor.TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .offset(x = x)
                        .width(labelWidth)
                )
            }
        }
    }
}

// ---------------------------------------------------------------- huy hiệu

@Composable
private fun BadgeCard(state: ReportUiState, actions: ReportActions) {
    MinCard(Modifier.fillMaxWidth(), onClick = actions::onOpenBadges) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.badge_screen_title), fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("${stringResource(R.string.badge_view_all)} ›", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MinColor.Pink)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.badge_summary, state.badgeUnlocked, state.badgeTotal),
            fontSize = 14.sp,
            color = MinColor.TextSecondary
        )
        Spacer(Modifier.height(8.dp))
        MinProgressBar(state.badgePercent / 100f, Modifier.fillMaxWidth())
        val next = state.nextBadge
        if (next != null) {
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MinColor.PinkSoft.copy(alpha = 0.6f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AndroidView(
                    factory = { ImageView(it) },
                    update = { BadgeIconBinder.bind(next, it, null) },
                    modifier = Modifier.size(52.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.badge_next_up), fontSize = 12.sp, color = MinColor.TextMuted)
                    Text(next.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(state.nextBadgeRemaining, fontSize = 13.sp, color = MinColor.Pink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- buổi tiếp theo

@Composable
private fun NextCard(state: ReportUiState, actions: ReportActions) {
    MinCard(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.suggestion_next), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(state.suggestionText, fontSize = 16.sp, color = MinColor.TextPrimary)
        if (state.streakTargetText.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text(state.streakTargetText, fontSize = 13.sp, color = MinColor.TextSecondary)
        }
        Spacer(Modifier.height(14.dp))
        MinButton("📅 " + stringResource(R.string.schedule), Modifier.fillMaxWidth(), onClick = actions::onScheduleNext)
    }
}
