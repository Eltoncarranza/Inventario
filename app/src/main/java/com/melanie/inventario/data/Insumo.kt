package com.melanie.inventario.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "insumos")
data class Insumo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val unidad: String, // kg, litros, unidades, etc.
    val stockActual: Double,
    val costo: Double // Agregamos el costo para tus reportes de inversión
)