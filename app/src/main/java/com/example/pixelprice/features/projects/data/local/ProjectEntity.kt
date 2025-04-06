package com.example.pixelprice.features.projects.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "projects",
    // Índice para asegurar que el nombre sea único y para búsquedas rápidas por nombre o userId
    indices = [Index(value = ["name"], unique = true), Index(value = ["user_id"])]
)
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "user_id") // Id del usuario al que pertenece (viene de UserInfoProvider)
    val userId: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "capital")
    val capital: Double,

    @ColumnInfo(name = "is_self_made")
    val isSelfMade: Boolean,

    @ColumnInfo(name = "created_at") // Guardar timestamp de creación
    val createdAt: Long = System.currentTimeMillis(), // Valor por defecto

    @ColumnInfo(name = "image_uri") // Guarda el URI local de la imagen seleccionada (antes de subir)
    val imageUri: String? = null,

    @ColumnInfo(name = "last_quotation_id") // Guarda el ID de la última cotización *solicitada* o *exitosa* (opcional)
    val lastQuotationId: Int? = null,

    @ColumnInfo(name = "has_pending_quotation") // Indica si se solicitó cotización y aún no hay resultado (opcional)
    val hasPendingQuotation: Boolean = false
)