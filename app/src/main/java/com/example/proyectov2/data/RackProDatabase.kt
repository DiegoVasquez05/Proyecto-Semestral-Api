package com.example.proyectov2.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Usuario::class, Levantamiento::class, Componente::class],
    version = 1,
    exportSchema = false
)
abstract class RackProDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao

    abstract fun levantamientoDao(): LevantamientoDao

    companion object {
        @Volatile
        private var instancia: RackProDatabase? = null

        fun obtener(context: Context): RackProDatabase {
            return instancia ?: synchronized(this) {
                val nuevaInstancia = Room.databaseBuilder(
                    context.applicationContext,
                    RackProDatabase::class.java,
                    "rack_pro.db"
                ).build()
                instancia = nuevaInstancia
                nuevaInstancia
            }
        }
    }
}
