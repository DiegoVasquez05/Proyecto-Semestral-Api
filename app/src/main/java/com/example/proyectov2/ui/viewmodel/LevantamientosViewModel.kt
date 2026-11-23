package com.example.proyectov2.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectov2.RackProApplication
import com.example.proyectov2.data.Estados
import com.example.proyectov2.data.local.Componente
import com.example.proyectov2.data.local.Levantamiento
import com.example.proyectov2.data.local.Usuario
import com.example.proyectov2.data.remoto.NuevoLevantamientoRequest
import com.example.proyectov2.notificaciones.Notificador
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado y acciones de los levantamientos: listado, detalle, componentes y aprobación. */
class LevantamientosViewModel(application: Application) : AndroidViewModel(application) {

    private val levantamientoRepositorio = (application as RackProApplication).levantamientoRepositorio

    val levantamientos: StateFlow<List<Levantamiento>> = levantamientoRepositorio.levantamientos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var sincronizando by mutableStateOf(false)
        private set

    var catalogoComponentes by mutableStateOf<List<String>>(emptyList())
        private set

    /** Mensaje para mostrar una sola vez en pantalla (error de red, confirmaciones, etc.). */
    var mensaje by mutableStateOf<String?>(null)
        private set

    init {
        sincronizar()
    }

    fun mensajeMostrado() {
        mensaje = null
    }

    fun sincronizar() {
        if (sincronizando) return
        viewModelScope.launch {
            sincronizando = true
            levantamientoRepositorio.sincronizar().onFailure { error -> mensaje = error.message }
            sincronizando = false
        }
    }

    fun observarLevantamiento(levantamientoId: Int): Flow<Levantamiento?> =
        levantamientoRepositorio.observarLevantamiento(levantamientoId)

    fun observarComponentes(levantamientoId: Int): Flow<List<Componente>> =
        levantamientoRepositorio.observarComponentes(levantamientoId)

    fun actualizarDetalle(levantamientoId: Int) {
        viewModelScope.launch {
            levantamientoRepositorio.actualizarLevantamiento(levantamientoId)
                .onFailure { error -> mensaje = error.message }
        }
    }

    fun crearLevantamiento(
        inspector: Usuario,
        codigoRack: String,
        dataCenter: String,
        sala: String,
        fecha: String,
        alCrear: (Int) -> Unit
    ) {
        viewModelScope.launch {
            val datosNuevos = NuevoLevantamientoRequest(
                codigoRack = codigoRack.trim().uppercase(),
                dataCenter = dataCenter.trim(),
                sala = sala.trim(),
                fecha = fecha.trim(),
                inspectorId = inspector.id,
                inspectorNombre = inspector.nombreCompleto
            )
            levantamientoRepositorio.crearLevantamiento(datosNuevos)
                .onSuccess { levantamiento ->
                    Notificador.levantamientoCreado(getApplication(), levantamiento.id, levantamiento.codigoRack)
                    alCrear(levantamiento.id)
                }
                .onFailure { error -> mensaje = error.message }
        }
    }

    fun guardarCambiosLevantamiento(levantamiento: Levantamiento, codigoRack: String, fecha: String) {
        viewModelScope.launch {
            levantamientoRepositorio.editarLevantamiento(levantamiento.id, codigoRack, fecha)
                .onSuccess { mensaje = "Cambios guardados" }
                .onFailure { error -> mensaje = error.message }
        }
    }

    fun guardarFotoLevantamiento(levantamientoId: Int, fotoUri: Uri) {
        viewModelScope.launch {
            levantamientoRepositorio.guardarFoto(levantamientoId, fotoUri.toString())
        }
    }

    fun aprobarLevantamiento(levantamiento: Levantamiento, supervisor: Usuario) {
        viewModelScope.launch {
            levantamientoRepositorio.aprobarLevantamiento(levantamiento.id, supervisor.nombreCompleto)
                .onSuccess { levantamientoAprobado ->
                    Notificador.levantamientoAprobado(
                        getApplication(),
                        levantamientoAprobado.id,
                        levantamientoAprobado.codigoRack
                    )
                }
                .onFailure { error -> mensaje = error.message }
        }
    }

    fun eliminarLevantamiento(levantamiento: Levantamiento, alEliminar: () -> Unit) {
        viewModelScope.launch {
            levantamientoRepositorio.eliminarLevantamiento(levantamiento.id)
                .onSuccess {
                    mensaje = "Levantamiento del Rack ${levantamiento.codigoRack} eliminado"
                    alEliminar()
                }
                .onFailure { error -> mensaje = error.message }
        }
    }

    fun cargarCatalogo() {
        if (catalogoComponentes.isNotEmpty()) return
        viewModelScope.launch {
            levantamientoRepositorio.obtenerCatalogo()
                .onSuccess { nombres -> catalogoComponentes = nombres }
                .onFailure { error -> mensaje = error.message }
        }
    }

    fun agregarComponente(levantamientoId: Int, nombreComponente: String) {
        viewModelScope.launch {
            levantamientoRepositorio.agregarComponente(levantamientoId, nombreComponente)
                .onFailure { error -> mensaje = error.message }
        }
    }

    fun cambiarEstadoComponente(componente: Componente) {
        val estadoNuevo = Estados.siguienteEstadoComponente(componente.estado)
        viewModelScope.launch {
            levantamientoRepositorio.cambiarEstadoComponente(componente, estadoNuevo)
                .onFailure { error -> mensaje = error.message }
        }
    }
}
