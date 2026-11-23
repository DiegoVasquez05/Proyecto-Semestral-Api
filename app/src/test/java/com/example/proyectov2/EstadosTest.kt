package com.example.proyectov2

import com.example.proyectov2.data.Estados
import org.junit.Assert.assertEquals
import org.junit.Test

class EstadosTest {

    @Test
    fun componentePendientePasaAValidado() {
        assertEquals(Estados.VALIDADO, Estados.siguienteEstadoComponente(Estados.PENDIENTE))
    }

    @Test
    fun componenteValidadoPasaAObservado() {
        assertEquals(Estados.OBSERVADO, Estados.siguienteEstadoComponente(Estados.VALIDADO))
    }

    @Test
    fun componenteObservadoVuelveAPendiente() {
        assertEquals(Estados.PENDIENTE, Estados.siguienteEstadoComponente(Estados.OBSERVADO))
    }
}
