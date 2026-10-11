package mint.woman.female.workout.activity.ui.compose

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import mint.woman.female.workout.activity.ui.onboarding.onboard.ObFontFamily
import mint.woman.female.workout.model.ProgramObject
import mint.woman.female.workout.retrofit.ApiUtils

/** Một bộ màu "Rose" (sáng hoặc tối). */
@Immutable
class MinPalette(
    val pink: Color,
    val pinkLight: Color,
    val pinkSoft: Color,
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val track: Color,
    val purple: Color,
    val plum: Color,
    val ok: Color,
    val warning: Color,
    val danger: Color,
    val isDark: Boolean,
)

val LightMinPalette = MinPalette(
    pink = Color(0xFFE91E63),
    pinkLight = Color(0xFFFF5C8A),
    pinkSoft = Color(0xFFFDE7EF),
    background = Color(0xFFFFF7FA),
    surface = Color.White,
    textPrimary = Color(0xFF1F1A1D),
    textSecondary = Color(0xFF6B5E66),
    textMuted = Color(0xFFA0939B),
    track = Color(0xFFF1E4EA),
    purple = Color(0xFF8E5CF7),
    plum = Color(0xFFC2459F),
    ok = Color(0xFF2EAD6B),
    warning = Color(0xFFFF9F43),
    danger = Color(0xFFE5484D),
    isDark = false,
)

/** Nền gần đen ánh hồng, thẻ xám hồng đậm, chữ sáng; màu nhấn giữ hồng nhưng sáng hơn chút để đủ tương phản. */
val DarkMinPalette = MinPalette(
    pink = Color(0xFFF0457E),
    pinkLight = Color(0xFFFF6E98),
    pinkSoft = Color(0xFF3B2330),
    background = Color(0xFF151013),
    surface = Color(0xFF221B1F),
    textPrimary = Color(0xFFF4ECEF),
    textSecondary = Color(0xFFC9BAC2),
    textMuted = Color(0xFF94868D),
    track = Color(0xFF3A2E34),
    purple = Color(0xFFA680FA),
    plum = Color(0xFFD66BB8),
    ok = Color(0xFF43C383),
    warning = Color(0xFFFFB066),
    danger = Color(0xFFFF6B6F),
    isDark = true,
)

/**
 * Bảng màu "Rose" dùng chung cho các màn Compose (Home, Report...). Đọc theo bộ màu hiện hành
 * ([MinTheme] chọn sáng / tối theo cài đặt giao diện của app), nên dùng được cả trong DrawScope.
 */
object MinColor {
    internal var palette by mutableStateOf(LightMinPalette)

    val isDark get() = palette.isDark
    val Pink get() = palette.pink
    val PinkLight get() = palette.pinkLight
    val PinkSoft get() = palette.pinkSoft
    val Background get() = palette.background
    val Surface get() = palette.surface
    val TextPrimary get() = palette.textPrimary
    val TextSecondary get() = palette.textSecondary
    val TextMuted get() = palette.textMuted
    val Track get() = palette.track
    val Purple get() = palette.purple
    val Plum get() = palette.plum
    val Ok get() = palette.ok
    val Warning get() = palette.warning
    val Danger get() = palette.danger

    val CtaBrush get() = Brush.horizontalGradient(listOf(PinkLight, Pink))
    val HeroBrush get() = Brush.verticalGradient(listOf(Pink, PinkLight))
}

/**
 * Theme Compose của Min. [darkTheme] mặc định theo cấu hình (Cài đặt → Giao diện: sáng / tối / theo
 * hệ thống, áp qua AppCompatDelegate nên isSystemInDarkTheme() phản ánh đúng lựa chọn đó).
 */
@Composable
fun MinTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val palette = if (darkTheme) DarkMinPalette else LightMinPalette
    // Gán trước khi vẽ nội dung để khung hình đầu đã đúng màu (giá trị không đổi thì không recompose).
    if (MinColor.palette !== palette) MinColor.palette = palette
    MaterialTheme(
        colorScheme = if (darkTheme) {
            darkColorScheme(primary = palette.pink, background = palette.background, surface = palette.surface,
                onSurface = palette.textPrimary, onBackground = palette.textPrimary)
        } else {
            lightColorScheme(primary = palette.pink, background = palette.background, surface = palette.surface,
                onSurface = palette.textPrimary, onBackground = palette.textPrimary)
        },
    ) {
        // Màu chữ mặc định qua LocalContentColor (không đặt trong TextStyle) để Button,
        // DropdownMenu... vẫn tự đổi màu chữ theo nền của chúng.
        CompositionLocalProvider(LocalContentColor provides MinColor.TextPrimary) {
            ProvideTextStyle(LocalTextStyle.current.copy(fontFamily = ObFontFamily)) {
                content()
            }
        }
    }
}

/** Thẻ trắng bo góc có bóng nhẹ. */
@Composable
fun MinCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .shadow(6.dp, RoundedCornerShape(22.dp), ambientColor = MinColor.Pink.copy(alpha = 0.15f), spotColor = MinColor.Pink.copy(alpha = 0.15f))
            .clip(RoundedCornerShape(22.dp))
            .background(MinColor.Surface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding),
        content = content
    )
}

/** Tiêu đề mục + nút phụ bên phải (vd "Xem thêm"). */
@Composable
fun MinSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
    actionSuffix: String = " ›"
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MinColor.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (action != null && onAction != null) {
            Text(
                action + actionSuffix,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MinColor.Pink,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

/** Tiêu đề lớn đầu màn (thay cho toolbar), phần bên phải tuỳ ý. */
@Composable
fun MinLargeHeader(
    title: String,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(start = horizontalPadding, end = horizontalPadding, top = 10.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MinColor.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        trailing()
    }
}

/** Nút CTA gradient hồng. */
@Composable
fun MinButton(text: String, modifier: Modifier = Modifier, height: Dp = 50.dp, onClick: () -> Unit) {
    Box(
        modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(MinColor.CtaBrush)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

/** Thanh tiến độ bo tròn. */
@Composable
fun MinProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    track: Color = MinColor.Track,
    brush: Brush = MinColor.CtaBrush
) {
    Box(
        modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(track)
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(brush)
        )
    }
}

/**
 * Ảnh chương trình (asset `file:///android_asset/...` hoặc URL). Dùng Coil – ảnh Compose gốc, có
 * cache bộ nhớ – thay cho ImageView + Glide bọc AndroidView vốn làm giật khi cuộn LazyColumn/LazyRow.
 * Giữ tên/tham số cũ để các màn không phải sửa.
 */
@Composable
fun GlideImage(
    url: String,
    modifier: Modifier = Modifier,
    scaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER_CROP,
    errorUrl: String? = null
) {
    var failed by remember(url) { mutableStateOf(false) }
    val (contentScale, alignment) = when (scaleType) {
        ImageView.ScaleType.FIT_END -> ContentScale.Fit to Alignment.BottomEnd
        ImageView.ScaleType.FIT_START -> ContentScale.Fit to Alignment.TopStart
        ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.CENTER_INSIDE -> ContentScale.Fit to Alignment.Center
        else -> ContentScale.Crop to Alignment.Center
    }
    AsyncImage(
        model = if (failed && errorUrl != null) errorUrl else url,
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale,
        alignment = alignment,
        onError = { if (errorUrl != null) failed = true }
    )
}

/** Đường dẫn ảnh của chương trình: ảnh có sẵn trong assets, bài "discover" lấy từ server. */
fun ProgramObject.imageBase(): String =
    if (discover > 0) ApiUtils.BASE_URL_IMAGES else "file:///android_asset/images/"

fun ProgramObject.backgroundUrl(): String = imageBase() + background
fun ProgramObject.modelUrl(): String = imageBase() + image
fun ProgramObject.fallbackBackgroundUrl(): String = imageBase() + "bg_1.png"
