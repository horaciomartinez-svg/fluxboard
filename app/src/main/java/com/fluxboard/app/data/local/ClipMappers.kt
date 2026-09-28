package com.fluxboard.app.data.local

import com.fluxboard.app.domain.models.ClipItem

/**
 * Mappers bidireccionales entre la capa de datos ([ClipEntity]) y el dominio
 * ([ClipItem]). Aíslan al dominio de las anotaciones y tipos de Room.
 */

fun ClipEntity.toDomain(): ClipItem = ClipItem(
    id = id,
    textContent = textContent,
    isPinned = isPinned,
    isSynced = isSynced,
    createdAt = createdAt
)

fun ClipItem.toEntity(): ClipEntity = ClipEntity(
    id = id,
    textContent = textContent,
    isPinned = isPinned,
    isSynced = isSynced,
    createdAt = createdAt
)
