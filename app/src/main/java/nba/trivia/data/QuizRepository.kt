package nba.trivia.data

import kotlinx.coroutines.flow.Flow

interface QuizRepository {
    val questions: Flow<List<Question>>
    val attempts: Flow<List<QuizAttempt>>
    val answers: Flow<List<AnswerRecord>>
    val eraProgress: Flow<List<EraProgress>>

    fun observeQuestion(id: Long): Flow<Question?>

    suspend fun findQuestion(id: Long): Question?

    suspend fun addQuestion(question: Question): Long

    suspend fun updateQuestion(question: Question)

    suspend fun deleteQuestion(id: Long)

    suspend fun questionsFor(mode: QuizMode): List<Question>

    suspend fun saveResult(result: QuizResult)

    suspend fun resetProgress()
}
