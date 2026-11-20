package nba.trivia.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.SharingStarted
import nba.trivia.data.QuizRepository

private const val STOP_TIMEOUT_MILLIS = 5_000L

val LocalQuizRepository = staticCompositionLocalOf<QuizRepository> { error("QuizRepository was not provided") }

val WhileUiSubscribed: SharingStarted = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS)

@Composable
inline fun <reified VM : ViewModel> triviaViewModel(crossinline create: CreationExtras.(QuizRepository) -> VM): VM {
    val repository = LocalQuizRepository.current
    return viewModel { create(repository) }
}
