package nba.trivia.ui.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import nba.trivia.data.EraProgress
import nba.trivia.data.QuizRepository
import nba.trivia.data.questionIdsToReview
import nba.trivia.ui.WhileUiSubscribed

data class PlayUiState(
    val isLoading: Boolean = true,
    val eras: List<EraProgress> = emptyList(),
    val reviewCount: Int = 0,
) {
    val clearedCount: Int get() = eras.count { it.status.isCleared }
}

class PlayViewModel(repository: QuizRepository) : ViewModel() {
    val uiState: StateFlow<PlayUiState> =
        combine(repository.eraProgress, repository.questions, repository.answers) { eras, questions, answers ->
            val existingIds = questions.mapTo(HashSet()) { it.id }
            PlayUiState(
                isLoading = false,
                eras = eras,
                reviewCount = questionIdsToReview(answers).count { it in existingIds },
            )
        }.stateIn(viewModelScope, WhileUiSubscribed, PlayUiState())
}
