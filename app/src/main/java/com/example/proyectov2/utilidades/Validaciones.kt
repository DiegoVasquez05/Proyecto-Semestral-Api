package com.example.proyectov2.utilidades

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

object Validaciones {

    private val formatoCorreo = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val formatoTelefono = Regex("^\\+?[0-9]{8,12}$")
    private val formatoCodigoRack = Regex("^[A-Za-z]-[0-9]{2}$")
    private val formatoFecha = Regex("^[0-9]{2}/[0-9]{2}/[0-9]{4}$")

    fun validarNombre(nombre: String): String? {
        return when {
            nombre.isBlank() -> "Ingresa tu nombre completo"
            nombre.trim().length < 3 -> "El nombre debe tener al menos 3 letras"
            else -> null
        }
    }

    fun validarCorreo(correo: String): String? {
        return when {
            correo.isBlank() -> "Ingresa tu correo"
            !formatoCorreo.matches(correo.trim()) -> "El correo no tiene un formato válido"
            else -> null
        }
    }

    fun validarContrasenaIngresada(contrasena: String): String? {
        return if (contrasena.isBlank()) "Ingresa tu contraseña" else null
    }

    fun validarContrasenaNueva(contrasena: String): String? {
        return when {
            contrasena.isBlank() -> "Ingresa una contraseña"
            contrasena.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
    }

    fun validarConfirmacion(contrasena: String, confirmacion: String): String? {
        return when {
            confirmacion.isBlank() -> "Confirma tu contraseña"
            contrasena != confirmacion -> "Las contraseñas no coinciden"
            else -> null
        }
    }

    fun validarTelefono(telefono: String): String? {
        val soloNumeros = telefono.replace(" ", "")
        return when {
            telefono.isBlank() -> "Ingresa tu teléfono"
            !formatoTelefono.matches(soloNumeros) -> "Ejemplo válido: +56 9 1234 5678"
            else -> null
        }
    }

    fun validarCampoObligatorio(valor: String, nombreCampo: String): String? {
        return if (valor.isBlank()) "Ingresa $nombreCampo" else null
    }

    fun validarCodigoRack(codigoRack: String): String? {
        return when {
            codigoRack.isBlank() -> "Ingresa el ID del rack"
            !formatoCodigoRack.matches(codigoRack.trim()) -> "Formato esperado: A-01"
            else -> null
        }
    }

    fun validarFecha(fecha: String): String? {
        if (fecha.isBlank()) return "Ingresa la fecha"
        if (!formatoFecha.matches(fecha.trim())) return "Formato esperado: dd/mm/aaaa"
        val lectorDeFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        lectorDeFecha.isLenient = false
        return try {
            lectorDeFecha.parse(fecha.trim())
            null
        } catch (error: ParseException) {
            "La fecha no existe"
        }
    }
}
