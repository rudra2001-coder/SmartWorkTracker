package com.rudra.smartworktracker.data.backup

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupCrypto {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BITS = 128
    private const val IV_LENGTH = 12
    private const val SALT_LENGTH = 16
    private const val ITERATIONS = 100000
    private const val KEY_LENGTH = 256

    data class EncryptedPackage(
        val salt: ByteArray,
        val iv: ByteArray,
        val ciphertext: ByteArray
    )

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val key = factory.generateSecret(spec)
        return SecretKeySpec(key.encoded, "AES")
    }

    fun encrypt(plaintext: ByteArray, password: String): ByteArray {
        val salt = ByteArray(SALT_LENGTH).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(IV_LENGTH).apply { SecureRandom().nextBytes(this) }
        val secretKey = deriveKey(password, salt)

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BITS, iv))

        val ciphertext = cipher.doFinal(plaintext)
        return salt + iv + ciphertext
    }

    fun decrypt(encrypted: ByteArray, password: String): ByteArray {
        val salt = encrypted.copyOfRange(0, SALT_LENGTH)
        val iv = encrypted.copyOfRange(SALT_LENGTH, SALT_LENGTH + IV_LENGTH)
        val ciphertext = encrypted.copyOfRange(SALT_LENGTH + IV_LENGTH, encrypted.size)

        val secretKey = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BITS, iv))

        return cipher.doFinal(ciphertext)
    }
}
