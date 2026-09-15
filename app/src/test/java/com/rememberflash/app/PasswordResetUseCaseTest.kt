package com.rememberflash.app

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.User
import com.rememberflash.app.domain.repository.UserRepository
import com.rememberflash.app.domain.usecase.auth.RequestPasswordResetUseCase
import com.rememberflash.app.domain.usecase.auth.ResetPasswordUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap

class PasswordResetUseCaseTest {

    private lateinit var fakeUserRepository: FakeUserRepository
    private lateinit var requestPasswordResetUseCase: RequestPasswordResetUseCase
    private lateinit var resetPasswordUseCase: ResetPasswordUseCase

    class FakeUserRepository : UserRepository {
        val users = mutableListOf<User>()
        val userPasswords = mutableMapOf<String, String>()
        val resetOtpMap = ConcurrentHashMap<String, Pair<String, Long>>()

        override suspend fun isCpfRegistered(cpf: String): Boolean =
            users.any { it.cpf == cpf }

        override suspend fun findByCpf(cpf: String): User? =
            users.firstOrNull { it.cpf == cpf }

        override suspend fun findByEmail(email: String): User? =
            users.firstOrNull { it.email.equals(email, ignoreCase = true) }

        override suspend fun saveUser(user: User, passwordHash: String?) {
            users.add(user)
            passwordHash?.let { userPasswords[user.email.lowercase()] = it }
        }

        override suspend fun updatePassword(email: String, newPasswordHash: String): Boolean {
            val cleanEmail = email.trim().lowercase()
            if (users.any { it.email.equals(cleanEmail, ignoreCase = true) }) {
                userPasswords[cleanEmail] = newPasswordHash
                return true
            }
            return false
        }

        override suspend fun savePasswordResetOtp(email: String, otp: String, expiresAt: Long) {
            resetOtpMap[email.trim().lowercase()] = Pair(otp, expiresAt)
        }

        override suspend fun getPasswordResetOtp(email: String): Pair<String, Long>? =
            resetOtpMap[email.trim().lowercase()]

        override suspend fun clearPasswordResetOtp(email: String) {
            resetOtpMap.remove(email.trim().lowercase())
        }
    }

    @Before
    fun setup() {
        fakeUserRepository = FakeUserRepository()
        requestPasswordResetUseCase = RequestPasswordResetUseCase(fakeUserRepository)
        resetPasswordUseCase = ResetPasswordUseCase(fakeUserRepository)
    }

    @Test
    fun `requestPasswordResetUseCase should fail when email is invalid or blank`() = runTest {
        val result = requestPasswordResetUseCase("emailInvalido")
        assertTrue(result is Result.Error)
        assertEquals("Informe um e-mail válido", (result as Result.Error).message)
    }

    @Test
    fun `requestPasswordResetUseCase should fail when user does not exist in Room`() = runTest {
        val result = requestPasswordResetUseCase("inexistente@email.com")
        assertTrue(result is Result.Error)
        assertEquals("E-mail não encontrado no sistema", (result as Result.Error).message)
    }

    @Test
    fun `requestPasswordResetUseCase should generate 6-digit OTP and 15 min expiration for existing user`() = runTest {
        fakeUserRepository.saveUser(
            User(id = "user1", name = "Lucas Lima", email = "lucas@email.com", cpf = "11144477735")
        )

        val result = requestPasswordResetUseCase("lucas@email.com")
        assertTrue(result is Result.Success)

        val otp = (result as Result.Success).data
        assertEquals(6, otp.length)
        assertTrue(otp.all { it.isDigit() })

        val savedOtpData = fakeUserRepository.getPasswordResetOtp("lucas@email.com")
        assertNotNull(savedOtpData)
        assertEquals(otp, savedOtpData?.first)
        assertTrue(savedOtpData!!.second > System.currentTimeMillis())
    }

    @Test
    fun `resetPasswordUseCase should fail when passwords do not match`() = runTest {
        val result = resetPasswordUseCase(
            email = "lucas@email.com",
            otp = "123456",
            newPassword = "Password@123",
            confirmPassword = "DifferentPassword@123"
        )
        assertTrue(result is Result.Error)
        assertEquals("As senhas não coincidem", (result as Result.Error).message)
    }

    @Test
    fun `resetPasswordUseCase should fail when OTP is invalid`() = runTest {
        fakeUserRepository.saveUser(
            User(id = "user1", name = "Lucas Lima", email = "lucas@email.com", cpf = "11144477735")
        )
        fakeUserRepository.savePasswordResetOtp("lucas@email.com", "654321", System.currentTimeMillis() + 900000)

        val result = resetPasswordUseCase(
            email = "lucas@email.com",
            otp = "111111",
            newPassword = "NewPassword@123",
            confirmPassword = "NewPassword@123"
        )
        assertTrue(result is Result.Error)
        assertEquals("Código de segurança inválido", (result as Result.Error).message)
    }

    @Test
    fun `resetPasswordUseCase should fail when OTP is expired`() = runTest {
        fakeUserRepository.saveUser(
            User(id = "user1", name = "Lucas Lima", email = "lucas@email.com", cpf = "11144477735")
        )
        // Expirado há 1 minuto
        fakeUserRepository.savePasswordResetOtp("lucas@email.com", "123456", System.currentTimeMillis() - 60000)

        val result = resetPasswordUseCase(
            email = "lucas@email.com",
            otp = "123456",
            newPassword = "NewPassword@123",
            confirmPassword = "NewPassword@123"
        )
        assertTrue(result is Result.Error)
        assertEquals("Código de segurança expirado. Solicite um novo código.", (result as Result.Error).message)
    }

    @Test
    fun `resetPasswordUseCase should validate password complexity`() = runTest {
        fakeUserRepository.saveUser(
            User(id = "user1", name = "Lucas Lima", email = "lucas@email.com", cpf = "11144477735")
        )
        fakeUserRepository.savePasswordResetOtp("lucas@email.com", "123456", System.currentTimeMillis() + 900000)

        // Menos de 8 chars
        val shortResult = resetPasswordUseCase("lucas@email.com", "123456", "Pass@1", "Pass@1")
        assertTrue(shortResult is Result.Error)
        assertEquals("A nova senha deve ter no mínimo 8 caracteres", (shortResult as Result.Error).message)

        // Sem números
        val noDigitResult = resetPasswordUseCase("lucas@email.com", "123456", "Password@", "Password@")
        assertTrue(noDigitResult is Result.Error)
        assertEquals("A nova senha deve conter caracteres alfanuméricos (letras e números)", (noDigitResult as Result.Error).message)

        // Sem símbolos
        val noSymbolResult = resetPasswordUseCase("lucas@email.com", "123456", "Password123", "Password123")
        assertTrue(noSymbolResult is Result.Error)
        assertEquals("A nova senha deve conter pelo menos um símbolo especial (ex: @, #, $, !)", (noSymbolResult as Result.Error).message)
    }

    @Test
    fun `resetPasswordUseCase should succeed, update password hash in Room and clear OTP`() = runTest {
        fakeUserRepository.saveUser(
            User(id = "user1", name = "Lucas Lima", email = "lucas@email.com", cpf = "11144477735")
        )
        fakeUserRepository.savePasswordResetOtp("lucas@email.com", "123456", System.currentTimeMillis() + 900000)

        val result = resetPasswordUseCase(
            email = "lucas@email.com",
            otp = "123456",
            newPassword = "StrongPassword@2026",
            confirmPassword = "StrongPassword@2026"
        )

        assertTrue(result is Result.Success)
        assertNotNull(fakeUserRepository.userPasswords["lucas@email.com"])
        // OTP deve ter sido limpo
        assertNull(fakeUserRepository.getPasswordResetOtp("lucas@email.com"))
    }
}
