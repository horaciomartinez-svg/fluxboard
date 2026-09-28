package com.fluxboard.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Base de datos local de FluxBoard (Room / SQLite).
 *
 * Fuente única de verdad (Single Source of Truth) para la estrategia
 * Offline-First: tanto la app principal como el servicio de teclado acceden
 * a los datos a través de este punto.
 */
@Database(
    entities = [ClipEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FluxDatabase : RoomDatabase() {

    abstract fun clipDao(): ClipDao
}
