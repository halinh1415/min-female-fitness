package mint.woman.female.workout.activity.ui.premium

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mint.woman.female.workout.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Vòng quay may mắn kiểu "Quay may mắn" (popup đè lên paywall): tiêu đề chữ nghiêng vàng có sao
 * lấp lánh, vòng 8 ô có viền đèn nhấp nháy + kim + chân đế, nút GO ở tâm có bàn tay chỉ vào. Vòng
 * quay chậm dần và luôn dừng ở ô JACKPOT (phần thưởng chính: % jackpot cấu hình được), sau đó bắn confetti và gọi [onWon].
 */

/** Bảng màu vòng quay - mỗi app truyền màu theo thương hiệu. */
class LuckyWheelColors(
    val rim: List<Color>,
    val segmentA: Color,
    val segmentB: Color,
    val jackpot: Color,
    val text: Color,
    val jackpotText: Color,
    val button: List<Color>,
    val title: Color = Color(0xFFFFE08A),
)

/** Ô trên vòng, theo chiều kim đồng hồ bắt đầu từ đỉnh. */
private enum class Slice { JACKPOT, NEXT, OFF20, NEXT2, OFF40, NEXT3, OFF20B, NEXT4 }

private val SLICES = Slice.entries
/** Ô trúng mặc định: JACKPOT (mở vé ưu đãi DiscountDialogFragment). */
private val WIN = Slice.JACKPOT

@Composable
fun LuckyWheelOverlay(colors: LuckyWheelColors, jackpotLabel: String, onWon: () -> Unit, onSkip: () -> Unit) {
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var spinning by remember { mutableStateOf(false) }
    var won by remember { mutableStateOf(false) }
    var confetti by remember { mutableStateOf(false) }
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    val labels = mapOf(
        Slice.JACKPOT to stringResource(R.string.ld_seg_jackpot),
        Slice.NEXT to stringResource(R.string.ld_seg_next_time),
        Slice.OFF20 to stringResource(R.string.ld_seg_off, "20%"),
        Slice.OFF40 to stringResource(R.string.ld_seg_off, "40%"),
    )
    fun label(slice: Slice) = when (slice) {
        Slice.NEXT2, Slice.NEXT3, Slice.NEXT4 -> labels.getValue(Slice.NEXT)
        Slice.OFF20B -> labels.getValue(Slice.OFF20)
        else -> labels.getValue(slice)
    }

    val spin: () -> Unit = spin@{
        if (spinning || won) return@spin
        spinning = true
        scope.launch {
            // Kim ở đỉnh: quay nhiều vòng rồi dừng sao cho tâm ô JACKPOT nằm dưới kim, lệch nhẹ ngẫu nhiên
            // trong ô để trông tự nhiên.
            val sweep = 360f / SLICES.size
            val center = WIN.ordinal * sweep + sweep / 2
            val jitter = (Random.nextFloat() - 0.5f) * sweep * 0.5f
            val target = 360f * 7 - center + jitter
            rotation.animateTo(target, tween(5200, easing = CubicBezierEasing(0.1f, 0.85f, 0.2f, 1f)))
            spinning = false
            won = true
            confetti = true
            // Cho người dùng thấy ô 50% sáng lên và dòng "Bạn trúng…" rồi mới hiện vé ưu đãi.
            delay(1600)
            onWon()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xD9000000))
            // Chặn chạm xuyên xuống paywall phía sau.
            .clickable(remember { MutableInteractionSource() }, indication = null) {},
    ) {
        AnimatedVisibility(appeared, enter = fadeIn(tween(250)) + scaleIn(tween(350), initialScale = 0.85f)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp),
            ) {
                Spacer(Modifier.weight(0.6f))
                Title(colors)
                Spacer(Modifier.height(18.dp))
                Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth(0.92f).aspectRatio(0.86f)) {
                    // Chân đế.
                    Stand(colors, Modifier.align(Alignment.BottomCenter).fillMaxWidth(0.5f).height(54.dp))
                    Box(Modifier.fillMaxWidth().aspectRatio(1f).padding(top = 22.dp)) {
                        WheelRim(colors, Modifier.fillMaxSize())
                        WheelFace(colors, ::label, highlight = won, modifier = Modifier.fillMaxSize().padding(22.dp).rotate(rotation.value))
                        GoButton(colors, enabled = !spinning && !won, onClick = spin, modifier = Modifier.align(Alignment.Center))
                        if (!spinning && !won) PointingHand(Modifier.align(Alignment.Center).offset(x = 46.dp, y = 44.dp))
                    }
                    Pointer(colors, Modifier.size(width = 44.dp, height = 50.dp))
                }
                AnimatedVisibility(won, enter = fadeIn() + scaleIn(initialScale = 0.6f)) {
                    Text(
                        stringResource(R.string.ld_seg_jackpot) + "! " + stringResource(R.string.ld_seg_off, jackpotLabel),
                        color = colors.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    stringResource(R.string.ld_skip),
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(bottom = 18.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = !spinning, onClick = onSkip)
                        .padding(10.dp),
                )
            }
        }
        if (confetti) Confetti(Modifier.fillMaxSize())
    }
}

@Composable
private fun Title(colors: LuckyWheelColors) {
    val twinkle by rememberInfiniteTransition(label = "twinkle").animateFloat(
        0.3f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "twinkleAlpha",
    )
    Box(contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(R.string.ld_lucky_title),
                style = TextStyle(
                    color = colors.title,
                    fontSize = 40.sp,
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    shadow = Shadow(Color(0x99000000), Offset(0f, 6f), 10f),
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.rotate(-4f),
            )
            Text(
                stringResource(R.string.ld_title),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier.rotate(-4f),
            )
        }
        Text("✦", color = Color.White.copy(alpha = twinkle), fontSize = 20.sp, modifier = Modifier.align(Alignment.TopStart).offset(x = (-26).dp, y = 2.dp))
        Text("✦", color = Color.White.copy(alpha = 1.3f - twinkle), fontSize = 16.sp, modifier = Modifier.align(Alignment.BottomEnd).offset(x = 22.dp, y = (-18).dp))
    }
}

/** Viền ngoài với đèn nhấp nháy xen kẽ. */
@Composable
private fun WheelRim(colors: LuckyWheelColors, modifier: Modifier) {
    val blink by rememberInfiniteTransition(label = "lights").animateFloat(
        0f, 1f, infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse), label = "blink",
    )
    Canvas(modifier.shadow(18.dp, CircleShape)) {
        val r = size.minDimension / 2
        val c = center
        drawCircle(Brush.radialGradient(colors.rim, center = c, radius = r), r, c)
        drawCircle(Color.White.copy(alpha = 0.25f), r - 3.dp.toPx(), c, style = Stroke(2.dp.toPx()))
        val lights = 16
        val lightR = r - 11.dp.toPx()
        repeat(lights) { i ->
            val a = Math.toRadians((i * 360f / lights - 90f).toDouble())
            val p = Offset(c.x + lightR * cos(a).toFloat(), c.y + lightR * sin(a).toFloat())
            val on = if (i % 2 == 0) blink else 1f - blink
            drawCircle(Color(0xFFFFF3B0).copy(alpha = 0.35f * on), 9.dp.toPx(), p)
            drawCircle(Color.White.copy(alpha = 0.45f + 0.55f * on), 4.5.dp.toPx(), p)
        }
    }
}

/** Mặt vòng 8 ô, chữ xoay theo bán kính. */
@Composable
private fun WheelFace(colors: LuckyWheelColors, label: (Slice) -> String, highlight: Boolean, modifier: Modifier) {
    val glow by rememberInfiniteTransition(label = "win").animateFloat(
        0.15f, 0.6f, infiniteRepeatable(tween(420), RepeatMode.Reverse), label = "winGlow",
    )
    Canvas(modifier) {
        val r = size.minDimension / 2
        val c = center
        val sweep = 360f / SLICES.size
        SLICES.forEachIndexed { i, slice ->
            val color = when {
                slice == Slice.JACKPOT -> colors.jackpot
                i % 2 == 0 -> colors.segmentA
                else -> colors.segmentB
            }
            drawArc(color, -90f + i * sweep, sweep, true, Offset(c.x - r, c.y - r), Size(r * 2, r * 2))
        }
        // Ô trúng thưởng sáng nhấp nháy sau khi dừng.
        if (highlight) {
            drawArc(Color.White.copy(alpha = glow), -90f + WIN.ordinal * sweep, sweep, true, Offset(c.x - r, c.y - r), Size(r * 2, r * 2))
        }
        // Đường chia ô.
        SLICES.indices.forEach { i ->
            val a = Math.toRadians((-90f + i * sweep).toDouble())
            drawLine(Color.White.copy(alpha = 0.7f), c, Offset(c.x + r * cos(a).toFloat(), c.y + r * sin(a).toFloat()), 2.dp.toPx())
        }
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                textAlign = android.graphics.Paint.Align.CENTER
            }
            SLICES.forEachIndexed { i, slice ->
                val angle = -90f + i * sweep + sweep / 2
                val text = label(slice)
                paint.color = (if (slice == Slice.JACKPOT) colors.jackpotText else colors.text).toArgb()
                paint.textSize = r * (if (slice == Slice.JACKPOT || slice == Slice.OFF40) 0.13f else 0.11f)
                // Chữ chạy dọc theo bán kính, đọc từ ngoài vào trong.
                canvas.nativeCanvas.save()
                canvas.nativeCanvas.rotate(angle + 90f, c.x, c.y)
                val words = text.split(" ")
                val lines = if (words.size > 1 && text.length > 8) listOf(words.dropLast(1).joinToString(" "), words.last()) else listOf(text)
                lines.forEachIndexed { line, part ->
                    canvas.nativeCanvas.drawText(part, c.x, c.y - r * 0.68f + line * paint.textSize * 1.1f, paint)
                }
                canvas.nativeCanvas.restore()
            }
        }
        drawCircle(Color.White.copy(alpha = 0.9f), r, c, style = Stroke(3.dp.toPx()))
    }
}

@Composable
private fun GoButton(colors: LuckyWheelColors, enabled: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val pulse by rememberInfiniteTransition(label = "go").animateFloat(
        1f, 1.08f, infiniteRepeatable(tween(650), RepeatMode.Reverse), label = "goScale",
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(82.dp)
            .scale(if (enabled) pulse else 1f)
            .shadow(10.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .padding(5.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(colors.button))
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        // Vệt sáng bóng phía trên.
        Box(Modifier.align(Alignment.TopCenter).padding(top = 6.dp).width(44.dp).height(16.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.35f)))
        Text(stringResource(R.string.ld_go), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
    }
}

/** Bàn tay chỉ vào nút GO, nhún nhẹ để gợi ý bấm. */
@Composable
private fun PointingHand(modifier: Modifier) {
    val bob by rememberInfiniteTransition(label = "hand").animateFloat(
        0f, 1f, infiniteRepeatable(tween(550), RepeatMode.Reverse), label = "handBob",
    )
    Text(
        "👆",
        fontSize = 46.sp,
        modifier = modifier.offset(x = (-6 * bob).dp, y = (-6 * bob).dp).rotate(-30f),
    )
}

/** Kim chỉ hình giọt nước ở đỉnh vòng. */
@Composable
private fun Pointer(colors: LuckyWheelColors, modifier: Modifier) {
    Canvas(modifier.shadow(6.dp, CircleShape)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(0f, h * 0.18f)
            quadraticTo(w / 2, -h * 0.12f, w, h * 0.18f)
            lineTo(w / 2, h)
            close()
        }
        drawPath(path, Brush.verticalGradient(listOf(Color(0xFFFFF1B5), Color(0xFFFFC94D), Color(0xFFE09A12))))
        drawPath(path, Color.White, style = Stroke(2.dp.toPx()))
    }
}

@Composable
private fun Stand(colors: LuckyWheelColors, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val neck = Path().apply {
            moveTo(w * 0.38f, 0f)
            lineTo(w * 0.62f, 0f)
            lineTo(w * 0.72f, h * 0.7f)
            lineTo(w * 0.28f, h * 0.7f)
            close()
        }
        drawPath(neck, Brush.verticalGradient(colors.rim.reversed()))
        drawRoundRect(
            Brush.verticalGradient(colors.rim),
            topLeft = Offset(0f, h * 0.62f),
            size = Size(w, h * 0.38f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()),
        )
    }
}

/** Confetti rơi từ trên xuống khi trúng thưởng. */
@Composable
private fun Confetti(modifier: Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(2200, easing = LinearEasing)) }
    val pieces = remember {
        List(70) {
            ConfettiPiece(
                x = Random.nextFloat(),
                delay = Random.nextFloat() * 0.35f,
                speed = 0.7f + Random.nextFloat() * 0.6f,
                drift = (Random.nextFloat() - 0.5f) * 0.25f,
                spin = Random.nextFloat() * 720f,
                color = listOf(Color(0xFFFFC94D), Color(0xFFFF7A2F), Color(0xFF6C8CFF), Color(0xFF22A06B), Color(0xFFFF5C8A), Color.White).random(),
            )
        }
    }
    Canvas(modifier) {
        val t = progress.value
        pieces.forEach { p ->
            val local = ((t - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
            if (local <= 0f || local >= 1f) return@forEach
            val x = (p.x + p.drift * local) * size.width
            val y = -20f + local * p.speed * size.height * 1.1f
            rotate(p.spin * local, Offset(x, y)) {
                drawRect(p.color.copy(alpha = 1f - local * 0.6f), Offset(x - 6f, y - 10f), Size(12f, 20f))
            }
        }
    }
}

private class ConfettiPiece(val x: Float, val delay: Float, val speed: Float, val drift: Float, val spin: Float, val color: Color)
