package com.rememberflash.app.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rememberflash.app.presentation.auth.LoginScreen
import com.rememberflash.app.presentation.auth.RegisterScreen
import com.rememberflash.app.presentation.auth.ForgotPasswordScreen
import com.rememberflash.app.presentation.auth.ResetPasswordScreen
import com.rememberflash.app.presentation.home.HomeScreen
import com.rememberflash.app.presentation.contest.form.ContestFormScreen
import com.rememberflash.app.presentation.contest.detail.ContestDetailScreen
import com.rememberflash.app.presentation.settings.SettingsScreen
import com.rememberflash.app.presentation.discipline.DisciplineScreen
import com.rememberflash.app.presentation.question.resolve.QuestionResolveScreen
import com.rememberflash.app.presentation.essay.capture.EssayCaptureScreen
import com.rememberflash.app.presentation.essay.result.EssayResultScreen

/**
 * Rotas de navegação do RememberFlash.
 * Nesta fase inicial, apenas os esqueletos de rotas são definidos.
 * As telas composables serão implementadas progressivamente.
 */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password/{email}"
    const val HOME = "home"
    const val CONTEST_FORM = "contest_form?contestId={contestId}"
    const val CONTEST_LIST = "contest_list"
    const val CONTEST_DETAIL = "contest_detail/{contestId}"
    const val DISCIPLINE_LIST = "discipline_list/{contestId}"
    const val FLASHCARD_DECK = "flashcard_deck/{disciplineId}"
    const val ESSAY_CAPTURE = "essay_capture"
    const val ESSAY_RESULT = "essay_result/{essayId}"
    const val SCHEDULE = "schedule/{contestId}"
    const val SETTINGS = "settings"
    const val QUESTION_RESOLVE = "question_resolve/{disciplineId}"

    fun contestDetail(contestId: Long) = "contest_detail/$contestId"
    fun contestForm(contestId: Long? = null) = if (contestId != null) "contest_form?contestId=$contestId" else "contest_form"
    fun disciplineList(contestId: Long) = "discipline_list/$contestId"
    fun flashcardDeck(disciplineId: Long) = "flashcard_deck/$disciplineId"
    fun essayResult(essayId: Long) = "essay_result/$essayId"
    fun schedule(contestId: Long) = "schedule/$contestId"
    fun resetPassword(email: String) = "reset_password/$email"
    fun questionResolve(disciplineId: Long) = "question_resolve/$disciplineId"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                }
            )
        }
        
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true } // Clear auth stack
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateToReset = { email ->
                    navController.navigate(Routes.resetPassword(email))
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.RESET_PASSWORD) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ResetPasswordScreen(
                email = email,
                onResetSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToCreateContest = {
                    navController.navigate(Routes.contestForm())
                },
                onNavigateToContestDetail = { contestId ->
                    navController.navigate(Routes.contestDetail(contestId))
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNavigateToEssayCapture = {
                    navController.navigate(Routes.ESSAY_CAPTURE)
                },
                onNavigateToEssayResult = { essayId ->
                    navController.navigate("essay_result/$essayId")
                }
            )
        }

        composable(
            route = Routes.CONTEST_FORM,
            arguments = listOf(androidx.navigation.navArgument("contestId") {
                type = androidx.navigation.NavType.StringType
                nullable = true
            })
        ) {
            ContestFormScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.CONTEST_LIST) {
            PlaceholderScreen(title = "Meus Concursos")
        }

        composable(
            route = Routes.CONTEST_DETAIL,
            arguments = listOf(androidx.navigation.navArgument("contestId") {
                type = androidx.navigation.NavType.StringType
            })
        ) {
            ContestDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEditContest = { contestId ->
                    navController.navigate(Routes.contestForm(contestId))
                },
                onNavigateToFlashcards = { disciplineId ->
                    navController.navigate(Routes.flashcardDeck(disciplineId))
                }
            )
        }

        composable(Routes.DISCIPLINE_LIST) {
            PlaceholderScreen(title = "Disciplinas")
        }

        composable(
            route = Routes.FLASHCARD_DECK,
            arguments = listOf(androidx.navigation.navArgument("disciplineId") {
                type = androidx.navigation.NavType.LongType
            })
        ) {
            DisciplineScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResolveQuestions = { disciplineId ->
                    navController.navigate(Routes.questionResolve(disciplineId))
                }
            )
        }

        composable(
            route = Routes.QUESTION_RESOLVE,
            arguments = listOf(androidx.navigation.navArgument("disciplineId") {
                type = androidx.navigation.NavType.LongType
            })
        ) {
            QuestionResolveScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.ESSAY_CAPTURE) {
            EssayCaptureScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResult = { essayId ->
                    navController.navigate("essay_result/$essayId")
                }
            )
        }

        composable(
            route = Routes.ESSAY_RESULT,
            arguments = listOf(androidx.navigation.navArgument("essayId") {
                type = androidx.navigation.NavType.LongType
            })
        ) {
            EssayResultScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SCHEDULE) {
            PlaceholderScreen(title = "Cronograma")
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
