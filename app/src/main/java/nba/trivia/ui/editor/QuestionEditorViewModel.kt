package nba.trivia.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nba.trivia.data.Difficulty
import nba.trivia.data.Era
import nba.trivia.data.NEW_QUESTION_ID
import nba.trivia.data.QuizRepository
import nba.trivia.ui.navigation.QUESTION_ID_ARGUMENT

data class QuestionEditorUiState(
    val form: QuestionForm = QuestionForm(),
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val showErrors: Boolean = false,
) {
    val errors: QuestionFormErrors get() = form.validate()
}

interface QuestionFormActions {
    fun onEraChange(era: Era)

    fun onTextChange(text: String)

    fun onOptionChange(index: Int, value: String)

    fun onCorrectOptionSelected(index: Int)

    fun onDifficultyChange(difficulty: Difficulty)

    fun onExplanationChange(explanation: String)

    fun onSave()
}

class QuestionEditorViewModel(
    private val repository: QuizRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), QuestionFormActions {
    private val questionId: Long = savedStateHandle.get<Long>(QUESTION_ID_ARGUMENT) ?: NEW_QUESTION_ID
    private val isEditing = questionId != NEW_QUESTION_ID
    private val _uiState = MutableStateFlow(QuestionEditorUiState(isEditing = isEditing, isLoading = isEditing))
    private val savedEvents = Channel<String>(Channel.BUFFERED)

    val uiState: StateFlow<QuestionEditorUiState> = _uiState.asStateFlow()
    val saved: Flow<String> = savedEvents.receiveAsFlow()

    init {
        if (isEditing) loadQuestion()
    }

    override fun onEraChange(era: Era) = updateForm { it.copy(era = era) }

    override fun onTextChange(text: String) = updateForm { it.copy(text = text) }

    override fun onOptionChange(index: Int, value: String) = updateForm { it.withOption(index, value) }

    override fun onCorrectOptionSelected(index: Int) = updateForm { it.copy(correctIndex = index) }

    override fun onDifficultyChange(difficulty: Difficulty) = updateForm { it.copy(difficulty = difficulty) }

    override fun onExplanationChange(explanation: String) = updateForm { it.copy(explanation = explanation) }

    override fun onSave() {
        val current = _uiState.value
        if (current.isSaving || current.isLoading) return
        if (!current.errors.isValid) {
            _uiState.update { it.copy(showErrors = true) }
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val question = current.form.toQuestion(questionId)
            if (isEditing) {
                repository.updateQuestion(question)
                savedEvents.send("Question updated")
            } else {
                repository.addQuestion(question)
                savedEvents.send("Question added to ${question.era.title}")
            }
        }
    }

    private fun loadQuestion() {
        viewModelScope.launch {
            val question = repository.findQuestion(questionId)
            _uiState.update { state ->
                state.copy(form = question?.let(QuestionForm::from) ?: state.form, isLoading = false)
            }
        }
    }

    private fun updateForm(change: (QuestionForm) -> QuestionForm) {
        _uiState.update { it.copy(form = change(it.form)) }
    }
}
