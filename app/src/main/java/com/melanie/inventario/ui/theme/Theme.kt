package com.melanie.inventario.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val EsquemaDigital = darkColorScheme(
    background = FondoNegro,
    surface = TarjetaPlana,
    primary = AzulIconico,
    onBackground = TextoClaro,
    onSurface = TextoClaro
)

@Composable
fun InventarioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaDigital,
        typography = Typography,
        content = content
    )
}