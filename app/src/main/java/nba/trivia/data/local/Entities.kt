package nba.trivia.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import nba.trivia.data.AnswerRecord
import nba.trivia.data.Difficulty
import nba.trivia.data.Era
import nba.trivia.data.GivenAnswer
import nba.trivia.data.Question
import nba.trivia.data.QuizAttempt
import nba.trivia.data.QuizMode
import nba.trivia.data.QuizResult

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val era: Era,
    val text: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctIndex: Int,
    val difficulty: Difficulty,
    val explanation: String,
) {
    fun toQuestion() = Question(
        id = id,
        era = era,
        text = text,
        options = listOf(optionA, optionB, optionC, optionD),
        correctIndex = correctIndex,
        difficulty = difficulty,
        explanation = explanation,
    )

    companion object {
        fun from(question: Question) = QuestionEntity(
            id = question.id,
            era = question.era,
            text = question.text,
            optionA = question.options[0],
            optionB = question.options[1],
            optionC = question.options[2],
            optionD = question.options[3],
            correctIndex = question.correctIndex,
            difficulty = question.difficulty,
            explanation = question.explanation,
        )
    }
}

enum class QuizKind { ERA, REVIEW }

@Entity(tableName = "quiz_attempts")
data class AttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: QuizKind,
    val era: Era?,
    val correctCount: Int,
    val totalCount: Int,
    val finishedAt: Long,
) {
    fun toAttempt() = QuizAttempt(
        id = id,
        mode = toMode(),
        correctCount = correctCount,
        totalCount = totalCount,
        finishedAt = finishedAt,
    )

    private fun toMode(): QuizMode = when (kind) {
        QuizKind.REVIEW -> QuizMode.Review
        QuizKind.ERA -> QuizMode.EraRound(checkNotNull(era) { "Era attempt $id has no era" })
    }

    companion object {
        fun from(result: QuizResult) = AttemptEntity(
            kind = when (result.mode) {
                is QuizMode.EraRound -> QuizKind.ERA
                QuizMode.Review -> QuizKind.REVIEW
            },
            era = (result.mode as? QuizMode.EraRound)?.era,
            correctCount = result.correctCount,
            totalCount = result.answers.size,
            finishedAt = result.finishedAt,
        )
    }
}

@Entity(
    tableName = "answers",
    foreignKeys = [
        ForeignKey(
            entity = AttemptEntity::class,
            parentColumns = ["id"],
            childColumns = ["attemptId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("attemptId"), Index("questionId")],
)
data class AnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attemptId: Long = 0,
    val questionId: Long,
    val selectedIndex: Int,
    val isCorrect: Boolean,
    val answeredAt: Long,
) {
    fun toRecord() = AnswerRecord(questionId = questionId, isCorrect = isCorrect, answeredAt = answeredAt)

    companion object {
        fun from(answer: GivenAnswer) = AnswerEntity(
            questionId = answer.questionId,
            selectedIndex = answer.selectedIndex,
            isCorrect = answer.isCorrect,
            answeredAt = answer.answeredAt,
        )
    }
}
