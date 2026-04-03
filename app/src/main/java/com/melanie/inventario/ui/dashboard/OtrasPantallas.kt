package com.melanie.inventario.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ReportesScreen() {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Text("Pantalla de Reportes (En construcción)", color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun VentasScreen() {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Text("Pantalla de Ventas de Alimentos (Próxima Actualización)", color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun AjustesScreen() {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Text("Configuraciones", color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun AlertasScreen() {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Text("Alerta: Insumos por agotar", color = MaterialTheme.colorScheme.error)
    }
}