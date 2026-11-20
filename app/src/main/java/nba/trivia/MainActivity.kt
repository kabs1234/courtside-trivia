package nba.trivia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.CompositionLocalProvider
import nba.trivia.ui.LocalQuizRepository
import nba.trivia.ui.TriviaNavHost
import nba.trivia.ui.theme.TriviaTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ComposeFoundationFlags.isInheritedTextStyleEnabled = true
        enableEdgeToEdge()
        val app = checkNotNull(application as? TriviaApp) { "Manifest must declare TriviaApp as the application" }
        val repository = app.container.repository
        setContent {
            TriviaTheme {
                CompositionLocalProvider(LocalQuizRepository provides repository) {
                    TriviaNavHost()
                }
            }
        }
    }
}
