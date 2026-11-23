package com.example.proyectov2.data.remoto

import com.example.proyectov2.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ClienteApi {

    // El plan gratuito de Render "duerme" la API tras 15 minutos sin uso y tarda
    // hasta un minuto en despertar, por eso los tiempos de espera son amplios.
    private const val SEGUNDOS_DE_ESPERA = 60L

    fun crear(): RackProApi {
        val registroDePeticiones = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val clienteHttp = OkHttpClient.Builder()
            .connectTimeout(SEGUNDOS_DE_ESPERA, TimeUnit.SECONDS)
            .readTimeout(SEGUNDOS_DE_ESPERA, TimeUnit.SECONDS)
            .addInterceptor(registroDePeticiones)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RackProApi::class.java)
    }
}
