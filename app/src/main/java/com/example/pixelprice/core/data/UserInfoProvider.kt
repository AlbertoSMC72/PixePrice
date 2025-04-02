package com.example.pixelprice.core.data

object UserInfoProvider {
    private var _userID: Int = 0
    private var _username: String? = null

    var userID: Int
        get() = _userID
        set(value) { _userID = value }

    var username: String?
        get() = _username
        set(value) { _username = value }
}