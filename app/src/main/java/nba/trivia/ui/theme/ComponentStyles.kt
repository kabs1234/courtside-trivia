package nba.trivia.ui.theme

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.StyleScope
import androidx.compose.foundation.style.StyleStateKey
import androidx.compose.foundation.style.animate
import androidx.compose.foundation.style.border
import androidx.compose.foundation.style.contentPadding
import androidx.compose.foundation.style.minSize
import androidx.compose.foundation.style.pressed
import androidx.compose.foundation.style.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import nba.trivia.data.EraStatus

private const val BANNER_NOTCH_RATIO = 0.12f
private const val PRESSED_SCALE = 0.97f

enum class AnswerMark { OPEN, CORRECT, WRONG, DIMMED }

val EraStatusKey = StyleStateKey(EraStatus.UNLOCKED)
val AnswerMarkKey = StyleStateKey(AnswerMark.OPEN)

var MutableStyleState.eraStatus: EraStatus
    get() = this[EraStatusKey]
    set(value) {
        this[EraStatusKey] = value
    }

var MutableStyleState.answerMark: AnswerMark
    get() = this[AnswerMarkKey]
    set(value) {
        this[AnswerMarkKey] = value
    }

val BannerShape = GenericShape { size, _ ->
    val notch = size.width * BANNER_NOTCH_RATIO
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height)
    lineTo(size.width / 2, size.height - notch)
    lineTo(0f, size.height)
    close()
}

private val StyleScope.banners: BannerColors get() = LocalBannerColors.currentValue

private val StyleScope.answers: AnswerColors get() = LocalAnswerColors.currentValue

private fun StyleScope.whenEra(status: EraStatus, block: () -> Unit) =
    state(EraStatusKey, block) { key, state -> state[key] == status }

private fun StyleScope.whenAnswer(mark: AnswerMark, block: () -> Unit) =
    state(AnswerMarkKey, block) { key, state -> state[key] == mark }

object TriviaStyles {
    val eraBanner: Style = Style {
        shape(BannerShape)
        minSize(0.dp, 236.dp)
        contentPadding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 44.dp)
        background(banners.unlocked)
        contentColor(banners.onBanner)
        whenEra(EraStatus.PASSED) {
            background(banners.passed)
        }
        whenEra(EraStatus.MASTERED) {
            background(banners.mastered)
            contentColor(banners.onMastered)
        }
        whenEra(EraStatus.LOCKED) {
            background(Color.Transparent)
            border(1.5.dp, banners.lockedLine)
            contentColor(banners.lockedLine)
        }
        pressed {
            animate { scale(PRESSED_SCALE) }
        }
    }

    val answerOption: Style = Style {
        shape(RoundedCornerShape(14.dp))
        contentPadding(horizontal = 18.dp, vertical = 16.dp)
        background(answers.open)
        border(1.5.dp, answers.openBorder)
        contentColor(answers.onOpen)
        pressed {
            animate {
                scale(PRESSED_SCALE)
                borderColor(answers.onOpen)
            }
        }
        whenAnswer(AnswerMark.CORRECT) {
            animate {
                background(answers.correct)
                borderColor(answers.correct)
                contentColor(answers.onCorrect)
            }
        }
        whenAnswer(AnswerMark.WRONG) {
            animate {
                background(answers.wrong)
                borderColor(answers.wrong)
                contentColor(answers.onWrong)
            }
        }
        whenAnswer(AnswerMark.DIMMED) {
            contentColor(answers.onDimmed)
        }
    }

    val tag: Style = Style {
        shape(RoundedCornerShape(4.dp))
        contentPadding(horizontal = 8.dp, vertical = 3.dp)
    }
}
