package com.melanie.inventario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen // Importante añadir esta
import androidx.lifecycle.ViewModelProvider
import com.melanie.inventario.data.InventarioDatabase
import com.melanie.inventario.ui.dashboard.MainScreen
import com.melanie.inventario.ui.theme.InventarioTheme
import com.melanie.inventario.viewmodel.InventarioViewModel
import com.melanie.inventario.viewmodel.InventarioViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Instalar la Splash Screen antes de super.onCreate
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // 2. Iniciamos el motor de la base de datos
        val database = InventarioDatabase.getDatabase(this)
        val dao = database.inventarioDao()

        // 3. Iniciamos el ViewModel
        val factory = InventarioViewModelFactory(dao)
        val viewModel = ViewModelProvider(this, factory)[InventarioViewModel::class.java]

        // 4. Dibujamos la interfaz
        setContent {
            InventarioTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}