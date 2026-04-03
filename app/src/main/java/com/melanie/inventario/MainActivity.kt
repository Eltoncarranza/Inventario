package com.melanie.inventario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.melanie.inventario.data.InventarioDatabase
import com.melanie.inventario.ui.dashboard.DashboardScreen
import com.melanie.inventario.ui.dashboard.MainScreen
import com.melanie.inventario.ui.theme.InventarioTheme
import com.melanie.inventario.viewmodel.InventarioViewModel
import com.melanie.inventario.viewmodel.InventarioViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Iniciamos el motor de la base de datos
        val database = InventarioDatabase.getDatabase(this)
        val dao = database.inventarioDao()

        // 2. Iniciamos el ViewModel que creaste
        val factory = InventarioViewModelFactory(dao)
        val viewModel = ViewModelProvider(this, factory)[InventarioViewModel::class.java]

        // 3. Dibujamos la pantalla aplicando los colores
        setContent {
            InventarioTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}