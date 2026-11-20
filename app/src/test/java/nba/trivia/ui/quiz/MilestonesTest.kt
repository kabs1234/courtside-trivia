package nba.trivia.ui.quiz

import nba.trivia.data.Era
import nba.trivia.data.EraProgress
import nba.trivia.data.EraStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class MilestonesTest {
    @Test
    fun passingTheFirstEraReportsThePassAndTheUnlock() {
        val before = listOf(progress(Era.EARLY_YEARS, EraStatus.UNLOCKED), progress(Era.MAGIC_AND_BIRD, EraStatus.LOCKED))
        val after = listOf(progress(Era.EARLY_YEARS, EraStatus.PASSED), progress(Era.MAGIC_AND_BIRD, EraStatus.UNLOCKED))

        assertEquals(
            listOf(Milestone.EraPassed(Era.EARLY_YEARS), Milestone.EraUnlocked(Era.MAGIC_AND_BIRD)),
            milestonesBetween(before, after),
        )
    }

    @Test
    fun masteringAPassedEraOnlyReportsMastery() {
        val before = listOf(progress(Era.EARLY_YEARS, EraStatus.PASSED))
        val after = listOf(progress(Era.EARLY_YEARS, EraStatus.MASTERED))

        assertEquals(listOf(Milestone.EraMastered(Era.EARLY_YEARS)), milestonesBetween(before, after))
    }

    private fun progress(era: Era, status: EraStatus) =
        EraProgress(era = era, status = status, questionCount = 9, attemptCount = 0, bestPercent = null)
}
