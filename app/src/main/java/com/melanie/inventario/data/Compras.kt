package com.melanie.inventario.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "compras")
data class Compra(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombreInsumo: String,
    val unidad: String,
    val cantidad: Double,
    val costo: Double,
    val fechaEnMilisegundos: Long
)