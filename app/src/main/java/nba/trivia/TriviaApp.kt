package nba.trivia

import android.app.Application

class TriviaApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
