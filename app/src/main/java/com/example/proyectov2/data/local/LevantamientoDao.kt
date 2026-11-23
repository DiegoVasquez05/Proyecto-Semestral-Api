package com.example.proyectov2.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LevantamientoDao {

    @Query("SELECT * FROM levantamientos ORDER BY id DESC")
    fun observarTodos(): Flow<List<Levantamiento>>

    @Query("SELECT * FROM levantamientos WHERE id = :levantamientoId")
    fun observarPorId(levantamientoId: Int): Flow<Levantamiento?>

    @Query("SELECT * FROM levantamientos")
    suspend fun obtenerTodos(): List<Levantamiento>

    @Query("SELECT fotoUri FROM levantamientos WHERE id = :levantamientoId")
    suspend fun obtenerFoto(levantamientoId: Int): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(levantamiento: Levantamiento)

    @Query("DELETE FROM levantamientos WHERE id = :levantamientoId")
    suspend fun eliminar(levantamientoId: Int)

    @Query("DELETE FROM levantamientos")
    suspend fun eliminarTodos()

    @Query("UPDATE levantamientos SET fotoUri = :fotoUri WHERE id = :levantamientoId")
    suspend fun actualizarFoto(levantamientoId: Int, fotoUri: String)

    @Query("SELECT * FROM componentes WHERE levantamientoId = :levantamientoId ORDER BY id")
    fun observarComponentes(levantamientoId: Int): Flow<List<Componente>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarComponentes(componentes: List<Componente>)

    @Query("DELETE FROM componentes WHERE levantamientoId = :levantamientoId")
    suspend fun eliminarComponentes(levantamientoId: Int)
}
