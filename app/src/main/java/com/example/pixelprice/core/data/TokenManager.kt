package com.example.pixelprice.core.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.VisibleForTesting
import java.util.concurrent.TimeUnit
import androidx.core.content.edit

object TokenManager {
    private const val PREFS_NAME = "pixelprice_auth_prefs"
    private const val KEY_TOKEN = "jwt_auth_token"
    private const val KEY_TOKEN_EXPIRY_TIMESTAMP = "jwt_token_expiry_timestamp"
    private const val KEY_USER_ID = "logged_in_user_id"
    private const val KEY_USERNAME = "logged_in_username"

    private val SESSION_DURATION_MS = TimeUnit.HOURS.toMillis(12)

    @SuppressLint("StaticFieldLeak")
    private lateinit var prefs: SharedPreferences

    private var isInitialized = false
    private val lock = Any()

    fun initialize(context: Context) {
        if (!isInitialized) {
            synchronized(lock) {
                if (!isInitialized) {
                    prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    isInitialized = true
                    Log.d("TokenManager", "TokenManager Inicializado.")
                }
            }
        }
    }

    private fun requirePrefs(): SharedPreferences {
        check(isInitialized) { "TokenManager no ha sido inicializado. Llama a initialize() en Application.onCreate." }
        return prefs
    }

    fun saveAuthData(token: String, userId: Int, username: String?) {
        Log.d("TokenManager", "Guardando datos de autenticación para usuario ID: $userId")
        val expiryTimestamp = System.currentTimeMillis() + SESSION_DURATION_MS
        requirePrefs().edit().apply {
            putString(KEY_TOKEN, token)
            putLong(KEY_TOKEN_EXPIRY_TIMESTAMP, expiryTimestamp)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username ?: "")
            apply()
        }
        Log.d("TokenManager", "Token expira en: ${java.util.Date(expiryTimestamp)}")
    }

    fun getToken(): String? {
        val prefs = requirePrefs()
        val token = prefs.getString(KEY_TOKEN, null)
        val expiryTimestamp = prefs.getLong(KEY_TOKEN_EXPIRY_TIMESTAMP, 0)
        val currentTimestamp = System.currentTimeMillis()

        if (token == null || expiryTimestamp == 0L) {
            return null
        }

        if (currentTimestamp >= expiryTimestamp) {
            Log.w("TokenManager", "Token JWT encontrado pero ha expirado. Limpiando...")
            clearToken()
            return null
        }

        return token
    }

    fun getUserId(): Int {
        return if(isInitialized && ::prefs.isInitialized) prefs.getInt(KEY_USER_ID, 0) else 0
    }

    fun getUsername(): String? {
        return if(isInitialized && ::prefs.isInitialized) prefs.getString(KEY_USERNAME, null) else null
    }


    fun clearToken() {
        Log.d("TokenManager", "Borrando todos los datos de autenticación (Token, Expiración, UserInfo).")
        if (isInitialized && ::prefs.isInitialized) {
            requirePrefs().edit() {
                remove(KEY_TOKEN)
                    .remove(KEY_TOKEN_EXPIRY_TIMESTAMP)
                    .remove(KEY_USER_ID)
                    .remove(KEY_USERNAME)
            }
        }
        UserInfoProvider.clearUserInfo()
    }

    fun isSessionActive(): Boolean {
        return getToken() != null
    }

    @VisibleForTesting
    internal fun resetForTest() {
        synchronized(lock) {
            isInitialized = false
            if (::prefs.isInitialized) {
                prefs.edit().clear().apply()
            }
        }
    }
}