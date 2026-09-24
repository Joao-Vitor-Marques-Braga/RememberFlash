package com.rememberflash.app

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.exception.AuthException
import com.rememberflash.app.domain.model.User
import com.rememberflash.app.domain.repository.AuthRepository
import com.rememberflash.app.domain.repository.UserRepository
import com.rememberflash.app.domain.usecase.auth.RegisterUseCase
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RegisterUseCaseTest {

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeUserRepository: FakeUserRepository
    private lateinit var registerUseCase: RegisterUseCase

    class FakeAuthRepository : AuthRepository {
        var savedToken: String? = null
        var savedUser: User? = null
        var registerShouldFail = false

        override suspend fun saveSession(token: String, user: User) {
            savedToken = token
            savedUser = user
        }

        override suspend fun getCurrentSession(): Result<User> =
            savedUser?.let { Result.success(it) } ?: Result.error("Sem sessão")

        override suspend fun clearSession() {
            savedToken = null
            savedUser = null
        }

        override suspend fun isSessionValid(): Boolean = savedToken != null

        override suspend fun saveGeminiApiKey(apiKey: String) {}

        override suspend fun getGeminiApiKey(): String? = null

        override suspend fun hasGeminiApiKey(): Boolean = false

        override suspend fun registerUser(
            name: String,
            cpf: String,
            email: String,
            passwordKey: String,
        ): Result<User> {
            if (registerShouldFail) {
                return Result.error("Falha ao registrar")
            }
            return Result.success(
                User(id = UUID.randomUUID().toString(), name = name, cpf = cpf, email = email)
            )
        }

        override suspend fun authenticateUser(email: String, passwordKey: String): Result<User> =
            Result.error("Não implementado no fake")

        override suspend fun changePassword(
            currentPasswordKey: String,
            newPasswordKey: String,
        ): Result<Unit> = Result.success(Unit)

        override suspend fun changeEmail(newEmail: String, passwordKey: String): Result<Unit> =
            Result.success(Unit)
    }

    class FakeUserRepository : UserRepository {
        val registeredUsers = mutableListOf<User>()

        override suspend fun isCpfRegistered(cpf: String): Boolean {
            val clean = cpf.filter { it.isDigit() }
            return registeredUsers.any { it.cpf == clean }
        }

        override suspend fun findByCpf(cpf: String): User? {
            val clean = cpf.filter { it.isDigit() }
            return registeredUsers.firstOrNull { it.cpf == clean }
        }

        override suspend fun findByEmail(email: String): User? {
            return registeredUsers.firstOrNull { it.email.equals(email, ignoreCase = true) }
        }

        override suspend fun saveUser(user: User, passwordHash: String?) {
            registeredUsers.add(user)
        }

        override suspend fun updatePassword(email: String, newPasswordHash: String): Boolean {
            return true
        }

        override suspend fun savePasswordResetOtp(email: String, otp: String, expiresAt: Long) {
            // No-op for registration test
        }

        override suspend fun getPasswordResetOtp(email: String): Pair<String, Long>? {
            return null
        }

        override suspend fun clearPasswordResetOtp(email: String) {
            // No-op for registration test
        }
    }

    @Before
    fun setup() {
        fakeAuthRepository = FakeAuthRepository()
        fakeUserRepository = FakeUserRepository()
        registerUseCase = RegisterUseCase(fakeAuthRepository, fakeUserRepository)
    }

    @Test
    fun `validateCpf should throw AuthException when CPF has invalid length`() {
        var thrown = false
        try {
            registerUseCase.validateCpf("123456")
        } catch (e: AuthException) {
            thrown = true
            assertEquals("CPF deve conter exatamente 11 dígitos numéricos", e.message)
        }
        assertTrue(thrown)
    }

    @Test
    fun `validateCpf should throw AuthException when CPF has all identical digits`() {
        val repeatedCpfs =
            listOf(
                "00000000000",
                "11111111111",
                "22222222222",
                "33333333333",
                "44444444444",
                "55555555555",
                "66666666666",
                "77777777777",
                "88888888888",
                "99999999999",
            )
        for (cpf in repeatedCpfs) {
            var thrown = false
            try {
                registerUseCase.validateCpf(cpf)
            } catch (e: AuthException) {
                thrown = true
                assertEquals("CPF inválido: número com todos os dígitos repetidos", e.message)
            }
            assertTrue("Deveria ter lançado para $cpf", thrown)
        }
    }

    @Test
    fun `validateCpf should throw AuthException when first check digit is mathematically invalid`() {
        val invalidCpf = "11144477705"
        var thrown = false
        try {
            registerUseCase.validateCpf(invalidCpf)
        } catch (e: AuthException) {
            thrown = true
            assertEquals("CPF inválido: primeiro dígito verificador incorreto", e.message)
        }
        assertTrue(thrown)
    }

    @Test
    fun `validateCpf should throw AuthException when second check digit is mathematically invalid`() {
        val invalidCpf = "11144477730"
        var thrown = false
        try {
            registerUseCase.validateCpf(invalidCpf)
        } catch (e: AuthException) {
            thrown = true
            assertEquals("CPF inválido: segundo dígito verificador incorreto", e.message)
        }
        assertTrue(thrown)
    }

    @Test
    fun `validateCpf should pass for valid CPFs`() {
        val validCpfs = listOf("11144477735", "52998224725", "12345678909", "21544975007")
        for (cpf in validCpfs) {
            registerUseCase.validateCpf(cpf)
        }
    }

    @Test
    fun `registerUseCase should fail with AuthException message when CPF is already registered in Room`() =
        runTest {
            val validCpf = "11144477735"
            fakeUserRepository.saveUser(
                User(
                    id = "existing_user",
                    name = "Usuário Existente",
                    email = "existente@email.com",
                    cpf = validCpf,
                )
            )

            val result =
                registerUseCase(
                    name = "Novo Usuário",
                    cpf = "111.444.777-35",
                    email = "novo@email.com",
                    password = "senhaSegura123",
                )

            assertTrue(result is Result.Error)
            assertEquals("CPF já cadastrado no sistema", (result as Result.Error).message)
        }

    @Test
    fun `registerUseCase should succeed for valid CPF and save cryptographic token session`() =
        runTest {
            val validCpf = "111.444.777-35"
            val result =
                registerUseCase(
                    name = "Carlos Silva",
                    cpf = validCpf,
                    email = "carlos@email.com",
                    password = "senhaSegura123",
                )

            assertTrue(result is Result.Success)
            assertNotNull(fakeAuthRepository.savedToken)
            assertTrue(fakeAuthRepository.savedToken!!.length >= 32)
            assertEquals("Carlos Silva", fakeAuthRepository.savedUser?.name)
            assertEquals("11144477735", fakeAuthRepository.savedUser?.cpf)
            assertEquals(1, fakeUserRepository.registeredUsers.size)
        }
}
