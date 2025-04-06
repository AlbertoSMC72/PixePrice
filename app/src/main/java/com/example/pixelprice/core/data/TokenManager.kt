package com.example.pixelprice.core.data

import android.annotation.SuppressLint // Necesario para SharedPreferences estáticas
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.VisibleForTesting // Opcional para pruebas

object TokenManager {
    private const val PREFS_NAME = "pixelprice_auth_prefs"
    private const val KEY_TOKEN = "jwt_auth_token"

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

    // *** MODIFICADO: Métodos ya no necesitan context ***
    fun saveToken(token: String) {
        Log.d("TokenManager", "Guardando token JWT.")
        requirePrefs().edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        val token = requirePrefs().getString(KEY_TOKEN, null)
        // Log.d("TokenManager", "Recuperando token JWT: ${token != null}") // Log opcional
        return token
    }

    fun clearToken() {
        Log.d("TokenManager", "Borrando token JWT.")
        requirePrefs().edit().remove(KEY_TOKEN).apply()
        // Llamar a UserInfoProvider para mantener consistencia
        UserInfoProvider.clearUserInfo() // Usar el nuevo método
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