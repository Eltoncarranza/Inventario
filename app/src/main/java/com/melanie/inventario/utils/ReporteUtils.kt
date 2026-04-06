package com.melanie.inventario.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

fun enviarCierreWhatsApp(context: Context, totalVentas: Double, totalGastos: Double) {
    val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    val gananciaNeto = totalVentas - totalGastos

    val mensaje = """
        *CIERRE DE DÍA - ANTOJITOS MELANIE* 🍗
        📅 Fecha: $fecha
        ----------------------------------
        💰 Ventas Totales: S/ ${String.format("%.2f", totalVentas)}
        💸 Gastos (Compras): S/ ${String.format("%.2f", totalGastos)}
        
        📈 *GANANCIA NETA: S/ ${String.format("%.2f", gananciaNeto)}*
        ----------------------------------
        _Reporte generado desde la App_
    """.trimIndent()

    try {
        val intent = Intent(Intent.ACTION_VIEW)
        // REEMPLAZA EL NÚMERO AQUÍ (Ejemplo: 51900000000)
        val numeroMelanie = "51939154420"
        val url = "https://api.whatsapp.com/send?phone=$numeroMelanie&text=" + URLEncoder.encode(mensaje, "UTF-8")
        intent.data = Uri.parse(url)
        context.startActivity(intent)
    } catch (e: Exception) {
        // Manejar error si no hay WhatsApp
    }
}