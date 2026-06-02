package com.stephan.mobil.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurityUtil {

    private const val PREFS_NAME = "secure_bank_prefs"
    private const val KEY_PIN = "user_pin"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_BIOMETRIC = "biometric_enabled"
    private const val KEY_DEVICE_ID = "device_id"

    private fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // --- PIN Management ---
    fun savePinCode(context: Context, pin: String) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit().putString(KEY_PIN, pin).apply()
    }

    fun verifyPinCode(context: Context, pin: String): Boolean {
        val prefs = getEncryptedSharedPreferences(context)
        val savedPin = prefs.getString(KEY_PIN, null)
        return savedPin == pin
    }
    
    fun hasPinCode(context: Context): Boolean {
        val prefs = getEncryptedSharedPreferences(context)
        return prefs.contains(KEY_PIN)
    }

    // --- Auth Token Management (Sanctum) ---
    fun saveAuthToken(context: Context, token: String) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(context: Context): String? {
        val prefs = getEncryptedSharedPreferences(context)
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        getEncryptedSharedPreferences(context).edit().putBoolean(KEY_BIOMETRIC, enabled).apply()
    }

    fun isBiometricEnabled(context: Context): Boolean {
        return getEncryptedSharedPreferences(context).getBoolean(KEY_BIOMETRIC, false)
    }

    fun getDeviceId(context: Context): String {
        val prefs = getEncryptedSharedPreferences(context)
        val existing = prefs.getString(KEY_DEVICE_ID, null)
        if (!existing.isNullOrBlank()) return existing

        val generated = java.util.UUID.randomUUID().toString()
        prefs.edit().putString(KEY_DEVICE_ID, generated).apply()
        return generated
    }

    fun clearData(context: Context) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit().clear().apply()
    }
}
