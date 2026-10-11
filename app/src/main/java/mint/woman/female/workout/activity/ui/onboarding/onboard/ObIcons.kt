package mint.woman.female.workout.activity.ui.onboarding.onboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.Bed
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Face3
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Healing
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.SentimentSatisfiedAlt
import androidx.compose.material.icons.rounded.SentimentVerySatisfied
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.SportsGymnastics
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Bộ icon vector cho onboarding, thay cho emoji trong dữ liệu màn hỏi ([ObOption.icon],
 * [OnboardStep.hintEmoji]...). Dữ liệu vẫn giữ emoji làm khoá để không phải sửa từng màn; emoji chưa
 * có trong bảng thì vẫn hiện emoji (dự phòng). Ảnh minh hoạ lớn (ObArt) không thuộc phạm vi này.
 */
private val ICONS: Map<String, Pair<ImageVector, Color>> = mapOf(
    // Mục tiêu / cảm xúc
    "👍" to (Icons.Rounded.ThumbUp to ObColor.Pink),
    "🙂" to (Icons.Rounded.SentimentSatisfiedAlt to ObColor.Warning),
    "😊" to (Icons.Rounded.SentimentVerySatisfied to ObColor.Warning),
    "🤩" to (Icons.Rounded.Stars to ObColor.Warning),
    "❤️" to (Icons.Rounded.Favorite to Color(0xFFE5484D)),
    "🔋" to (Icons.Rounded.BatteryChargingFull to ObColor.Ok),
    "💪" to (Icons.Rounded.FitnessCenter to ObColor.Purple),
    "⚖️" to (Icons.Rounded.MonitorWeight to ObColor.Ok),
    "✨" to (Icons.Rounded.AutoAwesome to ObColor.Pink),
    "🎂" to (Icons.Rounded.Cake to ObColor.Pink),
    "🔥" to (Icons.Rounded.LocalFireDepartment to Color(0xFFFF6B2C)),
    "🎯" to (Icons.Rounded.TrackChanges to ObColor.Pink),
    "⏱️" to (Icons.Rounded.Timer to ObColor.Purple),
    "🎁" to (Icons.Rounded.CardGiftcard to ObColor.Pink),
    // Nơi tập / kiểu tập / cường độ
    "🛏️" to (Icons.Rounded.Bed to ObColor.Purple),
    "🧘" to (Icons.Rounded.SelfImprovement to Color(0xFF13A88C)),
    "🏋️" to (Icons.Rounded.AccessibilityNew to ObColor.Pink),
    "🤸" to (Icons.Rounded.SportsGymnastics to ObColor.Purple),
    "🛌" to (Icons.Rounded.Hotel to Color(0xFF13A88C)),
    "💧" to (Icons.Rounded.WaterDrop to Color(0xFF3D8BFD)),
    "🏆" to (Icons.Rounded.EmojiEvents to ObColor.Warning),
    // Chấn thương
    "👌" to (Icons.Rounded.CheckCircle to ObColor.Ok),
    "🦴" to (Icons.Rounded.Healing to ObColor.Purple),
    "🦵" to (Icons.AutoMirrored.Rounded.DirectionsWalk to ObColor.Purple),
    "🫀" to (Icons.Rounded.MonitorHeart to Color(0xFFE5484D)),
    "➕" to (Icons.Rounded.AddCircle to ObColor.TextMuted),
    // Lối sống
    "🖥️" to (Icons.Rounded.Computer to ObColor.Purple),
    "🏠" to (Icons.Rounded.Home to ObColor.Pink),
    "👣" to (Icons.AutoMirrored.Rounded.DirectionsWalk to ObColor.Ok),
    "🚶" to (Icons.AutoMirrored.Rounded.DirectionsRun to Color(0xFFFF6B2C)),
    "👭" to (Icons.Rounded.Groups to ObColor.Pink),
    // Sự kiện / động lực
    "👕" to (Icons.Rounded.Checkroom to ObColor.Purple),
    "👗" to (Icons.Rounded.Face3 to ObColor.Pink),
    "🏝️" to (Icons.Rounded.BeachAccess to Color(0xFF3D8BFD)),
    "📷" to (Icons.Rounded.PhotoCamera to ObColor.TextSecondary),
    "🎸" to (Icons.Rounded.MusicNote to ObColor.Purple),
    "💬" to (Icons.Rounded.ChatBubble to ObColor.TextMuted),
    "🌿" to (Icons.Rounded.Spa to ObColor.Ok),
    // Nút
    "✓" to (Icons.Rounded.Check to ObColor.Ok),
    "✕" to (Icons.Rounded.Close to ObColor.Pink),
)

/** Icon + màu cho emoji, null nếu chưa có trong bảng. */
fun obIconFor(emoji: String): Pair<ImageVector, Color>? = ICONS[emoji]

/**
 * Icon tròn nền nhạt cùng màu (mặc định 40dp). [emoji] không có trong bảng thì hiện emoji như cũ.
 * [plain] = true: chỉ icon, không có nền tròn.
 */
@Composable
fun ObIcon(
    emoji: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    plain: Boolean = false,
    tint: Color? = null,
) {
    val entry = obIconFor(emoji)
    if (entry == null) {
        Box(modifier.size(size), contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = (size.value * 0.6f).sp)
        }
        return
    }
    val (vector, color) = entry
    val iconColor = tint ?: color
    if (plain) {
        Icon(vector, contentDescription = null, tint = iconColor, modifier = modifier.size(size))
        return
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(iconColor.copy(alpha = 0.14f)),
    ) {
        Icon(vector, contentDescription = null, tint = iconColor, modifier = Modifier.size(size * 0.58f))
    }
}
