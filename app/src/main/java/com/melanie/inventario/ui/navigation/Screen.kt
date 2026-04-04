package com.melanie.inventario.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Inicio : Screen("inicio", "Inventario", Icons.Default.Home)
    object Reportes : Screen("reportes", "Reportes", Icons.Default.List)
    object Ventas : Screen("ventas", "Ventas", Icons.Default.ShoppingCart)
    object Ajustes : Screen("ajustes", "Ajustes", Icons.Default.Settings)
    object Alertas : Screen("alertas", "Por Agotar", Icons.Default.Warning)

    // NUEVAS SUB-PANTALLAS PARA LOS CUADROS
    object AdministrarEliminar : Screen("admin_eliminar", "Eliminar", Icons.Default.Delete)
    object ConfigurarAlertas : Screen("config_alertas", "Stock Mínimo", Icons.Default.Notifications)
    object Perfil : Screen("perfil", "Perfil", Icons.Default.Person)

    // RUTAS PARA LOS REPORTES Y COMPRAS
    object ReporteConsumo : Screen("reporte_consumo", "Rep. Consumo", Icons.Default.List)
    object ReporteCompras : Screen("reporte_compras", "Rep. Compras", Icons.Default.ShoppingCart)
    object ReporteVentas : Screen("reporte_ventas", "Rep. Ventas", Icons.Default.ShoppingCart)
    object AgregarCompras : Screen("agregar_compras", "Agregar Compras", Icons.Default.Add)
}