package com.example.proyectov2.data

object Roles {
    const val INSPECTOR = "Inspector"
    const val SUPERVISOR = "Supervisor"
}

object Estados {
    const val PENDIENTE = "Pendiente"
    const val APROBADO = "Aprobado"
    const val VALIDADO = "Validado"
    const val OBSERVADO = "Observado"

    /** Estado al que pasa un componente cuando el supervisor lo toca: Pendiente → Validado → Observado → Pendiente. */
    fun siguienteEstadoComponente(estadoActual: String): String {
        return when (estadoActual) {
            PENDIENTE -> VALIDADO
            VALIDADO -> OBSERVADO
            else -> PENDIENTE
        }
    }
}
