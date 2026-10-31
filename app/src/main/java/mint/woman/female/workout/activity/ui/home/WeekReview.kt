package mint.woman.female.workout.activity.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.compose.MinColor

/**
 * Thẻ "Tuần x xong 🎉" dưới thẻ plan cá nhân: bấm mở hộp hỏi cảm nhận tuần (dễ / vừa / khó) và thời lượng
 * buổi (ngắn / giữ / dài) để sinh lại các ngày còn lại (PersonalPlanStore.applyWeekReview).
 */
@Composable
internal fun WeekReviewCard(
    week: Int, onApply: (feel: Int, length: Int, injury: Int) -> Unit, onSkip: () -> Unit, modifier: Modifier = Modifier,
    /** Tên chấn thương plan đang tránh (string res): có thì hỏi thêm vẫn đau / đỡ nhiều / khỏi rồi (0 / 1 / 2). */
    injuries: List<Int> = emptyList(),
) {
    var open by rememberSaveable { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MinColor.Surface)
            .border(1.dp, MinColor.Pink.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .clickable { open = true }
            .padding(16.dp),
    ) {
        Text("🏆", fontSize = 28.sp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.pp_review_title, week), color = MinColor.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(2.dp))
            Text(stringResource(R.string.pp_review_desc), color = MinColor.TextSecondary, fontSize = 13.sp, lineHeight = 17.sp)
        }
        Spacer(Modifier.width(10.dp))
        Text(
            stringResource(R.string.pp_review_button),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(MinColor.Pink).padding(horizontal = 14.dp, vertical = 9.dp),
        )
    }
    if (open) {
        WeekReviewDialog(
            week = week,
            injuries = injuries,
            onApply = { feel, length, injury ->
                open = false
                onApply(feel, length, injury)
            },
            onSkip = {
                open = false
                onSkip()
            },
            onDismiss = { open = false },
        )
    }
}

@Composable
private fun WeekReviewDialog(
    week: Int, injuries: List<Int>, onApply: (Int, Int, Int) -> Unit, onSkip: () -> Unit, onDismiss: () -> Unit,
) {
    var feel by rememberSaveable { mutableIntStateOf(0) }
    var length by rememberSaveable { mutableIntStateOf(0) }
    var injury by rememberSaveable { mutableIntStateOf(0) }
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MinColor.Surface)
                .padding(horizontal = 20.dp, vertical = 22.dp),
        ) {
            Text(
                stringResource(R.string.pp_review_title, week),
                color = MinColor.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.pp_review_q_feel), color = MinColor.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            ChoiceRow(
                options = listOf(
                    1 to stringResource(R.string.pp_review_easy),
                    0 to stringResource(R.string.pp_review_right),
                    -1 to stringResource(R.string.pp_review_hard),
                ),
                selected = feel,
                onSelect = { feel = it },
            )
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.pp_review_q_length), color = MinColor.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            ChoiceRow(
                options = listOf(
                    -1 to stringResource(R.string.pp_review_shorter),
                    0 to stringResource(R.string.pp_review_same),
                    1 to stringResource(R.string.pp_review_longer),
                ),
                selected = length,
                onSelect = { length = it },
            )
            if (injuries.isNotEmpty()) {
                val names = injuries.map { stringResource(it) }.joinToString(", ")
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.pp_review_q_injury, names), color = MinColor.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                ChoiceRow(
                    options = listOf(
                        0 to stringResource(R.string.pp_injury_still),
                        1 to stringResource(R.string.pp_injury_better),
                        2 to stringResource(R.string.pp_injury_healed),
                    ),
                    selected = injury,
                    onSelect = { injury = it },
                )
            }
            Spacer(Modifier.height(22.dp))
            Button(
                onClick = { onApply(feel, length, injury) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MinColor.Pink),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text(stringResource(R.string.pp_review_apply), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
            Text(
                stringResource(R.string.pp_review_later),
                color = MinColor.TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onSkip)
                    .padding(vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun ChoiceRow(options: List<Pair<Int, String>>, selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        options.forEach { (value, label) ->
            val on = value == selected
            Text(
                label,
                color = if (on) Color.White else MinColor.TextPrimary,
                fontSize = 14.sp,
                fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (on) MinColor.Pink else MinColor.Background)
                    .border(1.dp, if (on) MinColor.Pink else MinColor.Track, RoundedCornerShape(14.dp))
                    .clickable { onSelect(value) }
                    .padding(vertical = 12.dp),
            )
        }
    }
}
