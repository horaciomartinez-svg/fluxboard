package com.fluxboard.app.data.local

import androidx.room.TypeConverter
import java.util.Date
import java.util.UUID

/**
 * Conversores de tipo para Room.
 *
 * Room no soporta nativamente [UUID] ni [Date], por lo que se persisten como
 * TEXT y INTEGER (epoch millis) respectivamente.
 */
class Converters {

    @TypeConverter
    fun fromUuid(value: UUID?): String? = value?.toString()

    @TypeConverter
    fun toUuid(value: String?): UUID? = value?.let(UUID::fromString)

    @TypeConverter
    fun fromDate(value: Date?): Long? = value?.time

    @TypeConverter
    fun toDate(value: Long?): Date? = value?.let(::Date)
}
