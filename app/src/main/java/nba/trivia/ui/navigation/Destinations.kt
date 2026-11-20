package nba.trivia.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import nba.trivia.data.Era

const val QUESTION_ID_ARGUMENT = "questionId"
const val ERA_ARGUMENT = "era"

enum class TopLevelDestination(val route: String, val label: String, val icon: ImageVector) {
    BANK("bank", "Questions", Icons.AutoMirrored.Filled.List),
    PLAY("play", "Play", Icons.Filled.PlayArrow),
    STATS("stats", "Stats", Icons.Filled.Star),
}

object Routes {
    const val QUESTION = "question/{$QUESTION_ID_ARGUMENT}"
    const val EDITOR = "editor?$QUESTION_ID_ARGUMENT={$QUESTION_ID_ARGUMENT}"
    const val QUIZ = "quiz?$ERA_ARGUMENT={$ERA_ARGUMENT}"

    fun question(id: Long) = "question/$id"

    fun newQuestion() = "editor"

    fun editQuestion(id: Long) = "editor?$QUESTION_ID_ARGUMENT=$id"

    fun eraQuiz(era: Era) = "quiz?$ERA_ARGUMENT=${era.name}"

    fun reviewQuiz() = "quiz"
}
