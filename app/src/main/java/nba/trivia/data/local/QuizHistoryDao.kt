package nba.trivia.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuizHistoryDao {
    @Query("SELECT * FROM quiz_attempts ORDER BY finishedAt DESC, id DESC")
    abstract fun observeAttempts(): Flow<List<AttemptEntity>>

    @Query("SELECT * FROM answers ORDER BY answeredAt, id")
    abstract fun observeAnswers(): Flow<List<AnswerEntity>>

    @Query("SELECT * FROM answers ORDER BY answeredAt, id")
    abstract suspend fun findAllAnswers(): List<AnswerEntity>

    @Insert
    abstract suspend fun insertAttempt(attempt: AttemptEntity): Long

    @Insert
    abstract suspend fun insertAnswers(answers: List<AnswerEntity>)

    @Transaction
    open suspend fun insertAttemptWithAnswers(attempt: AttemptEntity, answers: List<AnswerEntity>) {
        val attemptId = insertAttempt(attempt)
        insertAnswers(answers.map { it.copy(attemptId = attemptId) })
    }

    @Query("DELETE FROM quiz_attempts")
    abstract suspend fun deleteAllAttempts()
}
