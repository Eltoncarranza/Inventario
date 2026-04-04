package com.melanie.inventario.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.melanie.inventario.data.Insumo
import com.melanie.inventario.ui.navigation.Screen
import com.melanie.inventario.viewmodel.InventarioViewModel

@Composable
fun AjustesScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Text("Configuraciones", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { CuadroMenu("Compras", "Ingresar compras", Icons.Default.Add) { navController.navigate(Screen.AgregarCompras.route) } } // NUEVO CUADRO
            item { CuadroMenu("Editar / Eliminar", "Nombres y costos", Icons.Default.Edit) { navController.navigate(Screen.AdministrarEliminar.route) } }
            item { CuadroMenu("Stock Mínimo", "Fijar alarmas", Icons.Default.Notifications) { navController.navigate(Screen.ConfigurarAlertas.route) } }
            item { CuadroMenu("Perfil", "Datos del local", Icons.Default.Person) { navController.navigate(Screen.Perfil.route) } }
        }
    }
}

@Composable
fun CuadroMenu(titulo: String, subtitulo: String, icono: ImageVector, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icono, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(titulo, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
            Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun EliminarInsumosScreen(viewModel: InventarioViewModel) {
    val insumos by viewModel.todosLosInsumos.collectAsState(initial = emptyList())
    var insumoAEliminar by remember { mutableStateOf<Insumo?>(null) }

    // RECUPERAMOS: Variables para editar
    var insumoAEditar by remember { mutableStateOf<Insumo?>(null) }
    var editNombre by remember { mutableStateOf("") }
    var editCosto by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Text("Editar / Eliminar", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Corrige nombres, costos o bórralos.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(insumos) { insumo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // CORREGIDO: Vuelve el color azul oscuro
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(insumo.nombre, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Costo: S/ ${insumo.costo}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                        }
                        Row {
                            // RECUPERAMOS: Botón Lápiz
                            IconButton(onClick = {
                                insumoAEditar = insumo
                                editNombre = insumo.nombre
                                editCosto = insumo.costo.toString()
                            }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary) }

                            IconButton(onClick = { insumoAEliminar = insumo }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (insumoAEditar != null) {
        AlertDialog(
            onDismissRequest = { insumoAEditar = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Editar ${insumoAEditar!!.nombre}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = editNombre, onValueChange = { editNombre = it }, label = { Text("Nombre") }, colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background))
                    OutlinedTextField(value = editCosto, onValueChange = { editCosto = it }, label = { Text("Costo (S/)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background))
                }
            },
            confirmButton = {
                Button(onClick = {
                    val costo = editCosto.toDoubleOrNull() ?: 0.0
                    if (editNombre.isNotBlank()) {
                        // Respetamos el stock mínimo que ya tenía
                        viewModel.actualizarInsumo(insumoAEditar!!, editNombre, insumoAEditar!!.stockMinimo, costo)
                        insumoAEditar = null
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Guardar", color = MaterialTheme.colorScheme.background) }
            },
            dismissButton = { TextButton(onClick = { insumoAEditar = null }) { Text("Cancelar", color = MaterialTheme.colorScheme.primary) } }
        )
    }

    if (insumoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { insumoAEliminar = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Eliminar Insumo", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de eliminar '${insumoAEliminar!!.nombre}' definitivamente?", color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = { Button(onClick = { viewModel.eliminarInsumoDefinitivo(insumoAEliminar!!); insumoAEliminar = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Eliminar", color = MaterialTheme.colorScheme.background) } },
            dismissButton = { TextButton(onClick = { insumoAEliminar = null }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurface) } }
        )
    }
}

@Composable
fun ConfigurarAlertasScreen(viewModel: InventarioViewModel) {
    val insumos by viewModel.todosLosInsumos.collectAsState(initial = emptyList())
    var insumoAEditar by remember { mutableStateOf<Insumo?>(null) }
    var nuevoMinimo by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Text("Fijar Alarma de Stock", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Define cuándo debe avisarte la app que algo se acaba.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(insumos) { insumo ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { insumoAEditar = insumo; nuevoMinimo = insumo.stockMinimo.toString() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // CORREGIDO: Vuelve el color azul oscuro
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(insumo.nombre, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text("Avisa al llegar a: ${insumo.stockMinimo} ${insumo.unidad}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    if (insumoAEditar != null) {
        AlertDialog(
            onDismissRequest = { insumoAEditar = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Ajustar Alarma", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = nuevoMinimo, onValueChange = { nuevoMinimo = it },
                    label = { Text("Cantidad Mínima") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background)
                )
            },
            confirmButton = {
                Button(onClick = {
                    val minStock = nuevoMinimo.toDoubleOrNull() ?: 0.0
                    viewModel.actualizarInsumo(insumoAEditar!!, insumoAEditar!!.nombre, minStock, insumoAEditar!!.costo)
                    insumoAEditar = null
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Guardar", color = MaterialTheme.colorScheme.background) }
            },
            dismissButton = { TextButton(onClick = { insumoAEditar = null }) { Text("Cancelar", color = MaterialTheme.colorScheme.primary) } }
        )
    }
}

@Composable
fun PerfilScreen() {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Perfil del Local", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = "Antojitos Melanie", onValueChange = {}, label = { Text("Nombre del Local") }, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background))
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = "Laredo, Trujillo", onValueChange = {}, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Proyecto: GestionP - Cibertec", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
    }
}
// --- MENÚ PRINCIPAL DE REPORTES (Botones Rectangulares) ---
@Composable
fun ReportesMenuScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Text("Centro de Reportes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Selecciona el reporte que deseas visualizar", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(24.dp))

        BotonRectangular("Reporte de Consumo", "Insumos gastados / mermas", Icons.Default.List) { navController.navigate(Screen.ReporteConsumo.route) }
        Spacer(modifier = Modifier.height(16.dp))
        BotonRectangular("Reporte de Compras", "Registro de ingresos e inversión", Icons.Default.ShoppingCart) { navController.navigate(Screen.ReporteCompras.route) }
        Spacer(modifier = Modifier.height(16.dp))
        BotonRectangular("Reporte de Ventas", "Ventas de platillos", Icons.Default.Check) { navController.navigate(Screen.ReporteVentas.route) }
    }
}

// Plantilla gráfica para los botones rectangulares
@Composable
fun BotonRectangular(titulo: String, subtitulo: String, icono: ImageVector, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(titulo, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge)
                Text(subtitulo, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// --- PANTALLA PARA AGREGAR COMPRAS (Desde el nuevo cuadro en Ajustes) ---
@Composable
fun AgregarComprasScreen(viewModel: InventarioViewModel) {
    val insumos by viewModel.todosLosInsumos.collectAsState(initial = emptyList())
    var insumoAComprar by remember { mutableStateOf<Insumo?>(null) }
    var cantidadText by remember { mutableStateOf("") }
    var costoText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Text("Registrar Compra", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Toca un insumo de la lista para sumar lo que trajiste del mercado.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(insumos) { insumo ->
                Card(modifier = Modifier.fillMaxWidth().clickable { insumoAComprar = insumo }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(insumo.nombre, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Stock actual: ${insumo.stockActual} ${insumo.unidad}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    if (insumoAComprar != null) {
        AlertDialog(
            onDismissRequest = { insumoAComprar = null; cantidadText = ""; costoText = "" },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Comprar ${insumoAComprar!!.nombre}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = cantidadText, onValueChange = { cantidadText = it }, label = { Text("Cantidad comprada") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = costoText, onValueChange = { costoText = it }, label = { Text("Costo (S/)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background))
                }
            },
            confirmButton = {
                Button(onClick = {
                    val cant = cantidadText.toDoubleOrNull() ?: 0.0
                    val cost = costoText.toDoubleOrNull() ?: 0.0
                    if(cant > 0) {
                        // Llama a la función del ViewModel que suma al stock y guarda en el reporte
                        // viewModel.recargarInsumo(insumoAComprar!!, cant, cost)
                        insumoAComprar = null; cantidadText = ""; costoText = ""
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Guardar", color = MaterialTheme.colorScheme.background) }
            },
            dismissButton = { TextButton(onClick = { insumoAComprar = null }) { Text("Cancelar", color = MaterialTheme.colorScheme.primary) } }
        )
    }
}

// Pantallas Placeholders Temporales para los Reportes y Ventas
@Composable
fun ReporteConsumoScreen(viewModel: InventarioViewModel) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Reporte de Consumo (Próximamente)", color = MaterialTheme.colorScheme.onBackground) } }

@Composable
fun ReporteComprasScreen(viewModel: InventarioViewModel) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Reporte de Compras (Próximamente)", color = MaterialTheme.colorScheme.onBackground) } }

@Composable
fun ReporteVentasScreen(viewModel: InventarioViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Reporte de Ventas (Próximamente)", color = MaterialTheme.colorScheme.onBackground)
    }
}
@Composable
fun AlertasScreen(viewModel: InventarioViewModel) {
    val insumos by viewModel.todosLosInsumos.collectAsState(initial = emptyList())
    val insumosPorAgotar = insumos.filter { it.stockActual <= it.stockMinimo }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)) {
            Icon(Icons.Default.Warning, contentDescription = "Alerta", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(36.dp).padding(end = 12.dp))
            Column {
                Text("Por Agotar / Agotados", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("Insumos bajo tu nivel de alarma", color = MaterialTheme.colorScheme.error)
            }
        }

        if (insumosPorAgotar.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("¡Todo bien!\nTienes buen stock de todos tus insumos.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(insumosPorAgotar) { insumo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(insumo.nombre, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                                if (insumo.stockActual <= 0.0) {
                                    Text("¡AGOTADO!", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold)
                                } else {
                                    Text("Alarma activada (Mín: ${insumo.stockMinimo})", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Text("${insumo.stockActual} ${insumo.unidad}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportesScreen() { Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) { Text("Pantalla de Reportes (En construcción)", color = MaterialTheme.colorScheme.onBackground) } }
@Composable
fun VentasScreen(viewModel: InventarioViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Módulo de Ventas (Próxima actualización)", color = MaterialTheme.colorScheme.onBackground)
    }
}