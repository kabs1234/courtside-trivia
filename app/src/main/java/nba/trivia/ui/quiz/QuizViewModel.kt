package nba.trivia.ui.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nba.trivia.data.Era
import nba.trivia.data.EraProgress
import nba.trivia.data.EraStatus
import nba.trivia.data.GivenAnswer
import nba.trivia.data.Question
import nba.trivia.data.QuizMode
import nba.trivia.data.QuizRepository
import nba.trivia.data.QuizResult
import nba.trivia.data.percentOf
import nba.trivia.data.questionIdsToReview
import nba.trivia.ui.navigation.ERA_ARGUMENT

sealed interface Milestone {
    data class EraPassed(val era: Era) : Milestone

    data class EraMastered(val era: Era) : Milestone

    data class EraUnlocked(val era: Era) : Milestone

    data class ReviewCleared(val count: Int) : Milestone
}

sealed interface QuizUiState {
    val mode: QuizMode

    data class Loading(override val mode: QuizMode) : QuizUiState

    data class NoQuestions(override val mode: QuizMode) : QuizUiState

    data class InProgress(
        override val mode: QuizMode,
        val question: Question,
        val number: Int,
        val total: Int,
        val score: Int,
        val selectedIndex: Int?,
    ) : QuizUiState {
        val isAnswered: Boolean get() = selectedIndex != null
        val isLastQuestion: Boolean get() = number == total
    }

    data class Saving(override val mode: QuizMode) : QuizUiState

    data class Finished(
        override val mode: QuizMode,
        val correctCount: Int,
        val total: Int,
        val milestones: List<Milestone>,
    ) : QuizUiState {
        val percent: Int get() = percentOf(correctCount, total)
    }
}

class QuizViewModel(
    private val repository: QuizRepository,
    savedStateHandle: SavedStateHandle,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private val mode: QuizMode = savedStateHandle.get<String>(ERA_ARGUMENT)
        ?.let { name -> Era.entries.find { it.name == name } }
        ?.let(QuizMode::EraRound)
        ?: QuizMode.Review
    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading(mode))
    private var questions: List<Question> = emptyList()
    private var answers: List<GivenAnswer> = emptyList()
    private var progressBefore: List<EraProgress> = emptyList()
    private var reviewCountBefore = 0

    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    val hasUnsavedAnswers: Boolean get() = _uiState.value is QuizUiState.InProgress && answers.isNotEmpty()

    init {
        viewModelScope.launch {
            progressBefore = repository.eraProgress.first()
            reviewCountBefore = reviewCount()
            questions = repository.questionsFor(mode)
            _uiState.value = if (questions.isEmpty()) QuizUiState.NoQuestions(mode) else questionState(0)
        }
    }

    fun onOptionSelected(index: Int) {
        val current = _uiState.value as? QuizUiState.InProgress ?: return
        if (current.isAnswered) return
        val answer = GivenAnswer(
            questionId = current.question.id,
            selectedIndex = index,
            isCorrect = current.question.isCorrect(index),
            answeredAt = clock(),
        )
        answers = answers + answer
        _uiState.value = current.copy(selectedIndex = index, score = answers.count { it.isCorrect })
    }

    fun onNext() {
        val current = _uiState.value as? QuizUiState.InProgress ?: return
        if (!current.isAnswered) return
        if (current.isLastQuestion) finish() else _uiState.value = questionState(current.number)
    }

    private fun questionState(index: Int) = QuizUiState.InProgress(
        mode = mode,
        question = questions[index],
        number = index + 1,
        total = questions.size,
        score = answers.count { it.isCorrect },
        selectedIndex = null,
    )

    private fun finish() {
        _uiState.value = QuizUiState.Saving(mode)
        viewModelScope.launch {
            val result = QuizResult(mode, answers, finishedAt = clock())
            repository.saveResult(result)
            val progressAfter = repository.eraProgress.first()
            val reviewCleared = (reviewCountBefore - reviewCount()).coerceAtLeast(0)
            _uiState.value = QuizUiState.Finished(
                mode = mode,
                correctCount = result.correctCount,
                total = answers.size,
                milestones = milestonesBetween(progressBefore, progressAfter) + reviewMilestone(reviewCleared),
            )
        }
    }

    private suspend fun reviewCount(): Int = questionIdsToReview(repository.answers.first()).size

    private fun reviewMilestone(cleared: Int): List<Milestone> =
        if (mode == QuizMode.Review && cleared > 0) listOf(Milestone.ReviewCleared(cleared)) else emptyList()
}

fun milestonesBetween(before: List<EraProgress>, after: List<EraProgress>): List<Milestone> {
    val statusBefore = before.associate { it.era to it.status }
    return after.mapNotNull { progress ->
        val previous = statusBefore[progress.era] ?: EraStatus.LOCKED
        when {
            progress.status == previous -> null
            progress.status == EraStatus.MASTERED -> Milestone.EraMastered(progress.era)
            progress.status == EraStatus.PASSED && !previous.isCleared -> Milestone.EraPassed(progress.era)
            previous == EraStatus.LOCKED -> Milestone.EraUnlocked(progress.era)
            else -> null
        }
    }
}
