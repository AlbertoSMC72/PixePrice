package com.example.pixelprice.core.local

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.annotation.VisibleForTesting

object DatabaseProvider {
    private const val DATABASE_NAME = "pixelprice_projects.db"

    @SuppressLint("StaticFieldLeak")
    private lateinit var projectDatabase: ProjectDatabase
    private var isInitialized = false
    private val lock = Any()

    fun initialize(context: Context) {
        if (!isInitialized) {
            synchronized(lock) {
                if (!isInitialized) {
                    projectDatabase = Room.databaseBuilder(
                        context.applicationContext,
                        ProjectDatabase::class.java,
                        DATABASE_NAME
                    ).fallbackToDestructiveMigration()
                        .build()
                    isInitialized = true
                    Log.d("DatabaseProvider", "ProjectDatabase Inicializado.")
                }
            }
        }
    }

    fun getAppDataBase(): ProjectDatabase {
        check(isInitialized) { "DatabaseProvider no ha sido inicializado. Llama a initialize() en Application.onCreate." }
        return projectDatabase
    }

    private fun destroyAppDatabase() {
        synchronized(lock) {
            if (isInitialized && ::projectDatabase.isInitialized) {
                Log.d("DatabaseProvider", "Cerrando y destruyendo instancia de ProjectDatabase.")
                if (projectDatabase.isOpen) {
                    projectDatabase.close()
                }
                isInitialized = false
            }
        }
    }

    @VisibleForTesting
    internal fun resetForTest() {
        destroyAppDatabase()
    }
}