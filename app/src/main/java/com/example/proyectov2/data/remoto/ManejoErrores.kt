package com.example.proyectov2.data.remoto

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.HttpException
import java.io.IOException

const val MENSAJE_SIN_CONEXION = "No se pudo conectar con el servidor. Revisa tu conexión a internet."
const val MENSAJE_ERROR_DESCONOCIDO = "Ocurrió un error inesperado. Inténtalo nuevamente."

/**
 * Ejecuta una llamada a la API y la envuelve en un [Result].
 * Si falla, el error contiene un mensaje en español listo para mostrar en pantalla.
 */
suspend fun <T> llamarApi(llamada: suspend () -> T): Result<T> {
    return try {
        Result.success(llamada())
    } catch (error: HttpException) {
        val cuerpoDelError = error.response()?.errorBody()?.string()
        Result.failure(Exception(mensajeDeErrorHttp(error.code(), cuerpoDelError)))
    } catch (error: IOException) {
        Result.failure(Exception(MENSAJE_SIN_CONEXION))
    }
}

/** Obtiene el mensaje que envía la API en el cuerpo {"mensaje": "..."} o uno genérico según el código HTTP. */
fun mensajeDeErrorHttp(codigoHttp: Int, cuerpoDelError: String?): String {
    val mensajeDeLaApi = try {
        Gson().fromJson(cuerpoDelError, ErrorDto::class.java)?.mensaje
    } catch (error: JsonSyntaxException) {
        null
    }
    return when {
        !mensajeDeLaApi.isNullOrBlank() -> mensajeDeLaApi
        codigoHttp >= 500 -> "El servidor tuvo un problema. Inténtalo en unos minutos."
        else -> MENSAJE_ERROR_DESCONOCIDO
    }
}
