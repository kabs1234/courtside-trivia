package nba.trivia.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nba.trivia.data.AnswerRecord
import nba.trivia.data.EraProgress
import nba.trivia.data.Question
import nba.trivia.data.QuizAttempt
import nba.trivia.data.QuizRepository
import nba.trivia.data.percentOf
import nba.trivia.ui.WhileUiSubscribed

private const val MOST_MISSED_LIMIT = 5

data class EraAccuracy(val progress: EraProgress, val answered: Int, val correct: Int) {
    val percent: Int get() = percentOf(correct, answered)
}

data class MissedQuestion(val question: Question, val misses: Int)

data class StatsUiState(
    val isLoading: Boolean = true,
    val totalAnswered: Int = 0,
    val totalCorrect: Int = 0,
    val eras: List<EraAccuracy> = emptyList(),
    val mostMissed: List<MissedQuestion> = emptyList(),
    val history: List<QuizAttempt> = emptyList(),
) {
    val accuracy: Int get() = percentOf(totalCorrect, totalAnswered)
    val hasHistory: Boolean get() = history.isNotEmpty()
}

class StatsViewModel(private val repository: QuizRepository) : ViewModel() {
    private val resetEvents = Channel<String>(Channel.BUFFERED)

    val reset: Flow<String> = resetEvents.receiveAsFlow()

    val uiState: StateFlow<StatsUiState> = combine(
        repository.questions,
        repository.attempts,
        repository.answers,
        repository.eraProgress,
        ::QuizHistory,
    ).map(::buildStats).stateIn(viewModelScope, WhileUiSubscribed, StatsUiState())

    fun onResetConfirmed() {
        viewModelScope.launch {
            repository.resetProgress()
            resetEvents.send("Progress reset – eras are locked again")
        }
    }
}

private data class QuizHistory(
    val questions: List<Question>,
    val attempts: List<QuizAttempt>,
    val answers: List<AnswerRecord>,
    val eraProgress: List<EraProgress>,
)

private fun buildStats(history: QuizHistory): StatsUiState {
    val (questions, attempts, answers, eraProgress) = history
    val questionsById = questions.associateBy { it.id }
    val answersByEra = answers.groupBy { questionsById[it.questionId]?.era }
    return StatsUiState(
        isLoading = false,
        totalAnswered = answers.size,
        totalCorrect = answers.count { it.isCorrect },
        eras = eraProgress.map { progress ->
            val eraAnswers = answersByEra[progress.era].orEmpty()
            EraAccuracy(progress, answered = eraAnswers.size, correct = eraAnswers.count { it.isCorrect })
        },
        mostMissed = answers
            .filterNot { it.isCorrect }
            .groupingBy { it.questionId }
            .eachCount()
            .mapNotNull { (id, misses) -> questionsById[id]?.let { MissedQuestion(it, misses) } }
            .sortedByDescending { it.misses }
            .take(MOST_MISSED_LIMIT),
        history = attempts,
    )
}
