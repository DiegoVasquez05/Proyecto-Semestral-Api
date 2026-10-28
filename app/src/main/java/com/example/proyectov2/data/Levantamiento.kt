package com.example.proyectov2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "levantamientos")
data class Levantamiento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val codigoRack: String,
    val dataCenter: String,
    val sala: String,
    val fecha: String,
    val estado: String = Estados.PENDIENTE,
    val fotoUri: String? = null,
    val inspectorId: Int,
    val inspectorNombre: String,
    val creadoEn: Long = System.currentTimeMillis(),
    val actualizadoEn: Long? = null,
    val aprobadoPor: String? = null,
    val aprobadoEn: Long? = null
)
