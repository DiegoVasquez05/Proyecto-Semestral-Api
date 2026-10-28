package com.example.proyectov2

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectov2.data.Componente
import com.example.proyectov2.data.Estados
import com.example.proyectov2.data.Levantamiento
import com.example.proyectov2.data.RackProDatabase
import com.example.proyectov2.data.Roles
import com.example.proyectov2.data.Usuario
import com.example.proyectov2.utilidades.Fechas
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RackProViewModel(application: Application) : AndroidViewModel(application) {

    private val baseDeDatos = RackProDatabase.obtener(application)
    private val usuarioDao = baseDeDatos.usuarioDao()
    private val levantamientoDao = baseDeDatos.levantamientoDao()

    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set

    var cargandoSesion by mutableStateOf(true)
        private set

    val levantamientos: StateFlow<List<Levantamiento>> = levantamientoDao.observarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            cargarDatosDeEjemplo()
            usuarioActual = usuarioDao.buscarSesionActiva()
            cargandoSesion = false
        }
    }

    fun iniciarSesion(correo: String, contrasena: String, rol: String, alTerminar: (String?) -> Unit) {
        viewModelScope.launch {
            val usuario = usuarioDao.buscarPorCorreo(correo.trim().lowercase())
            when {
                usuario == null -> alTerminar("No existe una cuenta con este correo")
                usuario.contrasena != contrasena -> alTerminar("La contraseña es incorrecta")
                usuario.rol != rol -> alTerminar("Esta cuenta está registrada como ${usuario.rol}")
                else -> {
                    val usuarioConSesion = usuario.copy(sesionActiva = true)
                    usuarioDao.cerrarTodasLasSesiones()
                    usuarioDao.actualizar(usuarioConSesion)
                    usuarioActual = usuarioConSesion
                    alTerminar(null)
                }
            }
        }
    }

    fun registrarUsuario(
        nombreCompleto: String,
        correo: String,
        contrasena: String,
        rol: String,
        alTerminar: (String?) -> Unit
    ) {
        viewModelScope.launch {
            val correoLimpio = correo.trim().lowercase()
            if (usuarioDao.buscarPorCorreo(correoLimpio) != null) {
                alTerminar("Ya existe una cuenta con este correo")
                return@launch
            }
            val nuevoUsuario = Usuario(
                nombreCompleto = nombreCompleto.trim(),
                correo = correoLimpio,
                contrasena = contrasena,
                rol = rol,
                cargo = if (rol == Roles.SUPERVISOR) "Supervisor de proyecto" else "Técnico de terreno",
                sesionActiva = true
            )
            usuarioDao.cerrarTodasLasSesiones()
            val idGenerado = usuarioDao.insertar(nuevoUsuario).toInt()
            usuarioActual = nuevoUsuario.copy(id = idGenerado)
            alTerminar(null)
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
        viewModelScope.launch {
            val correoLimpio = correo.trim().lowercase()
            val otroUsuarioConCorreo = usuarioDao.buscarPorCorreo(correoLimpio)
            if (otroUsuarioConCorreo != null && otroUsuarioConCorreo.id != usuario.id) {
                alTerminar("Ese correo ya está en uso por otra cuenta")
                return@launch
            }
            val usuarioEditado = usuario.copy(
                nombreCompleto = nombreCompleto.trim(),
                correo = correoLimpio,
                cargo = cargo.trim(),
                telefono = telefono.trim(),
                fotoUri = fotoUri
            )
            usuarioDao.actualizar(usuarioEditado)
            usuarioActual = usuarioEditado
            alTerminar(null)
        }
    }

    fun cerrarSesion(alTerminar: () -> Unit) {
        viewModelScope.launch {
            usuarioDao.cerrarTodasLasSesiones()
            usuarioActual = null
            alTerminar()
        }
    }

    fun observarLevantamiento(levantamientoId: Int): Flow<Levantamiento?> {
        return levantamientoDao.observarPorId(levantamientoId)
    }

    fun observarComponentes(levantamientoId: Int): Flow<List<Componente>> {
        return levantamientoDao.observarComponentes(levantamientoId)
    }

    fun crearLevantamiento(
        codigoRack: String,
        dataCenter: String,
        sala: String,
        fecha: String,
        alTerminar: (Int) -> Unit
    ) {
        val usuario = usuarioActual ?: return
        viewModelScope.launch {
            val nuevoLevantamiento = Levantamiento(
                codigoRack = codigoRack.trim().uppercase(),
                dataCenter = dataCenter.trim(),
                sala = sala.trim(),
                fecha = fecha.trim(),
                inspectorId = usuario.id,
                inspectorNombre = usuario.nombreCompleto
            )
            val idGenerado = levantamientoDao.insertar(nuevoLevantamiento).toInt()
            alTerminar(idGenerado)
        }
    }

    fun guardarCambiosLevantamiento(levantamiento: Levantamiento, codigoRack: String, fecha: String) {
        viewModelScope.launch {
            levantamientoDao.actualizar(
                levantamiento.copy(
                    codigoRack = codigoRack.trim().uppercase(),
                    fecha = fecha.trim(),
                    actualizadoEn = System.currentTimeMillis()
                )
            )
        }
    }

    fun guardarFotoLevantamiento(levantamientoId: Int, fotoUri: Uri) {
        viewModelScope.launch {
            levantamientoDao.actualizarFoto(levantamientoId, fotoUri.toString())
        }
    }

    fun aprobarLevantamiento(levantamiento: Levantamiento) {
        val supervisor = usuarioActual ?: return
        viewModelScope.launch {
            levantamientoDao.actualizar(
                levantamiento.copy(
                    estado = Estados.APROBADO,
                    aprobadoPor = supervisor.nombreCompleto,
                    aprobadoEn = System.currentTimeMillis()
                )
            )
        }
    }

    fun agregarComponente(levantamientoId: Int, nombreComponente: String) {
        viewModelScope.launch {
            levantamientoDao.insertarComponente(
                Componente(levantamientoId = levantamientoId, nombre = nombreComponente)
            )
        }
    }

    fun cambiarEstadoComponente(componente: Componente) {
        val siguienteEstado = when (componente.estado) {
            Estados.PENDIENTE -> Estados.VALIDADO
            Estados.VALIDADO -> Estados.OBSERVADO
            else -> Estados.PENDIENTE
        }
        viewModelScope.launch {
            levantamientoDao.actualizarComponente(componente.copy(estado = siguienteEstado))
        }
    }

    private suspend fun cargarDatosDeEjemplo() {
        if (levantamientoDao.contar() > 0) return

        val ejemplos = listOf(
            Levantamiento(
                codigoRack = "C-03",
                dataCenter = "DC Norte",
                sala = "Sala 3",
                fecha = "10/10/2026",
                estado = Estados.APROBADO,
                inspectorId = 0,
                inspectorNombre = "N. Apellido",
                creadoEn = Fechas.aMilisegundos("10/10/2026 08:40"),
                actualizadoEn = Fechas.aMilisegundos("10/10/2026 10:15"),
                aprobadoPor = "Supervisor CMC360",
                aprobadoEn = Fechas.aMilisegundos("10/10/2026 16:20")
            ) to listOf(Componente(levantamientoId = 0, nombre = "Servidor 2U", estado = Estados.VALIDADO)),
            Levantamiento(
                codigoRack = "B-05",
                dataCenter = "DC Respaldo",
                sala = "Sala 1",
                fecha = "12/10/2026",
                estado = Estados.APROBADO,
                inspectorId = 0,
                inspectorNombre = "N. Apellido",
                creadoEn = Fechas.aMilisegundos("12/10/2026 09:05"),
                actualizadoEn = Fechas.aMilisegundos("12/10/2026 12:30"),
                aprobadoPor = "Supervisor CMC360",
                aprobadoEn = Fechas.aMilisegundos("12/10/2026 17:45")
            ) to listOf(
                Componente(levantamientoId = 0, nombre = "UPS 20 kVA", estado = Estados.VALIDADO),
                Componente(levantamientoId = 0, nombre = "Firewall 1U", estado = Estados.VALIDADO)
            ),
            Levantamiento(
                codigoRack = "A-02",
                dataCenter = "DC Principal",
                sala = "Sala 2",
                fecha = "14/10/2026",
                inspectorId = 0,
                inspectorNombre = "N. Apellido",
                creadoEn = Fechas.aMilisegundos("14/10/2026 09:40")
            ) to listOf(
                Componente(levantamientoId = 0, nombre = "PDU 32A", estado = Estados.PENDIENTE),
                Componente(levantamientoId = 0, nombre = "Patch panel 24p", estado = Estados.PENDIENTE)
            ),
            Levantamiento(
                codigoRack = "A-01",
                dataCenter = "DC Principal",
                sala = "Sala 2",
                fecha = "14/10/2026",
                inspectorId = 0,
                inspectorNombre = "N. Apellido",
                creadoEn = Fechas.aMilisegundos("14/10/2026 09:12"),
                actualizadoEn = Fechas.aMilisegundos("14/10/2026 11:30")
            ) to listOf(
                Componente(levantamientoId = 0, nombre = "UPS 20 kVA", estado = Estados.VALIDADO),
                Componente(levantamientoId = 0, nombre = "Switch core 48p", estado = Estados.PENDIENTE),
                Componente(levantamientoId = 0, nombre = "PDU 32A", estado = Estados.OBSERVADO)
            )
        )

        for ((levantamiento, componentes) in ejemplos) {
            val idGenerado = levantamientoDao.insertar(levantamiento).toInt()
            for (componente in componentes) {
                levantamientoDao.insertarComponente(componente.copy(levantamientoId = idGenerado))
            }
        }
    }
}
