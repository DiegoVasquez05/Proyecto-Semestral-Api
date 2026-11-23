package com.example.proyectov2

import com.example.proyectov2.data.remoto.MENSAJE_ERROR_DESCONOCIDO
import com.example.proyectov2.data.remoto.mensajeDeErrorHttp
import org.junit.Assert.assertEquals
import org.junit.Test

class ManejoErroresTest {

    @Test
    fun seMuestraElMensajeQueEnviaLaApi() {
        val mensaje = mensajeDeErrorHttp(409, """{"mensaje":"Ya existe una cuenta con este correo"}""")
        assertEquals("Ya existe una cuenta con este correo", mensaje)
    }

    @Test
    fun errorDelServidorSinMensajeUsaTextoGenerico() {
        val mensaje = mensajeDeErrorHttp(500, "<html>Error</html>")
        assertEquals("El servidor tuvo un problema. Inténtalo en unos minutos.", mensaje)
    }

    @Test
    fun errorSinCuerpoUsaMensajeDesconocido() {
        assertEquals(MENSAJE_ERROR_DESCONOCIDO, mensajeDeErrorHttp(400, null))
    }
}
