package com.example.pixelprice.core.data

import android.annotation.SuppressLint // Necesario para SharedPreferences estáticas
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.VisibleForTesting // Opcional para pruebas
import java.util.concurrent.TimeUnit
import androidx.core.content.edit

object TokenManager {
    private const val PREFS_NAME = "pixelprice_auth_prefs"
    private const val KEY_TOKEN = "jwt_auth_token"
    private const val KEY_TOKEN_EXPIRY_TIMESTAMP = "jwt_token_expiry_timestamp"
    private const val KEY_USER_ID = "logged_in_user_id"
    private const val KEY_USERNAME = "logged_in_username"

    private val SESSION_DURATION_MS = TimeUnit.HOURS.toMillis(12)

    //SuppressLint justificado porque se inicializa en Application.onCreate
    @SuppressLint("StaticFieldLeak")
    private lateinit var prefs: SharedPreferences // Usar lateinit

    // Bandera para asegurar inicialización única
    private var isInitialized = false
    private val lock = Any() // Para sincronización segura de inicialización

    // *** NUEVO: Función de inicialización ***
    fun initialize(context: Context) {
        // Doble check locking para seguridad en inicialización
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

    // Helper interno para obtener prefs asegurando inicialización
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
            // Guardar username, usar un valor por defecto si es nulo para evitar errores
            putString(KEY_USERNAME, username ?: "")
            apply() // Usar apply para guardar asíncronamente
        }
        Log.d("TokenManager", "Token expira en: ${java.util.Date(expiryTimestamp)}")
    }

    // *** MODIFICADO: getToken ahora verifica la expiración ***
    fun getToken(): String? {
        val prefs = requirePrefs()
        val token = prefs.getString(KEY_TOKEN, null)
        val expiryTimestamp = prefs.getLong(KEY_TOKEN_EXPIRY_TIMESTAMP, 0)
        val currentTimestamp = System.currentTimeMillis()

        if (token == null || expiryTimestamp == 0L) {
            // No hay token o timestamp guardado
            return null
        }

        if (currentTimestamp >= expiryTimestamp) {
            // Token expirado
            Log.w("TokenManager", "Token JWT encontrado pero ha expirado. Limpiando...")
            clearToken() // Limpiar datos expirados
            return null
        }

        // Token válido y no expirado
        // Log.d("TokenManager", "Token JWT válido recuperado.") // Log opcional
        return token
    }

    // *** NUEVOS MÉTODOS: Para obtener ID y Username ***
    fun getUserId(): Int {
        // Devuelve 0 si no está guardado o no está inicializado
        return if(isInitialized && ::prefs.isInitialized) prefs.getInt(KEY_USER_ID, 0) else 0
    }

    fun getUsername(): String? {
        // Devuelve null si no está guardado
        return if(isInitialized && ::prefs.isInitialized) prefs.getString(KEY_USERNAME, null) else null
    }


    // *** MODIFICADO: clearToken ahora limpia todo ***
    fun clearToken() {
        Log.d("TokenManager", "Borrando todos los datos de autenticación (Token, Expiración, UserInfo).")
        if (isInitialized && ::prefs.isInitialized) { // Asegurarse que prefs está listo
            requirePrefs().edit() {
                remove(KEY_TOKEN)
                    .remove(KEY_TOKEN_EXPIRY_TIMESTAMP)
                    .remove(KEY_USER_ID)
                    .remove(KEY_USERNAME)
            }
        }
        // Llamar a UserInfoProvider para mantener consistencia (aunque ya no se guarde aquí)
        // Esto limpia la copia en memoria si alguien la está usando.
        UserInfoProvider.clearUserInfo()
    }

    // *** Opcional: Método para verificar si hay sesión activa (token válido) ***
    fun isSessionActive(): Boolean {
        return getToken() != null
    }

    // Opcional: Para pruebas unitarias si necesitas resetear
    @VisibleForTesting
    internal fun resetForTest() {
        synchronized(lock) {
            isInitialized = false
            // prefs = null // No puedes reasignar lateinit a null, pero puedes limpiar las prefs
            if (::prefs.isInitialized) { // Verifica si fue inicializada antes de limpiar
                prefs.edit().clear().apply()
            }
        }
    }
}