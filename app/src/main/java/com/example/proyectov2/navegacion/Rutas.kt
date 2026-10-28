package com.example.proyectov2.navegacion

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val LEVANTAMIENTOS = "levantamientos"
    const val DETALLE = "detalle/{levantamientoId}"
    const val PERFIL = "perfil"
    const val EDITAR_PERFIL = "editar_perfil"
    const val ACERCA_DE = "acerca_de"

    fun detalle(levantamientoId: Int): String = "detalle/$levantamientoId"
}
