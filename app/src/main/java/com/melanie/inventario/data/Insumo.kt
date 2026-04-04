package com.melanie.inventario.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "insumos")
data class Insumo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val unidad: String,
    val stockActual: Double,
    val costo: Double = 0.0,
    val stockMinimo: Double = 2.0 // NUEVO: La alarma personalizada para cada producto
)