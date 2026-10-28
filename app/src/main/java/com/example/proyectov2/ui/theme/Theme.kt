package com.example.proyectov2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ColoresRackPro = lightColorScheme(
    primary = VerdeAzulado,
    onPrimary = Blanco,
    secondary = AzulPetroleo,
    onSecondary = Blanco,
    background = FondoGris,
    onBackground = TextoPrincipal,
    surface = Blanco,
    onSurface = TextoPrincipal,
    onSurfaceVariant = TextoSecundario,
    outline = BordeGris,
    error = RojoTexto
)

@Composable
fun RackProTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColoresRackPro,
        typography = Typography,
        content = content
    )
}
