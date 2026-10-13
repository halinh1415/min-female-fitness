package mint.woman.female.workout.activity.ui.weekgoal

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.compose.MinButton
import mint.woman.female.workout.activity.ui.compose.MinCard
import mint.woman.female.workout.activity.ui.compose.MinColor
import mint.woman.female.workout.activity.ui.compose.MinTheme
import mint.woman.female.workout.utils.LanguageHelper
import mint.woman.female.workout.utils.MyPreference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Đặt mục tiêu tuần (số ngày tập 1–7 + ngày đầu tuần), viết bằng Jetpack Compose (thay layout activity_set_goal +
 * NumberPicker + Spinner). Lưu vào MyPreference rồi trả RESULT_OK như bản cũ để Home / Week goal vẽ lại tuần.
 */
class SetGoalActivity : AppCompatActivity() {

    private lateinit var pref: MyPreference
    private var goalOfWeek by mutableIntStateOf(5)
    private var firstDayOfWeek by mutableIntStateOf(Calendar.MONDAY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAnalytics.getInstance(this).logEvent("scr_set_goal", Bundle())
        pref = MyPreference(this)
        goalOfWeek = savedInstanceState?.getInt(KEY_GOAL) ?: pref.goalOfWeek.coerceIn(1, 7)
        firstDayOfWeek = savedInstanceState?.getInt(KEY_FIRST_DAY) ?: pref.firstDayOfWeek
            .takeIf { it in FIRST_DAY_VALUES } ?: Calendar.MONDAY

        val dayNames = FIRST_DAY_VALUES.map { dayName(it) }
        setContent {
            MinTheme {
                SetGoalScreen(
                    goal = goalOfWeek,
                    firstDay = firstDayOfWeek,
                    firstDayOptions = FIRST_DAY_VALUES.zip(dayNames),
                    onGoal = { goalOfWeek = it },
                    onFirstDay = { firstDayOfWeek = it },
                    onBack = ::finish,
                    onSave = ::saveGoal,
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_GOAL, goalOfWeek)
        outState.putInt(KEY_FIRST_DAY, firstDayOfWeek)
    }

    /** Tên thứ theo ngôn ngữ app (vd "Thứ Hai", "Monday"). */
    private fun dayName(calendarDay: Int): String {
        val calendar = Calendar.getInstance(Locale.getDefault()).apply { set(Calendar.DAY_OF_WEEK, calendarDay) }
        return SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
            .replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }

    private fun saveGoal() {
        FirebaseAnalytics.getInstance(this).logEvent("click_save_scr_set_goal", Bundle().apply {
            putInt("GoalOfWeek", goalOfWeek)
            putInt("FirstDayOf", firstDayOfWeek)
        })
        pref.goalOfWeek = goalOfWeek
        pref.firstDayOfWeek = firstDayOfWeek
        // Intent mới, không trả lại getIntent(): tránh Intent Redirection
        setResult(RESULT_OK, Intent().putExtra("RESULT", false))
        finish()
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = PreferenceManager.getDefaultSharedPreferences(newBase).getString("ST_LANGUAGE", "").orEmpty()
        super.attachBaseContext(
            LanguageHelper.setLanguage(newBase, if (lang.length > 1) lang.substring(0, 2) else Locale.getDefault().language)
        )
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (overrideConfiguration != null) {
            val uiMode = overrideConfiguration.uiMode
            overrideConfiguration.setTo(baseContext.resources.configuration)
            overrideConfiguration.uiMode = uiMode
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    companion object {
        private const val KEY_GOAL = "goal"
        private const val KEY_FIRST_DAY = "first_day"
        /** Ngày đầu tuần được hỗ trợ (giống Spinner cũ). */
        private val FIRST_DAY_VALUES = listOf(Calendar.MONDAY, Calendar.SATURDAY, Calendar.SUNDAY)
    }
}

@Composable
private fun SetGoalScreen(
    goal: Int,
    firstDay: Int,
    firstDayOptions: List<Pair<Int, String>>,
    onGoal: (Int) -> Unit,
    onFirstDay: (Int) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MinColor.Background)
    ) {
        // thanh trên: quay lại + tiêu đề (giống màn Personal plan)
        Box(Modifier.fillMaxWidth().height(56.dp)) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) { Text("←", fontSize = 24.sp, color = MinColor.TextPrimary) }
            Text(
                stringResource(R.string.txt_week_goal),
                color = MinColor.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .padding(top = 8.dp)
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MinColor.PinkSoft),
                contentAlignment = Alignment.Center
            ) {
                Image(painterResource(R.drawable.ic_target), contentDescription = null, modifier = Modifier.size(60.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(
                stringResource(R.string.txt_set_your_goal),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MinColor.TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.txt_set_goal_description),
                fontSize = 15.sp,
                lineHeight = 21.sp,
                color = MinColor.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(20.dp))

            // Số ngày tập mỗi tuần: 7 ô 1–7 thay cho NumberPicker
            MinCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.txt_set_day_goal_of_week),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinColor.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "$goal/7",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinColor.Pink
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    (1..7).forEach { day ->
                        DayOption(day, selected = day == goal, filled = day <= goal, modifier = Modifier.weight(1f)) {
                            onGoal(day)
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))

            // Ngày đầu tuần: 3 lựa chọn thay cho Spinner
            MinCard(Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.txt_first_day_of_week),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MinColor.TextPrimary
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    firstDayOptions.forEach { (value, name) ->
                        val selected = value == firstDay
                        Text(
                            name,
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) Color.White else MinColor.TextPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (selected) MinColor.Pink else MinColor.PinkSoft)
                                .clickable { onFirstDay(value) }
                                .padding(vertical = 12.dp, horizontal = 4.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        MinButton(
            stringResource(R.string.save),
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            onClick = onSave
        )
    }
}

@Composable
private fun DayOption(day: Int, selected: Boolean, filled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                when {
                    selected -> MinColor.Pink
                    filled -> MinColor.Pink.copy(alpha = 0.18f)
                    else -> MinColor.Track
                }
            )
            .then(if (selected) Modifier.border(2.dp, MinColor.Pink, CircleShape) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "$day",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                selected -> Color.White
                filled -> MinColor.Pink
                else -> MinColor.TextMuted
            }
        )
    }
}
