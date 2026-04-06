package com.melanie.inventario.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InventarioDao {

    // --- INSUMOS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarInsumo(insumo: Insumo)

    @Query("SELECT * FROM insumos")
    fun obtenerTodosLosInsumos(): Flow<List<Insumo>>

    @Query("SELECT * FROM insumos WHERE nombre = :nombreInsumo LIMIT 1")
    suspend fun buscarInsumoPorNombre(nombreInsumo: String): Insumo?

    @Update
    suspend fun actualizarInsumo(insumo: Insumo)

    @Query("UPDATE insumos SET stockActual = stockActual - :cantidadUsada WHERE id = :insumoId")
    suspend fun restarStock(insumoId: Int, cantidadUsada: Double)

    @Query("UPDATE insumos SET stockActual = stockActual + :cantidadASumar WHERE id = :insumoId")
    suspend fun sumarStock(insumoId: Int, cantidadASumar: Double)

    @Query("UPDATE insumos SET stockActual = stockActual + :cantidad, costo = costo + :nuevoCosto WHERE id = :insumoId")
    suspend fun recargarStock(insumoId: Int, cantidad: Double, nuevoCosto: Double)

    @Query("DELETE FROM insumos WHERE id = :insumoId")
    suspend fun eliminarInsumo(insumoId: Int)

    // --- VENTAS ---
    @Insert
    suspend fun registrarVenta(venta: Venta)

    // REPORTE DE VENTAS DEFINITIVO (Muestra cada venta con su hora y nota)
    @Query("""
        SELECT 
            i.nombre AS nombre, 
            v.cantidadVendida AS totalCantidadVendida, 
            v.precioTotal AS totalIngresos,
            v.notas AS notas,
            v.fechaEnMilisegundos AS fecha
        FROM ventas AS v
        INNER JOIN insumos AS i ON v.insumoId = i.id
        WHERE v.fechaEnMilisegundos BETWEEN :fechaInicio AND :fechaFin
        ORDER BY v.fechaEnMilisegundos DESC
    """)
    fun obtenerReporteVentas(fechaInicio: Long, fechaFin: Long): Flow<List<ReporteVentaItem>>

    // --- COMPRAS ---
    @Insert
    suspend fun registrarCompra(compra: Compra)

    @Query("""
        SELECT nombreInsumo as nombre, unidad, SUM(cantidad) as totalComprado, SUM(costo) as costoTotal 
        FROM compras 
        WHERE fechaEnMilisegundos BETWEEN :fechaInicio AND :fechaFin 
        GROUP BY nombreInsumo, unidad
    """)
    fun obtenerReporteComprasPeriodo(fechaInicio: Long, fechaFin: Long): Flow<List<ReporteCompraItem>>

    @Query("SELECT SUM(costo) FROM compras WHERE fechaEnMilisegundos BETWEEN :fechaInicio AND :fechaFin")
    fun obtenerTotalGastadoPeriodo(fechaInicio: Long, fechaFin: Long): Flow<Double?>

    // --- CONSUMOS ---
    @Insert
    suspend fun registrarConsumo(consumo: Consumo)

    @Query("""
        SELECT i.nombre as nombre, i.unidad as unidad, SUM(c.cantidadUsada) as totalConsumido
        FROM consumos c
        INNER JOIN insumos i ON c.insumoId = i.id
        WHERE c.fechaEnMilisegundos BETWEEN :fechaInicio AND :fechaFin
        GROUP BY i.id
    """)
    fun obtenerReporteConsumoPeriodo(fechaInicio: Long, fechaFin: Long): Flow<List<ReporteConsumoItem>>

    @Query("SELECT SUM(cantidadUsada) FROM consumos WHERE insumoId = :insumoId AND fechaEnMilisegundos BETWEEN :fechaInicio AND :fechaFin")
    fun obtenerConsumoPorPeriodo(insumoId: Int, fechaInicio: Long, fechaFin: Long): Flow<Double?>
}