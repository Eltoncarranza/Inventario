package com.melanie.inventario.ui.dashboard

import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.melanie.inventario.data.Insumo
import com.melanie.inventario.viewmodel.InventarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: InventarioViewModel) {
    val insumos by viewModel.todosLosInsumos.collectAsState(initial = emptyList())

    // Variables para agregar insumo
    var mostrarDialogoAgregar by remember { mutableStateOf(false) }
    var nombreText by remember { mutableStateOf("") }
    var unidadSeleccionada by remember { mutableStateOf("kg") }
    val opcionesUnidad = listOf("kg", "Litros", "Unidades")
    var cantidadText by remember { mutableStateOf("") }
    var costoText by remember { mutableStateOf("") }

    // Variables para el consumo diario (restar)
    var insumoSeleccionado by remember { mutableStateOf<Insumo?>(null) }
    var cantidadConsumoText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            if (insumos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Inventario vacío.\nToca el botón + para agregar.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            } else {
                Text(text = "Lista de Insumos", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(insumos) { insumo ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(insumo.nombre, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                                    Text("Costo: S/ ${insumo.costo}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${insumo.stockActual} ${insumo.unidad}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Botón para restar el consumo
                                    IconButton(
                                        onClick = { insumoSeleccionado = insumo },
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    ) {
                                        Text(
                                            text = "-",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { mostrarDialogoAgregar = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.background
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Agregar Insumo")
        }
    }

    // --- VENTANA PARA AGREGAR ---
    if (mostrarDialogoAgregar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoAgregar = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Nuevo Insumo", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = nombreText, onValueChange = { nombreText = it }, label = { Text("Nombre (ej. Pollo, Aceite)") })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        opcionesUnidad.forEach { unidad ->
                            FilterChip(
                                selected = unidadSeleccionada == unidad,
                                onClick = { unidadSeleccionada = unidad },
                                label = { Text(unidad) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.background)
                            )
                        }
                    }
                    OutlinedTextField(value = cantidadText, onValueChange = { cantidadText = it }, label = { Text("Cantidad") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = costoText, onValueChange = { costoText = it }, label = { Text("Costo Total (S/)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cantidad = cantidadText.toDoubleOrNull() ?: 0.0
                        val costo = costoText.toDoubleOrNull() ?: 0.0
                        if (nombreText.isNotBlank()) {
                            viewModel.agregarInsumo(nombreText, unidadSeleccionada, cantidad, costo)
                            mostrarDialogoAgregar = false; nombreText = ""; cantidadText = ""; costoText = ""; unidadSeleccionada = "kg"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Guardar", color = MaterialTheme.colorScheme.background) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoAgregar = false }) { Text("Cancelar", color = MaterialTheme.colorScheme.primary) }
            }
        )
    }

    // --- VENTANA PARA CONSUMIR (RESTAR) ---
    if (insumoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { insumoSeleccionado = null; cantidadConsumoText = "" },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Descontar ${insumoSeleccionado!!.nombre}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Stock actual: ${insumoSeleccionado!!.stockActual} ${insumoSeleccionado!!.unidad}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = cantidadConsumoText,
                        onValueChange = { cantidadConsumoText = it },
                        label = { Text("Cantidad a restar") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            },

            confirmButton = {
                Button(
                    onClick = {
                        val cantidad = cantidadConsumoText.toDoubleOrNull() ?: 0.0
                        if (cantidad > 0) {
                            // AQUÍ ESTÁ EL CAMBIO: Ahora enviamos todo el insumoSeleccionado!!
                            viewModel.registrarConsumo(insumoSeleccionado!!, cantidad)
                            insumoSeleccionado = null
                            cantidadConsumoText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Confirmar", color = MaterialTheme.colorScheme.background) }
            },
            dismissButton = {
                TextButton(onClick = { insumoSeleccionado = null; cantidadConsumoText = "" }) { Text("Cancelar", color = MaterialTheme.colorScheme.primary) }
            }
        )
    }
}