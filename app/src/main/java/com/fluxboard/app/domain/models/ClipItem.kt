package com.fluxboard.app.domain.models

import java.util.Date
import java.util.UUID

/**
 * Entidad de negocio principal de FluxBoard.
 *
 * Modelo puro de Kotlin: sin anotaciones de Room, sin directivas de Supabase.
 * Representa un recorte del portapapeles tal como lo consume la capa de dominio.
 */
data class ClipItem(
    val id: UUID = UUID.randomUUID(),
    val textContent: String,
    val isPinned: Boolean = false,
    val isSynced: Boolean = false,
    val createdAt: Date = Date()
)
