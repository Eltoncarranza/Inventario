package com.melanie.inventario.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.melanie.inventario.data.Consumo
import com.melanie.inventario.data.Insumo
import com.melanie.inventario.data.InventarioDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class InventarioViewModel(private val dao: InventarioDao) : ViewModel() {

    val todosLosInsumos: Flow<List<Insumo>> = dao.obtenerTodosLosInsumos()

    fun agregarInsumo(nombre: String, unidad: String, stockInicial: Double, costo: Double) {
        viewModelScope.launch {
            dao.agregarInsumo(Insumo(nombre = nombre, unidad = unidad, stockActual = stockInicial, costo = costo))
        }
    }

    fun registrarConsumo(insumo: Insumo, cantidadAUsar: Double) {
        viewModelScope.launch {
            val fechaActual = System.currentTimeMillis()

            // 1. Guardamos el registro para tus reportes
            dao.registrarConsumo(Consumo(
                insumoId = insumo.id,
                cantidadUsada = cantidadAUsar,
                fechaEnMilisegundos = fechaActual
            ))

            // 2. Calculamos cuánto quedará
            val stockRestante = insumo.stockActual - cantidadAUsar

            // 3. Regla automática: Si se acaba, se borra. Si sobra, se resta.
            if (stockRestante <= 0) {
                dao.eliminarInsumo(insumo.id)
            } else {
                dao.restarStock(insumo.id, cantidadAUsar)
            }
        }
    }
}

class InventarioViewModelFactory(private val dao: InventarioDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventarioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventarioViewModel(dao) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}