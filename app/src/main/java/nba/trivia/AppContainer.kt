package nba.trivia

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import nba.trivia.data.QuizRepository
import nba.trivia.data.local.RoomQuizRepository
import nba.trivia.data.local.TriviaDatabase

class AppContainer(context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val database = TriviaDatabase.create(context.applicationContext, applicationScope)

    val repository: QuizRepository = RoomQuizRepository(database.questionDao(), database.quizHistoryDao())
}
