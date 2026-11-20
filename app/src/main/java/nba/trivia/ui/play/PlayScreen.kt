package nba.trivia.ui.play

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nba.trivia.data.Era
import nba.trivia.data.EraProgress
import nba.trivia.data.EraStatus
import nba.trivia.data.PASS_PERCENT
import nba.trivia.ui.components.BannerAction
import nba.trivia.ui.components.EraBanner
import nba.trivia.ui.components.TriviaTopBar
import nba.trivia.ui.triviaViewModel

private const val GRID_COLUMNS = 2

@Composable
fun PlayScreen(
    onStartEra: (Era) -> Unit,
    onStartReview: () -> Unit,
    viewModel: PlayViewModel = triviaViewModel { PlayViewModel(it) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { TriviaTopBar(title = "Play") }) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            BannerGrid(state, onStartEra, onStartReview, Modifier.padding(padding))
        }
    }
}

@Composable
private fun BannerGrid(
    state: PlayUiState,
    onStartEra: (Era) -> Unit,
    onStartReview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val nextEra = state.eras.firstOrNull { it.status == EraStatus.UNLOCKED && it.isPlayable }?.era
    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMNS),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "intro", span = { GridItemSpan(maxLineSpan) }) {
            Text(
                "Score $PASS_PERCENT% in an era to hang its banner and open the next one. " +
                    "${state.clearedCount} of ${state.eras.size} banners up.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item(key = "review", span = { GridItemSpan(maxLineSpan) }) {
            ReviewStrip(state.reviewCount, onStartReview)
        }
        items(
            state.eras.sortedByDescending { it.era == nextEra },
            key = { it.era.name },
            span = { GridItemSpan(if (it.era == nextEra) maxLineSpan else 1) },
        ) { progress ->
            EraBanner(
                status = progress.status,
                action = if (progress.isPlayable) BannerAction("Play ${progress.era.title}") { onStartEra(progress.era) } else null,
            ) {
                if (progress.era == nextEra) NextBannerContent(progress) else BannerContent(progress)
            }
        }
    }
}

@Composable
private fun BannerContent(progress: EraProgress) {
    BasicText(progress.era.startYear, style = MaterialTheme.typography.displaySmall)
    BannerDetails(progress)
}

@Composable
private fun NextBannerContent(progress: EraProgress) {
    BasicText("Up next", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 4.dp))
    BasicText(progress.era.startYear, style = MaterialTheme.typography.displayMedium)
    BannerDetails(progress)
    BasicText(
        "Play this round",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp),
    )
}

@Composable
private fun BannerDetails(progress: EraProgress) {
    val type = MaterialTheme.typography
    BasicText(progress.era.title, style = type.titleMedium, modifier = Modifier.padding(top = 4.dp))
    BasicText(bannerCaption(progress), style = type.bodySmall, modifier = Modifier.padding(top = 6.dp))
}

@Composable
private fun ReviewStrip(reviewCount: Int, onStart: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.outlineVariant, MaterialTheme.shapes.medium)
            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
            Text("Review pile", style = MaterialTheme.typography.titleMedium)
            Text(
                when (reviewCount) {
                    0 -> "Empty. Questions you miss land here."
                    1 -> "1 missed question"
                    else -> "$reviewCount missed questions"
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
        TextButton(onClick = onStart, enabled = reviewCount > 0) { Text("Review") }
    }
}

private fun bannerCaption(progress: EraProgress): String = when {
    progress.status == EraStatus.LOCKED -> "Pass ${progress.era.previous?.title} first"
    progress.questionCount == 0 -> "No questions yet"
    progress.status == EraStatus.MASTERED -> "Mastered with a perfect round"
    progress.status == EraStatus.PASSED -> "Passed, best ${progress.bestPercent}%"
    progress.bestPercent != null -> "Best ${progress.bestPercent}%, needs $PASS_PERCENT%"
    else -> "${progress.questionCount} questions"
}
