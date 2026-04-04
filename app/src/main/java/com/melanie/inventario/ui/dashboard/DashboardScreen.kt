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

    var mostrarDialogoAgregar by remember { mutableStateOf(false) }
    var nombreText by remember { mutableStateOf("") }
    var unidadSeleccionada by remember { mutableStateOf("kg") }
    val opcionesUnidad = listOf("kg", "Litros", "Unidades")
    var cantidadText by remember { mutableStateOf("") }
    var costoText by remember { mutableStateOf("") }

    var insumoSeleccionado by remember { mutableStateOf<Insumo?>(null) }
    var cantidadConsumoText by remember { mutableStateOf("") }

    var insumoADespresar by remember { mutableStateOf<Insumo?>(null) }
    var kilosADespresarText by remember { mutableStateOf("") }
    var presasCaldoText by remember { mutableStateOf("") }
    var presasSalchipolloText by remember { mutableStateOf("") }

    // NUEVO: Variables para recargar compras diarias (Arroz)
    var insumoARecargar by remember { mutableStateOf<Insumo?>(null) }
    var cantidadRecargaText by remember { mutableStateOf("") }
    var costoRecargaText by remember { mutableStateOf("") }

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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(insumo.nombre, color = if(insumo.stockActual <= 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                                    Text("Costo: S/ ${insumo.costo}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {

                                    if (insumo.nombre.contains("pollo", ignoreCase = true) && insumo.unidad == "kg" && insumo.stockActual > 0) {
                                        Button(
                                            onClick = { insumoADespresar = insumo },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(36.dp).padding(end = 12.dp)
                                        ) {
                                            Text("Despresar", fontSize = 12.sp, color = MaterialTheme.colorScheme.background, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Text("${insumo.stockActual} ${insumo.unidad}", color = if(insumo.stockActual <= 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(12.dp))

                                    // NUEVO: Botón de Recargar (+)
                                    IconButton(
                                        onClick = { insumoARecargar = insumo },
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(bottom = 2.dp))
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Botón de Restar (-)
                                    IconButton(
                                        onClick = { insumoSeleccionado = insumo },
                                        enabled = insumo.stockActual > 0, // Desactiva el botón si ya está en cero
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("-", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = if(insumo.stockActual > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.padding(bottom = 2.dp))
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

    // --- VENTANA PARA RECARGAR STOCK (COMPRAS DIARIAS) ---
    if (insumoARecargar != null) {
        AlertDialog(
            onDismissRequest = { insumoARecargar = null; cantidadRecargaText = ""; costoRecargaText = "" },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Recargar ${insumoARecargar!!.nombre}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Stock actual: ${insumoARecargar!!.stockActual} ${insumoARecargar!!.unidad}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = cantidadRecargaText, onValueChange = { cantidadRecargaText = it },
                        label = { Text("Cantidad comprada hoy") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = costoRecargaText, onValueChange = { costoRecargaText = it },
                        label = { Text("Costo de la compra (S/)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cantidad = cantidadRecargaText.toDoubleOrNull() ?: 0.0
                        val costo = costoRecargaText.toDoubleOrNull() ?: 0.0
                        if (cantidad > 0) {
                            viewModel.recargarInsumo(insumoARecargar!!, cantidad, costo)
                            insumoARecargar = null; cantidadRecargaText = ""; costoRecargaText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) { Text("Recargar", color = MaterialTheme.colorScheme.background) }
            },
            dismissButton = {
                TextButton(onClick = { insumoARecargar = null; cantidadRecargaText = ""; costoRecargaText = "" }) { Text("Cancelar", color = MaterialTheme.colorScheme.secondary) }
            }
        )
    }

    // --- LAS DEMÁS VENTANAS (Agregar, Restar, Despresar) SE MANTIENEN IGUAL ---
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
                            FilterChip(selected = unidadSeleccionada == unidad, onClick = { unidadSeleccionada = unidad }, label = { Text(unidad) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.background))
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
            dismissButton = { TextButton(onClick = { mostrarDialogoAgregar = false }) { Text("Cancelar", color = MaterialTheme.colorScheme.primary) } }
        )
    }

    if (insumoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { insumoSeleccionado = null; cantidadConsumoText = "" },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Descontar ${insumoSeleccionado!!.nombre}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Stock actual: ${insumoSeleccionado!!.stockActual} ${insumoSeleccionado!!.unidad}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = cantidadConsumoText, onValueChange = { cantidadConsumoText = it }, label = { Text("Cantidad a restar") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cantidad = cantidadConsumoText.toDoubleOrNull() ?: 0.0
                        if (cantidad > 0) {
                            viewModel.registrarConsumo(insumoSeleccionado!!, cantidad)
                            insumoSeleccionado = null; cantidadConsumoText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Confirmar", color = MaterialTheme.colorScheme.background) }
            },
            dismissButton = { TextButton(onClick = { insumoSeleccionado = null; cantidadConsumoText = "" }) { Text("Cancelar", color = MaterialTheme.colorScheme.primary) } }
        )
    }

    if (insumoADespresar != null) {
        AlertDialog(
            onDismissRequest = { insumoADespresar = null; kilosADespresarText = ""; presasCaldoText = ""; presasSalchipolloText = "" },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Despresar Pollo", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Selecciona cuántos kilos vas a cortar y cuántas presas sacaste.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(value = kilosADespresarText, onValueChange = { kilosADespresarText = it }, label = { Text("Kilos a despresar (max ${insumoADespresar!!.stockActual})") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background))
                    OutlinedTextField(value = presasCaldoText, onValueChange = { presasCaldoText = it }, label = { Text("Nº Presas para Caldo") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background))
                    OutlinedTextField(value = presasSalchipolloText, onValueChange = { presasSalchipolloText = it }, label = { Text("Nº Presas para Salchipollo") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val kilos = kilosADespresarText.toDoubleOrNull() ?: 0.0
                        val presasCaldo = presasCaldoText.toIntOrNull() ?: 0
                        val presasSalchi = presasSalchipolloText.toIntOrNull() ?: 0
                        if (kilos > 0 && (presasCaldo > 0 || presasSalchi > 0)) {
                            viewModel.despresarPollo(insumoADespresar!!, kilos, presasCaldo, presasSalchi)
                            insumoADespresar = null; kilosADespresarText = ""; presasCaldoText = ""; presasSalchipolloText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Convertir", color = MaterialTheme.colorScheme.background) }
            },
            dismissButton = { TextButton(onClick = { insumoADespresar = null; kilosADespresarText = ""; presasCaldoText = ""; presasSalchipolloText = "" }) { Text("Cancelar", color = MaterialTheme.colorScheme.primary) } }
        )
    }
}