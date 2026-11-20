package nba.trivia.ui.bank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import nba.trivia.data.Era
import nba.trivia.data.Question
import nba.trivia.data.QuizRepository
import nba.trivia.data.questionIdsToReview
import nba.trivia.ui.WhileUiSubscribed

data class QuestionRow(val question: Question, val isInReview: Boolean)

data class EraSection(val era: Era, val rows: List<QuestionRow>)

data class QuestionBankUiState(
    val isLoading: Boolean = true,
    val sections: List<EraSection> = emptyList(),
) {
    val questionCount: Int get() = sections.sumOf { it.rows.size }
}

class QuestionBankViewModel(repository: QuizRepository) : ViewModel() {
    val uiState: StateFlow<QuestionBankUiState> =
        combine(repository.questions, repository.answers) { questions, answers ->
            val idsToReview = questionIdsToReview(answers)
            QuestionBankUiState(
                isLoading = false,
                sections = questions
                    .groupBy { it.era }
                    .toSortedMap()
                    .map { (era, eraQuestions) ->
                        EraSection(era, eraQuestions.map { QuestionRow(it, it.id in idsToReview) })
                    },
            )
        }.stateIn(viewModelScope, WhileUiSubscribed, QuestionBankUiState())
}
