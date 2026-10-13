package mint.woman.female.workout.activity.ui.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.compose.MinColor
import java.util.Locale

/**
 * Thẻ Flash Sale trên Home (port từ Women Workout): chỉ hiện khi đang có đợt giảm 50% ([PaywallPromo], chỉ
 * được mở khi Play có base plan yearly-special-50-off) và người dùng chưa Premium. Đồng hồ đếm ngược tới
 * [endsAt]; hết giờ gọi [onExpired] để Home ẩn thẻ.
 */
@Composable
fun FlashSaleBanner(endsAt: Long, onExpired: () -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val expired by rememberUpdatedState(onExpired)
    var remaining by remember(endsAt) { mutableLongStateOf(endsAt - System.currentTimeMillis()) }
    LaunchedEffect(endsAt) {
        while (true) {
            remaining = endsAt - System.currentTimeMillis()
            if (remaining <= 0) {
                expired()
                break
            }
            delay(1000L - System.currentTimeMillis() % 1000L)
        }
    }
    val seconds = (remaining.coerceAtLeast(0) + 999) / 1000
    val time = String.format(Locale.US, "%02d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            // Nền hồng nhạt + viền hồng thay cho gradient hồng đậm (Home bớt chói); chữ và nút giữ màu hồng nhấn.
            .background(MinColor.PinkSoft)
            .border(1.dp, MinColor.Pink.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.fs_title_percent, "50%"),
                color = MinColor.Pink,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                stringResource(R.string.fs_ends, time),
                color = MinColor.TextSecondary,
                fontSize = 13.sp,
                style = TextStyle(fontFeatureSettings = "tnum"),
            )
        }
        Text(
            stringResource(R.string.fs_cta),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MinColor.Pink)
                .padding(horizontal = 14.dp, vertical = 7.dp),
        )
    }
}
