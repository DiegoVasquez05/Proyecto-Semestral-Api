package com.example.proyectov2.ui.pantallas

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectov2.data.Roles
import com.example.proyectov2.ui.componentes.BotonPrincipal
import com.example.proyectov2.ui.componentes.BotonSecundario
import com.example.proyectov2.ui.componentes.CampoFormulario
import com.example.proyectov2.ui.componentes.Encabezado
import com.example.proyectov2.ui.componentes.SelectorRol
import com.example.proyectov2.ui.componentes.TextoInformativo
import com.example.proyectov2.ui.theme.FondoGris
import com.example.proyectov2.ui.theme.RojoTexto
import com.example.proyectov2.ui.theme.TextoSecundario
import com.example.proyectov2.ui.viewmodel.SesionViewModel
import com.example.proyectov2.utilidades.Validaciones

@Composable
fun RegistroScreen(
    viewModel: SesionViewModel,
    onRegistroExitoso: () -> Unit,
    onIrALogin: () -> Unit
) {
    var nombreCompleto by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }
    var rolSeleccionado by rememberSaveable { mutableStateOf(Roles.INSPECTOR) }

    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorContrasena by remember { mutableStateOf<String?>(null) }
    var errorConfirmacion by remember { mutableStateOf<String?>(null) }
    var errorGeneral by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoGris)
            .imePadding()
    ) {
        Encabezado(subtitulo = "Rack - Pro", titulo = "Crear cuenta")

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
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
                etiqueta = "Contraseña",
                valor = contrasena,
                onValorCambia = { contrasena = it; errorContrasena = null },
                error = errorContrasena,
                esContrasena = true
            )
            CampoFormulario(
                etiqueta = "Confirmar contraseña",
                valor = confirmacion,
                onValorCambia = { confirmacion = it; errorConfirmacion = null },
                error = errorConfirmacion,
                esContrasena = true
            )
            Text("Registrarme como", color = TextoSecundario, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            SelectorRol(
                rolSeleccionado = rolSeleccionado,
                onRolCambia = { rolSeleccionado = it },
                roles = listOf(Roles.INSPECTOR, Roles.SUPERVISOR)
            )
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedVisibility(visible = errorGeneral != null) {
                TextoInformativo(texto = errorGeneral ?: "", color = RojoTexto)
            }
        }

        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            BotonPrincipal(
                texto = if (viewModel.procesando) "Creando cuenta..." else "Crear cuenta",
                habilitado = !viewModel.procesando,
                onClick = {
                    errorNombre = Validaciones.validarNombre(nombreCompleto)
                    errorCorreo = Validaciones.validarCorreo(correo)
                    errorContrasena = Validaciones.validarContrasenaNueva(contrasena)
                    errorConfirmacion = Validaciones.validarConfirmacion(contrasena, confirmacion)

                    val formularioValido = errorNombre == null && errorCorreo == null &&
                        errorContrasena == null && errorConfirmacion == null

                    if (formularioValido) {
                        viewModel.registrarUsuario(
                            nombreCompleto = nombreCompleto,
                            correo = correo,
                            contrasena = contrasena,
                            rol = rolSeleccionado
                        ) { mensajeDeError ->
                            if (mensajeDeError == null) {
                                onRegistroExitoso()
                            } else {
                                errorGeneral = mensajeDeError
                            }
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            BotonSecundario(texto = "¿Ya tienes cuenta? Inicia sesión", onClick = onIrALogin)
        }
    }
}
