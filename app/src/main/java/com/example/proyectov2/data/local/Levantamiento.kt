package com.example.proyectov2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.proyectov2.data.Estados

/**
 * Copia local de un levantamiento de la API. Room funciona como caché para mostrar los datos
 * sin esperar la red. La foto de evidencia solo se guarda en el teléfono.
 */
@Entity(tableName = "levantamientos")
data class Levantamiento(
    @PrimaryKey val id: Int,
    val codigoRack: String,
    val dataCenter: String,
    val sala: String,
    val fecha: String,
    val estado: String = Estados.PENDIENTE,
    val fotoUri: String? = null,
    val inspectorId: Int,
    val inspectorNombre: String,
    val creadoEn: Long,
    val actualizadoEn: Long? = null,
    val aprobadoPor: String? = null,
    val aprobadoEn: Long? = null
)
