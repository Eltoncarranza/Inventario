package com.melanie.inventario.data


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InventarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarInsumo(insumo: Insumo)

    @Insert
    suspend fun registrarVenta(venta: Venta)

    @Query("""
        SELECT i.nombre as nombreInsumo, SUM(v.cantidadVendida) as totalCantidad, SUM(v.precioTotal) as totalIngresos 
        FROM ventas v 
        INNER JOIN insumos i ON v.insumoId = i.id 
        WHERE v.fechaEnMilisegundos BETWEEN :fechaInicio AND :fechaFin 
        GROUP BY i.id
    """)
    fun obtenerReporteVentasPeriodo(fechaInicio: Long, fechaFin: Long): Flow<List<ReporteVentaItem>>

    @Insert
    suspend fun registrarConsumo(consumo: Consumo)

    @Query("SELECT * FROM insumos")
    fun obtenerTodosLosInsumos(): Flow<List<Insumo>>

    @Query("SELECT SUM(cantidadUsada) FROM consumos WHERE insumoId = :insumoId AND fechaEnMilisegundos >= :fechaInicio AND fechaEnMilisegundos <= :fechaFin")
    fun obtenerConsumoPorPeriodo(insumoId: Int, fechaInicio: Long, fechaFin: Long): Flow<Double?>

    // NUEVO: Función para restar la cantidad usada al stock actual
    @Query("UPDATE insumos SET stockActual = stockActual - :cantidadUsada WHERE id = :insumoId")
    suspend fun restarStock(insumoId: Int, cantidadUsada: Double)

    // NUEVO: Función para eliminar el insumo si se acaba
    @Query("DELETE FROM insumos WHERE id = :insumoId")
    suspend fun eliminarInsumo(insumoId: Int)

    // --- NUEVAS FUNCIONES PARA EL CONVERTIDOR ---

    // Busca si ya existe un insumo con un nombre específico (ej. "Presa de Caldo")
    @Query("SELECT * FROM insumos WHERE nombre = :nombreInsumo LIMIT 1")
    suspend fun buscarInsumoPorNombre(nombreInsumo: String): Insumo?

    // Permite sumar stock a un insumo que ya existe
    @Query("UPDATE insumos SET stockActual = stockActual + :cantidadASumar WHERE id = :insumoId")
    suspend fun sumarStock(insumoId: Int, cantidadASumar: Double)

    // NUEVO: Función para cuando vas al mercado y compras más de algo que ya tienes
    @Query("UPDATE insumos SET stockActual = stockActual + :cantidad, costo = costo + :nuevoCosto WHERE id = :insumoId")
    suspend fun recargarStock(insumoId: Int, cantidad: Double, nuevoCosto: Double)

    // NUEVO: Permite editar un insumo completo (cambiar nombre, alarma, etc.)
    @Update
    suspend fun actualizarInsumo(insumo: Insumo)

    @Insert
    suspend fun registrarCompra(compra: Compra)

    @Query("""
        SELECT nombreInsumo as nombre, unidad, SUM(cantidad) as totalComprado, SUM(costo) as costoTotal 
        FROM compras 
        WHERE fechaEnMilisegundos BETWEEN :fechaInicio AND :fechaFin 
        GROUP BY nombreInsumo, unidad
    """)
    fun obtenerReporteComprasPeriodo(fechaInicio: Long, fechaFin: Long): Flow<List<ReporteCompraItem>>
}
