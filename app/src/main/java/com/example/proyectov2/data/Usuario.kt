package com.example.proyectov2.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios", indices = [Index(value = ["correo"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombreCompleto: String,
    val correo: String,
    val contrasena: String,
    val rol: String,
    val cargo: String = "",
    val telefono: String = "",
    val fotoUri: String? = null,
    val sesionActiva: Boolean = false
)
