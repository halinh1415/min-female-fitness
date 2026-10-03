package mint.woman.female.workout.activity.ui.workout.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.airbnb.lottie.LottieAnimationView
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.compose.MinColor

@Immutable
data class FinishedState(
    val programName: String = "",
    val durationText: String = "0:00",
    val kcal: String = "0",
    val exercises: Int = 0,
    /** Câu streak ("🔥 Chuỗi 3 ngày!"), null khi đang tính. */
    val streakText: String? = null,
    /** 0..4 = quá khó..quá dễ; null = chưa chọn. */
    val feel: Int? = null,
    val weightText: String = "",
    val bmiText: String = "",
    val reminderText: String = "",
)

class FinishedActions(
    val onFeel: (Int) -> Unit,
    val onUpdateWeight: () -> Unit,
    val onEditReminder: () -> Unit,
    val onShare: () -> Unit,
    val onDone: () -> Unit,
    val onClose: () -> Unit,
)

// Bảng màu Rose của app Min.
private val HeroGradient = listOf(Color(0xFFC2185B), MinColor.Pink, MinColor.PinkLight)
private val Border = Color(0xFFF1E4EA)
private val FeelEmojis = listOf("😫", "😓", "🙂", "😄", "😎")
private val FeelLabels = listOf(R.string.fin_feel_1, R.string.fin_feel_2, R.string.fin_feel_3, R.string.fin_feel_4, R.string.fin_feel_5)

/**
 * Màn kết thúc buổi tập (Compose): hero ăn mừng + pháo giấy, 3 chỉ số, streak, đánh giá độ khó (lưu
 * lại để thống kê), cân nặng, nhắc tập và nút Chia sẻ / Xong. [banner] = quảng cáo banner.
 */
@Composable
fun FinishedScreen(state: FinishedState, actions: FinishedActions, banner: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().background(MinColor.Background)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Hero(state, actions.onClose)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 14.dp, bottom = 16.dp),
            ) {
                FeelCard(state.feel, actions.onFeel)
                InfoRow(
                    emoji = "⚖️",
                    title = stringResource(R.string.fin_weight),
                    value = state.weightText,
                    sub = state.bmiText,
                    action = stringResource(R.string.fin_update),
                    onAction = actions.onUpdateWeight,
                )
                InfoRow(
                    emoji = "⏰",
                    title = stringResource(R.string.fin_reminder),
                    value = state.reminderText,
                    sub = null,
                    action = stringResource(R.string.fin_edit),
                    onAction = actions.onEditReminder,
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().background(MinColor.Surface).padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            OutlinedButton(
                onClick = actions.onShare,
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MinColor.Pink),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier.weight(1f),
            ) { Text("↗ " + stringResource(R.string.fin_share), color = MinColor.Pink, fontWeight = FontWeight.Bold) }
            Button(
                onClick = actions.onDone,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MinColor.Pink, contentColor = Color.White),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier.weight(1.6f),
            ) { Text(stringResource(R.string.fin_done), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold) }
        }
        Box(Modifier.fillMaxWidth().background(MinColor.Surface).navigationBarsPadding()) { banner() }
    }
}

@Composable
private fun Hero(state: FinishedState, onClose: () -> Unit) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val trophyScale by animateFloatAsState(if (appeared) 1f else 0.3f, tween(600), label = "trophy")
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Brush.verticalGradient(HeroGradient)),
    ) {
        // Pháo giấy (lottie success.json sẵn có của app).
        AndroidView(
            factory = { ctx ->
                LottieAnimationView(ctx).apply {
                    setAnimation("success.json")
                    playAnimation()
                }
            },
            modifier = Modifier.matchParentSizeCompat(),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(bottom = 22.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onClose),
                ) { Text("✕", color = Color.White, fontSize = 20.sp) }
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(96.dp).scale(trophyScale).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
            ) { Text("🏆", fontSize = 52.sp) }
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.fin_title), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                state.programName,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
                Stat("⏱", state.durationText, stringResource(R.string.fin_duration), Modifier.weight(1f))
                Stat("🔥", state.kcal, "kcal", Modifier.weight(1f))
                Stat("💪", state.exercises.toString(), stringResource(R.string.fin_exercises), Modifier.weight(1f))
            }
            AnimatedVisibility(state.streakText != null, enter = fadeIn() + slideInVertically { it / 2 }) {
                Text(
                    state.streakText.orEmpty(),
                    color = Color(0xFFC2541A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 14.dp, start = 16.dp, end = 16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFFFEFE5))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}

/** AndroidView không có matchParentSize ngoài BoxScope: đặt kích thước theo hero bằng fillMaxWidth + chiều cao cố định. */
private fun Modifier.matchParentSizeCompat(): Modifier = this.fillMaxWidth().height(320.dp)

@Composable
private fun Stat(emoji: String, value: String, label: String, modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .padding(vertical = 12.dp),
    ) {
        Text(emoji, fontSize = 18.sp)
        Text(value, color = MinColor.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = MinColor.TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun FeelCard(feel: Int?, onFeel: (Int) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MinColor.Surface)
            .border(1.dp, Border, RoundedCornerShape(22.dp))
            .padding(16.dp),
    ) {
        Text(stringResource(R.string.fin_how), color = MinColor.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FeelEmojis.forEachIndexed { index, emoji ->
                val selected = feel == index
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) MinColor.PinkSoft else Color.Transparent)
                        .border(if (selected) 2.dp else 0.dp, if (selected) MinColor.Pink else Color.Transparent, RoundedCornerShape(16.dp))
                        .clickable { onFeel(index) }
                        .padding(vertical = 8.dp),
                ) {
                    Text(emoji, fontSize = if (selected) 30.sp else 26.sp)
                    Text(
                        stringResource(FeelLabels[index]),
                        color = if (selected) MinColor.Pink else MinColor.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(emoji: String, title: String, value: String, sub: String?, action: String, onAction: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MinColor.Surface)
            .border(1.dp, Border, RoundedCornerShape(22.dp))
            .clickable(onClick = onAction)
            .padding(16.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(MinColor.PinkSoft),
        ) { Text(emoji, fontSize = 22.sp) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = MinColor.TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(value, color = MinColor.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            sub?.takeIf { it.isNotBlank() }?.let { Text(it, color = MinColor.TextSecondary, fontSize = 12.sp) }
        }
        Text(
            action,
            color = MinColor.Pink,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clip(RoundedCornerShape(50)).background(MinColor.PinkSoft).padding(horizontal = 14.dp, vertical = 7.dp),
        )
    }
}
