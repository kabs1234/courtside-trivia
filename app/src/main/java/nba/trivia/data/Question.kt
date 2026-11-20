package nba.trivia.data

const val OPTION_COUNT = 4
const val NEW_QUESTION_ID = 0L

data class Question(
    val id: Long = NEW_QUESTION_ID,
    val era: Era,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val difficulty: Difficulty,
    val explanation: String = "",
) {
    init {
        require(options.size == OPTION_COUNT) { "A question needs exactly $OPTION_COUNT options" }
        require(correctIndex in options.indices) { "Correct option $correctIndex is out of range" }
    }

    val correctAnswer: String get() = options[correctIndex]

    fun isCorrect(selectedIndex: Int): Boolean = selectedIndex == correctIndex
}
