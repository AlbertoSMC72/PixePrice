package com.example.pixelprice.features.projects.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "projects",
    indices = [Index(value = ["name"], unique = true), Index(value = ["user_id"])]
)
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "capital")
    val capital: Double,

    @ColumnInfo(name = "is_self_made")
    val isSelfMade: Boolean,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "image_uri")
    val imageUri: String? = null,

    @ColumnInfo(name = "last_quotation_id")
    val lastQuotationId: Int? = null,

    @ColumnInfo(name = "has_pending_quotation")
    val hasPendingQuotation: Boolean = false
)