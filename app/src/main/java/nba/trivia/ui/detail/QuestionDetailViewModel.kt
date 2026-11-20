package nba.trivia.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nba.trivia.data.Question
import nba.trivia.data.QuestionStats
import nba.trivia.data.QuizRepository
import nba.trivia.data.statsFor
import nba.trivia.ui.WhileUiSubscribed
import nba.trivia.ui.navigation.QUESTION_ID_ARGUMENT

sealed interface QuestionDetailUiState {
    data object Loading : QuestionDetailUiState

    data object Missing : QuestionDetailUiState

    data class Loaded(val question: Question, val stats: QuestionStats) : QuestionDetailUiState
}

class QuestionDetailViewModel(
    private val repository: QuizRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val questionId: Long =
        checkNotNull(savedStateHandle.get<Long>(QUESTION_ID_ARGUMENT)) { "Missing $QUESTION_ID_ARGUMENT" }
    private val deletedEvents = Channel<String>(Channel.BUFFERED)

    val deleted: Flow<String> = deletedEvents.receiveAsFlow()

    val uiState: StateFlow<QuestionDetailUiState> =
        combine(repository.observeQuestion(questionId), repository.answers) { question, answers ->
            if (question == null) {
                QuestionDetailUiState.Missing
            } else {
                QuestionDetailUiState.Loaded(question, statsFor(question.id, answers))
            }
        }.stateIn(viewModelScope, WhileUiSubscribed, QuestionDetailUiState.Loading)

    fun onDeleteConfirmed() {
        viewModelScope.launch {
            repository.deleteQuestion(questionId)
            deletedEvents.send("Question deleted")
        }
    }
}
