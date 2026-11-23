package com.example.proyectov2.data.remoto

import com.example.proyectov2.data.local.Componente
import com.example.proyectov2.data.local.Levantamiento
import com.example.proyectov2.data.local.Usuario

// Conversión de los datos de la API a las entidades de Room.
// Las fotos solo existen en el teléfono, por eso se reciben aparte para no perderlas.

fun UsuarioDto.aEntidad(fotoUri: String?): Usuario {
    return Usuario(
        id = id,
        nombreCompleto = nombreCompleto,
        correo = correo,
        rol = rol,
        cargo = cargo.orEmpty(),
        telefono = telefono.orEmpty(),
        fotoUri = fotoUri
    )
}

fun LevantamientoDto.aEntidad(fotoUri: String?): Levantamiento {
    return Levantamiento(
        id = id,
        codigoRack = codigoRack,
        dataCenter = dataCenter,
        sala = sala,
        fecha = fecha,
        estado = estado,
        fotoUri = fotoUri,
        inspectorId = inspectorId,
        inspectorNombre = inspectorNombre,
        creadoEn = creadoEn,
        actualizadoEn = actualizadoEn,
        aprobadoPor = aprobadoPor,
        aprobadoEn = aprobadoEn
    )
}

fun ComponenteDto.aEntidad(): Componente {
    return Componente(id = id, levantamientoId = levantamientoId, nombre = nombre, estado = estado)
}
