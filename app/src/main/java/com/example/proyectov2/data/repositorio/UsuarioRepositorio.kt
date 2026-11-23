package com.example.proyectov2.data.repositorio

import com.example.proyectov2.data.local.Usuario
import com.example.proyectov2.data.local.UsuarioDao
import com.example.proyectov2.data.remoto.LoginRequest
import com.example.proyectov2.data.remoto.PerfilRequest
import com.example.proyectov2.data.remoto.RackProApi
import com.example.proyectov2.data.remoto.RegistroRequest
import com.example.proyectov2.data.remoto.aEntidad
import com.example.proyectov2.data.remoto.llamarApi

/**
 * Único punto de acceso a los datos del usuario: valida las credenciales con la API
 * y guarda la sesión en Room para recordarla al volver a abrir la app.
 */
class UsuarioRepositorio(
    private val api: RackProApi,
    private val usuarioDao: UsuarioDao
) {

    suspend fun obtenerSesionGuardada(): Usuario? = usuarioDao.obtenerSesion()

    suspend fun iniciarSesion(correo: String, contrasena: String, rol: String): Result<Usuario> {
        return llamarApi {
            val usuarioDto = api.iniciarSesion(LoginRequest(correo.trim().lowercase(), contrasena, rol))
            guardarSesion(usuarioDto.aEntidad(fotoUri = null))
        }
    }

    suspend fun registrar(
        nombreCompleto: String,
        correo: String,
        contrasena: String,
        rol: String
    ): Result<Usuario> {
        return llamarApi {
            val datosDeRegistro = RegistroRequest(nombreCompleto.trim(), correo.trim().lowercase(), contrasena, rol)
            guardarSesion(api.registrarUsuario(datosDeRegistro).aEntidad(fotoUri = null))
        }
    }

    suspend fun actualizarPerfil(
        usuario: Usuario,
        nombreCompleto: String,
        correo: String,
        cargo: String,
        telefono: String,
        fotoUri: String?
    ): Result<Usuario> {
        return llamarApi {
            val datosDelPerfil = PerfilRequest(
                nombreCompleto = nombreCompleto.trim(),
                correo = correo.trim().lowercase(),
                cargo = cargo.trim(),
                telefono = telefono.trim()
            )
            guardarSesion(api.actualizarPerfil(usuario.id, datosDelPerfil).aEntidad(fotoUri))
        }
    }

    suspend fun cerrarSesion() = usuarioDao.borrarSesion()

    private suspend fun guardarSesion(usuario: Usuario): Usuario {
        usuarioDao.borrarSesion()
        usuarioDao.guardar(usuario)
        return usuario
    }
}
