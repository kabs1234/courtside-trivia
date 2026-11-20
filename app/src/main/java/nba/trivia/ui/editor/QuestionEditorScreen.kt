package nba.trivia.ui.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import nba.trivia.data.Difficulty
import nba.trivia.data.Era
import nba.trivia.ui.components.TriviaTopBar
import nba.trivia.ui.triviaViewModel

private const val OPTION_LETTERS = "ABCD"

@Composable
fun QuestionEditorScreen(
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: QuestionEditorViewModel = triviaViewModel { QuestionEditorViewModel(it, createSavedStateHandle()) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.saved.collect(onSaved)
    }

    Scaffold(
        topBar = {
            TriviaTopBar(title = if (state.isEditing) "Edit question" else "New question", onBack = onBack) {
                TextButton(onClick = viewModel::onSave, enabled = !state.isSaving) { Text("Save") }
            }
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            QuestionFormFields(
                state = state,
                actions = viewModel,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun QuestionFormFields(
    state: QuestionEditorUiState,
    actions: QuestionFormActions,
    modifier: Modifier = Modifier,
) {
    val form = state.form
    val errors = if (state.showErrors) state.errors else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionLabel("Era")
        EraSelector(selected = form.era, onSelect = actions::onEraChange)

        OutlinedTextField(
            value = form.text,
            onValueChange = actions::onTextChange,
            label = { Text("Question") },
            isError = errors?.text != null,
            supportingText = { Text(errors?.text ?: "${form.text.trim().length}/$MAX_QUESTION_LENGTH") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )

        SectionLabel("Answers – tap the circle next to the correct one")
        form.options.forEachIndexed { index, option ->
            OptionField(
                letter = OPTION_LETTERS[index],
                value = option,
                isCorrect = form.correctIndex == index,
                error = errors?.options?.get(index),
                onValueChange = { actions.onOptionChange(index, it) },
                onMarkCorrect = { actions.onCorrectOptionSelected(index) },
            )
        }
        errors?.correctAnswer?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        SectionLabel("Difficulty")
        DifficultySelector(selected = form.difficulty, onSelect = actions::onDifficultyChange)

        OutlinedTextField(
            value = form.explanation,
            onValueChange = actions::onExplanationChange,
            label = { Text("Did you know? (optional)") },
            isError = errors?.explanation != null,
            supportingText = { Text(errors?.explanation ?: "${form.explanation.length}/$MAX_EXPLANATION_LENGTH") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )

        Button(onClick = actions::onSave, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.isEditing) "Save changes" else "Add question")
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun EraSelector(selected: Era, onSelect: (Era) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Era.entries.forEach { era ->
            FilterChip(
                selected = era == selected,
                onClick = { onSelect(era) },
                label = { Text(era.years) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DifficultySelector(selected: Difficulty, onSelect: (Difficulty) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        Difficulty.entries.forEachIndexed { index, difficulty ->
            SegmentedButton(
                selected = difficulty == selected,
                onClick = { onSelect(difficulty) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = Difficulty.entries.size),
            ) { Text(difficulty.label) }
        }
    }
}

@Composable
private fun OptionField(
    letter: Char,
    value: String,
    isCorrect: Boolean,
    error: String?,
    onValueChange: (String) -> Unit,
    onMarkCorrect: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = isCorrect, onClick = onMarkCorrect)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Option $letter") },
            singleLine = true,
            isError = error != null,
            supportingText = error?.let { message -> { Text(message) } },
            modifier = Modifier.weight(1f),
        )
    }
}
