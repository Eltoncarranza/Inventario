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

    // --- VARIABLES DE ESTADO ---
    // Nuevo Insumo
    var mostrarDialogoAgregar by remember { mutableStateOf(false) }
    var nombreText by remember { mutableStateOf("") }
    var unidadSeleccionada by remember { mutableStateOf("kg") }
    val opcionesUnidad = listOf("kg", "Litros", "Unidades")
    var errorNombre by remember { mutableStateOf(false) }
    var insumoAConvertirMaracuya by remember { mutableStateOf<Insumo?>(null) }
    var kilosMaracuyaText by remember { mutableStateOf("") }
    var litrosObtenidosText by remember { mutableStateOf("") }
    // Consumo Rápido (-)
    var insumoSeleccionado by remember { mutableStateOf<Insumo?>(null) }
    var cantidadConsumoText by remember { mutableStateOf("") }

    // Despresar Pollo
    var insumoADespresar by remember { mutableStateOf<Insumo?>(null) }
    var kilosADespresarText by remember { mutableStateOf("") }
    var presasCaldoText by remember { mutableStateOf("") }
    var presasSalchipolloText by remember { mutableStateOf("") }

    // Convertir Huesitos
    var insumoAConvertirHueso by remember { mutableStateOf<Insumo?>(null) }
    var kilosHuesoText by remember { mutableStateOf("") }
    var presasTallarinText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            if (insumos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Inventario vacío.\nToca el botón + para agregar.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            } else {
                Text(text = "Lista de Insumos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 16.dp))

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
                                    Text("Costo ref: S/ ${insumo.costo}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {

                                    // BOTÓN DINÁMICO: Pollo
                                    if (insumo.nombre.contains("pollo", ignoreCase = true) && insumo.unidad == "kg" && insumo.stockActual > 0) {
                                        Button(
                                            onClick = { insumoADespresar = insumo },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.height(34.dp).padding(end = 8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Text("Despresar", fontSize = 10.sp, color = MaterialTheme.colorScheme.background, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // BOTÓN DINÁMICO: Huesitos
                                    if (insumo.nombre.contains("hueso", ignoreCase = true) && insumo.stockActual > 0) {
                                        Button(
                                            onClick = { insumoAConvertirHueso = insumo },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                            modifier = Modifier.height(34.dp).padding(end = 8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Text("Convertir", fontSize = 10.sp, color = MaterialTheme.colorScheme.onTertiary, fontWeight = FontWeight.Bold)
                                        }
                                        val nombreMinusculas = insumo.nombre.lowercase()

                                    }

                                    val nombreMinus = insumo.nombre.lowercase()
                                    if ((nombreMinus.contains("maracuya") || nombreMinus.contains("maracuyá")) && insumo.unidad == "kg") {
                                        Button(
                                            onClick = { insumoAConvertirMaracuya = insumo },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.height(34.dp).padding(end = 8.dp)
                                        ) {
                                            Text("Preparar", fontSize = 10.sp)
                                        }
                                    }

                                    Text("${insumo.stockActual} ${insumo.unidad}", color = if(insumo.stockActual <= 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)

                                    Spacer(modifier = Modifier.width(12.dp))

                                    IconButton(
                                        onClick = { insumoSeleccionado = insumo },
                                        enabled = insumo.stockActual > 0,
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text("-", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
            Icon(Icons.Filled.Add, contentDescription = "Nuevo Ingrediente")
        }
    }

    // --- DIÁLOGOS DE ACCIÓN ---

    // 1. NUEVO INGREDIENTE (Solo nombre y unidad)
    if (mostrarDialogoAgregar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoAgregar = false; errorNombre = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Nuevo Ingrediente", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Registra el nombre. El stock se añade en 'Ajustes > Compras'.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = nombreText,
                        onValueChange = { nombreText = it; errorNombre = it.isBlank() },
                        label = { Text("Nombre") },
                        isError = errorNombre,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Unidad:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        opcionesUnidad.forEach { unidad ->
                            FilterChip(selected = unidadSeleccionada == unidad, onClick = { unidadSeleccionada = unidad }, label = { Text(unidad) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (nombreText.isNotBlank()) {
                        viewModel.agregarInsumo(nombreText, unidadSeleccionada, 0.0, 0.0)
                        mostrarDialogoAgregar = false; nombreText = ""; unidadSeleccionada = "kg"
                    } else { errorNombre = true }
                }) { Text("Crear") }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoAgregar = false }) { Text("Cancelar") } }
        )
    }

    // 2. DESCONTAR MERMA (-)
    if (insumoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { insumoSeleccionado = null; cantidadConsumoText = "" },
            title = { Text("Descontar ${insumoSeleccionado!!.nombre}", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(value = cantidadConsumoText, onValueChange = { cantidadConsumoText = it }, label = { Text("Cantidad a restar") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            },
            confirmButton = {
                Button(onClick = {
                    val cant = cantidadConsumoText.toDoubleOrNull() ?: 0.0
                    if (cant > 0) {
                        viewModel.registrarConsumo(insumoSeleccionado!!, cant)
                        insumoSeleccionado = null; cantidadConsumoText = ""
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { insumoSeleccionado = null }) { Text("Cancelar") } }
        )
    }

    // 3. DESPRESAR POLLO
    if (insumoADespresar != null) {
        AlertDialog(
            onDismissRequest = { insumoADespresar = null; kilosADespresarText = ""; presasCaldoText = ""; presasSalchipolloText = "" },
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
                    val pc = presasCaldoText.toIntOrNull() ?: 0
                    val ps = presasSalchipolloText.toIntOrNull() ?: 0
                    if (kilos > 0 && (pc > 0 || ps > 0)) {
                        viewModel.despresarPollo(insumoADespresar!!, kilos, pc, ps)
                        insumoADespresar = null; kilosADespresarText = ""; presasCaldoText = ""; presasSalchipolloText = ""
                    }
                }) { Text("Procesar") }
            },
            dismissButton = { TextButton(onClick = { insumoADespresar = null }) { Text("Cancelar") } }
        )
    }

    // 4. CONVERTIR HUESITOS
    if (insumoAConvertirHueso != null) {
        AlertDialog(
            onDismissRequest = { insumoAConvertirHueso = null; kilosHuesoText = ""; presasTallarinText = "" },
            title = { Text("Procesar Huesitos", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Convierte bolsa de huesos en presas para tallarín.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(value = kilosHuesoText, onValueChange = { kilosHuesoText = it }, label = { Text("Kilos a usar") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = presasTallarinText, onValueChange = { presasTallarinText = it }, label = { Text("Presas obtenidas") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                Button(onClick = {
                    val k = kilosHuesoText.toDoubleOrNull() ?: 0.0
                    val c = presasTallarinText.toIntOrNull() ?: 0
                    if (k > 0 && c > 0) {
                        viewModel.convertirHuesitos(insumoAConvertirHueso!!, k, c)
                        insumoAConvertirHueso = null; kilosHuesoText = ""; presasTallarinText = ""
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { insumoAConvertirHueso = null }) { Text("Cancelar") } }
        )
    }
    // Dentro del LazyColumn de DashboardScreen
    if (insumoAConvertirMaracuya != null) {
        AlertDialog(
            onDismissRequest = { insumoAConvertirMaracuya = null },
            title = { Text("Preparar Maracuyá") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = kilosMaracuyaText, onValueChange = { kilosMaracuyaText = it }, label = { Text("Kilos usados") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = litrosObtenidosText, onValueChange = { litrosObtenidosText = it }, label = { Text("Litros preparados") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                Button(onClick = {
                    val k = kilosMaracuyaText.toDoubleOrNull() ?: 0.0
                    val l = litrosObtenidosText.toDoubleOrNull() ?: 0.0
                    if (k > 0 && l > 0) {
                        viewModel.prepararMaracuya(insumoAConvertirMaracuya!!, k, l)
                        insumoAConvertirMaracuya = null
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { insumoAConvertirMaracuya = null }) { Text("Cancelar") } }
        )
    }
}