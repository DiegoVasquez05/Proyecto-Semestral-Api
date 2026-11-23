package com.example.proyectov2.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectov2.RackProApplication
import com.example.proyectov2.model.Usuario
import kotlinx.coroutines.launch

/** Estado de la sesión: login, registro, edición del perfil y cierre de sesión. */
class SesionViewModel(application: Application) : AndroidViewModel(application) {

    private val usuarioRepositorio = (application as RackProApplication).usuarioRepositorio

    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set

    var cargandoSesion by mutableStateOf(true)
        private set

    /** Indica que hay una petición en curso, para deshabilitar los botones. */
    var procesando by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            usuarioActual = usuarioRepositorio.obtenerSesionGuardada()
            cargandoSesion = false
        }
    }

    fun iniciarSesion(correo: String, contrasena: String, rol: String, alTerminar: (String?) -> Unit) {
        ejecutarConUsuario(alTerminar) { usuarioRepositorio.iniciarSesion(correo, contrasena, rol) }
    }

    fun registrarUsuario(
        nombreCompleto: String,
        correo: String,
        contrasena: String,
        rol: String,
        alTerminar: (String?) -> Unit
    ) {
        ejecutarConUsuario(alTerminar) {
            usuarioRepositorio.registrar(nombreCompleto, correo, contrasena, rol)
        }
    }

    fun actualizarPerfil(
        nombreCompleto: String,
        correo: String,
        cargo: String,
        telefono: String,
        fotoUri: String?,
        alTerminar: (String?) -> Unit
    ) {
        val usuario = usuarioActual ?: return
        ejecutarConUsuario(alTerminar) {
            usuarioRepositorio.actualizarPerfil(usuario, nombreCompleto, correo, cargo, telefono, fotoUri)
        }
    }

    fun cerrarSesion(alTerminar: () -> Unit) {
        viewModelScope.launch {
            usuarioRepositorio.cerrarSesion()
            usuarioActual = null
            alTerminar()
        }
    }

    /** Ejecuta una operación que devuelve el usuario y entrega null (éxito) o el mensaje de error. */
    private fun ejecutarConUsuario(alTerminar: (String?) -> Unit, operacion: suspend () -> Result<Usuario>) {
        if (procesando) return
        viewModelScope.launch {
            procesando = true
            operacion()
                .onSuccess { usuario ->
                    usuarioActual = usuario
                    alTerminar(null)
                }
                .onFailure { error -> alTerminar(error.message) }
            procesando = false
        }
    }
}
