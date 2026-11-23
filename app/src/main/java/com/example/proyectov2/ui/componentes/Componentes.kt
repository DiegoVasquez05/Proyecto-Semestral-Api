package com.example.proyectov2.ui.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectov2.camara.FotoGuardada
import com.example.proyectov2.model.Estados
import com.example.proyectov2.navegacion.Rutas
import com.example.proyectov2.ui.theme.AmarilloFondo
import com.example.proyectov2.ui.theme.AmarilloTexto
import com.example.proyectov2.ui.theme.AzulPetroleo
import com.example.proyectov2.ui.theme.Blanco
import com.example.proyectov2.ui.theme.BordeGris
import com.example.proyectov2.ui.theme.RojoFondo
import com.example.proyectov2.ui.theme.RojoTexto
import com.example.proyectov2.ui.theme.TextoPrincipal
import com.example.proyectov2.ui.theme.TextoSecundario
import com.example.proyectov2.ui.theme.VerdeAzulado
import com.example.proyectov2.ui.theme.VerdeFondo
import com.example.proyectov2.ui.theme.VerdeTexto

@Composable
fun Encabezado(subtitulo: String, titulo: String, onVolver: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AzulPetroleo)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(text = subtitulo, color = Blanco.copy(alpha = 0.8f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onVolver != null) {
                Text(
                    text = "‹",
                    color = Blanco,
                    fontSize = 26.sp,
                    modifier = Modifier
                        .clickable { onVolver() }
                        .padding(end = 10.dp)
                )
            }
            Text(text = titulo, color = Blanco, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CampoFormulario(
    etiqueta: String,
    valor: String,
    onValorCambia: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
    esContrasena: Boolean = false,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    habilitado: Boolean = true
) {
    Column(modifier = modifier.padding(bottom = 12.dp)) {
        Text(text = etiqueta, color = TextoSecundario, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = onValorCambia,
            singleLine = true,
            enabled = habilitado,
            isError = error != null,
            shape = RoundedCornerShape(8.dp),
            visualTransformation = if (esContrasena) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (esContrasena) KeyboardType.Password else tipoTeclado
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Blanco,
                unfocusedContainerColor = Blanco,
                disabledContainerColor = Blanco,
                unfocusedBorderColor = BordeGris,
                focusedBorderColor = VerdeAzulado,
                disabledTextColor = TextoPrincipal
            ),
            modifier = Modifier.fillMaxWidth()
        )
        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = error ?: "",
                color = RojoTexto,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VerdeAzulado),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(text = texto, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (habilitado) VerdeAzulado else BordeGris),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdeAzulado, containerColor = Blanco),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(text = texto, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SelectorRol(rolSeleccionado: String, onRolCambia: (String) -> Unit, roles: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        roles.forEach { rol ->
            val estaSeleccionado = rol == rolSeleccionado
            val colorFondo by animateColorAsState(
                targetValue = if (estaSeleccionado) VerdeAzulado else Blanco,
                label = "colorFondoRol"
            )
            val colorTexto by animateColorAsState(
                targetValue = if (estaSeleccionado) Blanco else VerdeAzulado,
                label = "colorTextoRol"
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorFondo)
                    .border(1.dp, VerdeAzulado, RoundedCornerShape(8.dp))
                    .clickable { onRolCambia(rol) }
            ) {
                Text(text = rol, color = colorTexto, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EtiquetaEstado(estado: String) {
    val (colorFondoDestino, colorTextoDestino) = when (estado) {
        Estados.APROBADO, Estados.VALIDADO -> VerdeFondo to VerdeTexto
        Estados.OBSERVADO -> RojoFondo to RojoTexto
        else -> AmarilloFondo to AmarilloTexto
    }
    val colorFondo by animateColorAsState(colorFondoDestino, label = "colorFondoEstado")
    val colorTexto by animateColorAsState(colorTextoDestino, label = "colorTextoEstado")

    Text(
        text = estado,
        color = colorTexto,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(colorFondo)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    )
}

@Composable
fun Tarjeta(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Blanco,
        border = BorderStroke(1.dp, BordeGris),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), content = contenido)
    }
}

@Composable
fun AvatarUsuario(nombre: String, fotoUri: String?, tamano: Dp, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(VerdeAzulado)
    ) {
        Text(
            text = obtenerIniciales(nombre),
            color = Blanco,
            fontSize = (tamano.value / 3).sp,
            fontWeight = FontWeight.Bold
        )
        if (fotoUri != null) {
            FotoGuardada(
                fotoUri = fotoUri,
                descripcion = "Foto de perfil",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

fun obtenerIniciales(nombre: String): String {
    return nombre.trim()
        .split(" ")
        .filter { palabra -> palabra.isNotBlank() }
        .take(2)
        .joinToString("") { palabra -> palabra.first().uppercase() }
}

@Composable
fun BarraInferior(rutaActual: String?, onNavegar: (String) -> Unit) {
    val pestanas = listOf(
        Rutas.LEVANTAMIENTOS to "Levantamientos",
        Rutas.PERFIL to "Perfil",
        Rutas.ACERCA_DE to "Acerca de"
    )

    Surface(color = Blanco, shadowElevation = 8.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            pestanas.forEach { (ruta, titulo) ->
                val estaSeleccionada = ruta == rutaActual
                val colorTexto by animateColorAsState(
                    targetValue = if (estaSeleccionada) VerdeAzulado else TextoSecundario,
                    label = "colorPestana"
                )
                Text(
                    text = titulo,
                    color = colorTexto,
                    fontSize = 13.sp,
                    fontWeight = if (estaSeleccionada) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavegar(ruta) }
                        .padding(vertical = 18.dp)
                )
            }
        }
    }
}

@Composable
fun TextoInformativo(texto: String, color: Color = TextoSecundario) {
    Text(
        text = texto,
        color = color,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}
