package nba.trivia.data

import nba.trivia.data.local.SeedQuestions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressTest {
    private val questions = SeedQuestions.ALL.mapIndexed { index, question -> question.copy(id = index + 1L) }

    @Test
    fun onlyTheFirstEraIsUnlockedWithoutHistory() {
        val statuses = calculateEraProgress(questions, emptyList()).map { it.status }

        assertEquals(EraStatus.UNLOCKED, statuses.first())
        assertTrue(statuses.drop(1).all { it == EraStatus.LOCKED })
    }

    @Test
    fun passingAnEraUnlocksTheNextOne() {
        val progress = calculateEraProgress(questions, listOf(earlyYearsRound(correct = 6, total = 8)))

        assertEquals(EraStatus.PASSED, progress[0].status)
        assertEquals(EraStatus.UNLOCKED, progress[1].status)
        assertEquals(EraStatus.LOCKED, progress[2].status)
    }

    @Test
    fun scoringBelowThePassMarkKeepsTheNextEraLocked() {
        val progress = calculateEraProgress(questions, listOf(earlyYearsRound(correct = 5, total = 8)))

        assertEquals(EraStatus.UNLOCKED, progress[0].status)
        assertEquals(EraStatus.LOCKED, progress[1].status)
        assertEquals(62, progress[0].bestPercent)
    }

    @Test
    fun aPerfectRoundMastersTheEra() {
        val attempts = listOf(
            earlyYearsRound(correct = 4, total = 8),
            earlyYearsRound(correct = 8, total = 8),
        )

        val first = calculateEraProgress(questions, attempts).first()

        assertEquals(EraStatus.MASTERED, first.status)
        assertEquals(2, first.attemptCount)
    }

    @Test
    fun anEmptyEraLetsThePlayerMoveOn() {
        val withoutEarlyYears = questions.filterNot { it.era == Era.EARLY_YEARS }

        val progress = calculateEraProgress(withoutEarlyYears, emptyList())

        assertFalse(progress[0].isPlayable)
        assertEquals(EraStatus.UNLOCKED, progress[1].status)
    }

    @Test
    fun aMissedQuestionStaysInReviewUntilAnsweredCorrectlyTwice() {
        val missed = listOf(miss(questionId = 1, at = 1))
        val onceRight = missed + hit(questionId = 1, at = 2)
        val twiceRight = onceRight + hit(questionId = 1, at = 3)

        assertEquals(setOf(1L), questionIdsToReview(missed))
        assertEquals(setOf(1L), questionIdsToReview(onceRight))
        assertTrue(questionIdsToReview(twiceRight).isEmpty())
    }

    @Test
    fun missingAgainResetsTheReviewCount() {
        val answers = listOf(
            miss(questionId = 2, at = 1),
            hit(questionId = 2, at = 2),
            miss(questionId = 2, at = 3),
            hit(questionId = 2, at = 4),
        )

        assertEquals(setOf(2L), questionIdsToReview(answers))
        assertEquals(QuestionStats(timesAnswered = 4, timesCorrect = 2, isInReview = true), statsFor(2, answers))
    }

    private fun earlyYearsRound(correct: Int, total: Int) = QuizAttempt(
        id = 0,
        mode = QuizMode.EraRound(Era.EARLY_YEARS),
        correctCount = correct,
        totalCount = total,
        finishedAt = 0,
    )

    private fun hit(questionId: Long, at: Long) = AnswerRecord(questionId, isCorrect = true, answeredAt = at)

    private fun miss(questionId: Long, at: Long) = AnswerRecord(questionId, isCorrect = false, answeredAt = at)
}
