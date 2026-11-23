package com.example.proyectov2.navegacion

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyectov2.R
import com.example.proyectov2.ui.componentes.BarraInferior
import com.example.proyectov2.ui.pantallas.AcercaDeScreen
import com.example.proyectov2.ui.pantallas.DetalleRackScreen
import com.example.proyectov2.ui.pantallas.EditarPerfilScreen
import com.example.proyectov2.ui.pantallas.LevantamientosScreen
import com.example.proyectov2.ui.pantallas.LoginScreen
import com.example.proyectov2.ui.pantallas.PerfilScreen
import com.example.proyectov2.ui.pantallas.RegistroScreen
import com.example.proyectov2.ui.theme.AzulPetroleo
import com.example.proyectov2.viewmodel.LevantamientosViewModel
import com.example.proyectov2.viewmodel.SesionViewModel

@Composable
fun NavegacionApp(
    sesionViewModel: SesionViewModel = viewModel(),
    levantamientosViewModel: LevantamientosViewModel = viewModel()
) {
    MostrarMensajes(levantamientosViewModel)

    if (sesionViewModel.cargandoSesion) {
        PantallaDeCarga()
        return
    }

    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val rutasConBarraInferior = listOf(Rutas.LEVANTAMIENTOS, Rutas.PERFIL, Rutas.ACERCA_DE)
    val rutaInicial = remember {
        if (sesionViewModel.usuarioActual != null) Rutas.LEVANTAMIENTOS else Rutas.LOGIN
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (rutaActual in rutasConBarraInferior) {
                BarraInferior(
                    rutaActual = rutaActual,
                    onNavegar = { ruta -> navegarAPestana(navController, ruta) }
                )
            }
        }
    ) { espacioInterior ->
        NavHost(
            navController = navController,
            startDestination = rutaInicial,
            modifier = Modifier.padding(espacioInterior)
        ) {
            composable(Rutas.LOGIN) {
                LoginScreen(
                    viewModel = sesionViewModel,
                    onIngresoExitoso = { irAInicioLimpiandoHistorial(navController) },
                    onIrARegistro = { navController.navigate(Rutas.REGISTRO) }
                )
            }
            composable(Rutas.REGISTRO) {
                RegistroScreen(
                    viewModel = sesionViewModel,
                    onRegistroExitoso = { irAInicioLimpiandoHistorial(navController) },
                    onIrALogin = { navController.popBackStack() }
                )
            }
            composable(Rutas.LEVANTAMIENTOS) {
                LevantamientosScreen(
                    sesionViewModel = sesionViewModel,
                    viewModel = levantamientosViewModel,
                    onAbrirDetalle = { levantamientoId ->
                        navController.navigate(Rutas.detalle(levantamientoId))
                    }
                )
            }
            composable(
                route = Rutas.DETALLE,
                arguments = listOf(navArgument("levantamientoId") { type = NavType.IntType })
            ) { entrada ->
                val levantamientoId = entrada.arguments?.getInt("levantamientoId") ?: 0
                DetalleRackScreen(
                    sesionViewModel = sesionViewModel,
                    viewModel = levantamientosViewModel,
                    levantamientoId = levantamientoId,
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(Rutas.PERFIL) {
                PerfilScreen(
                    sesionViewModel = sesionViewModel,
                    levantamientosViewModel = levantamientosViewModel,
                    onEditarPerfil = { navController.navigate(Rutas.EDITAR_PERFIL) },
                    onSesionCerrada = {
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                )
            }
            composable(Rutas.EDITAR_PERFIL) {
                EditarPerfilScreen(
                    viewModel = sesionViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(Rutas.ACERCA_DE) {
                AcercaDeScreen()
            }
        }
    }
}

private fun navegarAPestana(navController: NavHostController, ruta: String) {
    navController.navigate(ruta) {
        popUpTo(Rutas.LEVANTAMIENTOS) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun irAInicioLimpiandoHistorial(navController: NavHostController) {
    navController.navigate(Rutas.LEVANTAMIENTOS) {
        popUpTo(navController.graph.id) { inclusive = true }
    }
}

/** Muestra como Toast los mensajes del ViewModel (errores de red, confirmaciones) una sola vez. */
@Composable
private fun MostrarMensajes(levantamientosViewModel: LevantamientosViewModel) {
    val context = LocalContext.current
    val mensaje = levantamientosViewModel.mensaje
    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
            levantamientosViewModel.mensajeMostrado()
        }
    }
}

@Composable
private fun PantallaDeCarga() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(AzulPetroleo)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_rack),
            contentDescription = "Logo Rack - Pro",
            modifier = Modifier.size(96.dp)
        )
    }
}
