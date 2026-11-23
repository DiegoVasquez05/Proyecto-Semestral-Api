package com.example.proyectov2.ui.pantallas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectov2.R
import com.example.proyectov2.model.Roles
import com.example.proyectov2.ui.componentes.BotonPrincipal
import com.example.proyectov2.ui.componentes.BotonSecundario
import com.example.proyectov2.ui.componentes.CampoFormulario
import com.example.proyectov2.ui.componentes.SelectorRol
import com.example.proyectov2.ui.componentes.TextoInformativo
import com.example.proyectov2.ui.theme.AzulPetroleo
import com.example.proyectov2.ui.theme.Blanco
import com.example.proyectov2.ui.theme.FondoGris
import com.example.proyectov2.ui.theme.RojoTexto
import com.example.proyectov2.ui.theme.TextoSecundario
import com.example.proyectov2.utilidades.Validaciones
import com.example.proyectov2.viewmodel.SesionViewModel

@Composable
fun LoginScreen(
    viewModel: SesionViewModel,
    onIngresoExitoso: () -> Unit,
    onIrARegistro: () -> Unit
) {
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var rolSeleccionado by rememberSaveable { mutableStateOf(Roles.INSPECTOR) }

    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorContrasena by remember { mutableStateOf<String?>(null) }
    var errorGeneral by remember { mutableStateOf<String?>(null) }

    var mostrarLogo by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { mostrarLogo = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoGris)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulPetroleo)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            AnimatedVisibility(
                visible = mostrarLogo,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { altura -> -altura / 2 }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("CMC360 SpA", color = Blanco.copy(alpha = 0.8f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Rack - Pro", color = Blanco, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Levantamiento digital de Data Centers",
                            color = Blanco.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                    Image(
                        painter = painterResource(R.drawable.ic_rack),
                        contentDescription = "Logo Rack - Pro",
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            CampoFormulario(
                etiqueta = "Correo",
                valor = correo,
                onValorCambia = { textoNuevo ->
                    correo = textoNuevo
                    errorCorreo = null
                    errorGeneral = null
                },
                error = errorCorreo,
                tipoTeclado = KeyboardType.Email
            )
            CampoFormulario(
                etiqueta = "Contraseña",
                valor = contrasena,
                onValorCambia = { textoNuevo ->
                    contrasena = textoNuevo
                    errorContrasena = null
                    errorGeneral = null
                },
                error = errorContrasena,
                esContrasena = true
            )
            Text("Ingresar como", color = TextoSecundario, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            SelectorRol(
                rolSeleccionado = rolSeleccionado,
                onRolCambia = { rolNuevo ->
                    rolSeleccionado = rolNuevo
                    errorGeneral = null
                },
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
                texto = if (viewModel.procesando) "Ingresando..." else "Ingresar",
                habilitado = !viewModel.procesando,
                onClick = {
                    errorCorreo = Validaciones.validarCorreo(correo)
                    errorContrasena = Validaciones.validarContrasenaIngresada(contrasena)
                    if (errorCorreo == null && errorContrasena == null) {
                        viewModel.iniciarSesion(correo, contrasena, rolSeleccionado) { mensajeDeError ->
                            if (mensajeDeError == null) {
                                onIngresoExitoso()
                            } else {
                                errorGeneral = mensajeDeError
                            }
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            BotonSecundario(texto = "¿No tienes cuenta? Regístrate", onClick = onIrARegistro)
            Spacer(modifier = Modifier.height(12.dp))
            TextoInformativo(texto = "Cuentas de prueba: inspector@cmc360.cl y supervisor@cmc360.cl · clave rackpro123")
        }
    }
}
