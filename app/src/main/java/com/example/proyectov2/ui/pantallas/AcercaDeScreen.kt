package com.example.proyectov2.ui.pantallas

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectov2.R
import com.example.proyectov2.ui.componentes.Encabezado
import com.example.proyectov2.ui.componentes.Tarjeta
import com.example.proyectov2.ui.theme.AzulPetroleo
import com.example.proyectov2.ui.theme.FondoGris
import com.example.proyectov2.ui.theme.TextoPrincipal
import com.example.proyectov2.ui.theme.TextoSecundario

@Composable
fun AcercaDeScreen() {
    val animacionLogo = rememberInfiniteTransition(label = "animacionLogo")
    val escalaLogo by animacionLogo.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escalaLogo"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoGris)
    ) {
        Encabezado(subtitulo = "Rack - Pro", titulo = "Acerca de")

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Tarjeta {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(72.dp)
                        .scale(escalaLogo)
                        .clip(CircleShape)
                        .background(AzulPetroleo)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_rack),
                        contentDescription = "Logo Rack - Pro",
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Rack - Pro",
                    color = TextoPrincipal,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = "Versión 1.0.0 · MVP",
                    color = TextoSecundario,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            DatoAcercaDe(titulo = "Proyecto", valor = "Académico DSY1105")
            DatoAcercaDe(titulo = "Caso", valor = "CMC360 SpA · Levantamiento de Data Centers")
            DatoAcercaDe(titulo = "Plataforma", valor = "Android")
        }
    }
}

@Composable
private fun DatoAcercaDe(titulo: String, valor: String) {
    Tarjeta {
        Text(text = titulo, color = TextoSecundario, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = valor, color = TextoPrincipal, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
