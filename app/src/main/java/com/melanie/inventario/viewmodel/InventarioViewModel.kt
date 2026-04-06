package com.melanie.inventario.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.melanie.inventario.data.Compra
import com.melanie.inventario.data.Consumo
import com.melanie.inventario.data.Insumo
import com.melanie.inventario.data.InventarioDao
import com.melanie.inventario.data.ReporteCompraItem
import com.melanie.inventario.data.ReporteConsumoItem
import com.melanie.inventario.data.ReporteVentaItem
import com.melanie.inventario.data.Venta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class InventarioViewModel(private val dao: InventarioDao) : ViewModel() {

    val todosLosInsumos: Flow<List<Insumo>> = dao.obtenerTodosLosInsumos()

    fun agregarInsumo(nombre: String, unidad: String, stockInicial: Double, costo: Double, stockMinimo: Double = 2.0) {
        viewModelScope.launch {
            dao.agregarInsumo(Insumo(
                nombre = nombre,
                unidad = unidad,
                stockActual = stockInicial,
                costo = costo,
                stockMinimo = stockMinimo
            ))
        }
    }

    // REGISTRO DE VENTAS (Con descuento automático de stock y registro de consumo)
    fun registrarVentaPlato(nombreInsumoBase: String, precioVenta: Double) {
        viewModelScope.launch {
            val insumo = dao.buscarInsumoPorNombre(nombreInsumoBase)
            if (insumo != null && insumo.stockActual >= 1) {
                val fecha = System.currentTimeMillis()

                // 1. Registra el ingreso de dinero
                dao.registrarVenta(Venta(insumoId = insumo.id, cantidadVendida = 1.0, precioTotal = precioVenta, fechaEnMilisegundos = fecha))

                // 2. Registra la salida para el reporte de consumo
                dao.registrarConsumo(Consumo(insumoId = insumo.id, cantidadUsada = 1.0, fechaEnMilisegundos = fecha))

                // 3. Resta del inventario
                dao.restarStock(insumo.id, 1.0)
            }
        }
    }

    // REGISTRO DE CONSUMO MANUAL (Mermas o gastos directos)
    fun registrarConsumo(insumo: Insumo, cantidadAUsar: Double) {
        viewModelScope.launch {
            val fechaActual = System.currentTimeMillis()
            dao.registrarConsumo(Consumo(insumoId = insumo.id, cantidadUsada = cantidadAUsar, fechaEnMilisegundos = fechaActual))

            val stockRestante = insumo.stockActual - cantidadAUsar
            if (stockRestante <= 0) {
                dao.restarStock(insumo.id, insumo.stockActual)
            } else {
                dao.restarStock(insumo.id, cantidadAUsar)
            }
        }
    }

    // REGISTRO DE COMPRAS (Suma al stock y guarda en reporte de gastos)
    fun recargarInsumo(insumo: Insumo, cantidadComprada: Double, costoCompra: Double) {
        viewModelScope.launch {
            val fechaActual = System.currentTimeMillis()
            dao.registrarCompra(Compra(nombreInsumo = insumo.nombre, unidad = insumo.unidad, cantidad = cantidadComprada, costo = costoCompra, fechaEnMilisegundos = fechaActual))
            dao.recargarStock(insumo.id, cantidadComprada, costoCompra)
        }
    }

    // --- FUNCIONES DE REPORTES (Las que pedía la pantalla) ---
    fun obtenerReporteConsumo(fechaInicio: Long, fechaFin: Long): Flow<List<ReporteConsumoItem>> {
        return dao.obtenerReporteConsumoPeriodo(fechaInicio, fechaFin)
    }

    fun obtenerReporteCompras(fechaInicio: Long, fechaFin: Long): Flow<List<ReporteCompraItem>> {
        return dao.obtenerReporteComprasPeriodo(fechaInicio, fechaFin)
    }

    fun obtenerReporteVentas(fechaInicio: Long, fechaFin: Long): Flow<List<ReporteVentaItem>> {
        return dao.obtenerReporteVentasPeriodo(fechaInicio, fechaFin)
    }

    // --- GESTIÓN DE INSUMOS ---
    fun actualizarInsumo(insumoActual: Insumo, nuevoNombre: String, nuevoStockMinimo: Double, nuevoCosto: Double) {
        viewModelScope.launch {
            val insumoModificado = insumoActual.copy(nombre = nuevoNombre, stockMinimo = nuevoStockMinimo, costo = nuevoCosto)
            dao.actualizarInsumo(insumoModificado)
        }
    }

    fun eliminarInsumoDefinitivo(insumo: Insumo) {
        viewModelScope.launch {
            dao.eliminarInsumo(insumo.id)
        }
    }

    // --- FUNCIÓN PARA DESPRESAR POLLO ---
    fun despresarPollo(insumoPolloEntero: Insumo, kilosUsados: Double, presasCaldo: Int, presasSalchipollo: Int) {
        viewModelScope.launch {
            val fecha = System.currentTimeMillis()

            // 1. Restar del pollo entero y registrar consumo del pollo pesado
            dao.registrarConsumo(Consumo(insumoId = insumoPolloEntero.id, cantidadUsada = kilosUsados, fechaEnMilisegundos = fecha))
            dao.restarStock(insumoPolloEntero.id, kilosUsados)

            // 2. Gestionar presas para Caldo
            if (presasCaldo > 0) {
                val nombreCaldo = "Presas para Caldo"
                val existente = dao.buscarInsumoPorNombre(nombreCaldo)
                if (existente != null) {
                    dao.sumarStock(existente.id, presasCaldo.toDouble())
                } else {
                    dao.agregarInsumo(Insumo(nombre = nombreCaldo, unidad = "Unidades", stockActual = presasCaldo.toDouble(), costo = 0.0, stockMinimo = 5.0))
                }
            }

            // 3. Gestionar presas para Salchipollo
            if (presasSalchipollo > 0) {
                val nombreSalchi = "Presas para Salchipollo"
                val existente = dao.buscarInsumoPorNombre(nombreSalchi)
                if (existente != null) {
                    dao.sumarStock(existente.id, presasSalchipollo.toDouble())
                } else {
                    dao.agregarInsumo(Insumo(nombre = nombreSalchi, unidad = "Unidades", stockActual = presasSalchipollo.toDouble(), costo = 0.0, stockMinimo = 5.0))
                }
            }
        }
    }
}

// FACTORY
class InventarioViewModelFactory(private val dao: InventarioDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventarioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventarioViewModel(dao) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}