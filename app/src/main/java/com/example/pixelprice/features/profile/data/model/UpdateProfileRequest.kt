package com.example.pixelprice.features.profile.data.model

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("name")
    val name: String?,

    @SerializedName("last_name")
    val lastName: String?
)