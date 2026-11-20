package nba.trivia.ui.bank

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nba.trivia.data.Era
import nba.trivia.ui.components.DifficultyTag
import nba.trivia.ui.components.EmptyState
import nba.trivia.ui.components.ReviewTag
import nba.trivia.ui.components.TriviaTopBar
import nba.trivia.ui.triviaViewModel

private val FAB_CLEARANCE = 88.dp

@Composable
fun QuestionBankScreen(
    onQuestionClick: (Long) -> Unit,
    onAddQuestion: () -> Unit,
    viewModel: QuestionBankViewModel = triviaViewModel { QuestionBankViewModel(it) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TriviaTopBar(title = "Questions") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddQuestion,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New question") },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.sections.isEmpty() -> EmptyState(
                icon = Icons.Filled.Info,
                title = "No questions yet",
                message = "Tap \"New question\" to write the first one.",
                modifier = Modifier.padding(padding),
            )
            else -> QuestionList(state.sections, onQuestionClick, Modifier.padding(padding))
        }
    }
}

@Composable
private fun QuestionList(sections: List<EraSection>, onQuestionClick: (Long) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = FAB_CLEARANCE),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        sections.forEach { section ->
            item(key = "era-${section.era.name}", contentType = "header") {
                EraHeader(section.era, section.rows.size)
            }
            items(section.rows, key = { it.question.id }, contentType = { "question" }) { row ->
                QuestionRow(row, onClick = { onQuestionClick(row.question.id) })
            }
        }
    }
}

@Composable
private fun EraHeader(era: Era, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 4.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(era.startYear, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f).padding(bottom = 4.dp)) {
            Text(era.title, style = MaterialTheme.typography.titleMedium)
            Text("$count questions", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun QuestionRow(row: QuestionRow, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(row.question.text, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DifficultyTag(row.question.difficulty)
            if (row.isInReview) ReviewTag()
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}