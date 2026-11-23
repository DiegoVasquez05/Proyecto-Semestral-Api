package com.example.proyectov2.ui.pantallas

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectov2.camara.FotoGuardada
import com.example.proyectov2.camara.rememberLanzadorCamara
import com.example.proyectov2.camara.rememberLanzadorGaleria
import com.example.proyectov2.data.Estados
import com.example.proyectov2.data.Roles
import com.example.proyectov2.data.local.Componente
import com.example.proyectov2.data.local.Levantamiento
import com.example.proyectov2.data.local.Usuario
import com.example.proyectov2.ui.componentes.BotonPrincipal
import com.example.proyectov2.ui.componentes.BotonSecundario
import com.example.proyectov2.ui.componentes.CampoFormulario
import com.example.proyectov2.ui.componentes.Encabezado
import com.example.proyectov2.ui.componentes.EtiquetaEstado
import com.example.proyectov2.ui.componentes.Tarjeta
import com.example.proyectov2.ui.componentes.TextoInformativo
import com.example.proyectov2.ui.theme.AzulPetroleo
import com.example.proyectov2.ui.theme.Blanco
import com.example.proyectov2.ui.theme.FondoGris
import com.example.proyectov2.ui.theme.GrisFoto
import com.example.proyectov2.ui.theme.RojoTexto
import com.example.proyectov2.ui.theme.TextoPrincipal
import com.example.proyectov2.ui.theme.TextoSecundario
import com.example.proyectov2.ui.theme.VerdeAzulado
import com.example.proyectov2.ui.viewmodel.LevantamientosViewModel
import com.example.proyectov2.ui.viewmodel.SesionViewModel
import com.example.proyectov2.utilidades.Fechas
import com.example.proyectov2.utilidades.Validaciones

@Composable
fun DetalleRackScreen(
    sesionViewModel: SesionViewModel,
    viewModel: LevantamientosViewModel,
    levantamientoId: Int,
    onVolver: () -> Unit
) {
    val usuario = sesionViewModel.usuarioActual ?: return
    val levantamientoFlow = remember(levantamientoId) { viewModel.observarLevantamiento(levantamientoId) }
    val componentesFlow = remember(levantamientoId) { viewModel.observarComponentes(levantamientoId) }
    val levantamiento by levantamientoFlow.collectAsState(initial = null)
    val componentes by componentesFlow.collectAsState(initial = emptyList())

    // Al abrir el detalle se piden a la API los datos más recientes de este levantamiento.
    LaunchedEffect(levantamientoId) { viewModel.actualizarDetalle(levantamientoId) }

    val levantamientoCargado = levantamiento
    if (levantamientoCargado == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoGris)
        ) {
            Encabezado(subtitulo = "Levantamientos · Detalle", titulo = "Cargando...", onVolver = onVolver)
        }
        return
    }

    ContenidoDetalle(
        viewModel = viewModel,
        usuario = usuario,
        levantamiento = levantamientoCargado,
        componentes = componentes,
        onVolver = onVolver
    )
}

@Composable
private fun ContenidoDetalle(
    viewModel: LevantamientosViewModel,
    usuario: Usuario,
    levantamiento: Levantamiento,
    componentes: List<Componente>,
    onVolver: () -> Unit
) {
    val esSupervisor = usuario.rol == Roles.SUPERVISOR
    val estaAprobado = levantamiento.estado == Estados.APROBADO
    val sePuedeEditar = !estaAprobado

    var codigoRack by remember(levantamiento.id) { mutableStateOf(levantamiento.codigoRack) }
    var fecha by remember(levantamiento.id) { mutableStateOf(levantamiento.fecha) }
    var errorCodigo by remember { mutableStateOf<String?>(null) }
    var errorFecha by remember { mutableStateOf<String?>(null) }

    var mostrarDialogoComponente by remember { mutableStateOf(false) }
    var mostrarDialogoAprobar by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    val tomarFoto = rememberLanzadorCamara { uriDeLaFoto ->
        viewModel.guardarFotoLevantamiento(levantamiento.id, uriDeLaFoto)
    }
    val elegirDeGaleria = rememberLanzadorGaleria { uriDeLaFoto ->
        viewModel.guardarFotoLevantamiento(levantamiento.id, uriDeLaFoto)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoGris)
            .imePadding()
    ) {
        Encabezado(
            subtitulo = "Levantamientos · Detalle",
            titulo = "Rack ${levantamiento.codigoRack}",
            onVolver = onVolver
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GrisFoto)
            ) {
                Crossfade(targetState = levantamiento.fotoUri, label = "fotoRack") { fotoUri ->
                    if (fotoUri != null) {
                        FotoGuardada(
                            fotoUri = fotoUri,
                            descripcion = "Foto frontal del rack",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (levantamiento.fotoUri != null) "Evidencia: frontal" else "Sin evidencia",
                        color = TextoPrincipal,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Blanco.copy(alpha = 0.7f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    BotonFoto(texto = "Cámara", habilitado = sePuedeEditar, onClick = tomarFoto)
                    Spacer(modifier = Modifier.width(8.dp))
                    BotonFoto(texto = "Galería", habilitado = sePuedeEditar, onClick = elegirDeGaleria)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoFormulario(
                    etiqueta = "ID de rack",
                    valor = codigoRack,
                    onValorCambia = { codigoRack = it; errorCodigo = null },
                    error = errorCodigo,
                    habilitado = sePuedeEditar,
                    modifier = Modifier.weight(1f)
                )
                CampoFormulario(
                    etiqueta = "Fecha",
                    valor = fecha,
                    onValorCambia = { fecha = it; errorFecha = null },
                    error = errorFecha,
                    habilitado = sePuedeEditar,
                    modifier = Modifier.weight(1f)
                )
            }

            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Componentes del catálogo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (sePuedeEditar) {
                        Text(
                            text = "+ Agregar",
                            color = VerdeAzulado,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable {
                                viewModel.cargarCatalogo()
                                mostrarDialogoComponente = true
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (componentes.isEmpty()) {
                    Text("Aún no hay componentes registrados", color = TextoSecundario, fontSize = 13.sp)
                }
                componentes.forEach { componente ->
                    val sePuedeCambiarEstado = esSupervisor && sePuedeEditar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = sePuedeCambiarEstado) {
                                viewModel.cambiarEstadoComponente(componente)
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(text = componente.nombre, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        EtiquetaEstado(estado = componente.estado)
                    }
                }
                if (esSupervisor && sePuedeEditar && componentes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Toca un componente para cambiar su estado",
                        color = TextoSecundario,
                        fontSize = 11.sp
                    )
                }
            }

            Tarjeta {
                Text("Trazabilidad", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = textoTrazabilidad(levantamiento),
                    color = TextoSecundario,
                    fontSize = 12.sp
                )
                if (levantamiento.aprobadoEn != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Aprobado ${Fechas.diaYHora(levantamiento.aprobadoEn)} · ${levantamiento.aprobadoPor}",
                        color = TextoSecundario,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            BotonPrincipal(
                texto = "Guardar cambios",
                habilitado = sePuedeEditar,
                onClick = {
                    errorCodigo = Validaciones.validarCodigoRack(codigoRack)
                    errorFecha = Validaciones.validarFecha(fecha)
                    if (errorCodigo == null && errorFecha == null) {
                        viewModel.guardarCambiosLevantamiento(levantamiento, codigoRack, fecha)
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            when {
                estaAprobado -> TextoInformativo(texto = "Este levantamiento ya fue aprobado", color = VerdeAzulado)
                esSupervisor -> BotonSecundario(
                    texto = "Aprobar levantamiento",
                    onClick = { mostrarDialogoAprobar = true }
                )
                else -> BotonSecundario(
                    texto = "Eliminar levantamiento",
                    onClick = { mostrarDialogoEliminar = true }
                )
            }
        }
    }

    if (mostrarDialogoComponente) {
        DialogoAgregarComponente(
            catalogo = viewModel.catalogoComponentes,
            onCancelar = { mostrarDialogoComponente = false },
            onElegir = { nombreComponente ->
                viewModel.agregarComponente(levantamiento.id, nombreComponente)
                mostrarDialogoComponente = false
            }
        )
    }

    if (mostrarDialogoAprobar) {
        val componentesPendientes = componentes.count { componente -> componente.estado != Estados.VALIDADO }
        AlertDialog(
            onDismissRequest = { mostrarDialogoAprobar = false },
            containerColor = Blanco,
            title = { Text("Aprobar levantamiento", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (componentesPendientes > 0) {
                        "Hay $componentesPendientes componente(s) sin validar. ¿Deseas aprobar el Rack ${levantamiento.codigoRack} de todas formas?"
                    } else {
                        "¿Confirmas la aprobación del Rack ${levantamiento.codigoRack}?"
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.aprobarLevantamiento(levantamiento, usuario)
                    mostrarDialogoAprobar = false
                }) {
                    Text("Aprobar", color = VerdeAzulado, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoAprobar = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            }
        )
    }

    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            containerColor = Blanco,
            title = { Text("Eliminar levantamiento", fontWeight = FontWeight.Bold) },
            text = { Text("¿Seguro que quieres eliminar el Rack ${levantamiento.codigoRack}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoEliminar = false
                    viewModel.eliminarLevantamiento(levantamiento, alEliminar = onVolver)
                }) {
                    Text("Eliminar", color = RojoTexto, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            }
        )
    }
}

@Composable
private fun BotonFoto(texto: String, habilitado: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo)
    ) {
        Text(text = texto, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DialogoAgregarComponente(catalogo: List<String>, onCancelar: () -> Unit, onElegir: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = Blanco,
        title = { Text("Agregar componente", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (catalogo.isEmpty()) {
                    Text("Cargando catálogo desde el servidor...", color = TextoSecundario, fontSize = 13.sp)
                }
                catalogo.forEach { nombreComponente ->
                    Text(
                        text = nombreComponente,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onElegir(nombreComponente) }
                            .padding(vertical = 12.dp)
                    )
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar", color = TextoSecundario)
            }
        }
    )
}

private fun textoTrazabilidad(levantamiento: Levantamiento): String {
    val creado = "Creado ${Fechas.diaYHora(levantamiento.creadoEn)}"
    val guardado = levantamiento.actualizadoEn?.let { " · Guardado ${Fechas.hora(it)}" } ?: ""
    return "$creado$guardado · Inspector ${levantamiento.inspectorNombre}"
}
