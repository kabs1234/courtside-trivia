package nba.trivia.ui.editor

import nba.trivia.data.Difficulty
import nba.trivia.data.Era
import nba.trivia.data.OPTION_COUNT
import nba.trivia.data.Question

const val MIN_QUESTION_LENGTH = 10
const val MAX_QUESTION_LENGTH = 200
const val MAX_OPTION_LENGTH = 60
const val MAX_EXPLANATION_LENGTH = 300

data class QuestionForm(
    val era: Era = Era.EARLY_YEARS,
    val text: String = "",
    val options: List<String> = List(OPTION_COUNT) { "" },
    val correctIndex: Int? = null,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val explanation: String = "",
) {
    fun withOption(index: Int, value: String): QuestionForm =
        copy(options = options.mapIndexed { i, option -> if (i == index) value else option })

    fun validate(): QuestionFormErrors = QuestionFormErrors(
        text = textError(),
        options = options.indices.map(::optionError),
        correctAnswer = if (correctIndex == null) "Mark which option is correct" else null,
        explanation = if (explanation.length > MAX_EXPLANATION_LENGTH) {
            "Keep it under $MAX_EXPLANATION_LENGTH characters"
        } else {
            null
        },
    )

    fun toQuestion(id: Long): Question = Question(
        id = id,
        era = era,
        text = text.trim(),
        options = options.map(String::trim),
        correctIndex = checkNotNull(correctIndex) { "Validate the form before saving it" },
        difficulty = difficulty,
        explanation = explanation.trim(),
    )

    private fun textError(): String? {
        val trimmed = text.trim()
        return when {
            trimmed.isEmpty() -> "Enter the question"
            trimmed.length < MIN_QUESTION_LENGTH -> "Too short – at least $MIN_QUESTION_LENGTH characters"
            trimmed.length > MAX_QUESTION_LENGTH -> "Too long – at most $MAX_QUESTION_LENGTH characters"
            else -> null
        }
    }

    private fun optionError(index: Int): String? {
        val trimmed = options[index].trim()
        val isRepeated = options.take(index).any { it.trim().equals(trimmed, ignoreCase = true) }
        return when {
            trimmed.isEmpty() -> "Option can't be empty"
            trimmed.length > MAX_OPTION_LENGTH -> "At most $MAX_OPTION_LENGTH characters"
            isRepeated -> "Same as another option"
            else -> null
        }
    }

    companion object {
        fun from(question: Question) = QuestionForm(
            era = question.era,
            text = question.text,
            options = question.options,
            correctIndex = question.correctIndex,
            difficulty = question.difficulty,
            explanation = question.explanation,
        )
    }
}

data class QuestionFormErrors(
    val text: String?,
    val options: List<String?>,
    val correctAnswer: String?,
    val explanation: String?,
) {
    val isValid: Boolean
        get() = text == null && options.all { it == null } && correctAnswer == null && explanation == null
}
