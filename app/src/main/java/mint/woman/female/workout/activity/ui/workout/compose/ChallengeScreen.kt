package mint.woman.female.workout.activity.ui.workout.compose

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mint.woman.female.workout.R
import mint.woman.female.workout.model.ProgramObject
import android.widget.ImageView
import mint.woman.female.workout.activity.ui.compose.GlideImage
import mint.woman.female.workout.activity.ui.compose.MinColor
import mint.woman.female.workout.activity.ui.compose.backgroundUrl
import mint.woman.female.workout.activity.ui.compose.fallbackBackgroundUrl
import mint.woman.female.workout.activity.ui.compose.modelUrl

@Immutable
data class ChallengeState(
    val program: ProgramObject? = null,
    /** Số ngày đã hoàn thành (MyPreference PLAN_PROGRESS_<id>). */
    val progress: Int = 0,
    val showEdit: Boolean = false,
)

class ChallengeActions(
    val onBack: () -> Unit,
    /** Ngày (0-based) được bấm. */
    val onDay: (Int) -> Unit,
    val onReset: () -> Unit,
    val onSetProgress: () -> Unit,
    val onEdit: () -> Unit,
)

// Bảng màu Rose của app Min.
private val Teal get() = MinColor.Pink
private val TealLight get() = MinColor.PinkLight
private val Gold = Color(0xFFF4B33D)
private val Border get() = MinColor.Track
private val Muted get() = MinColor.TextMuted

/**
 * Màn thử thách nhiều ngày (Compose): header ảnh 2 lớp + vòng tiến độ, lưới từng tuần với số ngày
 * đúng (8–14, 15–21...), ngày đã tập ✓, ngày hiện tại nổi bật, cúp cuối tuần, nút "Bắt đầu ngày X".
 */
@Composable
fun ChallengeScreen(state: ChallengeState, actions: ChallengeActions, banner: @Composable () -> Unit) {
    val program = state.program ?: return
    val total = program.total.coerceAtLeast(1)
    val done = state.progress.coerceIn(0, total)
    val finished = done >= total
    Column(Modifier.fillMaxSize().background(MinColor.Background)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Header(program, done, total, state, actions) }
            // Tuần cuối có thể thiếu ngày khi tổng không chia hết cho 7.
            val weeks = (total + 6) / 7
            items(weeks) { week -> WeekCard(week, total, done, actions.onDay) }
        }
        Column(Modifier.fillMaxWidth().background(MinColor.Surface).navigationBarsPadding()) {
            Button(
                onClick = { if (finished) actions.onReset() else actions.onDay(done) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Color.White),
                contentPadding = PaddingValues(vertical = 16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(
                    if (finished) stringResource(R.string.ch_restart) else stringResource(R.string.ch_start_day, done + 1),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            banner()
        }
    }
}

@Composable
private fun Header(program: ProgramObject, done: Int, total: Int, state: ChallengeState, actions: ChallengeActions) {
    var menu by remember { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)),
    ) {
        GlideImage(program.backgroundUrl(), Modifier.fillMaxSize(), errorUrl = program.fallbackBackgroundUrl())
        GlideImage(
            program.modelUrl(),
            Modifier.fillMaxSize().padding(top = 40.dp),
            scaleType = ImageView.ScaleType.FIT_END
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0f to Color(0x55000000), 0.35f to Color.Transparent, 1f to Color(0xE6880E4F)),
            ),
        )
        // Thanh trên: quay lại, đặt lại, menu.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp),
        ) {
            IconCircle("←", actions.onBack)
            Spacer(Modifier.weight(1f))
            IconCircle("↻", actions.onReset)
            Box {
                IconCircle("⋮") { menu = true }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.txt_set_progress)) }, onClick = {
                        menu = false
                        actions.onSetProgress()
                    })
                    if (state.showEdit) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.txt_edit)) }, onClick = {
                            menu = false
                            actions.onEdit()
                        })
                    }
                }
            }
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(18.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    program.name.orEmpty(),
                    color = Color.White,
                    fontSize = 26.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                program.subtitle?.takeIf { it.isNotBlank() }?.let {
                    Text(it, color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    if (done >= total) stringResource(R.string.ch_completed)
                    else stringResource(R.string.ch_day_of, done + 1, total) + " · " + "${total - done} " + stringResource(R.string.txt_day_left),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            ProgressRing(done.toFloat() / total, Modifier.size(78.dp))
        }
    }
}

@Composable
private fun IconCircle(label: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(4.dp).size(42.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.25f)).clickable(onClick = onClick),
    ) { Text(label, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun ProgressRing(fraction: Float, modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 8.dp.toPx()
            val inset = stroke / 2
            drawArc(Color.White.copy(alpha = 0.25f), 0f, 360f, false, Offset(inset, inset), Size(size.width - stroke, size.height - stroke), style = Stroke(stroke))
            drawArc(Color.White, -90f, 360f * fraction.coerceIn(0f, 1f), false, Offset(inset, inset), Size(size.width - stroke, size.height - stroke),
                style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Text("${(fraction * 100).toInt()}%", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
    }
}

/** Thẻ một tuần: 7 ngày dạng vòng tròn (2 hàng 4 + 3 & cúp), ngày đúng thứ tự trong cả thử thách. */
@Composable
private fun WeekCard(week: Int, total: Int, done: Int, onDay: (Int) -> Unit) {
    val first = week * 7
    val last = minOf(first + 7, total) - 1
    val weekDone = done > last
    val current = done in first..last
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MinColor.Surface)
            .border(if (current) 2.dp else 1.dp, if (current) TealLight else Border, RoundedCornerShape(22.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.txt_week_count) + " ${week + 1}",
                color = MinColor.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            val doneInWeek = (done - first).coerceIn(0, last - first + 1)
            Text(
                if (weekDone) "✓ " + stringResource(R.string.ch_week_done, week + 1) else "$doneInWeek/${last - first + 1}",
                color = if (weekDone) Teal else Muted,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(14.dp))
        // Đường zic-zắc như bản gốc: hàng trên trái→phải (ngày 1–4), nối xuống, hàng dưới phải→trái
        // (ngày 5–7 rồi cúp). Đoạn nối tô teal khi ngày phía trước đã tập xong.
        val days = (first..last).toList()
        val top = days.take(4)
        val bottom = days.drop(4)
        PathRow(top.map { Node.Day(it) }, done, onDay)
        if (bottom.isNotEmpty()) {
            // Nối dọc từ ô cuối hàng trên xuống ô đầu hàng dưới (cùng nằm ở cột phải).
            Box(Modifier.fillMaxWidth().height(18.dp)) {
                Box(
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = (DAY_SIZE - LINE) / 2)
                        .width(LINE)
                        .fillMaxSize()
                        .background(if (top.last() < done) Teal else Border),
                )
            }
            val nodes = bottom.map { Node.Day(it) as Node } + if (days.size == 7) listOf(Node.Cup(weekDone)) else emptyList()
            // Hàng dưới chạy phải→trái nhưng vẫn canh phải để thẳng cột với hàng trên.
            PathRow(nodes.reversed(), done, onDay, reversed = true, slots = 4)
        }
    }
}

private val DAY_SIZE = 42.dp
private val LINE = 3.dp

private sealed interface Node {
    data class Day(val day: Int) : Node
    data class Cup(val earned: Boolean) : Node
}

/**
 * Một hàng gồm tối đa 4 ô, giữa các ô là đoạn nối. [reversed]: thứ tự hiển thị đã đảo (phải→trái),
 * đoạn nối giữa hai ô tô teal theo ngày nằm bên phải (ngày đến trước).
 */
@Composable
private fun PathRow(nodes: List<Node>, done: Int, onDay: (Int) -> Unit, reversed: Boolean = false, slots: Int = 4) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        // Hàng thiếu ô (tuần cuối lẻ ngày): đệm bên trái để các ô thẳng cột với hàng trên.
        repeat(slots - nodes.size) {
            Spacer(Modifier.width(DAY_SIZE))
            Spacer(Modifier.weight(1f))
        }
        nodes.forEachIndexed { index, node ->
            if (index > 0) {
                val earlier = if (reversed) nodes[index] else nodes[index - 1]
                val lit = earlier is Node.Day && earlier.day < done
                Box(Modifier.weight(1f).height(LINE).background(if (lit) Teal else Border))
            }
            when (node) {
                is Node.Day -> DayCircle(node.day, done, onDay, Modifier.size(DAY_SIZE))
                is Node.Cup -> Trophy(node.earned, Modifier.size(DAY_SIZE))
            }
        }
    }
}

@Composable
private fun DayCircle(day: Int, done: Int, onDay: (Int) -> Unit, modifier: Modifier) {
    val completed = day < done
    val current = day == done
    val pulse = rememberInfiniteTransition(label = "day")
    val scale by pulse.animateFloat(1f, if (current) 1.08f else 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "s")
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .scale(scale)
            .clip(CircleShape)
            .background(
                when {
                    completed -> Brush.linearGradient(listOf(Teal, TealLight))
                    current -> Brush.linearGradient(listOf(Color(0xFFFF9A5C), Color(0xFFFF7A8A)))
                    else -> Brush.linearGradient(listOf(MinColor.PinkSoft, MinColor.PinkSoft))
                },
            )
            .clickable { onDay(day) },
    ) {
        Text(
            if (completed) "✓" else "${day + 1}",
            color = if (completed || current) Color.White else Muted,
            fontSize = if (current) 16.sp else 14.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Trophy(earned: Boolean, modifier: Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.clip(CircleShape).background(if (earned) Gold.copy(alpha = 0.25f) else MinColor.PinkSoft.copy(alpha = 0.5f)),
    ) { Text("🏆", fontSize = 18.sp, modifier = Modifier.scale(if (earned) 1.1f else 0.85f)) }
}
