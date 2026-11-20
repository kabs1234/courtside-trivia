package nba.trivia.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import nba.trivia.data.AnswerRecord
import nba.trivia.data.EraProgress
import nba.trivia.data.Question
import nba.trivia.data.QuizAttempt
import nba.trivia.data.QuizMode
import nba.trivia.data.QuizRepository
import nba.trivia.data.QuizResult
import nba.trivia.data.QUIZ_LENGTH
import nba.trivia.data.calculateEraProgress
import nba.trivia.data.questionIdsToReview

class RoomQuizRepository(
    private val questionDao: QuestionDao,
    private val historyDao: QuizHistoryDao,
) : QuizRepository {
    override val questions: Flow<List<Question>> =
        questionDao.observeAll().map { rows -> rows.map { it.toQuestion() } }

    override val attempts: Flow<List<QuizAttempt>> =
        historyDao.observeAttempts().map { rows -> rows.map { it.toAttempt() } }

    override val answers: Flow<List<AnswerRecord>> =
        historyDao.observeAnswers().map { rows -> rows.map { it.toRecord() } }

    override val eraProgress: Flow<List<EraProgress>> =
        combine(questions, attempts, ::calculateEraProgress)

    override fun observeQuestion(id: Long): Flow<Question?> = questionDao.observeById(id).map { it?.toQuestion() }

    override suspend fun findQuestion(id: Long): Question? = questionDao.findById(id)?.toQuestion()

    override suspend fun addQuestion(question: Question): Long = questionDao.insert(QuestionEntity.from(question))

    override suspend fun updateQuestion(question: Question) {
        questionDao.update(QuestionEntity.from(question))
    }

    override suspend fun deleteQuestion(id: Long) {
        questionDao.deleteById(id)
    }

    override suspend fun questionsFor(mode: QuizMode): List<Question> {
        val allQuestions = questionDao.findAll().map { it.toQuestion() }
        val candidates = when (mode) {
            is QuizMode.EraRound -> allQuestions.filter { it.era == mode.era }
            QuizMode.Review -> {
                val idsToReview = questionIdsToReview(historyDao.findAllAnswers().map { it.toRecord() })
                allQuestions.filter { it.id in idsToReview }
            }
        }
        return candidates.shuffled().take(QUIZ_LENGTH)
    }

    override suspend fun saveResult(result: QuizResult) {
        historyDao.insertAttemptWithAnswers(AttemptEntity.from(result), result.answers.map(AnswerEntity::from))
    }

    override suspend fun resetProgress() {
        historyDao.deleteAllAttempts()
    }
}
