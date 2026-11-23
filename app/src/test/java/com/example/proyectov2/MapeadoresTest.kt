package com.example.proyectov2

import com.example.proyectov2.data.remoto.ComponenteDto
import com.example.proyectov2.data.remoto.LevantamientoDto
import com.example.proyectov2.data.remoto.UsuarioDto
import com.example.proyectov2.data.remoto.aEntidad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MapeadoresTest {

    private val levantamientoDeLaApi = LevantamientoDto(
        id = 7,
        codigoRack = "A-01",
        dataCenter = "DC Principal",
        sala = "Sala 2",
        fecha = "14/10/2026",
        estado = "Pendiente",
        inspectorId = 1,
        inspectorNombre = "Inspector Demo",
        creadoEn = 1_760_000_000_000,
        actualizadoEn = null,
        aprobadoPor = null,
        aprobadoEn = null,
        componentes = emptyList()
    )

    @Test
    fun levantamientoConservaLaFotoLocal() {
        val entidad = levantamientoDeLaApi.aEntidad(fotoUri = "content://foto/1")

        assertEquals(7, entidad.id)
        assertEquals("A-01", entidad.codigoRack)
        assertEquals("content://foto/1", entidad.fotoUri)
        assertNull(entidad.aprobadoEn)
    }

    @Test
    fun usuarioSinCargoNiTelefonoQuedaConTextoVacio() {
        val usuarioDeLaApi = UsuarioDto(
            id = 3,
            nombreCompleto = "Ana Pérez",
            correo = "ana@cmc360.cl",
            rol = "Inspector",
            cargo = null,
            telefono = null
        )

        val entidad = usuarioDeLaApi.aEntidad(fotoUri = null)

        assertEquals("", entidad.cargo)
        assertEquals("", entidad.telefono)
    }

    @Test
    fun componenteMantieneSuLevantamiento() {
        val entidad = ComponenteDto(id = 4, levantamientoId = 7, nombre = "PDU 32A", estado = "Validado").aEntidad()

        assertEquals(7, entidad.levantamientoId)
        assertEquals("Validado", entidad.estado)
    }
}
