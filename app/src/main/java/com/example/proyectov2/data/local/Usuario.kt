package com.example.proyectov2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Usuario con la sesión iniciada. Se guarda en Room para no pedir el login cada vez que se abre la app.
 * El id es el mismo que entrega la API; la foto de perfil solo existe en el teléfono.
 */
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val id: Int,
    val nombreCompleto: String,
    val correo: String,
    val rol: String,
    val cargo: String = "",
    val telefono: String = "",
    val fotoUri: String? = null
)
