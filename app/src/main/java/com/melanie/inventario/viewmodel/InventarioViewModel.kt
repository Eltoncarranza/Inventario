package com.melanie.inventario.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.melanie.inventario.data.Compra
import com.melanie.inventario.data.Consumo
import com.melanie.inventario.data.Insumo
import com.melanie.inventario.data.InventarioDao
import com.melanie.inventario.data.ReporteCompraItem
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

    // NUEVO: Función inteligente de Punto de Venta
    fun registrarVentaPlato(nombreInsumoBase: String, precioVenta: Double) {
        viewModelScope.launch {
            // Buscamos la "caja" de donde sacar la presa (ej. "Presas para Salchipollo")
            val insumo = dao.buscarInsumoPorNombre(nombreInsumoBase)

            // Si la caja existe y tiene al menos 1 presa...
            if (insumo != null && insumo.stockActual >= 1) {
                val fecha = System.currentTimeMillis()

                dao.registrarVenta(
                    Venta(
                        insumoId = insumo.id,
                        cantidadVendida = 1.0,
                        precioTotal = precioVenta,
                        fechaEnMilisegundos = fecha
                    )
                )

                // 2. Le restamos 1 presa al inventario automáticamente
                val stockRestante = insumo.stockActual - 1.0
                if (stockRestante <= 0) {
                    dao.restarStock(insumo.id, insumo.stockActual)
                } else {
                    dao.restarStock(insumo.id, 1.0)
                }
            }
        }
    }

    // Para visualizar el gráfico de ventas después
    fun obtenerReporteVentas(fechaInicio: Long, fechaFin: Long): Flow<List<ReporteVentaItem>> {
        return dao.obtenerReporteVentasPeriodo(fechaInicio, fechaFin)
    }
    fun registrarConsumo(insumo: Insumo, cantidadAUsar: Double) {
        viewModelScope.launch {
            val fechaActual = System.currentTimeMillis()

            dao.registrarConsumo(Consumo(insumoId = insumo.id, cantidadUsada = cantidadAUsar, fechaEnMilisegundos = fechaActual))

            val stockRestante = insumo.stockActual - cantidadAUsar

            // CAMBIO: Ya no lo eliminamos. Si se acaba, lo dejamos exactamente en 0.
            if (stockRestante <= 0) {
                dao.restarStock(insumo.id, insumo.stockActual)
            } else {
                dao.restarStock(insumo.id, cantidadAUsar)
            }
        }
    }
    fun actualizarInsumo(insumoActual: Insumo, nuevoNombre: String, nuevoStockMinimo: Double, nuevoCosto: Double) {
        viewModelScope.launch {
            val insumoModificado = insumoActual.copy(
                nombre = nuevoNombre,
                stockMinimo = nuevoStockMinimo,
                costo = nuevoCosto
            )
            dao.actualizarInsumo(insumoModificado)
        }
    }
    fun eliminarInsumoDefinitivo(insumo: Insumo) {
        viewModelScope.launch {
            dao.eliminarInsumo(insumo.id)
        }
    }

    // NUEVA FUNCIÓN: Para agregar compras diarias (como el kilo de arroz)
    // 1. REEMPLAZA tu actual 'recargarInsumo' por esta:
    fun recargarInsumo(insumo: Insumo, cantidadComprada: Double, costoCompra: Double) {
        viewModelScope.launch {
            val fechaActual = System.currentTimeMillis()

            // Registramos la compra para tu nuevo reporte
            dao.registrarCompra(
                Compra(
                    nombreInsumo = insumo.nombre, unidad = insumo.unidad,
                    cantidad = cantidadComprada, costo = costoCompra,
                    fechaEnMilisegundos = fechaActual
                )
            )

            // Sumamos el stock físicamente al inventario
            dao.recargarStock(insumo.id, cantidadComprada, costoCompra)
        }
    }

    // 2. AGREGA esta nueva función para el reporte:
    fun obtenerReporteCompras(fechaInicio: Long, fechaFin: Long): Flow<List<ReporteCompraItem>> {
        return dao.obtenerReporteComprasPeriodo(fechaInicio, fechaFin)
    }
    // --- FUNCIÓN PARA DESPRESAR POLLO ---
    fun despresarPollo(
        insumoPolloEntero: Insumo,
        kilosUsados: Double,
        presasCaldo: Int,
        presasSalchipollo: Int
    ) {
        viewModelScope.launch {
            // 1. Restamos los kilos usados del pollo entero (usando tu regla automática)
            val stockRestante = insumoPolloEntero.stockActual - kilosUsados
            if (stockRestante <= 0) {
                dao.eliminarInsumo(insumoPolloEntero.id)
            } else {
                dao.restarStock(insumoPolloEntero.id, kilosUsados)
            }

            // 2. Gestionamos las presas para el Caldo de Pollo
            if (presasCaldo > 0) {
                val nombreCaldo = "Presas para Caldo"
                val insumoCaldoExistente = dao.buscarInsumoPorNombre(nombreCaldo)

                if (insumoCaldoExistente != null) {
                    // Si ya existe la categoría, le sumamos las nuevas presas
                    dao.sumarStock(insumoCaldoExistente.id, presasCaldo.toDouble())
                } else {
                    // Si no existe, creamos la categoría nueva
                    // Repartimos proporcionalmente el costo del pollo entero a estas presas
                    val proporcionCosto = (presasCaldo.toDouble() / (presasCaldo + presasSalchipollo)) * (insumoPolloEntero.costo * (kilosUsados / insumoPolloEntero.stockActual))

                    dao.agregarInsumo(Insumo(
                        nombre = nombreCaldo,
                        unidad = "Unidades",
                        stockActual = presasCaldo.toDouble(),
                        costo = if(proporcionCosto.isNaN()) 0.0 else proporcionCosto
                    ))
                }
            }

            // 3. Gestionamos las presas para el Salchipollo
            if (presasSalchipollo > 0) {
                val nombreSalchipollo = "Presas para Salchipollo"
                val insumoSalchipolloExistente = dao.buscarInsumoPorNombre(nombreSalchipollo)

                if (insumoSalchipolloExistente != null) {
                    dao.sumarStock(insumoSalchipolloExistente.id, presasSalchipollo.toDouble())
                } else {
                    val proporcionCosto = (presasSalchipollo.toDouble() / (presasCaldo + presasSalchipollo)) * (insumoPolloEntero.costo * (kilosUsados / insumoPolloEntero.stockActual))

                    dao.agregarInsumo(Insumo(
                        nombre = nombreSalchipollo,
                        unidad = "Unidades",
                        stockActual = presasSalchipollo.toDouble(),
                        costo = if(proporcionCosto.isNaN()) 0.0 else proporcionCosto
                    ))
                }
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