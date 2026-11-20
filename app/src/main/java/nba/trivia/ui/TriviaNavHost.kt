package nba.trivia.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import nba.trivia.data.NEW_QUESTION_ID
import nba.trivia.ui.bank.QuestionBankScreen
import nba.trivia.ui.detail.QuestionDetailScreen
import nba.trivia.ui.editor.QuestionEditorScreen
import nba.trivia.ui.navigation.ERA_ARGUMENT
import nba.trivia.ui.navigation.QUESTION_ID_ARGUMENT
import nba.trivia.ui.navigation.Routes
import nba.trivia.ui.navigation.TopLevelDestination
import nba.trivia.ui.play.PlayScreen
import nba.trivia.ui.quiz.QuizScreen
import nba.trivia.ui.stats.StatsScreen

@Composable
fun TriviaNavHost(navController: NavHostController = rememberNavController()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showMessage: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }
    val finishWithMessage: (String) -> Unit = { message ->
        navController.popBackStack()
        showMessage(message)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { TopLevelNavigationBar(navController) },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.PLAY.route,
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            composable(TopLevelDestination.BANK.route) {
                QuestionBankScreen(
                    onQuestionClick = { navController.navigate(Routes.question(it)) },
                    onAddQuestion = { navController.navigate(Routes.newQuestion()) },
                )
            }
            composable(TopLevelDestination.PLAY.route) {
                PlayScreen(
                    onStartEra = { navController.navigate(Routes.eraQuiz(it)) },
                    onStartReview = { navController.navigate(Routes.reviewQuiz()) },
                )
            }
            composable(TopLevelDestination.STATS.route) {
                StatsScreen(
                    onQuestionClick = { navController.navigate(Routes.question(it)) },
                    onMessage = showMessage,
                )
            }
            composable(
                Routes.QUESTION,
                arguments = listOf(navArgument(QUESTION_ID_ARGUMENT) { type = NavType.LongType }),
            ) {
                QuestionDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.editQuestion(it)) },
                    onDeleted = finishWithMessage,
                )
            }
            composable(
                Routes.EDITOR,
                arguments = listOf(
                    navArgument(QUESTION_ID_ARGUMENT) {
                        type = NavType.LongType
                        defaultValue = NEW_QUESTION_ID
                    },
                ),
            ) {
                QuestionEditorScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = finishWithMessage,
                )
            }
            composable(
                Routes.QUIZ,
                arguments = listOf(
                    navArgument(ERA_ARGUMENT) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) {
                QuizScreen(onExit = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun TopLevelNavigationBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val isOnTopLevel = TopLevelDestination.entries.any { it.route == currentDestination?.route }
    if (!isOnTopLevel) return

    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(destination.label) },
            )
        }
    }
}
