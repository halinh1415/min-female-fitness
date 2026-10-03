package mint.woman.female.workout.custom.weight

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class WeightEntryColors(
    val surface: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val title: Color,
    val body: Color,
    val muted: Color,
    val border: Color,
    val good: Color,
    val warning: Color,
    val bad: Color,
)

private val LightColors = WeightEntryColors(
    surface = Color.White,
    primary = Color(0xFFE91E63),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE8F0),
    title = Color(0xFF24191D),
    body = Color(0xFF51444A),
    muted = Color(0xFF85747B),
    border = Color(0xFFF0D8E1),
    good = Color(0xFF21865A),
    warning = Color(0xFFC27A10),
    bad = Color(0xFFC0392B),
)

private val DarkColors = WeightEntryColors(
    surface = Color(0xFF241B1F),
    primary = Color(0xFFFF8DB4),
    onPrimary = Color(0xFF4E0A24),
    primaryContainer = Color(0xFF432631),
    title = Color(0xFFFFF1F5),
    body = Color(0xFFE7CDD6),
    muted = Color(0xFFB99EA8),
    border = Color(0xFF543B45),
    good = Color(0xFF7FD7AB),
    warning = Color(0xFFF1BC64),
    bad = Color(0xFFFF9B91),
)

private val LocalWeightEntryColors = staticCompositionLocalOf { LightColors }

object WeightEntryThemeValues {
    val colors: WeightEntryColors
        @Composable get() = LocalWeightEntryColors.current
}

@Composable
fun WeightEntryTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    val scheme = (if (darkTheme) darkColorScheme() else lightColorScheme()).copy(
        primary = colors.primary,
        onPrimary = colors.onPrimary,
        primaryContainer = colors.primaryContainer,
        surface = colors.surface,
        onSurface = colors.title,
        onSurfaceVariant = colors.muted,
        outline = colors.border,
        error = colors.bad,
    )
    androidx.compose.runtime.CompositionLocalProvider(LocalWeightEntryColors provides colors) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
