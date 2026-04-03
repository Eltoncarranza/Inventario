package com.melanie.inventario.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ventas")
data class Venta(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val platillo: String, // Ej: "Salchipollo", "Caldo de pollo"
    val precio: Double,
    val fechaEnMilisegundos: Long
)