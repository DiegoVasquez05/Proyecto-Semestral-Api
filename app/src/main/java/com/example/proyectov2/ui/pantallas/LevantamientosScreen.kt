package com.example.proyectov2.ui.pantallas

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectov2.RackProViewModel
import com.example.proyectov2.data.Estados
import com.example.proyectov2.data.Levantamiento
import com.example.proyectov2.data.Roles
import com.example.proyectov2.ui.componentes.BotonPrincipal
import com.example.proyectov2.ui.componentes.CampoFormulario
import com.example.proyectov2.ui.componentes.Encabezado
import com.example.proyectov2.ui.componentes.EtiquetaEstado
import com.example.proyectov2.ui.componentes.TextoInformativo
import com.example.proyectov2.ui.theme.AzulPetroleo
import com.example.proyectov2.ui.theme.Blanco
import com.example.proyectov2.ui.theme.BordeGris
import com.example.proyectov2.ui.theme.FondoGris
import com.example.proyectov2.ui.theme.TextoPrincipal
import com.example.proyectov2.ui.theme.TextoSecundario
import com.example.proyectov2.ui.theme.VerdeAzulado
import com.example.proyectov2.utilidades.Fechas
import com.example.proyectov2.utilidades.Validaciones

private const val FILTRO_TODOS = "Todos"

@Composable
fun LevantamientosScreen(viewModel: RackProViewModel, onAbrirDetalle: (Int) -> Unit) {
    val usuario = viewModel.usuarioActual
    val levantamientos by viewModel.levantamientos.collectAsState()
    var filtroSeleccionado by rememberSaveable { mutableStateOf(FILTRO_TODOS) }
    var mostrarDialogoNuevaInspeccion by remember { mutableStateOf(false) }

    val levantamientosFiltrados = if (filtroSeleccionado == FILTRO_TODOS) {
        levantamientos
    } else {
        levantamientos.filter { levantamiento -> levantamiento.estado == filtroSeleccionado }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoGris)
    ) {
        Encabezado(subtitulo = "Rack - Pro · ${usuario?.rol ?: ""}", titulo = "Levantamientos")

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            listOf(FILTRO_TODOS, Estados.PENDIENTE, Estados.APROBADO).forEach { filtro ->
                ChipFiltro(
                    texto = filtro,
                    seleccionado = filtro == filtroSeleccionado,
                    onClick = { filtroSeleccionado = filtro }
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (levantamientosFiltrados.isEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = 40.dp)) {
                        TextoInformativo(texto = "No hay levantamientos para mostrar")
                    }
                }
            }
            items(levantamientosFiltrados, key = { levantamiento -> levantamiento.id }) { levantamiento ->
                TarjetaLevantamiento(
                    levantamiento = levantamiento,
                    onClick = { onAbrirDetalle(levantamiento.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }

        if (usuario?.rol == Roles.INSPECTOR) {
            BotonPrincipal(
                texto = "+ Nueva inspección",
                onClick = { mostrarDialogoNuevaInspeccion = true },
                modifier = Modifier.padding(16.dp)
            )
        } else {
            TextoInformativo(texto = "Abre un levantamiento pendiente para revisarlo y aprobarlo")
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (mostrarDialogoNuevaInspeccion) {
        DialogoNuevaInspeccion(
            onCancelar = { mostrarDialogoNuevaInspeccion = false },
            onCrear = { codigoRack, dataCenter, sala, fecha ->
                mostrarDialogoNuevaInspeccion = false
                viewModel.crearLevantamiento(codigoRack, dataCenter, sala, fecha) { idNuevo ->
                    onAbrirDetalle(idNuevo)
                }
            }
        )
    }
}

@Composable
private fun ChipFiltro(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    val colorFondo by animateColorAsState(
        targetValue = if (seleccionado) AzulPetroleo else Blanco,
        label = "colorFondoFiltro"
    )
    val colorTexto by animateColorAsState(
        targetValue = if (seleccionado) Blanco else TextoPrincipal,
        label = "colorTextoFiltro"
    )

    Text(
        text = texto,
        color = colorTexto,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(colorFondo)
            .border(1.dp, if (seleccionado) AzulPetroleo else BordeGris, RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@Composable
private fun TarjetaLevantamiento(
    levantamiento: Levantamiento,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Blanco,
        border = BorderStroke(1.dp, BordeGris),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Rack ${levantamiento.codigoRack} · ${levantamiento.dataCenter}",
                    color = TextoPrincipal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                EtiquetaEstado(estado = levantamiento.estado)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${levantamiento.sala} · ${levantamiento.fecha}",
                color = TextoSecundario,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DialogoNuevaInspeccion(
    onCancelar: () -> Unit,
    onCrear: (codigoRack: String, dataCenter: String, sala: String, fecha: String) -> Unit
) {
    var codigoRack by rememberSaveable { mutableStateOf("") }
    var dataCenter by rememberSaveable { mutableStateOf("") }
    var sala by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf(Fechas.hoy()) }

    var errorCodigo by remember { mutableStateOf<String?>(null) }
    var errorDataCenter by remember { mutableStateOf<String?>(null) }
    var errorSala by remember { mutableStateOf<String?>(null) }
    var errorFecha by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = FondoGris,
        title = { Text("Nueva inspección", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                CampoFormulario(
                    etiqueta = "ID de rack",
                    valor = codigoRack,
                    onValorCambia = { codigoRack = it; errorCodigo = null },
                    error = errorCodigo
                )
                CampoFormulario(
                    etiqueta = "Data center",
                    valor = dataCenter,
                    onValorCambia = { dataCenter = it; errorDataCenter = null },
                    error = errorDataCenter
                )
                CampoFormulario(
                    etiqueta = "Sala",
                    valor = sala,
                    onValorCambia = { sala = it; errorSala = null },
                    error = errorSala
                )
                CampoFormulario(
                    etiqueta = "Fecha (dd/mm/aaaa)",
                    valor = fecha,
                    onValorCambia = { fecha = it; errorFecha = null },
                    error = errorFecha
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                errorCodigo = Validaciones.validarCodigoRack(codigoRack)
                errorDataCenter = Validaciones.validarCampoObligatorio(dataCenter, "el data center")
                errorSala = Validaciones.validarCampoObligatorio(sala, "la sala")
                errorFecha = Validaciones.validarFecha(fecha)

                val formularioValido = errorCodigo == null && errorDataCenter == null &&
                    errorSala == null && errorFecha == null

                if (formularioValido) {
                    onCrear(codigoRack, dataCenter, sala, fecha)
                }
            }) {
                Text("Crear", color = VerdeAzulado, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar", color = TextoSecundario)
            }
        }
    )
}
