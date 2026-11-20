package nba.trivia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nba.trivia.R

@Immutable
data class BannerColors(
    val unlocked: Color,
    val passed: Color,
    val mastered: Color,
    val onBanner: Color,
    val onMastered: Color,
    val lockedLine: Color,
)

@Immutable
data class AnswerColors(
    val open: Color,
    val openBorder: Color,
    val onOpen: Color,
    val onDimmed: Color,
    val correct: Color,
    val onCorrect: Color,
    val wrong: Color,
    val onWrong: Color,
)

private val LightBanners = BannerColors(
    unlocked = Color(0xFF172033),
    passed = Color(0xFF23408E),
    mastered = Color(0xFFE2A73A),
    onBanner = Color(0xFFF4F6FA),
    onMastered = Color(0xFF172033),
    lockedLine = Color(0xFF9AA2B3),
)

private val DarkBanners = BannerColors(
    unlocked = Color(0xFF26314D),
    passed = Color(0xFF3557B8),
    mastered = Color(0xFFF0BE5A),
    onBanner = Color(0xFFF4F6FA),
    onMastered = Color(0xFF172033),
    lockedLine = Color(0xFF5C6680),
)

private val LightAnswers = AnswerColors(
    open = Color.White,
    openBorder = Color(0xFFC5CAD5),
    onOpen = Color(0xFF172033),
    onDimmed = Color(0xFF8A92A5),
    correct = Color(0xFFCDEBD7),
    onCorrect = Color(0xFF0D3B22),
    wrong = Color(0xFFF6D3D6),
    onWrong = Color(0xFF5C0A14),
)

private val DarkAnswers = AnswerColors(
    open = Color(0xFF1B253C),
    openBorder = Color(0xFF3A4560),
    onOpen = Color(0xFFE6E9F0),
    onDimmed = Color(0xFF6A7490),
    correct = Color(0xFF1C4A31),
    onCorrect = Color(0xFFCDEBD7),
    wrong = Color(0xFF5C1820),
    onWrong = Color(0xFFF6D3D6),
)

val LocalBannerColors = staticCompositionLocalOf { LightBanners }
val LocalAnswerColors = staticCompositionLocalOf { LightAnswers }

private val LightColors = lightColorScheme(
    primary = Color(0xFFC62235),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDADB),
    onPrimaryContainer = Color(0xFF40000A),
    secondary = Color(0xFF23408E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE2F5),
    onSecondaryContainer = Color(0xFF0B1A45),
    tertiary = Color(0xFF8A5A00),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFBE3B5),
    onTertiaryContainer = Color(0xFF2B1A00),
    background = Color(0xFFEEF0F3),
    onBackground = Color(0xFF172033),
    surface = Color(0xFFEEF0F3),
    onSurface = Color(0xFF172033),
    surfaceVariant = Color(0xFFDDE1E8),
    onSurfaceVariant = Color(0xFF4A5368),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF6F7F9),
    surfaceContainer = Color(0xFFE6E9EE),
    surfaceContainerHigh = Color(0xFFE0E3E9),
    surfaceContainerHighest = Color(0xFFD9DDE4),
    outline = Color(0xFF7A8296),
    outlineVariant = Color(0xFFC5CAD5),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF6B78),
    onPrimary = Color(0xFF5A0012),
    primaryContainer = Color(0xFF8E1424),
    onPrimaryContainer = Color(0xFFFFDADB),
    secondary = Color(0xFF9DB4F2),
    onSecondary = Color(0xFF0B1A45),
    secondaryContainer = Color(0xFF26386E),
    onSecondaryContainer = Color(0xFFDCE2F5),
    tertiary = Color(0xFFF0BE5A),
    onTertiary = Color(0xFF2B1A00),
    tertiaryContainer = Color(0xFF5E4300),
    onTertiaryContainer = Color(0xFFFBE3B5),
    background = Color(0xFF111A2E),
    onBackground = Color(0xFFE6E9F0),
    surface = Color(0xFF111A2E),
    onSurface = Color(0xFFE6E9F0),
    surfaceVariant = Color(0xFF2E3850),
    onSurfaceVariant = Color(0xFFB8BFCF),
    surfaceContainerLowest = Color(0xFF0C1324),
    surfaceContainerLow = Color(0xFF162036),
    surfaceContainer = Color(0xFF1B253C),
    surfaceContainerHigh = Color(0xFF222D46),
    surfaceContainerHighest = Color(0xFF2A3550),
    outline = Color(0xFF8790A6),
    outlineVariant = Color(0xFF3A4560),
)

private val BigShoulders = FontFamily(
    Font(R.font.big_shoulders_display, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.big_shoulders_display, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    Font(R.font.big_shoulders_display, FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontVariation.weight(800))),
)

private const val DISPLAY_LINE_HEIGHT_RATIO = 1.05f

private fun display(size: Int, weight: FontWeight) = TextStyle(
    fontFamily = BigShoulders,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = (size * DISPLAY_LINE_HEIGHT_RATIO).sp,
)

private val TriviaTypography = Typography().run {
    copy(
        displayLarge = display(size = 96, weight = FontWeight.ExtraBold),
        displayMedium = display(size = 64, weight = FontWeight.ExtraBold),
        displaySmall = display(size = 44, weight = FontWeight.ExtraBold),
        headlineMedium = display(size = 32, weight = FontWeight.Bold),
        titleLarge = display(size = 30, weight = FontWeight.Bold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 30.sp),
    )
}

private val TriviaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
)

@Composable
fun TriviaTheme(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    CompositionLocalProvider(
        LocalBannerColors provides if (isDark) DarkBanners else LightBanners,
        LocalAnswerColors provides if (isDark) DarkAnswers else LightAnswers,
    ) {
        MaterialTheme(
            colorScheme = if (isDark) DarkColors else LightColors,
            typography = TriviaTypography,
            shapes = TriviaShapes,
            content = content,
        )
    }
}
