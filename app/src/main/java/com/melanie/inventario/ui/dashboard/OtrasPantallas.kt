package com.melanie.inventario.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.melanie.inventario.data.Insumo
import com.melanie.inventario.ui.navigation.Screen
import com.melanie.inventario.utils.enviarCierreWhatsApp
import com.melanie.inventario.viewmodel.InventarioViewModel

// --- PANTALLA DE AJUSTES ---
@Composable
fun AjustesScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Text("Configuraciones", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { CuadroMenu("Compras", "Ingresar compras", Icons.Default.Add) { navController.navigate(Screen.AgregarCompras.route) } }
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

// --- PANTALLA DE VENTAS (CON DIÁLOGO DE CONFIRMACIÓN Y NOTAS) ---
@Composable
fun VentasScreen(viewModel: InventarioViewModel) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Menú", "Bebidas")

    // Estados para el Diálogo de Confirmación
    var platoSeleccionado by remember { mutableStateOf<String?>(null) }
    var precioSeleccionado by remember { mutableStateOf(0.0) }
    var insumoBase by remember { mutableStateOf("") }
    var cantidadADescontar by remember { mutableStateOf(1.0) }
    var descripcionVenta by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TabRow(
            selectedTabIndex = tabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title, fontWeight = FontWeight.Bold) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index }
                )
            }
        }

        when (tabIndex) {
            0 -> SeccionMenu(onPlatoClick = { nombre, precio, insumo, cant ->
                platoSeleccionado = nombre
                precioSeleccionado = precio
                insumoBase = insumo
                cantidadADescontar = cant
            })
            1 -> SeccionBebidas(onBebidaClick = { nombre, precio, insumo, cant ->
                platoSeleccionado = nombre
                precioSeleccionado = precio
                insumoBase = insumo
                cantidadADescontar = cant
            })
        }
    }

    // DIÁLOGO DE CONFIRMACIÓN (MENSAJE DE ALERTA)
    if (platoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = {
                platoSeleccionado = null
                descripcionVenta = ""
            },
            title = { Text("Confirmar Venta", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Producto: $platoSeleccionado", fontWeight = FontWeight.Medium)
                    Text("Precio: S/ ${String.format("%.2f", precioSeleccionado)}", color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Detalles (papa, arroz, sin ensalada, etc.):", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = descripcionVenta,
                        onValueChange = { descripcionVenta = it },
                        placeholder = { Text("Ej: con arroz, solo papa...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.registrarVentaPlato(
                            nombreInsumoBase = insumoBase,
                            precioVenta = precioSeleccionado,
                            cantidadADescontar = cantidadADescontar,
                            notas = descripcionVenta
                        )
                        platoSeleccionado = null
                        descripcionVenta = ""
                    }
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    platoSeleccionado = null
                    descripcionVenta = ""
                }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun SeccionMenu(onPlatoClick: (String, Double, String, Double) -> Unit) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Platos Principales", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

        CardVenta("Caldo de Pollo", "S/ 5.00") {
            onPlatoClick("Caldo de Pollo", 5.0, "Presas para Caldo", 1.0)
        }

        CardVenta("Salchipollo (Papa o Arroz)", "S/ 10.00") {
            onPlatoClick("Salchipollo", 10.0, "Presas para Salchipollo", 1.0)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Tallarines (Huesito)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                CardVenta("Económico", "S/ 5.0") {
                    onPlatoClick("Tallarín Económico", 5.0, "Presas para Tallarín", 1.0)
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                CardVenta("Extra", "S/ 8.0") {
                    onPlatoClick("Tallarín Extra", 8.0, "Presas para Tallarín", 1.0)
                }
            }
        }
    }
}

@Composable
fun SeccionBebidas(onBebidaClick: (String, Double, String, Double) -> Unit) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Refresco de Maracuyá", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

        CardVenta("Vaso Pequeño", "S/ 1.00") {
            onBebidaClick("Vaso Pequeño", 1.0, "Maracuyá", 0.25)
        }
        CardVenta("Vaso Grande", "S/ 1.50") {
            onBebidaClick("Vaso Grande", 1.5, "Maracuyá", 0.50)
        }
        CardVenta("Jarra 1 Litro", "S/ 4.50") {
            onBebidaClick("Jarra 1 Litro", 4.5, "Maracuyá", 1.0)
        }
    }
}

@Composable
fun CardVenta(nombre: String, precio: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(nombre, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
            Text(precio, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
        }
    }
}

// --- OTROS COMPONENTES (REPORTES, ALERTAS, ETC.) ---
@Composable
fun ReporteVentasScreen(viewModel: InventarioViewModel) {
    val unMesAtras = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
    val ventas by viewModel.obtenerReporteVentas(unMesAtras, System.currentTimeMillis()).collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Text("Ventas Realizadas", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Historial detallado con notas del cliente.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(24.dp))

        if (ventas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay ventas registradas aún.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(ventas) { venta ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(venta.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("S/ ${String.format("%.2f", venta.totalIngresos)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                            }

                            // Muestra la nota del pedido (ej. "Con arroz") si existe
                            if (venta.notas.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Nota: ${venta.notas}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(
                                text = "Cant: ${venta.totalCantidadVendida}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EliminarInsumosScreen(viewModel: InventarioViewModel) {
    val insumos by viewModel.todosLosInsumos.collectAsState(initial = emptyList())
    var insumoAEliminar by remember { mutableStateOf<Insumo?>(null) }
    var insumoAEditar by remember { mutableStateOf<Insumo?>(null) }
    var editNombre by remember { mutableStateOf("") }
    var editCosto by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Text("Editar / Eliminar", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(insumos) { insumo ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(insumo.nombre, fontWeight = FontWeight.Bold)
                            Text("Costo: S/ ${insumo.costo}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                        }
                        Row {
                            IconButton(onClick = { insumoAEditar = insumo; editNombre = insumo.nombre; editCosto = insumo.costo.toString() }) {
                                Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { insumoAEliminar = insumo }) {
                                Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
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
            title = { Text("Editar Insumo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = editNombre, onValueChange = { editNombre = it }, label = { Text("Nombre") })
                    OutlinedTextField(value = editCosto, onValueChange = { editCosto = it }, label = { Text("Costo") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                Button(onClick = {
                    val c = editCosto.toDoubleOrNull() ?: 0.0
                    viewModel.actualizarInsumo(insumoAEditar!!, editNombre, insumoAEditar!!.stockMinimo, c)
                    insumoAEditar = null
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { insumoAEditar = null }) { Text("Cancelar") } }
        )
    }

    if (insumoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { insumoAEliminar = null },
            title = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
            text = { Text("¿Seguro que quieres eliminar '${insumoAEliminar!!.nombre}'?") },
            confirmButton = {
                Button(onClick = { viewModel.eliminarInsumoDefinitivo(insumoAEliminar!!); insumoAEliminar = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { insumoAEliminar = null }) { Text("Cancelar") } }
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
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(insumos) { insumo ->
                Card(modifier = Modifier.fillMaxWidth().clickable { insumoAEditar = insumo; nuevoMinimo = insumo.stockMinimo.toString() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(insumo.nombre, fontWeight = FontWeight.Bold)
                            Text("Avisar a los: ${insumo.stockMinimo}", style = MaterialTheme.typography.bodySmall)
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
            title = { Text("Ajustar Alarma") },
            text = { OutlinedTextField(value = nuevoMinimo, onValueChange = { nuevoMinimo = it }, label = { Text("Mínimo") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) },
            confirmButton = {
                Button(onClick = {
                    val m = nuevoMinimo.toDoubleOrNull() ?: 0.0
                    viewModel.actualizarInsumo(insumoAEditar!!, insumoAEditar!!.nombre, m, insumoAEditar!!.costo)
                    insumoAEditar = null
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { insumoAEditar = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun PerfilScreen() {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Perfil del Local", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = "Antojitos Melanie", onValueChange = {}, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = "Laredo, Trujillo", onValueChange = {}, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))
        Text("GestionP - Cibertec", style = MaterialTheme.typography.bodySmall)
    }
}



@Composable
fun BotonRectangular(titulo: String, subtitulo: String, icono: ImageVector, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(titulo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text(subtitulo, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun ReporteConsumoScreen(viewModel: InventarioViewModel) {
    val unMesAtras = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
    val reporte by viewModel.obtenerReporteConsumo(unMesAtras, System.currentTimeMillis()).collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Text("Insumos Gastados", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))

        if (reporte.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Sin registros") }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(reporte) { item ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(item.nombre, fontWeight = FontWeight.Bold)
                                Text("Consumido", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Text("${item.totalConsumido} ${item.unidad}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ReportesMenuScreen(navController: NavController, viewModel: InventarioViewModel) {
    val context = LocalContext.current

    // Filtro de tiempo: Desde las 00:00 de hoy hasta ahora
    val hoy = System.currentTimeMillis()
    val inicioDia = hoy - (hoy % (24 * 60 * 60 * 1000))

    // Recolectamos los datos de la base de datos en tiempo real
    val ventasHoy by viewModel.obtenerReporteVentas(inicioDia, hoy).collectAsState(initial = emptyList())
    val gastosHoy by viewModel.obtenerTotalGastadoPeriodo(inicioDia, hoy).collectAsState(initial = 0.0)

    val totalVentas = ventasHoy.sumOf { it.totalIngresos }
    val totalGastos = gastosHoy ?: 0.0

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Text("Centro de Reportes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Revisa tus números antes de cerrar", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(24.dp))

        // Botones de navegación a los reportes detallados
        BotonRectangular("Consumo", "Mermas / Gastos", Icons.Default.List) { navController.navigate(Screen.ReporteConsumo.route) }
        Spacer(modifier = Modifier.height(16.dp))
        BotonRectangular("Compras", "Inversión realizada", Icons.Default.ShoppingCart) { navController.navigate(Screen.ReporteCompras.route) }
        Spacer(modifier = Modifier.height(16.dp))
        BotonRectangular("Ventas", "Ingresos detallados", Icons.Default.Check) { navController.navigate(Screen.ReporteVentas.route) }

        Spacer(modifier = Modifier.weight(1f))

        // BOTÓN DE CIERRE POR WHATSAPP (Color Morado/Tertiary)
        Button(
            onClick = { enviarCierreWhatsApp(context, totalVentas, totalGastos) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Icon(Icons.Default.Share, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("ENVIAR CIERRE POR WHATSAPP", fontWeight = FontWeight.ExtraBold)
        }
    }
}
@Composable
fun AgregarComprasScreen(viewModel: InventarioViewModel) {
    val insumos by viewModel.todosLosInsumos.collectAsState(initial = emptyList())
    var insumoAComprar by remember { mutableStateOf<Insumo?>(null) }
    var cantidadText by remember { mutableStateOf("") }
    var costoText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Text("Registrar Compra", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(insumos) { insumo ->
                Card(modifier = Modifier.fillMaxWidth().clickable { insumoAComprar = insumo }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(insumo.nombre, fontWeight = FontWeight.Bold)
                            Text("Stock: ${insumo.stockActual}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    if (insumoAComprar != null) {
        AlertDialog(
            onDismissRequest = { insumoAComprar = null },
            title = { Text("Comprar ${insumoAComprar!!.nombre}") },
            text = {
                Column {
                    OutlinedTextField(value = cantidadText, onValueChange = { cantidadText = it }, label = { Text("Cantidad") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = costoText, onValueChange = { costoText = it }, label = { Text("Costo Total") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                Button(onClick = {
                    val c = cantidadText.toDoubleOrNull() ?: 0.0
                    val cost = costoText.toDoubleOrNull() ?: 0.0
                    if (c > 0 && cost > 0) {
                        viewModel.recargarInsumo(insumoAComprar!!, c, cost)
                        insumoAComprar = null; cantidadText = ""; costoText = ""
                    }
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { insumoAComprar = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun ReporteComprasScreen(viewModel: InventarioViewModel) {
    val unMesAtras = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
    val compras by viewModel.obtenerReporteCompras(unMesAtras, System.currentTimeMillis()).collectAsState(initial = emptyList())
    val totalInversion = compras.sumOf { it.costoTotal }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Text("Reporte de Gastos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Inversión 30 días", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("S/ ${String.format("%.2f", totalInversion)}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(compras) { compra ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(compra.nombre, fontWeight = FontWeight.Medium)
                        Text("S/ ${compra.costoTotal}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun AlertasScreen(viewModel: InventarioViewModel) {
    val insumos by viewModel.todosLosInsumos.collectAsState(initial = emptyList())
    val insumosPorAgotar = insumos.filter { it.stockActual <= it.stockMinimo }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)) {
            Icon(Icons.Default.Warning, "Alerta", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(36.dp).padding(end = 12.dp))
            Text("Stock Crítico", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        if (insumosPorAgotar.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Stock suficiente", textAlign = TextAlign.Center) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(insumosPorAgotar) { insumo ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(insumo.nombre, fontWeight = FontWeight.Bold)
                            Text("${insumo.stockActual} ${insumo.unidad}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}