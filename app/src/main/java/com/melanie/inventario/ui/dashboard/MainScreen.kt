package com.melanie.inventario.ui.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.melanie.inventario.ui.navigation.Screen
import com.melanie.inventario.viewmodel.InventarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: InventarioViewModel) {
    val navController = rememberNavController()
    val items = listOf(Screen.Inicio, Screen.Alertas, Screen.Reportes, Screen.Ventas)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Antojitos Melanie", color = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                navigationIcon = {
                    val esPantallaPrincipal = items.any { it.route == currentRoute }
                    if (!esPantallaPrincipal && currentRoute != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                actions = {
                    if (currentRoute != Screen.Ajustes.route) {
                        IconButton(onClick = { navController.navigate(Screen.Ajustes.route) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, Screen.Inicio.route, Modifier.padding(innerPadding)) {
            composable(Screen.Inicio.route) { DashboardScreen(viewModel) }
            composable(Screen.Alertas.route) { AlertasScreen(viewModel) }

            // --- MENÚ DE REPORTES ---
            composable(Screen.Reportes.route) { ReportesMenuScreen(navController) }
            composable(Screen.ReporteConsumo.route) { ReporteConsumoScreen(viewModel) }
            composable(Screen.ReporteCompras.route) { ReporteComprasScreen(viewModel) }
            composable(Screen.ReporteVentas.route) { ReporteVentasScreen(viewModel) } // Dejamos la que tiene viewModel

            // --- MÓDULO DE VENTAS ---
            composable(Screen.Ventas.route) { VentasScreen(viewModel) } // Dejamos la que tiene viewModel

            // --- AJUSTES Y SUB-PANTALLAS ---
            composable(Screen.Ajustes.route) { AjustesScreen(navController) }
            composable(Screen.AdministrarEliminar.route) { EliminarInsumosScreen(viewModel) }
            composable(Screen.ConfigurarAlertas.route) { ConfigurarAlertasScreen(viewModel) }
            composable(Screen.Perfil.route) { PerfilScreen() }
            composable(Screen.AgregarCompras.route) { AgregarComprasScreen(viewModel) }
        }
    }
}