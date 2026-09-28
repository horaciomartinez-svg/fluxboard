package com.fluxboard.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * Entidad de persistencia local de un recorte (Room / SQLite).
 *
 * Esta clase es exclusiva de la capa de datos. Nunca debe filtrarse hacia el
 * dominio: para ello existen los mappers en [ClipMappers].
 */
@Entity(
    tableName = "clips",
    indices = [
        Index(value = ["created_at"]),
        Index(value = ["is_synced"])
    ]
)
data class ClipEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: UUID,

    @ColumnInfo(name = "text_content")
    val textContent: String,

    @ColumnInfo(name = "is_pinned", defaultValue = "0")
    val isPinned: Boolean = false,

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    val isSynced: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
)
