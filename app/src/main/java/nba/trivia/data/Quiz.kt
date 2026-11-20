package nba.trivia.data

const val QUIZ_LENGTH = 8
private const val FULL_PERCENT = 100

sealed interface QuizMode {
    val title: String

    data class EraRound(val era: Era) : QuizMode {
        override val title: String get() = era.title
    }

    data object Review : QuizMode {
        override val title: String = "Review missed"
    }
}

data class GivenAnswer(val questionId: Long, val selectedIndex: Int, val isCorrect: Boolean, val answeredAt: Long)

data class QuizResult(val mode: QuizMode, val answers: List<GivenAnswer>, val finishedAt: Long) {
    val correctCount: Int get() = answers.count { it.isCorrect }
}

data class QuizAttempt(
    val id: Long,
    val mode: QuizMode,
    val correctCount: Int,
    val totalCount: Int,
    val finishedAt: Long,
) {
    val percent: Int get() = percentOf(correctCount, totalCount)
}

data class AnswerRecord(val questionId: Long, val isCorrect: Boolean, val answeredAt: Long)

fun percentOf(part: Int, whole: Int): Int = if (whole == 0) 0 else part * FULL_PERCENT / whole
