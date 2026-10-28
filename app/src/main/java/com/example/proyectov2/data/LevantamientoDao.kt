package com.example.proyectov2.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LevantamientoDao {

    @Query("SELECT * FROM levantamientos ORDER BY id DESC")
    fun observarTodos(): Flow<List<Levantamiento>>

    @Query("SELECT * FROM levantamientos WHERE id = :levantamientoId")
    fun observarPorId(levantamientoId: Int): Flow<Levantamiento?>

    @Query("SELECT COUNT(*) FROM levantamientos")
    suspend fun contar(): Int

    @Insert
    suspend fun insertar(levantamiento: Levantamiento): Long

    @Update
    suspend fun actualizar(levantamiento: Levantamiento)

    @Query("UPDATE levantamientos SET fotoUri = :fotoUri WHERE id = :levantamientoId")
    suspend fun actualizarFoto(levantamientoId: Int, fotoUri: String)

    @Query("SELECT * FROM componentes WHERE levantamientoId = :levantamientoId ORDER BY id")
    fun observarComponentes(levantamientoId: Int): Flow<List<Componente>>

    @Insert
    suspend fun insertarComponente(componente: Componente)

    @Update
    suspend fun actualizarComponente(componente: Componente)
}
