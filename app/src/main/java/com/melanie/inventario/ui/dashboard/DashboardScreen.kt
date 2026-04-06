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

    // Variables para Nuevo Insumo
    var mostrarDialogoAgregar by remember { mutableStateOf(false) }
    var nombreText by remember { mutableStateOf("") }
    var unidadSeleccionada by remember { mutableStateOf("kg") }
    val opcionesUnidad = listOf("kg", "Litros", "Unidades")
    var cantidadText by remember { mutableStateOf("") }
    var costoText by remember { mutableStateOf("") }

    var errorNombre by remember { mutableStateOf(false) }
    var errorCostoNuevo by remember { mutableStateOf(false) }

    // Variables para Consumo Rápido (-)
    var insumoSeleccionado by remember { mutableStateOf<Insumo?>(null) }
    var cantidadConsumoText by remember { mutableStateOf("") }

    // Variables para Despresar
    var insumoADespresar by remember { mutableStateOf<Insumo?>(null) }
    var kilosADespresarText by remember { mutableStateOf("") }
    var presasCaldoText by remember { mutableStateOf("") }
    var presasSalchipolloText by remember { mutableStateOf("") }

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
                                    Text(insumo.nombre, color = if(insumo.stockActual <= 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Inversión: S/ ${insumo.costo}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
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

                                    Spacer(modifier = Modifier.width(16.dp)) // Aumentamos espacio al quitar el "+"

                                    // Solo queda el botón de Restar (-) para mermas o gastos rápidos
                                    IconButton(
                                        onClick = { insumoSeleccionado = insumo },
                                        enabled = insumo.stockActual > 0,
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("-", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = if(insumo.stockActual > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
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

    // --- DIÁLOGOS (Nuevo Insumo, Restar y Despresar) ---
    if (mostrarDialogoAgregar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoAgregar = false; errorNombre = false; errorCostoNuevo = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Nuevo Insumo", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nombreText, onValueChange = { nombreText = it; errorNombre = it.isBlank() },
                        label = { Text("Nombre (ej. Pollo, Aceite)") },
                        isError = errorNombre
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        opcionesUnidad.forEach { unidad ->
                            FilterChip(selected = unidadSeleccionada == unidad, onClick = { unidadSeleccionada = unidad }, label = { Text(unidad) })
                        }
                    }
                    OutlinedTextField(value = cantidadText, onValueChange = { cantidadText = it }, label = { Text("Stock Inicial") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                    OutlinedTextField(
                        value = costoText, onValueChange = { costoText = it; errorCostoNuevo = it.isBlank() },
                        label = { Text("Costo Total (S/)") },
                        isError = errorCostoNuevo,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    if (errorCostoNuevo) Text("El costo es obligatorio", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cant = cantidadText.toDoubleOrNull() ?: 0.0
                        val cost = costoText.toDoubleOrNull() ?: 0.0

                        if (nombreText.isNotBlank() && cost > 0) {
                            viewModel.agregarInsumo(nombreText, unidadSeleccionada, cant, cost)
                            mostrarDialogoAgregar = false; nombreText = ""; cantidadText = ""; costoText = ""; unidadSeleccionada = "kg"
                        } else {
                            errorNombre = nombreText.isBlank()
                            errorCostoNuevo = cost <= 0
                        }
                    },
                    enabled = nombreText.isNotBlank() && costoText.isNotBlank()
                ) { Text("Guardar", color = MaterialTheme.colorScheme.background) }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoAgregar = false }) { Text("Cancelar") } }
        )
    }

    if (insumoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { insumoSeleccionado = null; cantidadConsumoText = "" },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Descontar ${insumoSeleccionado!!.nombre}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Stock actual: ${insumoSeleccionado!!.stockActual} ${insumoSeleccionado!!.unidad}")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = cantidadConsumoText, onValueChange = { cantidadConsumoText = it }, label = { Text("Cantidad a restar") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                Button(onClick = {
                    val cantidad = cantidadConsumoText.toDoubleOrNull() ?: 0.0
                    if (cantidad > 0) {
                        viewModel.registrarConsumo(insumoSeleccionado!!, cantidad)
                        insumoSeleccionado = null; cantidadConsumoText = ""
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { insumoSeleccionado = null }) { Text("Cancelar") } }
        )
    }

    if (insumoADespresar != null) {
        AlertDialog(
            onDismissRequest = { insumoADespresar = null; kilosADespresarText = ""; presasCaldoText = ""; presasSalchipolloText = "" },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Despresar Pollo", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = kilosADespresarText, onValueChange = { kilosADespresarText = it }, label = { Text("Kilos a cortar") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = presasCaldoText, onValueChange = { presasCaldoText = it }, label = { Text("Nº Presas Caldo") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = presasSalchipolloText, onValueChange = { presasSalchipolloText = it }, label = { Text("Nº Presas Salchipollo") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                Button(onClick = {
                    val kilos = kilosADespresarText.toDoubleOrNull() ?: 0.0
                    val pCaldo = presasCaldoText.toIntOrNull() ?: 0
                    val pSalchi = presasSalchipolloText.toIntOrNull() ?: 0
                    if (kilos > 0 && (pCaldo > 0 || pSalchi > 0)) {
                        viewModel.despresarPollo(insumoADespresar!!, kilos, pCaldo, pSalchi)
                        insumoADespresar = null; kilosADespresarText = ""; presasCaldoText = ""; presasSalchipolloText = ""
                    }
                }) { Text("Convertir") }
            },
            dismissButton = { TextButton(onClick = { insumoADespresar = null }) { Text("Cancelar") } }
        )
    }
}