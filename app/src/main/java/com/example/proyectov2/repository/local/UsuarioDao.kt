package com.example.proyectov2.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.proyectov2.model.Usuario

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(usuario: Usuario)

    @Query("SELECT * FROM usuarios LIMIT 1")
    suspend fun obtenerSesion(): Usuario?

    @Query("DELETE FROM usuarios")
    suspend fun borrarSesion()
}
