package com.example.proyectov2.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Componente del catálogo registrado dentro de un levantamiento. El id es el de la API. */
@Entity(
    tableName = "componentes",
    foreignKeys = [
        ForeignKey(
            entity = Levantamiento::class,
            parentColumns = ["id"],
            childColumns = ["levantamientoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["levantamientoId"])]
)
data class Componente(
    @PrimaryKey val id: Int,
    val levantamientoId: Int,
    val nombre: String,
    val estado: String = Estados.PENDIENTE
)
