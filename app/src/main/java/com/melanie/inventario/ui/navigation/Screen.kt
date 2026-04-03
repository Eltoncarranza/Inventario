package com.melanie.inventario.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

// Definimos las rutas y los íconos para la barra de navegación
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Inicio : Screen("inicio", "Inventario", Icons.Default.Home)
    object Reportes : Screen("reportes", "Reportes", Icons.Default.List)
    object Ventas : Screen("ventas", "Ventas", Icons.Default.ShoppingCart)
    object Ajustes : Screen("ajustes", "Ajustes", Icons.Default.Settings)
    object Alertas : Screen("alertas", "Por Agotar", Icons.Default.Warning)
}