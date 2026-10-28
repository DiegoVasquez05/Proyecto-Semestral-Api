package com.example.proyectov2

import com.example.proyectov2.utilidades.Validaciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionesTest {

    @Test
    fun correoValidoNoEntregaError() {
        assertNull(Validaciones.validarCorreo("usuario@cmc360.cl"))
    }

    @Test
    fun correoSinArrobaEntregaError() {
        assertNotNull(Validaciones.validarCorreo("usuariocmc360.cl"))
    }

    @Test
    fun contrasenaCortaEntregaError() {
        assertNotNull(Validaciones.validarContrasenaNueva("123"))
    }

    @Test
    fun contrasenasDistintasEntreganError() {
        assertEquals(
            "Las contraseñas no coinciden",
            Validaciones.validarConfirmacion("secreto1", "secreto2")
        )
    }

    @Test
    fun codigoRackConFormatoCorrecto() {
        assertNull(Validaciones.validarCodigoRack("A-01"))
        assertNotNull(Validaciones.validarCodigoRack("A01"))
    }

    @Test
    fun fechaInexistenteEntregaError() {
        assertNull(Validaciones.validarFecha("14/10/2026"))
        assertNotNull(Validaciones.validarFecha("31/02/2026"))
    }

    @Test
    fun telefonoChilenoEsValido() {
        assertNull(Validaciones.validarTelefono("+56 9 1234 5678"))
        assertNotNull(Validaciones.validarTelefono("abc"))
    }
}
