package com.example.proyectov2.ui.pantallas

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.proyectov2.camara.CamaraFotos
import com.example.proyectov2.ui.componentes.BotonPrincipal
import com.example.proyectov2.ui.componentes.BotonSecundario
import com.example.proyectov2.ui.componentes.CampoFormulario
import com.example.proyectov2.ui.componentes.Encabezado
import com.example.proyectov2.ui.componentes.TextoInformativo
import com.example.proyectov2.ui.theme.FondoGris
import com.example.proyectov2.ui.theme.RojoTexto
import com.example.proyectov2.ui.viewmodel.SesionViewModel
import com.example.proyectov2.utilidades.Validaciones

@Composable
fun EditarPerfilScreen(viewModel: SesionViewModel, onVolver: () -> Unit) {
    val usuario = viewModel.usuarioActual ?: return
    val context = LocalContext.current

    var nombreCompleto by rememberSaveable { mutableStateOf(usuario.nombreCompleto) }
    var correo by rememberSaveable { mutableStateOf(usuario.correo) }
    var cargo by rememberSaveable { mutableStateOf(usuario.cargo) }
    var telefono by rememberSaveable { mutableStateOf(usuario.telefono) }
    var fotoUri by rememberSaveable { mutableStateOf(usuario.fotoUri) }

    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorCargo by remember { mutableStateOf<String?>(null) }
    var errorTelefono by remember { mutableStateOf<String?>(null) }
    var errorGeneral by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoGris)
            .imePadding()
    ) {
        Encabezado(subtitulo = "Mi perfil", titulo = "Editar perfil", onVolver = onVolver)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                CamaraFotos(
                    nombreUsuario = nombreCompleto,
                    fotoUri = fotoUri,
                    onFotoTomada = { uriDeLaFoto -> fotoUri = uriDeLaFoto.toString() }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            CampoFormulario(
                etiqueta = "Nombre completo",
                valor = nombreCompleto,
                onValorCambia = { nombreCompleto = it; errorNombre = null },
                error = errorNombre
            )
            CampoFormulario(
                etiqueta = "Correo",
                valor = correo,
                onValorCambia = { correo = it; errorCorreo = null; errorGeneral = null },
                error = errorCorreo,
                tipoTeclado = KeyboardType.Email
            )
            CampoFormulario(
                etiqueta = "Cargo",
                valor = cargo,
                onValorCambia = { cargo = it; errorCargo = null },
                error = errorCargo
            )
            CampoFormulario(
                etiqueta = "Teléfono",
                valor = telefono,
                onValorCambia = { telefono = it; errorTelefono = null },
                error = errorTelefono,
                tipoTeclado = KeyboardType.Phone
            )
            AnimatedVisibility(visible = errorGeneral != null) {
                TextoInformativo(texto = errorGeneral ?: "", color = RojoTexto)
            }
        }

        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            BotonPrincipal(
                texto = if (viewModel.procesando) "Guardando..." else "Guardar cambios",
                habilitado = !viewModel.procesando,
                onClick = {
                    errorNombre = Validaciones.validarNombre(nombreCompleto)
                    errorCorreo = Validaciones.validarCorreo(correo)
                    errorCargo = Validaciones.validarCampoObligatorio(cargo, "tu cargo")
                    errorTelefono = Validaciones.validarTelefono(telefono)

                    val formularioValido = errorNombre == null && errorCorreo == null &&
                        errorCargo == null && errorTelefono == null

                    if (formularioValido) {
                        viewModel.actualizarPerfil(nombreCompleto, correo, cargo, telefono, fotoUri) { mensajeDeError ->
                            if (mensajeDeError == null) {
                                Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                                onVolver()
                            } else {
                                errorGeneral = mensajeDeError
                            }
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            BotonSecundario(texto = "Cancelar", onClick = onVolver)
        }
    }
}
