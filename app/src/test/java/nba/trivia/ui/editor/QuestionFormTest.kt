package nba.trivia.ui.editor

import nba.trivia.data.Era
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionFormTest {
    private val validForm = QuestionForm(
        era = Era.JORDAN,
        text = "  Who won the 1991 Finals MVP?  ",
        options = listOf("Michael Jordan", "Magic Johnson", "Scottie Pippen", "James Worthy"),
        correctIndex = 0,
    )

    @Test
    fun aCompleteFormIsValidAndTrimmedOnSave() {
        assertTrue(validForm.validate().isValid)
        assertEquals("Who won the 1991 Finals MVP?", validForm.toQuestion(id = 7).text)
    }

    @Test
    fun anEmptyFormReportsEveryProblem() {
        val errors = QuestionForm().validate()

        assertFalse(errors.isValid)
        assertEquals("Enter the question", errors.text)
        assertTrue(errors.options.all { it == "Option can't be empty" })
        assertEquals("Mark which option is correct", errors.correctAnswer)
    }

    @Test
    fun repeatedOptionsAreRejectedIgnoringCase() {
        val errors = validForm.withOption(2, " michael jordan ").validate()

        assertNull(errors.options[0])
        assertEquals("Same as another option", errors.options[2])
    }

    @Test
    fun aVeryShortQuestionIsRejected() {
        assertEquals(
            "Too short – at least $MIN_QUESTION_LENGTH characters",
            validForm.copy(text = "Who?").validate().text,
        )
    }
}
