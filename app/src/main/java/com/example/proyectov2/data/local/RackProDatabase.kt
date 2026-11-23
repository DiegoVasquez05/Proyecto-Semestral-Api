package com.example.proyectov2.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos local (caché). La versión 2 reemplaza el modo 100 % local de la versión
 * anterior: ahora los datos vienen de la API, por eso se recrea la base en vez de migrarla.
 */
@Database(
    entities = [Usuario::class, Levantamiento::class, Componente::class],
    version = 2,
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
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                instancia = nuevaInstancia
                nuevaInstancia
            }
        }
    }
}
