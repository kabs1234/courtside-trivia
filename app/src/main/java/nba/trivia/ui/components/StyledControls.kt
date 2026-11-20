package nba.trivia.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import nba.trivia.data.EraStatus
import nba.trivia.ui.theme.AnswerMark
import nba.trivia.ui.theme.TriviaStyles
import nba.trivia.ui.theme.answerMark
import nba.trivia.ui.theme.eraStatus

data class BannerAction(val label: String, val onClick: () -> Unit)

@Composable
fun EraBanner(
    status: EraStatus,
    action: BannerAction?,
    modifier: Modifier = Modifier,
    style: Style = Style,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val styleState = rememberUpdatedStyleState(interactionSource) {
        it.isEnabled = action != null
        it.eraStatus = status
    }
    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = action != null,
                onClickLabel = action?.label,
                role = Role.Button,
                onClick = { action?.onClick?.invoke() },
            )
            .styleable(styleState, TriviaStyles.eraBanner, style),
        content = content,
    )
}

@Composable
fun AnswerOption(
    text: String,
    mark: AnswerMark,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: Style = Style,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isOpen = mark == AnswerMark.OPEN
    val styleState = rememberUpdatedStyleState(interactionSource) {
        it.isEnabled = isOpen
        it.answerMark = mark
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isOpen,
                role = Role.Button,
                onClick = onClick,
            )
            .styleable(styleState, TriviaStyles.answerOption, style),
    ) {
        BasicText(text, style = MaterialTheme.typography.bodyLarge)
    }
}
