package com.example.proyectov2.data.remoto

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Endpoints de la API REST de Rack - Pro (ver carpeta api/ del repositorio). */
interface RackProApi {

    @POST("usuarios/registro")
    suspend fun registrarUsuario(@Body datos: RegistroRequest): UsuarioDto

    @POST("usuarios/login")
    suspend fun iniciarSesion(@Body datos: LoginRequest): UsuarioDto

    @PUT("usuarios/{id}")
    suspend fun actualizarPerfil(@Path("id") usuarioId: Int, @Body datos: PerfilRequest): UsuarioDto

    @GET("levantamientos")
    suspend fun obtenerLevantamientos(): List<LevantamientoDto>

    @GET("levantamientos/{id}")
    suspend fun obtenerLevantamiento(@Path("id") levantamientoId: Int): LevantamientoDto

    @POST("levantamientos")
    suspend fun crearLevantamiento(@Body datos: NuevoLevantamientoRequest): LevantamientoDto

    @PUT("levantamientos/{id}")
    suspend fun editarLevantamiento(
        @Path("id") levantamientoId: Int,
        @Body datos: EdicionLevantamientoRequest
    ): LevantamientoDto

    @PATCH("levantamientos/{id}/aprobacion")
    suspend fun aprobarLevantamiento(
        @Path("id") levantamientoId: Int,
        @Body datos: AprobacionRequest
    ): LevantamientoDto

    @DELETE("levantamientos/{id}")
    suspend fun eliminarLevantamiento(@Path("id") levantamientoId: Int)

    @POST("levantamientos/{id}/componentes")
    suspend fun agregarComponente(
        @Path("id") levantamientoId: Int,
        @Body datos: NuevoComponenteRequest
    ): ComponenteDto

    @PATCH("componentes/{id}")
    suspend fun cambiarEstadoComponente(
        @Path("id") componenteId: Int,
        @Body datos: EstadoComponenteRequest
    ): ComponenteDto

    @GET("catalogo/componentes")
    suspend fun obtenerCatalogo(): List<ComponenteCatalogoDto>
}
