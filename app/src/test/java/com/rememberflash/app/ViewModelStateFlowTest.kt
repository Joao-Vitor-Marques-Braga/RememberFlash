package com.rememberflash.app

import app.cash.turbine.test
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.User
import com.rememberflash.app.domain.repository.AuthRepository
import com.rememberflash.app.domain.usecase.auth.LoginUseCase
import com.rememberflash.app.presentation.auth.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelStateFlowTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var viewModel: LoginViewModel

    private val testUser = User(
        id = "user_state_01",
        name = "Lucas Mendes",
        cpf = "12345678909",
        email = "lucas@concurso.com"
    )

    class FakeAuthRepository(val user: User) : AuthRepository {
        var shouldFail = false
        var savedSessionUser: User? = null

        override suspend fun saveSession(token: String, user: User) {
            savedSessionUser = user
        }
        override suspend fun getCurrentSession(): Result<User> = Result.error("Sem sessão")
        override suspend fun clearSession() { savedSessionUser = null }
        override suspend fun isSessionValid(): Boolean = false
        override suspend fun saveGeminiApiKey(apiKey: String) {}
        override suspend fun getGeminiApiKey(): String? = "fake_key"
        override suspend fun hasGeminiApiKey(): Boolean = true
        override suspend fun registerUser(name: String, cpf: String, email: String, passwordKey: String): Result<User> = Result.success(user)
        override suspend fun authenticateUser(email: String, passwordKey: String): Result<User> {
            return if (shouldFail) {
                Result.error("Credenciais inválidas")
            } else {
                Result.success(user)
            }
        }
        override suspend fun changePassword(currentPasswordKey: String, newPasswordKey: String): Result<Unit> = Result.success(Unit)
        override suspend fun changeEmail(newEmail: String, passwordKey: String): Result<Unit> = Result.success(Unit)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository(testUser)
        loginUseCase = LoginUseCase(fakeAuthRepository)
        viewModel = LoginViewModel(loginUseCase, fakeAuthRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login flow should consistently transition from Idle to Loading to Success via Turbine`() = runTest(testDispatcher) {
        viewModel.onEmailChanged("lucas@concurso.com")
        viewModel.onPasswordChanged("senhaForte123")

        viewModel.uiState.test {
            // 1. Estado Inicial (Idle / Preparado)
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)
            assertFalse(initialState.isSuccess)
            assertNull(initialState.error)

            // Dispara a ação de autenticação
            viewModel.onLoginClicked()

            // 2. Estado Intermediário (Loading ativo)
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertFalse(loadingState.isSuccess)
            assertNull(loadingState.error)

            // Avança a execução das corrotinas na JVM
            testScheduler.advanceUntilIdle()

            // 3. Estado Final de Sucesso (Loading desligado, isSuccess = true)
            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertTrue(successState.isSuccess)
            assertNull(successState.error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login flow should consistently transition from Idle to Loading to Error on authentication failure`() = runTest(testDispatcher) {
        fakeAuthRepository.shouldFail = true

        viewModel.onEmailChanged("lucas@concurso.com")
        viewModel.onPasswordChanged("senhaIncorreta")

        viewModel.uiState.test {
            // 1. Estado Inicial (Idle)
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)
            assertFalse(initialState.isSuccess)
            assertNull(initialState.error)

            // Dispara o login com credenciais incorretas
            viewModel.onLoginClicked()

            // 2. Estado Intermediário (Loading ativo)
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertFalse(loadingState.isSuccess)
            assertNull(loadingState.error)

            // Avança o scheduler de corrotinas
            testScheduler.advanceUntilIdle()

            // 3. Estado Final de Erro (Loading desligado, mensagem amigável exibida)
            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertFalse(errorState.isSuccess)
            assertEquals("Credenciais inválidas", errorState.error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onEmailChanged and onPasswordChanged should atomically clear error and preserve StateFlow consistency`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // Estado inicial

            // Força erro de validação (campos vazios)
            viewModel.onLoginClicked()
            val errorState = awaitItem()
            assertEquals("Preencha todos os campos", errorState.error)

            // Usuário começa a digitar: erro deve ser limpo imediatamente
            viewModel.onEmailChanged("lucas@concurso.com")
            val typedState = awaitItem()
            assertEquals("lucas@concurso.com", typedState.email)
            assertNull(typedState.error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onLoginClicked with blank inputs should bypass Loading and immediately emit validation error`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertNull(initial.error)

            // Clique com campos em branco
            viewModel.onLoginClicked()

            // Deve emitir erro de validação imediatamente sem passar por isLoading = true
            val validationError = awaitItem()
            assertEquals("Preencha todos os campos", validationError.error)
            assertFalse(validationError.isLoading)
            assertFalse(validationError.isSuccess)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
