package com.example.pixelprice.features.quotations.data.remote.model

import com.google.gson.annotations.SerializedName

data class QuotationListItemDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val projectName: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("status")
    val status: String?
)