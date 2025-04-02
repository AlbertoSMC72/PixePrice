package com.example.pixelprice.core.local.chapters.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "title",)
    val title: String,
    @ColumnInfo(name = "content",)
    val content: String,
    @ColumnInfo(name = "created_at",)
    val created_at: String
)