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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
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

    // En InventarioViewModel.kt
    fun registrarVentaPlato(
        nombreInsumoBase: String,
        precioVenta: Double,
        cantidadADescontar: Double = 1.0, // <-- Ahora sí existe este parámetro
        notas: String = ""               // <-- Para las notas de "arroz", "papa", etc.
    ) {
        viewModelScope.launch {
            val insumo = dao.buscarInsumoPorNombre(nombreInsumoBase)

            // Verificamos que el insumo exista y tenga stock suficiente
            if (insumo != null && insumo.stockActual >= cantidadADescontar) {
                val fecha = System.currentTimeMillis()

                // 1. Registramos la venta (con el campo 'notas' si ya actualizaste tu entidad Venta)
                dao.registrarVenta(
                    Venta(
                        insumoId = insumo.id,
                        cantidadVendida = cantidadADescontar,
                        precioTotal = precioVenta,
                        fechaEnMilisegundos = fecha,
                        // notas = notas // Descomenta esto cuando actualices tu data class Venta
                    )
                )

                // 2. Registramos el consumo para el reporte
                dao.registrarConsumo(
                    Consumo(
                        insumoId = insumo.id,
                        cantidadUsada = cantidadADescontar,
                        fechaEnMilisegundos = fecha
                    )
                )

                // 3. Restamos el stock exacto
                dao.restarStock(insumo.id, cantidadADescontar)
            }
        }
    }

    fun prepararMaracuya(insumoFruta: Insumo, kilosUsados: Double, litrosObtenidos: Double) {
        viewModelScope.launch {
            val fecha = System.currentTimeMillis()

            // 1. Descontamos los kilos de fruta
            dao.registrarConsumo(Consumo(insumoId = insumoFruta.id, cantidadUsada = kilosUsados, fechaEnMilisegundos = fecha))
            dao.restarStock(insumoFruta.id, kilosUsados)

            // 2. Sumamos los litros a la categoría "Maracuyá" (la que usa la pantalla de ventas)
            val nombreDestino = "Maracuyá"
            val existente = dao.buscarInsumoPorNombre(nombreDestino)

            if (existente != null) {
                dao.sumarStock(existente.id, litrosObtenidos)
            } else {
                dao.agregarInsumo(Insumo(
                    nombre = nombreDestino,
                    unidad = "Litros",
                    stockActual = litrosObtenidos,
                    costo = 0.0,
                    stockMinimo = 2.0
                ))
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
    // Dentro de la clase InventarioViewModel
    fun obtenerTotalGastadoPeriodo(fechaInicio: Long, fechaFin: Long): StateFlow<Double?> {
        return dao.obtenerTotalGastadoPeriodo(fechaInicio, fechaFin)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0.0
            )
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
    // En InventarioViewModel.kt
    fun obtenerTotalGastado(fechaInicio: Long, fechaFin: Long): Flow<Double?> {
        // Puedes crear una query simple en el DAO que sume la columna costo
        return dao.obtenerTotalGastadoPeriodo(fechaInicio, fechaFin)
    }
    fun obtenerReporteCompras(fechaInicio: Long, fechaFin: Long): Flow<List<ReporteCompraItem>> {
        return dao.obtenerReporteComprasPeriodo(fechaInicio, fechaFin)
    }

    fun obtenerReporteVentas(fechaInicio: Long, fechaFin: Long): Flow<List<ReporteVentaItem>> {
        return dao.obtenerReporteVentasPeriodo(fechaInicio, fechaFin)
    }
    fun convertirHuesitos(insumoBolsaHueso: Insumo, kilosUsados: Double, cantidadHuesosSacados: Int) {
        viewModelScope.launch {
            val fecha = System.currentTimeMillis()

            // 1. Registramos el consumo de la bolsa (para el reporte de gastos/salidas)
            dao.registrarConsumo(Consumo(insumoId = insumoBolsaHueso.id, cantidadUsada = kilosUsados, fechaEnMilisegundos = fecha))

            // 2. Restamos el kilo del inventario de "Bolsas"
            dao.restarStock(insumoBolsaHueso.id, kilosUsados)

            // 3. Lo convertimos a la categoría de presas para TALLARÍN
            val nombreDestino = "Presas para Tallarín"
            val existente = dao.buscarInsumoPorNombre(nombreDestino)

            if (existente != null) {
                dao.sumarStock(existente.id, cantidadHuesosSacados.toDouble())
            } else {
                dao.agregarInsumo(Insumo(
                    nombre = nombreDestino,
                    unidad = "Unidades",
                    stockActual = cantidadHuesosSacados.toDouble(),
                    costo = 0.0,
                    stockMinimo = 10.0
                ))
            }
        }
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