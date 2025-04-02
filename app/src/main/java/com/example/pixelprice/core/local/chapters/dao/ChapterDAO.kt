package com.example.pixelprice.core.local.chapters.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pixelprice.core.local.chapters.entities.ChapterEntity

@Dao
interface ChapterDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveChapter(author: ChapterEntity)

    @Query("SELECT * FROM chapters")
    suspend fun getAllChapters(): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE id = :chapterId LIMIT 1")
    suspend fun getChapterById(chapterId: Int): ChapterEntity?
}