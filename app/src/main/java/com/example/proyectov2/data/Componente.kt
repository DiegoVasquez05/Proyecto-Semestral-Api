package com.example.proyectov2.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val levantamientoId: Int,
    val nombre: String,
    val estado: String = Estados.PENDIENTE
)
