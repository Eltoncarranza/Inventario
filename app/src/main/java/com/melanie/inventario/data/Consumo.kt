package com.melanie.inventario.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "consumos")
data class Consumo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val insumoId: Int,
    val cantidadUsada: Double,
    val fechaEnMilisegundos: Long
)