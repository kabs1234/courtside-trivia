package nba.trivia.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import nba.trivia.data.CORRECT_ANSWERS_TO_CLEAR_REVIEW
import nba.trivia.data.Question
import nba.trivia.data.QuestionStats
import nba.trivia.data.percentOf
import nba.trivia.ui.components.ConfirmDialog
import nba.trivia.ui.components.DifficultyTag
import nba.trivia.ui.components.EmptyState
import nba.trivia.ui.components.ReviewTag
import nba.trivia.ui.components.TriviaTopBar
import nba.trivia.ui.triviaViewModel

@Composable
fun QuestionDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: (String) -> Unit,
    viewModel: QuestionDetailViewModel = triviaViewModel { QuestionDetailViewModel(it, createSavedStateHandle()) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isConfirmingDelete by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.deleted.collect(onDeleted)
    }

    val loaded = state as? QuestionDetailUiState.Loaded
    Scaffold(
        topBar = {
            TriviaTopBar(title = "Question", onBack = onBack) {
                if (loaded != null) {
                    IconButton(onClick = { onEdit(loaded.question.id) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit question")
                    }
                    IconButton(onClick = { isConfirmingDelete = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete question")
                    }
                }
            }
        },
    ) { padding ->
        when (val current = state) {
            QuestionDetailUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            QuestionDetailUiState.Missing -> EmptyState(
                icon = Icons.Filled.Warning,
                title = "Question not found",
                message = "This question was deleted.",
                modifier = Modifier.padding(padding),
            )
            is QuestionDetailUiState.Loaded -> QuestionDetails(
                question = current.question,
                stats = current.stats,
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (isConfirmingDelete) {
        ConfirmDialog(
            title = "Delete this question?",
            message = "Its answer history is deleted too. This can't be undone.",
            confirmLabel = "Delete",
            onConfirm = {
                isConfirmingDelete = false
                viewModel.onDeleteConfirmed()
            },
            onDismiss = { isConfirmingDelete = false },
        )
    }
}

@Composable
private fun QuestionDetails(question: Question, stats: QuestionStats, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(question.era.label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            DifficultyTag(question.difficulty)
        }
        Text(question.text, style = MaterialTheme.typography.headlineSmall)
        question.options.forEachIndexed { index, option ->
            OptionRow(option, isCorrect = index == question.correctIndex)
        }
        if (question.explanation.isNotBlank()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Did you know?", style = MaterialTheme.typography.titleSmall)
                    Text(question.explanation, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        StatsCard(stats)
    }
}

@Composable
private fun OptionRow(option: String, isCorrect: Boolean) {
    val colors = if (isCorrect) {
        CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    } else {
        CardDefaults.outlinedCardColors()
    }
    OutlinedCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(option, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            if (isCorrect) Icon(Icons.Filled.CheckCircle, contentDescription = "Correct answer")
        }
    }
}

@Composable
private fun StatsCard(stats: QuestionStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Your record", style = MaterialTheme.typography.titleSmall)
            if (stats.timesAnswered == 0) {
                Text("You haven't answered this question in a quiz yet.")
            } else {
                Text(
                    "Answered ${stats.timesAnswered} times, correct ${stats.timesCorrect} " +
                        "(${percentOf(stats.timesCorrect, stats.timesAnswered)}%)",
                )
            }
            if (stats.isInReview) {
                ReviewTag()
                Text(
                    "You missed this one. Answer it correctly $CORRECT_ANSWERS_TO_CLEAR_REVIEW times in a row " +
                        "to clear it from your review pile.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
