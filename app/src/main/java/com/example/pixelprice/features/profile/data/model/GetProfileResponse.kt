package com.example.pixelprice.features.profile.data.model

import com.google.gson.annotations.SerializedName

data class GetProfileResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: ProfileData?
)

data class ProfileData(
    @SerializedName("user")
    val user: UserResponseInfo?
)


data class UserResponseInfo(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String?,

    @SerializedName("last_name")
    val lastName: String?,

    @SerializedName("email")
    val email: String
)