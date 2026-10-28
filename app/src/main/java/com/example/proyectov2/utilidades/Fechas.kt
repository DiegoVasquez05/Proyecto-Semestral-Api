package com.example.proyectov2.utilidades

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Fechas {

    fun hoy(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    fun diaYHora(milisegundos: Long): String {
        return SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(milisegundos))
    }

    fun hora(milisegundos: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(milisegundos))
    }

    fun aMilisegundos(fechaConHora: String): Long {
        val lectorDeFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return lectorDeFecha.parse(fechaConHora)?.time ?: System.currentTimeMillis()
    }
}
