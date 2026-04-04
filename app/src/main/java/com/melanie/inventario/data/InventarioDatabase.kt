package com.melanie.inventario.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Insumo::class, Venta::class, Consumo::class, Compra::class], // ¡Deben estar las 4 aquí!
    version = 2, // Sube la versión a 2 para que no choque con la BD vieja
    exportSchema = false
)
abstract class InventarioDatabase : RoomDatabase() {

    abstract fun inventarioDao(): InventarioDao

    companion object {
        @Volatile
        private var INSTANCE: InventarioDatabase? = null

        fun getDatabase(context: Context): InventarioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InventarioDatabase::class.java,
                    "inventario_database"
                )
                    .fallbackToDestructiveMigration() // <--- ¡AGREGA ESTA LÍNEA AQUÍ!
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}