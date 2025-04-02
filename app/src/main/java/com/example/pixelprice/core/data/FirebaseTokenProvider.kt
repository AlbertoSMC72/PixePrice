package com.example.pixelprice.core.data

object FirebaseTokenProvider {
    private var _firebaseToken: String? = null

    var firebaseToken: String?
        get() = _firebaseToken
        set(value) { _firebaseToken = value }
}
