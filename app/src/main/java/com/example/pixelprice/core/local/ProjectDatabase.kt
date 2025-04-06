package com.example.pixelprice.core.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pixelprice.features.projects.data.local.ProjectDao
import com.example.pixelprice.features.projects.data.local.ProjectEntity

@Database(entities = [ProjectEntity::class], version = 1, exportSchema = false)
abstract class ProjectDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}