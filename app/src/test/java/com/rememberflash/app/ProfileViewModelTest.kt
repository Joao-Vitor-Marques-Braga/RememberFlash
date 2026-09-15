package com.rememberflash.app

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.model.User
import com.rememberflash.app.domain.repository.AuthRepository
import com.rememberflash.app.domain.repository.ContestRepository
import com.rememberflash.app.domain.usecase.contest.GetArchivedContestsUseCase
import com.rememberflash.app.domain.usecase.contest.ReactivateContestUseCase
import com.rememberflash.app.presentation.profile.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeContestRepository: FakeContestRepository
    private lateinit var getArchivedContestsUseCase: GetArchivedContestsUseCase
    private lateinit var reactivateContestUseCase: ReactivateContestUseCase
    private lateinit var viewModel: ProfileViewModel

    private val testUser = User(
        id = "u1",
        name = "João Silva",
        cpf = "12345678900",
        email = "joao@email.com"
    )

    class FakeAuthRepository(val defaultUser: User) : AuthRepository {
        var currentUser: User? = defaultUser
        var clearSessionCalled = false
        var lastChangedPasswordCurrent: String? = null
        var lastChangedPasswordNew: String? = null
        var lastChangedEmailNew: String? = null
        var shouldFailChangePassword = false
        var shouldFailChangeEmail = false

        override suspend fun saveSession(token: String, user: User) { currentUser = user }
        override suspend fun getCurrentSession(): Result<User> {
            return currentUser?.let { Result.success(it) } ?: Result.error("Sem sessão")
        }
        override suspend fun clearSession() {
            clearSessionCalled = true
            currentUser = null
        }
        override suspend fun isSessionValid(): Boolean = currentUser != null
        override suspend fun saveGeminiApiKey(apiKey: String) {}
        override suspend fun getGeminiApiKey(): String? = "key"
        override suspend fun hasGeminiApiKey(): Boolean = true
        override suspend fun registerUser(name: String, cpf: String, email: String, passwordKey: String): Result<User> =
            Result.success(defaultUser)
        override suspend fun authenticateUser(email: String, passwordKey: String): Result<User> =
            Result.success(defaultUser)
        override suspend fun changePassword(currentPasswordKey: String, newPasswordKey: String): Result<Unit> {
            lastChangedPasswordCurrent = currentPasswordKey
            lastChangedPasswordNew = newPasswordKey
            return if (shouldFailChangePassword) Result.error("Senha incorreta.") else Result.success(Unit)
        }
        override suspend fun changeEmail(newEmail: String, passwordKey: String): Result<Unit> {
            lastChangedEmailNew = newEmail
            if (shouldFailChangeEmail) return Result.error("E-mail já existente.")
            currentUser = currentUser?.copy(email = newEmail)
            return Result.success(Unit)
        }
    }

    class FakeContestRepository : ContestRepository {
        val archivedContests = mutableListOf<Contest>()
        val archivedFlow = MutableStateFlow<List<Contest>>(emptyList())
        var shouldFailReactivation = false
        var lastReactivatedId: Long? = null

        override suspend fun insert(contest: Contest): Result<Long> = Result.success(contest.id)
        override suspend fun update(contest: Contest): Result<Unit> = Result.success(Unit)
        override suspend fun softDelete(contestId: Long): Result<Unit> = Result.success(Unit)
        override suspend fun delete(contestId: Long): Result<Unit> = Result.success(Unit)
        override fun getActiveContestsByUser(userId: String): Flow<List<Contest>> = MutableStateFlow(emptyList())
        override fun getArchivedContestsByUser(userId: String): Flow<List<Contest>> = archivedFlow
        override suspend fun reactivate(contestId: Long): Result<Unit> {
            if (shouldFailReactivation) return Result.error("Falha de teste")
            lastReactivatedId = contestId
            archivedContests.removeAll { it.id == contestId }
            archivedFlow.value = archivedContests.toList()
            return Result.success(Unit)
        }
        override suspend fun getById(contestId: Long): Result<Contest> = Result.error("Não encontrado")
        override fun getAllByUser(userId: String): Flow<List<Contest>> = MutableStateFlow(emptyList())
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository(defaultUser = testUser)
        fakeContestRepository = FakeContestRepository()
        getArchivedContestsUseCase = GetArchivedContestsUseCase(fakeContestRepository)
        reactivateContestUseCase = ReactivateContestUseCase(fakeContestRepository)

        viewModel = ProfileViewModel(
            authRepository = fakeAuthRepository,
            getArchivedContestsUseCase = getArchivedContestsUseCase,
            reactivateContestUseCase = reactivateContestUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load user session and observe archived contests into uiState`() = runTest(testDispatcher) {
        val archived = listOf(
            Contest(id = 100L, userId = "u1", title = "INSS 2024", isActive = false),
            Contest(id = 200L, userId = "u1", title = "PF 2024", isActive = false)
        )
        fakeContestRepository.archivedContests.addAll(archived)
        fakeContestRepository.archivedFlow.value = archived

        testScheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(testUser, state.user)
        assertEquals(2, state.archivedContests.size)
        assertEquals("INSS 2024", state.archivedContests[0].title)
    }

    @Test
    fun `reactivateContest success should reactivate contest and set feedback message`() = runTest(testDispatcher) {
        val archived = listOf(
            Contest(id = 100L, userId = "u1", title = "INSS 2024", isActive = false)
        )
        fakeContestRepository.archivedContests.addAll(archived)
        fakeContestRepository.archivedFlow.value = archived
        testScheduler.advanceUntilIdle()

        viewModel.reactivateContest(100L)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(100L, fakeContestRepository.lastReactivatedId)
        assertNull(state.isReactivatingId)
        assertEquals("Concurso reativado com sucesso!", state.contestActionFeedback)
        assertEquals(0, state.archivedContests.size)
    }

    @Test
    fun `reactivateContest failure should set error feedback message`() = runTest(testDispatcher) {
        fakeContestRepository.shouldFailReactivation = true
        viewModel.reactivateContest(100L)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.isReactivatingId)
        assertEquals("Falha ao reativar concurso.", state.contestActionFeedback)
    }

    @Test
    fun `changePassword with blank fields should set error`() = runTest(testDispatcher) {
        viewModel.onCurrentPasswordChange("")
        viewModel.onNewPasswordChange("")
        viewModel.onConfirmPasswordChange("")
        viewModel.changePassword()

        val state = viewModel.uiState.value
        assertEquals("Todos os campos de senha são obrigatórios.", state.passwordChangeError)
    }

    @Test
    fun `changePassword with mismatch confirmation should set error`() = runTest(testDispatcher) {
        viewModel.onCurrentPasswordChange("123456")
        viewModel.onNewPasswordChange("novaSenha1")
        viewModel.onConfirmPasswordChange("novaSenha2")
        viewModel.changePassword()

        val state = viewModel.uiState.value
        assertEquals("A nova senha e a confirmação não coincidem.", state.passwordChangeError)
    }

    @Test
    fun `changePassword with short password should set error`() = runTest(testDispatcher) {
        viewModel.onCurrentPasswordChange("123456")
        viewModel.onNewPasswordChange("123")
        viewModel.onConfirmPasswordChange("123")
        viewModel.changePassword()

        val state = viewModel.uiState.value
        assertEquals("A nova senha deve ter pelo menos 6 caracteres.", state.passwordChangeError)
    }

    @Test
    fun `changePassword success should clear text fields and set success flag`() = runTest(testDispatcher) {
        viewModel.onCurrentPasswordChange("senhaAtual123")
        viewModel.onNewPasswordChange("novaSenhaSegura")
        viewModel.onConfirmPasswordChange("novaSenhaSegura")
        viewModel.changePassword()

        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.passwordChangeSuccess)
        assertNull(state.passwordChangeError)
        assertEquals("", state.currentPasswordText)
        assertEquals("", state.newPasswordText)
        assertEquals("", state.confirmPasswordText)
        assertEquals("novaSenhaSegura", fakeAuthRepository.lastChangedPasswordNew)
    }

    @Test
    fun `changePassword failure should show error message`() = runTest(testDispatcher) {
        fakeAuthRepository.shouldFailChangePassword = true
        viewModel.onCurrentPasswordChange("senhaErrada")
        viewModel.onNewPasswordChange("novaSenhaSegura")
        viewModel.onConfirmPasswordChange("novaSenhaSegura")
        viewModel.changePassword()

        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.passwordChangeSuccess)
        assertEquals("Senha incorreta.", state.passwordChangeError)
    }

    @Test
    fun `changeEmail with invalid format should set error`() = runTest(testDispatcher) {
        viewModel.onNewEmailChange("email_invalido_sem_arroba")
        viewModel.onConfirmPasswordForEmailChange("123456")
        viewModel.changeEmail()

        val state = viewModel.uiState.value
        assertEquals("Formato de e-mail inválido.", state.emailChangeError)
    }

    @Test
    fun `changeEmail success should update email and reload user`() = runTest(testDispatcher) {
        viewModel.onNewEmailChange("novomail@teste.com")
        viewModel.onConfirmPasswordForEmailChange("123456")
        viewModel.changeEmail()

        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.emailChangeSuccess)
        assertNull(state.emailChangeError)
        assertEquals("", state.newEmailText)
        assertEquals("", state.confirmPasswordForEmailText)
        assertEquals("novomail@teste.com", state.user?.email)
    }

    @Test
    fun `logout should clear session and set isLoggedOut`() = runTest(testDispatcher) {
        viewModel.logout()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isLoggedOut)
        assertTrue(fakeAuthRepository.clearSessionCalled)
    }
}
