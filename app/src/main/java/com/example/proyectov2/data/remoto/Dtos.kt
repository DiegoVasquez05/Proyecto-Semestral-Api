package com.example.proyectov2.data.remoto

// Objetos que viajan en formato JSON entre la app y la API (DTO = Data Transfer Object).

data class UsuarioDto(
    val id: Int,
    val nombreCompleto: String,
    val correo: String,
    val rol: String,
    val cargo: String?,
    val telefono: String?
)

data class ComponenteDto(
    val id: Int,
    val levantamientoId: Int,
    val nombre: String,
    val estado: String
)

data class LevantamientoDto(
    val id: Int,
    val codigoRack: String,
    val dataCenter: String,
    val sala: String,
    val fecha: String,
    val estado: String,
    val inspectorId: Int,
    val inspectorNombre: String,
    val creadoEn: Long,
    val actualizadoEn: Long?,
    val aprobadoPor: String?,
    val aprobadoEn: Long?,
    val componentes: List<ComponenteDto>?
)

data class ComponenteCatalogoDto(
    val id: Int,
    val nombre: String,
    val categoria: String
)

data class ErrorDto(val mensaje: String?)

// Cuerpos de las peticiones POST, PUT y PATCH.

data class RegistroRequest(
    val nombreCompleto: String,
    val correo: String,
    val contrasena: String,
    val rol: String
)

data class LoginRequest(val correo: String, val contrasena: String, val rol: String)

data class PerfilRequest(
    val nombreCompleto: String,
    val correo: String,
    val cargo: String,
    val telefono: String
)

data class NuevoLevantamientoRequest(
    val codigoRack: String,
    val dataCenter: String,
    val sala: String,
    val fecha: String,
    val inspectorId: Int,
    val inspectorNombre: String
)

data class EdicionLevantamientoRequest(val codigoRack: String, val fecha: String)

data class AprobacionRequest(val supervisorNombre: String)

data class NuevoComponenteRequest(val nombre: String)

data class EstadoComponenteRequest(val estado: String)
