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

    // Definimos los ítems de la barra inferior
    val itemsTab = listOf(Screen.Inicio, Screen.Alertas, Screen.Reportes, Screen.Ventas)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // --- LÓGICA SENIOR: CONTROL DE VISIBILIDAD ---
    // Solo estas rutas mostrarán la barra inferior.
    // Si entras a un reporte específico o a ajustes, se ocultará automáticamente.
    val rutasPrincipales = listOf(
        Screen.Inicio.route,
        Screen.Alertas.route,
        Screen.Reportes.route,
        Screen.Ventas.route
    )
    val mostrarBottomBar = currentRoute in rutasPrincipales

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Antojitos Melanie", color = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                navigationIcon = {
                    // Botón de volver: solo aparece si NO estamos en una pantalla principal
                    if (!rutasPrincipales.contains(currentRoute) && currentRoute != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                actions = {
                    // Ocultamos el icono de ajustes si ya estamos dentro de Ajustes
                    if (currentRoute != Screen.Ajustes.route && mostrarBottomBar) {
                        IconButton(onClick = { navController.navigate(Screen.Ajustes.route) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                        }
                    }
                }
            )
        },
        bottomBar = {
            // Solo renderizamos la barra si es una pantalla principal
            if (mostrarBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    val currentDestination = navBackStackEntry?.destination
                    itemsTab.forEach { screen ->
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Inicio.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- PANTALLAS PRINCIPALES ---
            composable(Screen.Inicio.route) { DashboardScreen(viewModel) }
            composable(Screen.Alertas.route) { AlertasScreen(viewModel) }

            // CORRECCIÓN AQUÍ: Se agrega el viewModel como parámetro
            composable(Screen.Reportes.route) {
                ReportesMenuScreen(navController = navController, viewModel = viewModel)
            }

            composable(Screen.Ventas.route) { VentasScreen(viewModel) }

            // --- SUB-PANTALLAS DE REPORTES ---
            composable(Screen.ReporteConsumo.route) { ReporteConsumoScreen(viewModel) }
            composable(Screen.ReporteCompras.route) { ReporteComprasScreen(viewModel) }
            composable(Screen.ReporteVentas.route) { ReporteVentasScreen(viewModel) }

            // --- SUB-PANTALLAS DE AJUSTES ---
            composable(Screen.Ajustes.route) { AjustesScreen(navController) }
            composable(Screen.AdministrarEliminar.route) { EliminarInsumosScreen(viewModel) }
            composable(Screen.ConfigurarAlertas.route) { ConfigurarAlertasScreen(viewModel) }
            composable(Screen.Perfil.route) { PerfilScreen() }
            composable(Screen.AgregarCompras.route) { AgregarComprasScreen(viewModel) }
        }
    }
}