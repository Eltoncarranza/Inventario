package com.melanie.inventario.data

// Plantilla para el reporte de compras
data class ReporteCompraItem(
    val nombre: String,
    val unidad: String,
    val totalComprado: Double,
    val costoTotal: Double
)