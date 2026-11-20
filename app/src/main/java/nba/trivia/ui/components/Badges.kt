package nba.trivia.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nba.trivia.data.Difficulty
import nba.trivia.data.EraStatus
import nba.trivia.ui.theme.LocalBannerColors
import nba.trivia.ui.theme.TriviaStyles

private const val PERCENT_SCALE = 100f

@Composable
fun Tag(text: String, modifier: Modifier = Modifier, style: Style = Style) {
    val styleState = remember { MutableStyleState(null) }
    Box(modifier.styleable(styleState, TriviaStyles.tag, style)) {
        BasicText(text, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun EraStatusTag(status: EraStatus, modifier: Modifier = Modifier) {
    val banners = LocalBannerColors.current
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (status) {
        EraStatus.LOCKED -> colors.surfaceVariant to colors.onSurfaceVariant
        EraStatus.UNLOCKED -> banners.unlocked to banners.onBanner
        EraStatus.PASSED -> banners.passed to banners.onBanner
        EraStatus.MASTERED -> banners.mastered to banners.onMastered
    }
    Tag(status.label, modifier, tagColors(container, content))
}

@Composable
fun DifficultyTag(difficulty: Difficulty, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Tag(difficulty.label, modifier, tagColors(colors.surfaceVariant, colors.onSurfaceVariant))
}

@Composable
fun ReviewTag(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Tag("In review", modifier, tagColors(colors.primaryContainer, colors.onPrimaryContainer))
}

@Composable
fun PercentBar(label: String, percent: Int, caption: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(caption, style = MaterialTheme.typography.labelLarge)
        }
        LinearProgressIndicator(
            progress = { percent / PERCENT_SCALE },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun tagColors(container: Color, content: Color): Style = Style {
    background(container)
    contentColor(content)
}
