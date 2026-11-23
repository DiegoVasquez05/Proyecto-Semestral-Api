package com.example.proyectov2.repository

import androidx.room.withTransaction
import com.example.proyectov2.model.AprobacionRequest
import com.example.proyectov2.model.Componente
import com.example.proyectov2.model.EdicionLevantamientoRequest
import com.example.proyectov2.model.EstadoComponenteRequest
import com.example.proyectov2.model.Levantamiento
import com.example.proyectov2.model.LevantamientoDto
import com.example.proyectov2.model.NuevoComponenteRequest
import com.example.proyectov2.model.NuevoLevantamientoRequest
import com.example.proyectov2.repository.local.RackProDatabase
import com.example.proyectov2.repository.remoto.RackProApi
import com.example.proyectov2.repository.remoto.aEntidad
import com.example.proyectov2.repository.remoto.llamarApi
import kotlinx.coroutines.flow.Flow

/**
 * Coordina la API (fuente de verdad) y Room (caché local).
 * Las pantallas observan Room; cada cambio se envía primero a la API y, si responde bien,
 * se guarda la respuesta en Room para que la interfaz se actualice sola.
 */
class LevantamientoRepositorio(
    private val api: RackProApi,
    private val baseDeDatos: RackProDatabase
) {
    private val levantamientoDao = baseDeDatos.levantamientoDao()

    val levantamientos: Flow<List<Levantamiento>> = levantamientoDao.observarTodos()

    fun observarLevantamiento(levantamientoId: Int): Flow<Levantamiento?> =
        levantamientoDao.observarPorId(levantamientoId)

    fun observarComponentes(levantamientoId: Int): Flow<List<Componente>> =
        levantamientoDao.observarComponentes(levantamientoId)

    /** Descarga todos los levantamientos y reemplaza la caché, conservando las fotos locales. */
    suspend fun sincronizar(): Result<Unit> {
        return llamarApi {
            val levantamientosDeLaApi = api.obtenerLevantamientos()
            baseDeDatos.withTransaction {
                val fotosPorLevantamiento = levantamientoDao.obtenerTodos()
                    .associate { levantamiento -> levantamiento.id to levantamiento.fotoUri }
                levantamientoDao.eliminarTodos()
                levantamientosDeLaApi.forEach { levantamientoDto ->
                    guardarEnCache(levantamientoDto, fotosPorLevantamiento[levantamientoDto.id])
                }
            }
        }
    }

    /** Vuelve a descargar un solo levantamiento (al abrir su detalle). */
    suspend fun actualizarLevantamiento(levantamientoId: Int): Result<Unit> {
        return llamarApi { guardarRespuesta(api.obtenerLevantamiento(levantamientoId)) }
    }

    suspend fun crearLevantamiento(datos: NuevoLevantamientoRequest): Result<Levantamiento> {
        return llamarApi { guardarRespuesta(api.crearLevantamiento(datos)) }
    }

    suspend fun editarLevantamiento(levantamientoId: Int, codigoRack: String, fecha: String): Result<Levantamiento> {
        return llamarApi {
            val datosEditados = EdicionLevantamientoRequest(codigoRack.trim().uppercase(), fecha.trim())
            guardarRespuesta(api.editarLevantamiento(levantamientoId, datosEditados))
        }
    }

    suspend fun aprobarLevantamiento(levantamientoId: Int, supervisorNombre: String): Result<Levantamiento> {
        return llamarApi {
            guardarRespuesta(api.aprobarLevantamiento(levantamientoId, AprobacionRequest(supervisorNombre)))
        }
    }

    suspend fun eliminarLevantamiento(levantamientoId: Int): Result<Unit> {
        return llamarApi {
            api.eliminarLevantamiento(levantamientoId)
            levantamientoDao.eliminar(levantamientoId)
        }
    }

    suspend fun agregarComponente(levantamientoId: Int, nombreComponente: String): Result<Unit> {
        return llamarApi {
            val componenteNuevo = api.agregarComponente(levantamientoId, NuevoComponenteRequest(nombreComponente))
            levantamientoDao.guardarComponentes(listOf(componenteNuevo.aEntidad()))
        }
    }

    suspend fun cambiarEstadoComponente(componente: Componente, estadoNuevo: String): Result<Unit> {
        return llamarApi {
            val componenteActualizado = api.cambiarEstadoComponente(componente.id, EstadoComponenteRequest(estadoNuevo))
            levantamientoDao.guardarComponentes(listOf(componenteActualizado.aEntidad()))
        }
    }

    suspend fun obtenerCatalogo(): Result<List<String>> {
        return llamarApi { api.obtenerCatalogo().map { componente -> componente.nombre } }
    }

    /** La foto de evidencia se guarda solo en el teléfono (no se envía a la API). */
    suspend fun guardarFoto(levantamientoId: Int, fotoUri: String) {
        levantamientoDao.actualizarFoto(levantamientoId, fotoUri)
    }

    private suspend fun guardarRespuesta(levantamientoDto: LevantamientoDto): Levantamiento {
        return baseDeDatos.withTransaction {
            guardarEnCache(levantamientoDto, levantamientoDao.obtenerFoto(levantamientoDto.id))
        }
    }

    private suspend fun guardarEnCache(levantamientoDto: LevantamientoDto, fotoUri: String?): Levantamiento {
        val levantamiento = levantamientoDto.aEntidad(fotoUri)
        levantamientoDao.guardar(levantamiento)
        levantamientoDao.eliminarComponentes(levantamiento.id)
        levantamientoDao.guardarComponentes(levantamientoDto.componentes.orEmpty().map { it.aEntidad() })
        return levantamiento
    }
}
