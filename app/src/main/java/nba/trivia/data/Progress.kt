package nba.trivia.data

const val PASS_PERCENT = 70
const val MASTERY_PERCENT = 100
const val CORRECT_ANSWERS_TO_CLEAR_REVIEW = 2

enum class EraStatus(val label: String) {
    LOCKED("Locked"),
    UNLOCKED("Unlocked"),
    PASSED("Passed"),
    MASTERED("Mastered");

    val isCleared: Boolean get() = this == PASSED || this == MASTERED
}

data class EraProgress(
    val era: Era,
    val status: EraStatus,
    val questionCount: Int,
    val attemptCount: Int,
    val bestPercent: Int?,
) {
    val isPlayable: Boolean get() = status != EraStatus.LOCKED && questionCount > 0

    val opensNextEra: Boolean get() = status.isCleared || (status != EraStatus.LOCKED && questionCount == 0)
}

data class QuestionStats(val timesAnswered: Int, val timesCorrect: Int, val isInReview: Boolean)

fun calculateEraProgress(questions: List<Question>, attempts: List<QuizAttempt>): List<EraProgress> {
    val questionCounts = questions.groupingBy { it.era }.eachCount()
    val attemptsByEra = attempts.groupBy { (it.mode as? QuizMode.EraRound)?.era }
    return Era.entries.fold(emptyList()) { built, era ->
        val isUnlocked = built.lastOrNull()?.opensNextEra ?: true
        val eraAttempts = attemptsByEra[era].orEmpty()
        val bestPercent = eraAttempts.maxOfOrNull { it.percent }
        built + EraProgress(
            era = era,
            status = if (isUnlocked) unlockedStatusFor(bestPercent) else EraStatus.LOCKED,
            questionCount = questionCounts[era] ?: 0,
            attemptCount = eraAttempts.size,
            bestPercent = bestPercent,
        )
    }
}

fun questionIdsToReview(answers: List<AnswerRecord>): Set<Long> =
    answers.groupBy { it.questionId }.filterValues { it.needsReview() }.keys

fun statsFor(questionId: Long, answers: List<AnswerRecord>): QuestionStats {
    val history = answers.filter { it.questionId == questionId }
    return QuestionStats(
        timesAnswered = history.size,
        timesCorrect = history.count { it.isCorrect },
        isInReview = history.needsReview(),
    )
}

private fun unlockedStatusFor(bestPercent: Int?): EraStatus = when {
    bestPercent == null -> EraStatus.UNLOCKED
    bestPercent >= MASTERY_PERCENT -> EraStatus.MASTERED
    bestPercent >= PASS_PERCENT -> EraStatus.PASSED
    else -> EraStatus.UNLOCKED
}

private fun List<AnswerRecord>.needsReview(): Boolean {
    val lastMiss = indexOfLast { !it.isCorrect }
    if (lastMiss == -1) return false
    val correctSinceMiss = size - lastMiss - 1
    return correctSinceMiss < CORRECT_ANSWERS_TO_CLEAR_REVIEW
}
