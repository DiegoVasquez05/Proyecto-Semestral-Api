package com.example.proyectov2

import android.app.Application
import com.example.proyectov2.notificaciones.Notificador
import com.example.proyectov2.repository.LevantamientoRepositorio
import com.example.proyectov2.repository.UsuarioRepositorio
import com.example.proyectov2.repository.local.RackProDatabase
import com.example.proyectov2.repository.remoto.ClienteApi

/**
 * Crea una sola vez la base de datos, el cliente de la API y los repositorios,
 * para que todos los ViewModel compartan las mismas instancias.
 */
class RackProApplication : Application() {

    private val baseDeDatos by lazy { RackProDatabase.obtener(this) }
    private val api by lazy { ClienteApi.crear() }

    val usuarioRepositorio by lazy { UsuarioRepositorio(api, baseDeDatos.usuarioDao()) }
    val levantamientoRepositorio by lazy { LevantamientoRepositorio(api, baseDeDatos) }

    override fun onCreate() {
        super.onCreate()
        Notificador.crearCanal(this)
    }
}
