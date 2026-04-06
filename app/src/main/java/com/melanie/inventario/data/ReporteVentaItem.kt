package com.melanie.inventario.data


data class ReporteVentaItem(
    val nombre: String,
    val totalCantidadVendida: Double,
    val totalIngresos: Double,
    val notas: String,
    val fecha: Long // <-- AGREGAR ESTA LÍNEA PARA LA HORA
)