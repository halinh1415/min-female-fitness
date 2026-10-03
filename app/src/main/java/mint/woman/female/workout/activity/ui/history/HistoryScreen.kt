package mint.woman.female.workout.activity.ui.history

import android.widget.ImageView
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.compose.GlideImage
import mint.woman.female.workout.activity.ui.compose.MinCard
import mint.woman.female.workout.activity.ui.compose.MinColor
import mint.woman.female.workout.model.HistoryItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ---------------------------------------------------------------- state

class HistoryUiState {
    /** Mili giây 00:00 ngày đầu tháng đang xem. */
    var monthStart by mutableLongStateOf(0L)
    var firstDayOfWeek by mutableStateOf(Calendar.SUNDAY)
    var items by mutableStateOf<List<HistoryItem>>(emptyList())
    var loading by mutableStateOf(true)

    /** Ngày đang lọc (mili giây 00:00), 0 = cả tháng. */
    var selectedDay by mutableLongStateOf(0L)
}

interface HistoryActions {
    fun onBack()
    fun onReset()
    fun onPrevMonth()
    fun onNextMonth()
    fun onItemClick(item: HistoryItem)
    fun imageUrl(item: HistoryItem): String
}

private fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun formatDuration(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)

// ---------------------------------------------------------------- screen

@Composable
fun HistoryScreen(state: HistoryUiState, actions: HistoryActions) {
    val byDay = remember(state.items) {
        state.items.groupBy { startOfDay(it.dateId) }.toSortedMap(compareByDescending { it })
    }
    val shownDays = if (state.selectedDay != 0L) byDay.filterKeys { it == state.selectedDay } else byDay

    Column(
        Modifier
            .fillMaxSize()
            .background(MinColor.Background)
    ) {
        HistoryTopBar(actions)
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { CalendarCard(state, byDay.keys, actions) }
            item { MonthSummary(state.items, byDay.size) }
            if (!state.loading && shownDays.isEmpty()) {
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🗓️", fontSize = 44.sp)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            stringResource(R.string.txt_no_workout),
                            fontSize = 15.sp,
                            color = MinColor.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            shownDays.forEach { (day, dayItems) ->
                item(key = "h$day") { DayHeader(day, dayItems) }
                items(dayItems.sortedByDescending { it.startTime }, key = { it.id }) { item ->
                    HistoryRow(item, actions)
                }
            }
        }
    }
}

@Composable
private fun HistoryTopBar(actions: HistoryActions) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .background(MinColor.Pink)
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(onClick = actions::onBack),
            contentAlignment = Alignment.Center
        ) { Text("←", fontSize = 24.sp, color = Color.White) }
        Text(
            stringResource(R.string.mn_history),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
        Box {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { menuOpen = true },
                contentAlignment = Alignment.Center
            ) { Text("⋮", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold) }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.txt_reset)) },
                    onClick = {
                        menuOpen = false
                        actions.onReset()
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------------- lịch

@Composable
private fun CalendarCard(state: HistoryUiState, activeDays: Set<Long>, actions: HistoryActions) {
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val weekdayFormat = remember { SimpleDateFormat("EEEEE", Locale.getDefault()) }
    val today = remember { startOfDay(System.currentTimeMillis()) }

    // các ô của lưới: null = ô trống trước ngày 1
    val cells = remember(state.monthStart, state.firstDayOfWeek) {
        val cal = Calendar.getInstance().apply { timeInMillis = state.monthStart }
        val leading = (cal.get(Calendar.DAY_OF_WEEK) - state.firstDayOfWeek + 7) % 7
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val list = ArrayList<Long?>()
        repeat(leading) { list += null }
        for (d in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, d)
            list += startOfDay(cal.timeInMillis)
        }
        while (list.size % 7 != 0) list += null
        list
    }
    val weekdayLabels = remember(state.firstDayOfWeek) {
        val cal = Calendar.getInstance()
        (0 until 7).map {
            cal.set(Calendar.DAY_OF_WEEK, (state.firstDayOfWeek - 1 + it) % 7 + 1)
            weekdayFormat.format(cal.time)
        }
    }

    MinCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MonthArrow("‹", actions::onPrevMonth)
            Text(
                monthFormat.format(Date(state.monthStart)).replaceFirstChar { it.titlecase(Locale.getDefault()) },
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            MonthArrow("›", actions::onNextMonth)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth()) {
            weekdayLabels.forEach {
                Text(
                    it,
                    fontSize = 13.sp,
                    color = MinColor.TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            DayCell(
                                day = day,
                                active = day in activeDays,
                                today = day == today,
                                selected = day == state.selectedDay,
                                onClick = {
                                    state.selectedDay = if (state.selectedDay == day || day !in activeDays) 0L else day
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthArrow(symbol: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MinColor.PinkSoft)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MinColor.Pink)
    }
}

@Composable
private fun DayCell(day: Long, active: Boolean, today: Boolean, selected: Boolean, onClick: () -> Unit) {
    val number = remember(day) { Calendar.getInstance().apply { timeInMillis = day }.get(Calendar.DAY_OF_MONTH) }
    var modifier = Modifier
        .fillMaxSize()
        .clip(CircleShape)
    modifier = when {
        active -> modifier.background(MinColor.CtaBrush)
        today -> modifier.background(Color.White)
        else -> modifier
    }
    if (today && !active) modifier = modifier.border(2.dp, MinColor.Pink, CircleShape)
    if (selected) modifier = modifier.border(3.dp, MinColor.Plum, CircleShape)
    Box(modifier.clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(
            "$number",
            fontSize = 15.sp,
            fontWeight = if (active || today) FontWeight.Bold else FontWeight.Normal,
            color = when {
                active -> Color.White
                today -> MinColor.Pink
                else -> MinColor.TextPrimary
            }
        )
    }
}

// ---------------------------------------------------------------- tổng kết tháng

@Composable
private fun MonthSummary(items: List<HistoryItem>, activeDays: Int) {
    val calories = items.sumOf { it.calories }
    val seconds = items.sumOf { it.realDuration }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryTile("🏋️", "${items.size}", stringResource(R.string.txt_workout), Modifier.weight(1f))
        SummaryTile("🔥", "$calories", "kcal", Modifier.weight(1f))
        SummaryTile("⏱", "${seconds / 60}", stringResource(R.string.minutes), Modifier.weight(1f))
        SummaryTile("📅", "$activeDays", stringResource(R.string.txt_days), Modifier.weight(1f))
    }
}

@Composable
private fun SummaryTile(emoji: String, value: String, label: String, modifier: Modifier) {
    MinCard(modifier, padding = 10.dp) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 18.sp)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(label, fontSize = 11.sp, color = MinColor.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ---------------------------------------------------------------- danh sách

@Composable
private fun DayHeader(day: Long, items: List<HistoryItem>) {
    val format = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()) }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(MinColor.Pink)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            format.format(Date(day)).replaceFirstChar { it.titlecase(Locale.getDefault()) },
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "${items.sumOf { it.calories }} kcal • ${formatDuration(items.sumOf { it.realDuration })}",
            fontSize = 13.sp,
            color = MinColor.TextSecondary
        )
    }
}

@Composable
private fun HistoryRow(item: HistoryItem, actions: HistoryActions) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    MinCard(Modifier.fillMaxWidth(), onClick = { actions.onItemClick(item) }, padding = 10.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MinColor.PinkSoft)
                    .clipToBounds()
            ) {
                GlideImage(actions.imageUrl(item), Modifier.fillMaxSize(), scaleType = ImageView.ScaleType.CENTER_CROP)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.name.orEmpty(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🕒 ${timeFormat.format(Date(item.startTime))}", fontSize = 13.sp, color = MinColor.TextSecondary)
                    Text("⏱ ${formatDuration(item.realDuration)}", fontSize = 13.sp, color = MinColor.TextSecondary)
                    Text("🔥 ${item.calories} kcal", fontSize = 13.sp, color = MinColor.TextSecondary)
                }
            }
            Box(
                Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (item.isPassed) MinColor.Ok else MinColor.Track),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
