package com.rememberflash.app

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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var viewModel: LoginViewModel

    private val testUser = User(
        id = "user1",
        name = "Maria Souza",
        cpf = "11122233344",
        email = "maria@email.com"
    )

    class FakeAuthRepository(val defaultUser: User, var isValidSession: Boolean = false) : AuthRepository {
        var authenticateShouldFail = false
        var savedToken: String? = null
        var savedUser: User? = null

        override suspend fun saveSession(token: String, user: User) {
            savedToken = token
            savedUser = user
            isValidSession = true
        }
        override suspend fun getCurrentSession(): Result<User> =
            savedUser?.let { Result.success(it) } ?: Result.error("Sem sessão")
        override suspend fun clearSession() {
            savedToken = null
            savedUser = null
            isValidSession = false
        }
        override suspend fun isSessionValid(): Boolean = isValidSession
        override suspend fun saveGeminiApiKey(apiKey: String) {}
        override suspend fun getGeminiApiKey(): String? = "key"
        override suspend fun hasGeminiApiKey(): Boolean = true
        override suspend fun registerUser(name: String, cpf: String, email: String, passwordKey: String): Result<User> =
            Result.success(defaultUser)
        override suspend fun authenticateUser(email: String, passwordKey: String): Result<User> {
            return if (authenticateShouldFail) {
                Result.error("Usuário não cadastrado.")
            } else {
                Result.success(defaultUser)
            }
        }
        override suspend fun changePassword(currentPasswordKey: String, newPasswordKey: String): Result<Unit> = Result.success(Unit)
        override suspend fun changeEmail(newEmail: String, passwordKey: String): Result<Unit> = Result.success(Unit)
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository(defaultUser = testUser, isValidSession = false)
        loginUseCase = LoginUseCase(fakeAuthRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should set isSuccess true if session is already valid`() = runTest(testDispatcher) {
        fakeAuthRepository.isValidSession = true
        viewModel = LoginViewModel(loginUseCase, fakeAuthRepository)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
    }

    @Test
    fun `onEmailChanged and onPasswordChanged should update state and clear error`() = runTest(testDispatcher) {
        viewModel = LoginViewModel(loginUseCase, fakeAuthRepository)
        viewModel.onEmailChanged("teste@email.com")
        viewModel.onPasswordChanged("123456")

        val state = viewModel.uiState.value
        assertEquals("teste@email.com", state.email)
        assertEquals("123456", state.password)
        assertNull(state.error)
    }

    @Test
    fun `onLoginClicked with empty fields should show error`() = runTest(testDispatcher) {
        viewModel = LoginViewModel(loginUseCase, fakeAuthRepository)
        viewModel.onLoginClicked()

        val state = viewModel.uiState.value
        assertEquals("Preencha todos os campos", state.error)
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
    }

    @Test
    fun `onLoginClicked with valid credentials should succeed`() = runTest(testDispatcher) {
        viewModel = LoginViewModel(loginUseCase, fakeAuthRepository)
        viewModel.onEmailChanged("maria@email.com")
        viewModel.onPasswordChanged("senhaCorreta")
        viewModel.onLoginClicked()

        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSuccess)
        assertNull(state.error)
        assertEquals(testUser, fakeAuthRepository.savedUser)
    }

    @Test
    fun `onLoginClicked with invalid credentials should show error`() = runTest(testDispatcher) {
        fakeAuthRepository.authenticateShouldFail = true
        viewModel = LoginViewModel(loginUseCase, fakeAuthRepository)
        viewModel.onEmailChanged("errado@email.com")
        viewModel.onPasswordChanged("senhaErrada")
        viewModel.onLoginClicked()

        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("Usuário não cadastrado.", state.error)
    }
}
