package mint.woman.female.workout.activity.ui.onboarding.onboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Thước kéo (dọc cho chiều cao, ngang cho cân nặng) vẽ bằng Canvas.
 *
 * Vị trí thước là một số thực tính theo đơn vị; vạch và vạch chỉ giữa đều vẽ từ cùng giá trị đó,
 * kéo xong luôn trượt về đúng một vạch, nên số hiển thị ([value]) luôn khớp vạch chỉ.
 * Bản cũ đọc giá trị ở giữa danh sách nhưng vẽ vạch chỉ lệch 44dp và không bắt vạch.
 *
 * @param majorEvery vạch dài có nhãn mỗi bao nhiêu đơn vị
 * @param midEvery vạch vừa (0 = không có)
 * @param markValue mốc phụ (vd cân hiện tại ở màn cân mục tiêu): vẽ chấm + tô dải tới vạch chỉ
 */
@Composable
fun ObRuler(
    min: Int,
    max: Int,
    value: Int,
    vertical: Boolean,
    majorEvery: Int,
    label: (Int) -> String,
    modifier: Modifier = Modifier,
    midEvery: Int = 0,
    unitSpacing: Dp = if (vertical) 9.dp else 12.dp,
    markValue: Int? = null,
    onValueChange: (Int) -> Unit
) {
    val density = LocalDensity.current
    val spacingPx = with(density) { unitSpacing.toPx() }
    val position = remember(min, max) { Animatable(value.coerceIn(min, max).toFloat()) }
    var dragging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val currentOnChange by rememberUpdatedState(onValueChange)
    val measurer = rememberTextMeasurer()

    // Giá trị ngoài đổi (đổi đơn vị, giá trị mặc định) khi người dùng không kéo → nhảy theo.
    LaunchedEffect(value, min, max) {
        if (!dragging && !position.isRunning && position.value.roundToInt() != value) {
            position.snapTo(value.coerceIn(min, max).toFloat())
        }
    }
    // Báo giá trị theo vạch gần nhất trong lúc kéo / trượt.
    LaunchedEffect(position) {
        var last = position.value.roundToInt()
        snapshotFlow { position.value.roundToInt() }.collect { v ->
            if (v != last) {
                last = v
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                currentOnChange(v)
            }
        }
    }

    // Thước dọc: số lớn ở trên, kéo xuống làm tăng giá trị. Thước ngang: kéo sang trái làm tăng.
    val sign = if (vertical) 1f else -1f
    val dragState = rememberDraggableState { delta ->
        scope.launch {
            position.snapTo((position.value + sign * delta / spacingPx).coerceIn(min.toFloat(), max.toFloat()))
        }
    }

    Canvas(
        modifier
            .semantics { stateDescription = label(value) }
            .draggable(
                state = dragState,
                orientation = if (vertical) Orientation.Vertical else Orientation.Horizontal,
                onDragStarted = { dragging = true; position.stop() },
                onDragStopped = { velocity ->
                    val unitsPerSecond = sign * velocity / spacingPx
                    val projected = exponentialDecay<Float>(frictionMultiplier = 2.2f)
                        .calculateTargetValue(position.value, unitsPerSecond)
                    val target = projected.roundToInt().coerceIn(min, max).toFloat()
                    scope.launch {
                        position.animateTo(
                            target,
                            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
                            initialVelocity = unitsPerSecond
                        )
                        dragging = false
                    }
                }
            )
    ) {
        val axisLength = if (vertical) size.height else size.width
        val center = axisLength / 2f
        val pos = position.value
        val visibleUnits = axisLength / spacingPx / 2f + 2f
        val first = floor(pos - visibleUnits).toInt().coerceAtLeast(min)
        val last = ceil(pos + visibleUnits).toInt().coerceAtMost(max)
        val labelStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = ObColor.TextSecondary)

        fun along(v: Float) = if (vertical) center - (v - pos) * spacingPx else center + (v - pos) * spacingPx
        fun fade(at: Float) = (1f - abs(at - center) / center).coerceIn(0f, 1f).let { 0.25f + 0.75f * it }

        // dải tô giữa mốc phụ và vạch chỉ (màn cân mục tiêu)
        if (markValue != null) {
            val a = along(markValue.toFloat().coerceIn(min.toFloat(), max.toFloat()))
            val lo = minOf(a, center)
            val hi = maxOf(a, center)
            if (vertical) {
                drawRect(ObColor.Pink.copy(alpha = 0.12f), Offset(size.width * 0.45f, lo), androidx.compose.ui.geometry.Size(size.width * 0.55f, hi - lo))
            } else {
                drawRect(ObColor.Pink.copy(alpha = 0.12f), Offset(lo, size.height * 0.30f), androidx.compose.ui.geometry.Size(hi - lo, size.height * 0.45f))
            }
        }

        for (v in first..last) {
            val at = along(v.toFloat())
            val major = v % majorEvery == 0
            val mid = !major && midEvery > 0 && v % midEvery == 0
            val tick = with(density) { (if (major) 30.dp else if (mid) 22.dp else 14.dp).toPx() }
            val color = (if (major) Color(0xFF9E9E9E) else if (mid) Color(0xFFBDBDBD) else Color(0xFFDADADA))
                .copy(alpha = fade(at))
            val stroke = with(density) { (if (major) 2.dp else 1.5.dp).toPx() }
            if (vertical) {
                drawLine(color, Offset(size.width - tick, at), Offset(size.width, at), stroke)
                // nhãn trùng vạch chỉ thì ẩn (số lớn đã hiển thị), tránh vạch hồng đè lên chữ
                if (major && abs(at - center) > with(density) { 12.dp.toPx() }) {
                    val layout = measurer.measure(label(v), labelStyle)
                    drawText(
                        layout,
                        topLeft = Offset(size.width - tick - layout.size.width - with(density) { 10.dp.toPx() }, at - layout.size.height / 2f),
                        alpha = fade(at)
                    )
                }
            } else {
                val top = size.height * 0.30f
                drawLine(color, Offset(at, top), Offset(at, top + tick), stroke)
                if (major) {
                    val layout = measurer.measure(label(v), labelStyle)
                    drawText(
                        layout,
                        topLeft = Offset(at - layout.size.width / 2f, top + tick + with(density) { 6.dp.toPx() }),
                        alpha = fade(at)
                    )
                }
            }
        }

        // mốc phụ: chấm nhỏ trên thước
        if (markValue != null) {
            val a = along(markValue.toFloat())
            if (a in 0f..axisLength) {
                val r = with(density) { 4.dp.toPx() }
                if (vertical) drawCircle(ObColor.Pink, r, Offset(size.width * 0.42f, a))
                else drawCircle(ObColor.Pink, r, Offset(a, size.height * 0.22f))
            }
        }

        // vạch chỉ ở giữa: cùng toạ độ với vạch của giá trị đang chọn
        val indicator = with(density) { 3.dp.toPx() }
        if (vertical) {
            drawLine(ObColor.Pink, Offset(0f, center), Offset(size.width, center), indicator, cap = StrokeCap.Round)
        } else {
            val top = size.height * 0.22f
            drawLine(ObColor.Pink, Offset(center, top), Offset(center, top + with(density) { 44.dp.toPx() }), indicator, cap = StrokeCap.Round)
        }
    }
}
