package nba.trivia.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [QuestionEntity::class, AttemptEntity::class, AnswerEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class TriviaDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao

    abstract fun quizHistoryDao(): QuizHistoryDao

    companion object {
        private const val FILE_NAME = "nba_trivia.db"

        fun create(context: Context, scope: CoroutineScope): TriviaDatabase {
            lateinit var database: TriviaDatabase
            val seedOnFirstLaunch = object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    scope.launch { database.questionDao().insertAll(SeedQuestions.ALL.map(QuestionEntity::from)) }
                }
            }
            database = Room.databaseBuilder(context, TriviaDatabase::class.java, FILE_NAME)
                .addCallback(seedOnFirstLaunch)
                .build()
            return database
        }
    }
}
