package com.example.proyectov2.navegacion

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.proyectov2.RackProViewModel
import com.example.proyectov2.ui.componentes.BarraInferior
import com.example.proyectov2.ui.pantallas.AcercaDeScreen
import com.example.proyectov2.ui.pantallas.DetalleRackScreen
import com.example.proyectov2.ui.pantallas.EditarPerfilScreen
import com.example.proyectov2.ui.pantallas.LevantamientosScreen
import com.example.proyectov2.ui.pantallas.LoginScreen
import com.example.proyectov2.ui.pantallas.PerfilScreen
import com.example.proyectov2.ui.pantallas.RegistroScreen
import com.example.proyectov2.ui.theme.AzulPetroleo

@Composable
fun NavegacionApp(viewModel: RackProViewModel = viewModel()) {
    if (viewModel.cargandoSesion) {
        PantallaDeCarga()
        return
    }

    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val rutasConBarraInferior = listOf(Rutas.LEVANTAMIENTOS, Rutas.PERFIL, Rutas.ACERCA_DE)
    val rutaInicial = remember {
        if (viewModel.usuarioActual != null) Rutas.LEVANTAMIENTOS else Rutas.LOGIN
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
                    viewModel = viewModel,
                    onIngresoExitoso = { irAInicioLimpiandoHistorial(navController) },
                    onIrARegistro = { navController.navigate(Rutas.REGISTRO) }
                )
            }
            composable(Rutas.REGISTRO) {
                RegistroScreen(
                    viewModel = viewModel,
                    onRegistroExitoso = { irAInicioLimpiandoHistorial(navController) },
                    onIrALogin = { navController.popBackStack() }
                )
            }
            composable(Rutas.LEVANTAMIENTOS) {
                LevantamientosScreen(
                    viewModel = viewModel,
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
                    viewModel = viewModel,
                    levantamientoId = levantamientoId,
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(Rutas.PERFIL) {
                PerfilScreen(
                    viewModel = viewModel,
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
                    viewModel = viewModel,
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
