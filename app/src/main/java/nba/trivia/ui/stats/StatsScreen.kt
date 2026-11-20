package nba.trivia.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.DateFormat
import java.util.Date
import nba.trivia.data.QuizAttempt
import nba.trivia.ui.components.ConfirmDialog
import nba.trivia.ui.components.EmptyState
import nba.trivia.ui.components.EraStatusTag
import nba.trivia.ui.components.PercentBar
import nba.trivia.ui.components.TriviaTopBar
import nba.trivia.ui.triviaViewModel

@Composable
fun StatsScreen(
    onQuestionClick: (Long) -> Unit,
    onMessage: (String) -> Unit,
    viewModel: StatsViewModel = triviaViewModel { StatsViewModel(it) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isConfirmingReset by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.reset.collect(onMessage)
    }

    Scaffold(
        topBar = {
            TriviaTopBar(title = "Stats") {
                if (state.hasHistory) {
                    IconButton(onClick = { isConfirmingReset = true }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reset progress")
                    }
                }
            }
        },
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            !state.hasHistory -> EmptyState(
                icon = Icons.Filled.Star,
                title = "No games played yet",
                message = "Finish a quiz on the Play tab to see your stats here.",
                modifier = Modifier.padding(padding),
            )
            else -> StatsContent(state, onQuestionClick, Modifier.padding(padding))
        }
    }

    if (isConfirmingReset) {
        ConfirmDialog(
            title = "Reset all progress?",
            message = "This deletes your quiz history, locks every era except the first and empties the review pile. " +
                "Your questions stay in the bank.",
            confirmLabel = "Reset",
            onConfirm = {
                isConfirmingReset = false
                viewModel.onResetConfirmed()
            },
            onDismiss = { isConfirmingReset = false },
        )
    }
}

@Composable
private fun StatsContent(state: StatsUiState, onQuestionClick: (Long) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "summary") { SummaryCard(state) }
        sectionTitle("Eras")
        items(state.eras, key = { "era-${it.progress.era.name}" }) { EraRow(it) }
        if (state.mostMissed.isNotEmpty()) {
            sectionTitle("Most missed")
            items(state.mostMissed, key = { "missed-${it.question.id}" }) { missed ->
                MissedRow(missed, onClick = { onQuestionClick(missed.question.id) })
            }
        }
        sectionTitle("History")
        items(state.history, key = { "attempt-${it.id}" }) { AttemptRow(it) }
    }
}

private fun LazyListScope.sectionTitle(title: String) {
    item(key = "title-$title") {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun SummaryCard(state: StatsUiState) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            SummaryNumber(value = "${state.history.size}", label = "Quizzes", modifier = Modifier.weight(1f))
            SummaryNumber(value = "${state.totalAnswered}", label = "Answers", modifier = Modifier.weight(1f))
            SummaryNumber(value = "${state.accuracy}%", label = "Accuracy", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryNumber(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.displaySmall)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun EraRow(accuracy: EraAccuracy) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    accuracy.progress.era.label,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                EraStatusTag(accuracy.progress.status)
            }
            PercentBar(
                label = accuracy.progress.bestPercent?.let { "Best round $it%" } ?: "Not played yet",
                percent = accuracy.percent,
                caption = "${accuracy.correct}/${accuracy.answered} correct",
            )
        }
    }
}

@Composable
private fun MissedRow(missed: MissedQuestion, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = { Text(missed.question.text, maxLines = 2, overflow = TextOverflow.Ellipsis) },
            supportingContent = { Text("Missed ${missed.misses} times in ${missed.question.era.title}") },
        )
    }
}

@Composable
private fun AttemptRow(attempt: QuizAttempt) {
    val dateFormat = remember { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT) }
    Card(Modifier.fillMaxWidth()) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = { Text(attempt.mode.title) },
            supportingContent = { Text(dateFormat.format(Date(attempt.finishedAt))) },
            trailingContent = {
                Column(horizontalAlignment = Alignment.End) {
                    Text("${attempt.correctCount}/${attempt.totalCount}", style = MaterialTheme.typography.titleMedium)
                    Text("${attempt.percent}%", style = MaterialTheme.typography.bodySmall)
                }
            },
        )
    }
}
