package mint.woman.female.workout.activity.ui.diet

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.compose.MinButton
import mint.woman.female.workout.activity.ui.compose.MinCard
import mint.woman.female.workout.activity.ui.compose.MinColor
import mint.woman.female.workout.activity.ui.compose.MinLargeHeader
import mint.woman.female.workout.activity.ui.compose.MinProgressBar

const val DIET_DAYS = 30

class DietUiState {
    /** Số ngày đã hoàn thành theo MyPreference.getProgressDiet(). */
    var progress by mutableIntStateOf(0)

    /** Ngày (1..30) đã đánh dấu hoàn thành. */
    var doneDays by mutableStateOf<Set<Int>>(emptySet())
}

interface DietActions {
    /** [day] tính từ 1. */
    fun onOpenDay(day: Int)
    fun onShoppingList()
}

@Composable
fun DietScreen(state: DietUiState, actions: DietActions) {
    // Ngày gợi ý tiếp theo: ngày đầu tiên chưa hoàn thành.
    val nextDay = (1..DIET_DAYS).firstOrNull { it !in state.doneDays } ?: DIET_DAYS
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(MinColor.Background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { MinLargeHeader(stringResource(R.string.txt_recipes), horizontalPadding = 0.dp) }
        item { PlanBanner(state, actions) }
        item { NextDayCard(nextDay, actions) }
        item { DayGrid(state, nextDay, actions) }
    }
}

@Composable
private fun PlanBanner(state: DietUiState, actions: DietActions) {
    val fraction by animateFloatAsState(
        state.progress / DIET_DAYS.toFloat(),
        tween(800, easing = FastOutSlowInEasing),
        label = "diet"
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(26.dp))
    ) {
        Image(
            painterResource(R.drawable.img_diet_banner),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0f to MinColor.Surface.copy(alpha = 0.95f),
                        0.55f to MinColor.Surface.copy(alpha = 0.6f),
                        1f to Color.Transparent
                    )
                )
        )
        Column(
            Modifier
                .fillMaxWidth(0.66f)
                .padding(18.dp)
        ) {
            Text(
                stringResource(R.string.txt_meal_plan_30),
                fontSize = 23.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(stringResource(R.string.txt_meal_plan_info), fontSize = 14.sp, color = MinColor.TextSecondary)
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${state.progress}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MinColor.Pink)
                Text("/$DIET_DAYS", fontSize = 16.sp, color = MinColor.TextSecondary, modifier = Modifier.padding(bottom = 4.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "${DIET_DAYS - state.progress} ${stringResource(R.string.txt_day_left)}",
                    fontSize = 13.sp,
                    color = MinColor.TextSecondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Spacer(Modifier.height(6.dp))
            MinProgressBar(fraction, Modifier.fillMaxWidth())
        }
        Row(
            Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MinColor.Pink)
                .clickable(onClick = actions::onShoppingList)
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painterResource(R.drawable.baseline_shopping_cart_24),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.txt_shopping_list),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun NextDayCard(nextDay: Int, actions: DietActions) {
    MinCard(Modifier.fillMaxWidth(), onClick = { actions.onOpenDay(nextDay) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MinColor.CtaBrush),
                contentAlignment = Alignment.Center
            ) {
                Text("$nextDay", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("🥗 ${stringResource(R.string.today)}", fontSize = 13.sp, color = MinColor.Ok, fontWeight = FontWeight.Bold)
                Text(
                    "${stringResource(R.string.txt_day)} $nextDay",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            MinButton("›", Modifier.width(56.dp), height = 44.dp) { actions.onOpenDay(nextDay) }
        }
    }
}

@Composable
private fun DayGrid(state: DietUiState, nextDay: Int, actions: DietActions) {
    MinCard(Modifier.fillMaxWidth(), padding = 14.dp) {
        Text(stringResource(R.string.txt_meal_plan_30), fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        (1..DIET_DAYS).chunked(5).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { day ->
                    DayCell(
                        day = day,
                        done = day in state.doneDays,
                        next = day == nextDay,
                        modifier = Modifier.weight(1f)
                    ) { actions.onOpenDay(day) }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DayCell(day: Int, done: Boolean, next: Boolean, modifier: Modifier, onClick: () -> Unit) {
    var cell = modifier
        .aspectRatio(0.9f)
        .clip(RoundedCornerShape(16.dp))
    cell = when {
        done -> cell.background(MinColor.CtaBrush)
        next -> cell
            .background(MinColor.Surface)
            .border(2.dp, MinColor.Pink, RoundedCornerShape(16.dp))
        else -> cell.background(MinColor.Track)
    }
    Column(
        cell.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.txt_day),
            fontSize = 11.sp,
            color = when {
                done -> Color.White.copy(alpha = 0.85f)
                next -> MinColor.Pink
                else -> MinColor.TextMuted
            },
            maxLines = 1
        )
        Text(
            if (done) "✓" else "$day",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                done -> Color.White
                next -> MinColor.Pink
                else -> MinColor.TextPrimary
            }
        )
    }
}

