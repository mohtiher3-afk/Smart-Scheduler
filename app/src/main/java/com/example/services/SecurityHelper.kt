package com.example.services

import android.content.Context
import android.util.Base64
import java.nio.charset.StandardCharsets

object SecurityHelper {
    private const val PREFS_NAME = "encrypted_secure_prefs"
    private const val SECRET_KEY_OBFUSCATOR = "M3SmartSchedulerX"

    /**
     * Simple, fast, clean string obfuscation/encryption using base64 and XOR key.
     * Prevents raw Zoom credentials or private links from being stored in plain text.
     */
    fun encryptZoomLink(plainText: String): String {
        if (plainText.isEmpty()) return ""
        try {
            val keyBytes = SECRET_KEY_OBFUSCATOR.toByteArray(StandardCharsets.UTF_8)
            val textBytes = plainText.toByteArray(StandardCharsets.UTF_8)
            val encryptedBytes = ByteArray(textBytes.size)
            
            for (i in textBytes.indices) {
                encryptedBytes[i] = (textBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
            }
            return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            return plainText
        }
    }

    /**
     * Decrypts an obfuscated Zoom link.
     */
    fun decryptZoomLink(encryptedText: String): String {
        if (encryptedText.isEmpty()) return ""
        try {
            val keyBytes = SECRET_KEY_OBFUSCATOR.toByteArray(StandardCharsets.UTF_8)
            val decodedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)
            val decryptedBytes = ByteArray(decodedBytes.size)
            
            for (i in decodedBytes.indices) {
                decryptedBytes[i] = (decodedBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
            }
            return String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            return encryptedText
        }
    }

    /**
     * Simulates / implements protected storage matching EncryptedSharedPreferences.
     */
    fun saveSecureSetting(context: Context, key: String, value: String) {
        val encryptedValue = encryptZoomLink(value)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(key, encryptedValue)
            .apply()
    }

    fun getSecureSetting(context: Context, key: String, defaultValue: String = ""): String {
        val encryptedValue = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(key, null) ?: return defaultValue
        return decryptZoomLink(encryptedValue)
    }
}
