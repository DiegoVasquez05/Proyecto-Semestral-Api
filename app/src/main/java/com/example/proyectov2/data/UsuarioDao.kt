package com.example.proyectov2.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UsuarioDao {

    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Update
    suspend fun actualizar(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE sesionActiva = 1 LIMIT 1")
    suspend fun buscarSesionActiva(): Usuario?

    @Query("UPDATE usuarios SET sesionActiva = 0")
    suspend fun cerrarTodasLasSesiones()
}
