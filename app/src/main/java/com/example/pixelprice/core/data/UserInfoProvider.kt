package com.example.pixelprice.core.data

import android.util.Log

object UserInfoProvider {
    private var _userID: Int = 0
    private var _username: String? = null

    var userID: Int
        get() = _userID
        set(value) { _userID = value }

    var username: String?
        get() = _username
        set(value) { _username = value }

    // *** NUEVO: Método para limpiar la info del usuario ***
    fun clearUserInfo() {
        Log.d("UserInfoProvider", "Limpiando información del usuario.")
        _userID = 0
        _username = null
    }

    // Opcional: Método para establecer toda la info a la vez
    fun setUserInfo(id: Int, name: String?) {
        _userID = id
        _username = name
    }
}