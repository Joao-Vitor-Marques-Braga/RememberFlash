package com.rememberflash.app.data.util

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AesEncryptionUtil {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val PEPPER = "RememberFlash-AI-Secure-Key-v1-2026"

    private fun getSecretKey(): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(PEPPER.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(plainText: String?): String? {
        if (plainText.isNullOrBlank()) return plainText
        return try {
            val key = getSecretKey()
            val cipher = Cipher.getInstance(ALGORITHM)
            val iv = ByteArray(16) { 0 }
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            "ENC:" + Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            plainText
        }
    }

    fun decrypt(cipherText: String?): String? {
        if (cipherText.isNullOrBlank()) return cipherText
        if (!cipherText.startsWith("ENC:")) return cipherText
        return try {
            val rawBase64 = cipherText.removePrefix("ENC:")
            val key = getSecretKey()
            val cipher = Cipher.getInstance(ALGORITHM)
            val iv = ByteArray(16) { 0 }
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
            val decodedBytes = Base64.decode(rawBase64, Base64.NO_WRAP)
            String(cipher.doFinal(decodedBytes), Charsets.UTF_8)
        } catch (e: Exception) {
            cipherText
        }
    }
}
