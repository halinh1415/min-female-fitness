package mint.woman.female.workout.activity.ui.home

import android.widget.ImageView
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.compose.GlideImage
import mint.woman.female.workout.activity.ui.compose.MinButton
import mint.woman.female.workout.activity.ui.compose.MinCard
import mint.woman.female.workout.activity.ui.compose.MinColor
import mint.woman.female.workout.activity.ui.compose.MinLargeHeader
import mint.woman.female.workout.activity.ui.compose.MinProgressBar
import mint.woman.female.workout.activity.ui.compose.MinSectionHeader
import mint.woman.female.workout.activity.ui.compose.backgroundUrl
import mint.woman.female.workout.activity.ui.compose.fallbackBackgroundUrl
import mint.woman.female.workout.activity.ui.compose.modelUrl
import mint.woman.female.workout.model.ProgramObject
import mint.woman.female.workout.activity.ui.premium.FlashSaleBanner

// ---------------------------------------------------------------- state

data class WeekDay(val label: String, val dayOfMonth: String, val done: Boolean, val today: Boolean)

data class HomeSection(val title: String, val programs: List<ProgramObject>, val challenge: Boolean)

/** Trạng thái màn Home – Fragment cập nhật từ HistoryViewModel/MyPreference. */
class HomeUiState {
    var dateText by mutableStateOf("")
    var streakDays by mutableIntStateOf(0)
    var coachMessage by mutableStateOf("")
    var suggestion by mutableStateOf<ProgramObject?>(null)
    var suggestionInfo by mutableStateOf("")
    var weekDays by mutableStateOf<List<WeekDay>>(emptyList())
    var goalDone by mutableIntStateOf(0)
    var goalTotal by mutableIntStateOf(0)
    var recentPrograms by mutableStateOf<List<ProgramObject>>(emptyList())
    var sections by mutableStateOf<List<HomeSection>>(emptyList())
    var isPremium by mutableStateOf(false)

    /** Thời điểm hết đợt giảm 50% đang chạy (null = không có / đã Premium) → thẻ Flash Sale có đếm ngược. */
    var promoEndsAt by mutableStateOf<Long?>(null)

    /** Plan cá nhân đã tạo (null = chưa có). */
    var personalPlan by mutableStateOf<ProgramObject?>(null)

    /** Chưa có buổi tập nào và chưa từng mời: hiện popup "Bắt đầu buổi tập đầu tiên". */
    var firstWorkoutNudge by mutableStateOf(false)

    /** Tiến độ từng chương trình (ngày đã tập) – đọc lại mỗi lần quay về màn. */
    var planProgress by mutableStateOf<Map<Int, Int>>(emptyMap())
}

interface HomeActions {
    fun onStartSuggestion()
    fun onProgramClick(program: ProgramObject)
    fun onRecentProgramClick(program: ProgramObject)
    fun onSeeMore(section: HomeSection)
    fun onEditGoal()
    fun onOpenHistory()
    fun onMyWorkouts()
    fun onPremium()
    fun onFlashSale()
    fun onPromoExpired()
    fun onOpenPersonalPlan()
    fun onAdjustPersonalPlan()

    /** Popup buổi tập đầu tiên: bấm bắt đầu mở [program]; cả hai cách đóng đều không mời lại. */
    fun onStartFirstWorkout(program: ProgramObject)
    fun onFirstWorkoutDismissed()
}

// ---------------------------------------------------------------- screen

@Composable
fun HomeScreen(state: HomeUiState, actions: HomeActions) {
    // Column cuộn thường thay vì LazyColumn (như Tai Chi): Home chỉ ~10 khối, dựng sẵn hết thì lúc cuộn
    // không phải dựng thêm khối mới (đo trên S8+: LazyColumn giật hơn hẳn trên máy cũ).
    Column(
        Modifier
            .fillMaxSize()
            .background(MinColor.Background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp)
    ) {
        HeroAndCoach(state, actions)
        state.promoEndsAt?.let { endsAt ->
            FlashSaleBanner(
                endsAt = endsAt,
                onExpired = actions::onPromoExpired,
                onClick = actions::onFlashSale,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp),
            )
        }
        PersonalPlanCard(state, actions, Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp))
        WeekGoalCard(
            state,
            actions,
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        if (state.recentPrograms.isNotEmpty()) {
            RecentCard(state, actions, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }
        MyTrainingTile(actions, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        state.sections.forEach { section ->
            key(section.title) { HomeSectionBlock(section, state, actions) }
        }
    }
    // Có plan cá nhân thì mời tập Ngày 1 của plan, không thì bài gợi ý hôm nay.
    val first = state.personalPlan ?: state.suggestion
    if (state.firstWorkoutNudge && first != null) {
        FirstWorkoutDialog(first, onStart = { actions.onStartFirstWorkout(first) }, onDismiss = actions::onFirstWorkoutDismissed)
    }
}

/** Popup mời tập buổi đầu tiên ngay sau khi vào app (giống Leap Fitness): ảnh bài, tên, thời lượng, nút bắt đầu. */
@Composable
private fun FirstWorkoutDialog(program: ProgramObject, onStart: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MinColor.Surface)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clipToBounds()
                    .background(MinColor.PinkSoft)
            ) {
                GlideImage(program.backgroundUrl(), Modifier.fillMaxSize(), errorUrl = program.fallbackBackgroundUrl())
                GlideImage(
                    program.modelUrl(),
                    Modifier
                        .fillMaxHeight()
                        .width(190.dp)
                        .align(Alignment.BottomEnd),
                    scaleType = ImageView.ScaleType.FIT_END
                )
                Text(
                    "✕",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.25f))
                        .clickable(onClick = onDismiss)
                        .padding(top = 6.dp)
                )
            }
            Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 22.dp)) {
                Text(
                    stringResource(R.string.home_first_title),
                    color = MinColor.TextPrimary,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (program.type == 1) "${program.name} · ${stringResource(R.string.txt_day)} 1/${program.total}"
                    else stringResource(R.string.home_first_sub, program.name, program.total),
                    color = MinColor.TextSecondary,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(20.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MinColor.CtaBrush)
                        .clickable(onClick = onStart)
                ) {
                    Text(stringResource(R.string.home_first_cta), color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

/**
 * Một mục bài tập ở Home. "Thêm" mở rộng ngay tại chỗ (như Home Workout) thay vì sang màn khác:
 * thử thách thành danh sách dọc, bài tập thường thành lưới 2 cột; "Thu gọn" trở lại hàng cuộn ngang.
 */
@Composable
private fun HomeSectionBlock(section: HomeSection, state: HomeUiState, actions: HomeActions) {
    var expanded by rememberSaveable(section.title) { mutableStateOf(false) }
    val canExpand = section.programs.size > if (section.challenge) 1 else 2
    Column(
        Modifier
            .padding(top = 18.dp)
            .animateContentSize()
    ) {
        MinSectionHeader(
            section.title,
            Modifier.padding(horizontal = 16.dp),
            action = if (!canExpand) null else stringResource(if (expanded) R.string.home_show_less else R.string.explore_more),
            onAction = if (!canExpand) null else ({ expanded = !expanded }),
            actionSuffix = if (expanded) " ˄" else " ›"
        )
        Spacer(Modifier.height(10.dp))
        if (expanded && section.challenge) {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                section.programs.forEach { program ->
                    ChallengeCard(program, state, Modifier.fillMaxWidth().height(180.dp)) { actions.onProgramClick(program) }
                }
            }
        } else if (expanded) {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                section.programs.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { program ->
                            ProgramCard(program, state, Modifier.weight(1f)) { actions.onProgramClick(program) }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(section.programs, key = { it.id }) { program ->
                    if (section.challenge) {
                        ChallengeCard(program, state) { actions.onProgramClick(program) }
                    } else {
                        ProgramCard(program, state) { actions.onProgramClick(program) }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- hero + coach

@Composable
private fun HeroAndCoach(state: HomeUiState, actions: HomeActions) {
    Column {
        MinLargeHeader(stringResource(R.string.app_name)) {
            StreakChip(state.streakDays)
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MinColor.PinkSoft)
                    .clickable(onClick = actions::onPremium),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painterResource(R.drawable.ic_diamond),
                    contentDescription = stringResource(R.string.txt_go_premium),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(state.dateText, fontSize = 14.sp, color = MinColor.TextMuted)
            Text(
                state.coachMessage,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MinColor.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            // Có plan cá nhân: thẻ "Ngày x/28" ngay bên dưới là việc chính của hôm nay, chỉ hiện gợi ý khi hôm nay đã tập.
            if (state.personalPlan == null || state.weekDays.any { it.today && it.done }) {
                Spacer(Modifier.height(14.dp))
                CoachCard(state, actions)
            }
        }
    }
}

@Composable
private fun StreakChip(days: Int) {
    Row(
        Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(MinColor.Track)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🔥", fontSize = 20.sp)
        Spacer(Modifier.width(6.dp))
        Text("$days", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MinColor.TextPrimary)
    }
}

@Composable
private fun CoachCard(state: HomeUiState, actions: HomeActions) {
    // Thẻ ảnh tràn viền kiểu thẻ bài tập (như thẻ thử thách bên dưới): ảnh + người mẫu phủ toàn thẻ, gradient tối
    // dưới đáy, chữ trắng và nút "Bắt đầu" dạng viên thuốc. Trước đây: nửa trên ảnh bị phủ trắng, 2 nhãn xanh lá /
    // cam lệch tông, nửa dưới trắng với nút hồng to in hoa → rời rạc và chói.
    val program = state.suggestion
    Box(
        Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(24.dp))
            .clipToBounds()
            .background(MinColor.PinkSoft)
            .clickable(onClick = actions::onStartSuggestion)
    ) {
        if (program != null) {
            GlideImage(
                program.backgroundUrl(),
                Modifier.fillMaxSize(),
                errorUrl = program.fallbackBackgroundUrl()
            )
            GlideImage(
                program.modelUrl(),
                Modifier
                    .fillMaxHeight()
                    .width(190.dp)
                    .align(Alignment.BottomEnd),
                scaleType = ImageView.ScaleType.FIT_END
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.10f),
                        0.45f to Color.Black.copy(alpha = 0.18f),
                        1f to Color.Black.copy(alpha = 0.68f)
                    )
                )
        )
        Text(
            stringResource(R.string.suggestion_today),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.28f))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .fillMaxWidth(0.66f)
        ) {
            Text(
                program?.name ?: "…",
                color = Color.White,
                fontSize = 22.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (state.suggestionInfo.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    state.suggestionInfo,
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
                    .clickable(onClick = actions::onStartSuggestion)
                    .padding(start = 14.dp, end = 18.dp, top = 9.dp, bottom = 9.dp)
            ) {
                Text("▶", color = MinColor.Pink, fontSize = 13.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.start_now),
                    color = MinColor.Pink,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

// ---------------------------------------------------------------- plan cá nhân

@Composable
private fun PersonalPlanCard(state: HomeUiState, actions: HomeActions, modifier: Modifier) {
    val plan = state.personalPlan
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            // Nền pastel (hồng nhạt → tím nhạt) thay cho gradient tím-hồng đậm: Home bớt chói, nút chính
            // "Bắt đầu" vẫn là điểm nhấn hồng đậm duy nhất.
            .background(
                Brush.linearGradient(
                    listOf(MinColor.PinkSoft, MinColor.Purple.copy(alpha = 0.14f).compositeOver(MinColor.Surface))
                )
            )
            .border(1.dp, MinColor.Purple.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
            .clickable(onClick = actions::onOpenPersonalPlan)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "✦ " + stringResource(R.string.pp_home_title),
                color = MinColor.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            if (plan == null) {
                Text(
                    stringResource(R.string.pp_home_desc),
                    color = MinColor.TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 19.sp
                )
            } else {
                val done = state.planProgress[plan.id] ?: 0
                Text(
                    "${stringResource(R.string.txt_day)} ${(done + 1).coerceAtMost(plan.total)}/${plan.total}",
                    color = MinColor.TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
                MinProgressBar(
                    done / plan.total.coerceAtLeast(1).toFloat(),
                    Modifier.fillMaxWidth(0.9f),
                    height = 6.dp,
                    track = MinColor.Track,
                    brush = Brush.horizontalGradient(listOf(MinColor.Purple, MinColor.Pink))
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "⚙ " + stringResource(R.string.pp_edit),
                    color = MinColor.Purple,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = actions::onAdjustPersonalPlan)
                        .padding(vertical = 4.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            if (plan == null) stringResource(R.string.pp_home_create) else stringResource(R.string.continue_text),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MinColor.Purple)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

// ---------------------------------------------------------------- week goal

@Composable
private fun WeekGoalCard(state: HomeUiState, actions: HomeActions, modifier: Modifier) {
    MinCard(modifier.fillMaxWidth(), onClick = actions::onOpenHistory) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.txt_week_goal).uppercase(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${state.goalDone}/${state.goalTotal}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MinColor.Pink
            )
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MinColor.PinkSoft)
                    .clickable(onClick = actions::onEditGoal),
                contentAlignment = Alignment.Center
            ) {
                Text("✎", fontSize = 16.sp, color = MinColor.Pink)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            state.weekDays.forEach { day -> DayBubble(day) }
        }
        Spacer(Modifier.height(14.dp))
        val fraction = if (state.goalTotal > 0) state.goalDone / state.goalTotal.toFloat() else 0f
        val animated by animateFloatAsState(fraction, tween(700), label = "weekGoal")
        MinProgressBar(animated, Modifier.fillMaxWidth())
    }
}

@Composable
private fun DayBubble(day: WeekDay) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            day.label,
            fontSize = 13.sp,
            color = if (day.today) MinColor.Pink else MinColor.TextMuted,
            fontWeight = if (day.today) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
        Spacer(Modifier.height(6.dp))
        val base = Modifier
            .size(38.dp)
            .clip(CircleShape)
        Box(
            when {
                day.done -> base.background(MinColor.CtaBrush)
                day.today -> base
                    .background(MinColor.Surface)
                    .border(2.dp, MinColor.Pink, CircleShape)
                else -> base.background(MinColor.Track)
            },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (day.done) "✓" else day.dayOfMonth,
                fontSize = if (day.done) 17.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    day.done -> Color.White
                    day.today -> MinColor.Pink
                    else -> MinColor.TextSecondary
                }
            )
        }
    }
}

// ---------------------------------------------------------------- tuần này

@Composable
private fun RecentCard(state: HomeUiState, actions: HomeActions, modifier: Modifier) {
    MinCard(modifier.fillMaxWidth()) {
        Text(stringResource(R.string.txt_this_week), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        state.recentPrograms.forEach { program ->
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { actions.onRecentProgramClick(program) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    GlideImage(program.backgroundUrl(), Modifier.fillMaxSize(), errorUrl = program.fallbackBackgroundUrl())
                    GlideImage(program.modelUrl(), Modifier.fillMaxSize(), scaleType = ImageView.ScaleType.FIT_CENTER)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(program.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(6.dp))
                    if (program.type == 1 && program.total > 0) {
                        val done = state.planProgress[program.id] ?: 0
                        val fraction = done / program.total.toFloat()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MinProgressBar(fraction, Modifier.weight(1f), height = 6.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("${(fraction * 100).toInt()}%", fontSize = 13.sp, color = MinColor.Pink, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            stringResource(R.string.x_minutes, program.total),
                            fontSize = 13.sp,
                            color = MinColor.TextSecondary
                        )
                    }
                }
                Text("›", fontSize = 26.sp, color = MinColor.TextMuted, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun MyTrainingTile(actions: HomeActions, modifier: Modifier) {
    MinCard(modifier.fillMaxWidth(), onClick = actions::onMyWorkouts, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MinColor.PinkSoft),
                contentAlignment = Alignment.Center
            ) { Text("🏋️‍♀️", fontSize = 22.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.txt_customize), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.txt_no_my_workout),
                    fontSize = 13.sp,
                    color = MinColor.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text("＋", fontSize = 22.sp, color = MinColor.Pink, fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------------------------------------------------------- thẻ chương trình

@Composable
private fun ChallengeCard(
    program: ProgramObject,
    state: HomeUiState,
    modifier: Modifier = Modifier.width(290.dp).height(180.dp),
    onClick: () -> Unit
) {
    val done = state.planProgress[program.id] ?: 0
    val total = program.total.coerceAtLeast(1)
    Box(
        modifier
            .clip(RoundedCornerShape(24.dp))
            .clipToBounds()
            .clickable(onClick = onClick)
    ) {
        GlideImage(program.backgroundUrl(), Modifier.fillMaxSize(), errorUrl = program.fallbackBackgroundUrl())
        GlideImage(
            program.modelUrl(),
            Modifier
                .fillMaxHeight()
                .width(160.dp)
                .align(Alignment.BottomEnd),
            scaleType = ImageView.ScaleType.FIT_END
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = 0.55f),
                        0.6f to Color.Black.copy(alpha = 0.15f),
                        1f to Color.Transparent
                    )
                )
        )
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .fillMaxWidth(0.62f)
        ) {
            Text(
                program.name,
                color = Color.White,
                fontSize = 21.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(stringResource(R.string.txt_challenge_7x4), color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            MinProgressBar(
                done / total.toFloat(),
                Modifier.fillMaxWidth(),
                height = 6.dp,
                track = Color.White.copy(alpha = 0.35f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "${(total - done).coerceAtLeast(0)} ${stringResource(R.string.txt_day_left)}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProgramCard(
    program: ProgramObject,
    state: HomeUiState,
    modifier: Modifier = Modifier.width(200.dp),
    onClick: () -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MinColor.Surface)
            .clickable(onClick = onClick)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clipToBounds()
        ) {
            GlideImage(program.backgroundUrl(), Modifier.fillMaxSize(), errorUrl = program.fallbackBackgroundUrl())
            GlideImage(program.modelUrl(), Modifier.fillMaxSize(), scaleType = ImageView.ScaleType.FIT_END)
            if (program.premium == 1 && !state.isPremium) {
                Text(
                    "👑",
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                listOfNotNull(program.name, program.subtitle?.takeIf { it.isNotBlank() }).joinToString(" "),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                if (program.type == 1) "🏆 " + stringResource(R.string.txt_challenge_7x4)
                else "⏱ " + stringResource(R.string.x_minutes, program.total),
                fontSize = 13.sp,
                color = MinColor.TextSecondary,
                textAlign = TextAlign.Start
            )
        }
    }
}

