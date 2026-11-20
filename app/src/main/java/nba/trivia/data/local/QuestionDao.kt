package nba.trivia.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY id")
    fun observeAll(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :id")
    fun observeById(id: Long): Flow<QuestionEntity?>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun findById(id: Long): QuestionEntity?

    @Query("SELECT * FROM questions")
    suspend fun findAll(): List<QuestionEntity>

    @Insert
    suspend fun insert(question: QuestionEntity): Long

    @Insert
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Update
    suspend fun update(question: QuestionEntity)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
