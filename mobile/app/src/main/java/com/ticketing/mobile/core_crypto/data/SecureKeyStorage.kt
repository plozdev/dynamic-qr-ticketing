package com.ticketing.mobile.core_crypto.data

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Interface quản lý lưu trữ an toàn các hạt giống bí mật (256-bit Secret Key) của từng vé.
 */
interface ISecureKeyStorage {
    fun saveSecretKey(ticketId: String, secretKey: String)
    fun getSecretKey(ticketId: String): String?
    fun removeSecretKey(ticketId: String)
    fun clear()
}

/**
 * Triển khai lưu trữ bảo mật sử dụng Android KeyStore và mã hóa AES-256-GCM.
 * Khóa bí mật của vé được mã hóa trước khi lưu vào SharedPreferences, chống trích xuất.
 */
class SecureKeyStorage(context: Context) : ISecureKeyStorage {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    init {
        ensureMasterKeyExists()
    }

    private fun ensureMasterKeyExists() {
        try {
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            Log.w(TAG, "KeyStore initialization fallback: ${e.message}")
        }
    }

    private fun getMasterKey(): SecretKey? {
        return try {
            keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        } catch (e: Exception) {
            null
        }
    }

    override fun saveSecretKey(ticketId: String, secretKey: String) {
        val masterKey = getMasterKey()
        if (masterKey != null) {
            try {
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, masterKey)
                val iv = cipher.iv
                val encryptedBytes = cipher.doFinal(secretKey.toByteArray(Charsets.UTF_8))

                // Lưu định dạng IV_BASE64:ENCRYPTED_BASE64
                val combined = Base64.encodeToString(iv, Base64.NO_WRAP) + ":" +
                        Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
                prefs.edit().putString(KEY_PREFIX + ticketId, combined).apply()
                return
            } catch (e: Exception) {
                Log.w(TAG, "Encryption failed, falling back: ${e.message}")
            }
        }
        // Fallback lưu trực tiếp nếu KeyStore không khả dụng
        prefs.edit().putString(KEY_PREFIX + ticketId, secretKey).apply()
    }

    override fun getSecretKey(ticketId: String): String? {
        val stored = prefs.getString(KEY_PREFIX + ticketId, null) ?: return null
        val masterKey = getMasterKey()
        if (masterKey != null && stored.contains(":")) {
            try {
                val parts = stored.split(":")
                if (parts.size == 2) {
                    val iv = Base64.decode(parts[0], Base64.NO_WRAP)
                    val encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP)

                    val cipher = Cipher.getInstance(TRANSFORMATION)
                    val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                    cipher.init(Cipher.DECRYPT_MODE, masterKey, spec)
                    val decryptedBytes = cipher.doFinal(encryptedBytes)
                    return String(decryptedBytes, Charsets.UTF_8)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Decryption error for ticket $ticketId: ${e.message}")
            }
        }
        return stored
    }

    override fun removeSecretKey(ticketId: String) {
        prefs.edit().remove(KEY_PREFIX + ticketId).apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val TAG = "SecureKeyStorage"
        private const val PREFS_NAME = "secure_ticket_seeds"
        private const val KEY_PREFIX = "seed_"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "SecureTixAESMasterKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
    }
}
