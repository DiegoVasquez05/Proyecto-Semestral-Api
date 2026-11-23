package com.example.proyectov2.ui.pantallas

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import com.example.proyectov2.data.Estados
import com.example.proyectov2.data.Roles
import com.example.proyectov2.ui.componentes.AvatarUsuario
import com.example.proyectov2.ui.componentes.BotonPrincipal
import com.example.proyectov2.ui.componentes.BotonSecundario
import com.example.proyectov2.ui.componentes.Encabezado
import com.example.proyectov2.ui.componentes.Tarjeta
import com.example.proyectov2.ui.theme.Blanco
import com.example.proyectov2.ui.theme.FondoGris
import com.example.proyectov2.ui.theme.RojoTexto
import com.example.proyectov2.ui.theme.TextoPrincipal
import com.example.proyectov2.ui.theme.TextoSecundario
import com.example.proyectov2.ui.theme.VerdeAzulado
import com.example.proyectov2.ui.theme.VerdeFondo
import com.example.proyectov2.ui.theme.VerdeTexto
import com.example.proyectov2.ui.viewmodel.LevantamientosViewModel
import com.example.proyectov2.ui.viewmodel.SesionViewModel

@Composable
fun PerfilScreen(
    sesionViewModel: SesionViewModel,
    levantamientosViewModel: LevantamientosViewModel,
    onEditarPerfil: () -> Unit,
    onSesionCerrada: () -> Unit
) {
    val usuario = sesionViewModel.usuarioActual ?: return
    val todosLosLevantamientos by levantamientosViewModel.levantamientos.collectAsState()
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }

    val misLevantamientos = if (usuario.rol == Roles.SUPERVISOR) {
        todosLosLevantamientos
    } else {
        todosLosLevantamientos.filter { levantamiento -> levantamiento.inspectorId == usuario.id }
    }
    val totalRealizadas = misLevantamientos.size
    val totalAprobadas = misLevantamientos.count { levantamiento -> levantamiento.estado == Estados.APROBADO }
    val totalPendientes = misLevantamientos.count { levantamiento -> levantamiento.estado == Estados.PENDIENTE }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoGris)
    ) {
        Encabezado(subtitulo = "Rack - Pro", titulo = "Mi perfil")

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            AvatarUsuario(nombre = usuario.nombreCompleto, fotoUri = usuario.fotoUri, tamano = 80.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = usuario.nombreCompleto,
                color = TextoPrincipal,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = usuario.rol,
                color = VerdeTexto,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(VerdeFondo)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TarjetaEstadistica(valor = totalRealizadas, titulo = "Realizadas", modifier = Modifier.weight(1f))
                TarjetaEstadistica(valor = totalAprobadas, titulo = "Aprobadas", modifier = Modifier.weight(1f))
                TarjetaEstadistica(valor = totalPendientes, titulo = "Pendientes", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))

            Tarjeta {
                DatoPerfil(titulo = "Correo", valor = usuario.correo)
                DatoPerfil(titulo = "Cargo", valor = usuario.cargo.ifBlank { "Sin cargo registrado" })
                DatoPerfil(titulo = "Teléfono", valor = usuario.telefono.ifBlank { "Sin teléfono registrado" })
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            BotonPrincipal(texto = "Editar perfil", onClick = onEditarPerfil)
            Spacer(modifier = Modifier.height(10.dp))
            BotonSecundario(texto = "Cerrar sesión", onClick = { mostrarDialogoCerrarSesion = true })
        }
    }

    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarSesion = false },
            containerColor = Blanco,
            title = { Text("Cerrar sesión", fontWeight = FontWeight.Bold) },
            text = { Text("¿Seguro que quieres salir de Rack - Pro?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoCerrarSesion = false
                    sesionViewModel.cerrarSesion(onSesionCerrada)
                }) {
                    Text("Salir", color = RojoTexto, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrarSesion = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            }
        )
    }
}

@Composable
private fun TarjetaEstadistica(valor: Int, titulo: String, modifier: Modifier = Modifier) {
    var mostrarValor by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { mostrarValor = true }
    val valorAnimado by animateIntAsState(
        targetValue = if (mostrarValor) valor else 0,
        animationSpec = tween(durationMillis = 900),
        label = "contadorEstadistica"
    )

    Tarjeta(modifier = modifier) {
        Text(
            text = valorAnimado.toString(),
            color = TextoPrincipal,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = titulo,
            color = TextoSecundario,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun DatoPerfil(titulo: String, valor: String) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Text(text = titulo, color = TextoSecundario, fontSize = 11.sp)
        Text(text = valor, color = TextoPrincipal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
