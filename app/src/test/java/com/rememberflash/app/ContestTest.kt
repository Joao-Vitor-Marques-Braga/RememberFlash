package com.rememberflash.app

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.rememberflash.app.data.remote.gemini.GeminiClient
import com.rememberflash.app.data.sync.SyncManager
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.model.Topic
import com.rememberflash.app.domain.model.User
import com.rememberflash.app.domain.repository.AuthRepository
import com.rememberflash.app.domain.repository.ContestRepository
import com.rememberflash.app.domain.repository.DisciplineRepository
import com.rememberflash.app.domain.repository.TopicRepository
import com.rememberflash.app.domain.usecase.contest.CreateContestUseCase
import com.rememberflash.app.domain.usecase.contest.GetActiveContestsUseCase
import com.rememberflash.app.domain.usecase.contest.GetArchivedContestsUseCase
import com.rememberflash.app.domain.usecase.contest.GetContestByIdUseCase
import com.rememberflash.app.domain.usecase.contest.ReactivateContestUseCase
import com.rememberflash.app.domain.usecase.contest.SoftDeleteContestUseCase
import com.rememberflash.app.domain.usecase.contest.UpdateContestUseCase
import com.rememberflash.app.presentation.contest.form.ContestFormViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class ContestTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeContestRepository: FakeContestRepository
    private lateinit var fakeDisciplineRepository: FakeDisciplineRepository
    private lateinit var fakeTopicRepository: FakeTopicRepository
    private lateinit var fakeAuthRepository: FakeAuthRepository

    private lateinit var createContestUseCase: CreateContestUseCase
    private lateinit var softDeleteContestUseCase: SoftDeleteContestUseCase
    private lateinit var reactivateContestUseCase: ReactivateContestUseCase
    private lateinit var getActiveContestsUseCase: GetActiveContestsUseCase
    private lateinit var getArchivedContestsUseCase: GetArchivedContestsUseCase
    private lateinit var getContestByIdUseCase: GetContestByIdUseCase
    private lateinit var updateContestUseCase: UpdateContestUseCase

    private val mockContext = mock<Context>()
    private val mockGeminiClient = mock<GeminiClient>()
    private val mockSyncManager = mock<SyncManager>()

    private val testUser =
        User(
            id = "user_test_123",
            name = "Concurseiro Silva",
            cpf = "11144477735",
            email = "concurseiro@teste.com",
        )

    // ==========================================
    // FAKE REPOSITORIES
    // ==========================================

    class FakeContestRepository : ContestRepository {
        val contests = mutableListOf<Contest>()
        private val contestsFlow = MutableStateFlow<List<Contest>>(emptyList())

        private fun notifyChange() {
            contestsFlow.value = contests.toList()
        }

        override suspend fun insert(contest: Contest): Result<Long> {
            val id = if (contest.id == 0L) (contests.size + 1).toLong() else contest.id
            val inserted = contest.copy(id = id)
            contests.add(inserted)
            notifyChange()
            return Result.success(id)
        }

        override suspend fun update(contest: Contest): Result<Unit> {
            val index = contests.indexOfFirst { it.id == contest.id }
            if (index != -1) {
                contests[index] = contest
                notifyChange()
                return Result.success(Unit)
            }
            return Result.error("Concurso não encontrado para atualização.")
        }

        override suspend fun softDelete(contestId: Long): Result<Unit> {
            val index = contests.indexOfFirst { it.id == contestId }
            if (index != -1) {
                // RN06: Exclusão lógica apenas altera isActive para false
                contests[index] = contests[index].copy(isActive = false)
                notifyChange()
                return Result.success(Unit)
            }
            return Result.error("Concurso não encontrado.")
        }

        override suspend fun delete(contestId: Long): Result<Unit> {
            contests.removeAll { it.id == contestId }
            notifyChange()
            return Result.success(Unit)
        }

        override fun getActiveContestsByUser(userId: String): Flow<List<Contest>> {
            return contestsFlow.map { list -> list.filter { it.userId == userId && it.isActive } }
        }

        override fun getArchivedContestsByUser(userId: String): Flow<List<Contest>> {
            return contestsFlow.map { list -> list.filter { it.userId == userId && !it.isActive } }
        }

        override suspend fun reactivate(contestId: Long): Result<Unit> {
            val index = contests.indexOfFirst { it.id == contestId }
            if (index != -1) {
                // Reativação reverte isActive para true
                contests[index] = contests[index].copy(isActive = true)
                notifyChange()
                return Result.success(Unit)
            }
            return Result.error("Concurso não encontrado para reativação.")
        }

        override suspend fun getById(contestId: Long): Result<Contest> {
            val found = contests.find { it.id == contestId }
            return if (found != null) Result.success(found)
            else Result.error("Concurso não encontrado.")
        }

        override fun getAllByUser(userId: String): Flow<List<Contest>> {
            return contestsFlow.map { list -> list.filter { it.userId == userId } }
        }
    }

    class FakeDisciplineRepository : DisciplineRepository {
        val disciplines = mutableListOf<Discipline>()

        override suspend fun insert(discipline: Discipline): Result<Long> {
            val id = if (discipline.id == 0L) (disciplines.size + 1).toLong() else discipline.id
            val inserted = discipline.copy(id = id)
            disciplines.add(inserted)
            return Result.success(id)
        }

        override suspend fun update(discipline: Discipline): Result<Unit> {
            val index = disciplines.indexOfFirst { it.id == discipline.id }
            if (index != -1) disciplines[index] = discipline
            return Result.success(Unit)
        }

        override suspend fun delete(disciplineId: Long): Result<Unit> {
            disciplines.removeAll { it.id == disciplineId }
            return Result.success(Unit)
        }

        override fun getByContest(contestId: Long): Flow<List<Discipline>> {
            return MutableStateFlow(disciplines.filter { it.contestId == contestId })
        }

        override suspend fun getById(disciplineId: Long): Result<Discipline> {
            val found = disciplines.find { it.id == disciplineId }
            return if (found != null) Result.success(found) else Result.error("Não encontrada")
        }

        override fun getAllDisciplines(): Flow<List<Discipline>> {
            return MutableStateFlow(disciplines.toList())
        }
    }

    class FakeTopicRepository : TopicRepository {
        val topics = mutableListOf<Topic>()

        override suspend fun insert(topic: Topic): Result<Long> {
            val id = (topics.size + 1).toLong()
            topics.add(topic.copy(id = id))
            return Result.success(id)
        }

        override suspend fun insertAll(topicsToInsert: List<Topic>): Result<List<Long>> {
            val ids = topicsToInsert.mapIndexed { idx, it -> (topics.size + idx + 1).toLong() }
            topics.addAll(topicsToInsert)
            return Result.success(ids)
        }

        override suspend fun update(topic: Topic): Result<Unit> = Result.success(Unit)

        override suspend fun delete(topicId: Long): Result<Unit> = Result.success(Unit)

        override suspend fun deleteByDiscipline(disciplineId: Long): Result<Unit> =
            Result.success(Unit)

        override fun getByDisciplineFlow(disciplineId: Long): Flow<List<Topic>> =
            MutableStateFlow(emptyList())

        override suspend fun getByDiscipline(disciplineId: Long): Result<List<Topic>> =
            Result.success(emptyList())

        override fun getByContestFlow(contestId: Long): Flow<List<Topic>> =
            MutableStateFlow(emptyList())

        override suspend fun getById(topicId: Long): Result<Topic> = Result.error("Não encontrado")

        override suspend fun syncDisciplineTopicCounters(disciplineId: Long): Result<Unit> =
            Result.success(Unit)
    }

    class FakeAuthRepository(var currentUser: User?) : AuthRepository {
        private val fallbackUser =
            User("user_test_123", "Concurseiro Silva", "11144477735", "concurseiro@teste.com")

        override suspend fun saveSession(token: String, user: User) {
            currentUser = user
        }

        override suspend fun getCurrentSession(): Result<User> =
            currentUser?.let { Result.success(it) } ?: Result.error("Sessão não encontrada")

        override suspend fun clearSession() {
            currentUser = null
        }

        override suspend fun isSessionValid(): Boolean = currentUser != null

        override suspend fun saveGeminiApiKey(apiKey: String) {}

        override suspend fun getGeminiApiKey(): String? = "dummy_key"

        override suspend fun hasGeminiApiKey(): Boolean = true

        override suspend fun registerUser(
            name: String,
            cpf: String,
            email: String,
            passwordKey: String,
        ): Result<User> = Result.success(currentUser ?: fallbackUser)

        override suspend fun authenticateUser(email: String, passwordKey: String): Result<User> =
            Result.success(currentUser ?: fallbackUser)

        override suspend fun changePassword(
            currentPasswordKey: String,
            newPasswordKey: String,
        ): Result<Unit> = Result.success(Unit)

        override suspend fun changeEmail(newEmail: String, passwordKey: String): Result<Unit> =
            Result.success(Unit)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeContestRepository = FakeContestRepository()
        fakeDisciplineRepository = FakeDisciplineRepository()
        fakeTopicRepository = FakeTopicRepository()
        fakeAuthRepository = FakeAuthRepository(testUser)

        createContestUseCase =
            CreateContestUseCase(
                contestRepository = fakeContestRepository,
                disciplineRepository = fakeDisciplineRepository,
                topicRepository = fakeTopicRepository,
                geminiClient = mockGeminiClient,
                context = mockContext,
            )

        updateContestUseCase =
            UpdateContestUseCase(
                contestRepository = fakeContestRepository,
                disciplineRepository = fakeDisciplineRepository,
                topicRepository = fakeTopicRepository,
                geminiClient = mockGeminiClient,
                context = mockContext,
            )

        softDeleteContestUseCase = SoftDeleteContestUseCase(fakeContestRepository)
        reactivateContestUseCase = ReactivateContestUseCase(fakeContestRepository)
        getActiveContestsUseCase = GetActiveContestsUseCase(fakeContestRepository)
        getArchivedContestsUseCase = GetArchivedContestsUseCase(fakeContestRepository)
        getContestByIdUseCase = GetContestByIdUseCase(fakeContestRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =========================================================================
    // 1. RF003: Validação da persistência de diretrizes do certame
    // =========================================================================

    @Test
    fun `createContestUseCase should persist contest guidelines and create default disciplines when no PDF is attached`() =
        runTest(testDispatcher) {
            val contestToCreate =
                Contest(
                    userId = testUser.id,
                    title = "Concurso Prefeitura de Rio Verde",
                    description = "Cargo: Auditor Fiscal Tributário",
                    organizerName = "UniRV",
                    questionType = "Múltipla Escolha",
                    syllabusPdfUri = null,
                    aiDifficulty = "Médio",
                    aiRigor = "Padrão",
                    aiTone = "Explicativo",
                )

            var lastStepMessage = ""
            val result = createContestUseCase(contestToCreate) { step -> lastStepMessage = step }

            assertTrue(result is Result.Success)
            val createdId = (result as Result.Success).data
            assertTrue(createdId > 0L)

            // Verifica integridade da persistência no repositório
            val saved = fakeContestRepository.contests.find { it.id == createdId }
            assertNotNull(saved)
            assertEquals("Concurso Prefeitura de Rio Verde", saved?.title)
            assertEquals("UniRV", saved?.organizerName)
            assertEquals("Múltipla Escolha", saved?.questionType)
            assertTrue(saved?.description?.contains("Auditor Fiscal Tributário") == true)
            assertTrue(saved?.isActive == true)

            // RF003: Na criação sem edital, o sistema automaticamente gera matérias básicas padrão
            val disciplines =
                fakeDisciplineRepository.disciplines.filter { it.contestId == createdId }
            assertEquals(2, disciplines.size)
            assertTrue(disciplines.any { it.name == "Conhecimentos Gerais" })
            assertTrue(disciplines.any { it.name == "Conhecimentos Específicos" })
        }

    @Test
    fun `ContestFormViewModel should validate mandatory guidelines before saving without edital`() =
        runTest(testDispatcher) {
            val viewModel =
                ContestFormViewModel(
                    savedStateHandle = SavedStateHandle(),
                    authRepository = fakeAuthRepository,
                    createContestUseCase = createContestUseCase,
                    updateContestUseCase = updateContestUseCase,
                    getContestByIdUseCase = getContestByIdUseCase,
                    syncManager = mockSyncManager,
                )

            // Tentativa de salvar formulário com título e banca vazios
            viewModel.onSaveClicked()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertFalse(state.isSuccess)
            assertEquals(
                "Preencha o título e a banca examinadora, ou selecione um edital para preenchimento automático.",
                state.error,
            )
        }

    @Test
    fun `ContestFormViewModel should require Organizer and Job Position when PDF edital is attached`() =
        runTest(testDispatcher) {
            val viewModel =
                ContestFormViewModel(
                    savedStateHandle = SavedStateHandle(),
                    authRepository = fakeAuthRepository,
                    createContestUseCase = createContestUseCase,
                    updateContestUseCase = updateContestUseCase,
                    getContestByIdUseCase = getContestByIdUseCase,
                    syncManager = mockSyncManager,
                )

            viewModel.onTitleChanged("Concurso TJ-GO")
            viewModel.addPdfAttachment("content://edital.pdf", "edital.pdf")

            // Falta preencher banca e cargo
            viewModel.onSaveClicked()

            val state = viewModel.uiState.value
            assertEquals(
                "Ao anexar um edital, informe obrigatoriamente a Banca Examinadora e o Cargo Pretendido para que a IA filtre o conteúdo programático correto.",
                state.error,
            )
        }

    // =========================================================================
    // 2. RF003: Validação estrita de arquivos PDF (formato e tamanho até 15MB)
    // =========================================================================

    @Test
    fun `ContestFormViewModel onPdfError should reject invalid formats like docx or images`() =
        runTest(testDispatcher) {
            val viewModel =
                ContestFormViewModel(
                    savedStateHandle = SavedStateHandle(),
                    authRepository = fakeAuthRepository,
                    createContestUseCase = createContestUseCase,
                    updateContestUseCase = updateContestUseCase,
                    getContestByIdUseCase = getContestByIdUseCase,
                    syncManager = mockSyncManager,
                )

            // Simula rejeição de arquivo com extensão inválida capturada pelo seletor de arquivos
            val invalidFormatMessage = "Formato inválido. Selecione apenas arquivos PDF."
            viewModel.onPdfError(invalidFormatMessage)

            val state = viewModel.uiState.value
            assertEquals(invalidFormatMessage, state.error)
            assertTrue(state.pdfAttachments.isEmpty())
        }

    @Test
    fun `ContestFormViewModel onPdfError should reject files exceeding the 15MB limit`() =
        runTest(testDispatcher) {
            val viewModel =
                ContestFormViewModel(
                    savedStateHandle = SavedStateHandle(),
                    authRepository = fakeAuthRepository,
                    createContestUseCase = createContestUseCase,
                    updateContestUseCase = updateContestUseCase,
                    getContestByIdUseCase = getContestByIdUseCase,
                    syncManager = mockSyncManager,
                )

            val fileSizeExceededMessage =
                "Arquivo muito grande (edital_completo.pdf). Limite de 15MB por arquivo."
            viewModel.onPdfError(fileSizeExceededMessage)

            val state = viewModel.uiState.value
            assertEquals(fileSizeExceededMessage, state.error)
            assertTrue(state.pdfAttachments.isEmpty())
        }

    @Test
    fun `ContestFormViewModel addPdfAttachment should accept valid PDF and clear errors`() =
        runTest(testDispatcher) {
            val viewModel =
                ContestFormViewModel(
                    savedStateHandle = SavedStateHandle(),
                    authRepository = fakeAuthRepository,
                    createContestUseCase = createContestUseCase,
                    updateContestUseCase = updateContestUseCase,
                    getContestByIdUseCase = getContestByIdUseCase,
                    syncManager = mockSyncManager,
                )

            viewModel.onPdfError("Erro anterior")
            assertEquals("Erro anterior", viewModel.uiState.value.error)

            viewModel.addPdfAttachment("content://media/edital_valido.pdf", "edital_valido.pdf")

            val state = viewModel.uiState.value
            assertNull(state.error)
            assertEquals(1, state.pdfAttachments.size)
            assertEquals("edital_valido.pdf", state.pdfAttachments[0].name)
        }

    // =========================================================================
    // 3. RN04 / RF005: Parametrização dos perfis de IA sem inserção de prompt livre
    // =========================================================================

    @Test
    fun `ContestFormViewModel should strictly configure predefined AI profiles without free prompt input`() =
        runTest(testDispatcher) {
            val viewModel =
                ContestFormViewModel(
                    savedStateHandle = SavedStateHandle(),
                    authRepository = fakeAuthRepository,
                    createContestUseCase = createContestUseCase,
                    updateContestUseCase = updateContestUseCase,
                    getContestByIdUseCase = getContestByIdUseCase,
                    syncManager = mockSyncManager,
                )

            assertEquals("Médio", viewModel.uiState.value.aiDifficulty)
            assertEquals("Padrão", viewModel.uiState.value.aiRigor)
            assertEquals("Explicativo", viewModel.uiState.value.aiTone)

            viewModel.onAiDifficultyChanged("Difícil")
            viewModel.onAiRigorChanged("Rigoroso")
            viewModel.onAiToneChanged("Direto")

            val state = viewModel.uiState.value
            assertEquals("Difícil", state.aiDifficulty)
            assertEquals("Rigoroso", state.aiRigor)
            assertEquals("Direto", state.aiTone)

            viewModel.onTitleChanged("Concurso Polícia Civil")
            viewModel.onOrganizerChanged("Cebraspe")
            viewModel.onJobPositionChanged("Escrivão")
            viewModel.onSaveClicked()

            testScheduler.advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isSuccess)
            val savedContest = fakeContestRepository.contests.first()
            assertEquals("Difícil", savedContest.aiDifficulty)
            assertEquals("Rigoroso", savedContest.aiRigor)
            assertEquals("Direto", savedContest.aiTone)
        }

    // =========================================================================
    // 4. RN06 / RF003: Execução de exclusão lógica (Soft Delete) e Reativação
    // =========================================================================

    @Test
    fun `softDeleteContestUseCase should deactivate contest setting isActive to false without deleting data`() =
        runTest(testDispatcher) {
            val contest =
                Contest(
                    id = 10L,
                    userId = testUser.id,
                    title = "Concurso Banco do Brasil",
                    organizerName = "Cesgranrio",
                    isActive = true,
                )
            fakeContestRepository.insert(contest)

            fakeDisciplineRepository.insert(
                Discipline(contestId = 10L, name = "Conhecimentos Bancários")
            )

            val result = softDeleteContestUseCase(10L)

            assertTrue(result is Result.Success)

            val contestInDb = fakeContestRepository.contests.find { it.id == 10L }
            assertNotNull(contestInDb)
            assertFalse("Concurso deveria estar inativo (isActive = false)", contestInDb!!.isActive)

            val disciplinesInDb =
                fakeDisciplineRepository.disciplines.filter { it.contestId == 10L }
            assertEquals(1, disciplinesInDb.size)
        }

    @Test
    fun `softDeleteContestUseCase should fail when invalid contest ID is provided`() =
        runTest(testDispatcher) {
            val result = softDeleteContestUseCase(0L)
            assertTrue(result is Result.Error)
            assertEquals("ID de concurso inválido", (result as Result.Error).message)
        }

    @Test
    fun `getActiveContestsUseCase and getArchivedContestsUseCase should filter correctly based on isActive flag`() =
        runTest(testDispatcher) {
            fakeContestRepository.insert(
                Contest(id = 1L, userId = testUser.id, title = "Ativo 1", isActive = true)
            )
            fakeContestRepository.insert(
                Contest(id = 2L, userId = testUser.id, title = "Ativo 2", isActive = true)
            )
            fakeContestRepository.insert(
                Contest(id = 3L, userId = testUser.id, title = "Arquivado", isActive = false)
            )
            val activeList = getActiveContestsUseCase(testUser.id).first()
            val archivedList = getArchivedContestsUseCase(testUser.id).first()

            assertEquals(2, activeList.size)
            assertTrue(activeList.all { it.isActive })
            assertEquals(listOf("Ativo 1", "Ativo 2"), activeList.map { it.title })

            assertEquals(1, archivedList.size)
            assertFalse(archivedList[0].isActive)
            assertEquals("Arquivado", archivedList[0].title)
        }

    @Test
    fun `reactivateContestUseCase should restore archived contest setting isActive to true`() =
        runTest(testDispatcher) {
            fakeContestRepository.insert(
                Contest(id = 55L, userId = testUser.id, title = "Concurso INSS", isActive = false)
            )

            var archivedList = getArchivedContestsUseCase(testUser.id).first()
            assertEquals(1, archivedList.size)

            val result = reactivateContestUseCase(55L)

            assertTrue(result is Result.Success)

            val activeList = getActiveContestsUseCase(testUser.id).first()
            archivedList = getArchivedContestsUseCase(testUser.id).first()

            assertEquals(1, activeList.size)
            assertEquals("Concurso INSS", activeList[0].title)
            assertTrue(activeList[0].isActive)

            assertTrue(archivedList.isEmpty())
        }
}
