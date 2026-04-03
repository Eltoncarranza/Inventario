package com.melanie.inventario.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InventarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarInsumo(insumo: Insumo)

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
}
