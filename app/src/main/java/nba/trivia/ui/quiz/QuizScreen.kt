package nba.trivia.ui.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import nba.trivia.data.PASS_PERCENT
import nba.trivia.data.QuizMode
import nba.trivia.ui.components.AnswerOption
import nba.trivia.ui.theme.AnswerMark
import nba.trivia.ui.theme.LocalBannerColors
import kotlin.math.roundToInt
import nba.trivia.ui.components.ConfirmDialog
import nba.trivia.ui.components.EmptyState
import nba.trivia.ui.components.TriviaTopBar
import nba.trivia.ui.triviaViewModel

private const val COUNT_UP_MILLIS = 900

@Composable
fun QuizScreen(
    onExit: () -> Unit,
    viewModel: QuizViewModel = triviaViewModel { QuizViewModel(it, createSavedStateHandle()) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isConfirmingExit by rememberSaveable { mutableStateOf(false) }
    val requestExit = {
        if (viewModel.hasUnsavedAnswers) isConfirmingExit = true else onExit()
    }

    BackHandler(onBack = requestExit)

    Scaffold(
        topBar = {
            TriviaTopBar(title = state.mode.title) {
                IconButton(onClick = requestExit) { Icon(Icons.Filled.Close, contentDescription = "Leave quiz") }
            }
        },
    ) { padding ->
        val contentModifier = Modifier.padding(padding)
        when (val current = state) {
            is QuizUiState.Loading, is QuizUiState.Saving -> Box(
                contentModifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            is QuizUiState.NoQuestions -> EmptyState(
                icon = Icons.Filled.Info,
                title = "No questions to play",
                message = "Add some in the question bank first.",
                modifier = contentModifier,
            ) { Button(onClick = onExit) { Text("Back") } }
            is QuizUiState.InProgress -> QuestionStep(
                state = current,
                onOptionSelected = viewModel::onOptionSelected,
                onNext = viewModel::onNext,
                modifier = contentModifier,
            )
            is QuizUiState.Finished -> QuizSummary(current, onDone = onExit, modifier = contentModifier)
        }
    }

    if (isConfirmingExit) {
        ConfirmDialog(
            title = "Leave this quiz?",
            message = "Your answers in this round won't be saved and won't count toward unlocking eras.",
            confirmLabel = "Leave",
            onConfirm = {
                isConfirmingExit = false
                onExit()
            },
            onDismiss = { isConfirmingExit = false },
        )
    }
}

@Composable
private fun QuestionStep(
    state: QuizUiState.InProgress,
    onOptionSelected: (Int) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text("${state.number}/${state.total}", style = MaterialTheme.typography.titleLarge)
            Text("${state.score} correct", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        }
        LinearProgressIndicator(
            progress = { state.number.toFloat() / state.total },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(state.question.text, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 8.dp))
        state.question.options.forEachIndexed { index, option ->
            AnswerOption(text = option, mark = markFor(state, index), onClick = { onOptionSelected(index) })
        }
        if (state.isAnswered) {
            AnswerFeedback(state)
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.isLastQuestion) "See results" else "Next question")
            }
        }
    }
}

private fun markFor(state: QuizUiState.InProgress, index: Int): AnswerMark = when {
    !state.isAnswered -> AnswerMark.OPEN
    index == state.question.correctIndex -> AnswerMark.CORRECT
    index == state.selectedIndex -> AnswerMark.WRONG
    else -> AnswerMark.DIMMED
}

@Composable
private fun AnswerFeedback(state: QuizUiState.InProgress) {
    val isCorrect = state.selectedIndex?.let(state.question::isCorrect) == true
    Column(Modifier.padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            if (isCorrect) "Correct." else "It was ${state.question.correctAnswer}.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        if (state.question.explanation.isNotBlank()) {
            Text(
                state.question.explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun QuizSummary(state: QuizUiState.Finished, onDone: () -> Unit, modifier: Modifier = Modifier) {
    val shownScore = remember { Animatable(0f) }
    LaunchedEffect(state.correctCount) {
        shownScore.animateTo(state.correctCount.toFloat(), tween(COUNT_UP_MILLIS))
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("${shownScore.value.roundToInt()}", style = MaterialTheme.typography.displayLarge)
            Text(
                "of ${state.total}\n${state.percent}% correct",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 14.dp),
            )
        }
        if (state.milestones.isEmpty()) {
            Text(
                noMilestoneMessage(state),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        state.milestones.forEach { MilestoneRow(it) }
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Done") }
    }
}

@Composable
private fun MilestoneRow(milestone: Milestone) {
    val banners = LocalBannerColors.current
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (milestone) {
        is Milestone.EraMastered -> banners.mastered to banners.onMastered
        is Milestone.EraPassed -> banners.passed to banners.onBanner
        is Milestone.EraUnlocked -> banners.unlocked to banners.onBanner
        is Milestone.ReviewCleared -> colors.surfaceContainerHigh to colors.onSurface
    }
    Surface(color = container, contentColor = content, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
        Text(milestoneText(milestone), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
    }
}

private fun milestoneText(milestone: Milestone): String = when (milestone) {
    is Milestone.EraMastered -> "${milestone.era.title} mastered with a perfect round"
    is Milestone.EraPassed -> "${milestone.era.title} passed. Its banner is up."
    is Milestone.EraUnlocked -> "${milestone.era.label} is open"
    is Milestone.ReviewCleared ->
        "${milestone.count} question${if (milestone.count == 1) "" else "s"} cleared from your review pile"
}
private fun noMilestoneMessage(state: QuizUiState.Finished): String = when {
    state.mode == QuizMode.Review -> "Questions you missed stay in your review pile."
    state.percent >= PASS_PERCENT -> "Round saved."
    else -> "You need $PASS_PERCENT% to pass. Questions you missed are in your review pile."
}
