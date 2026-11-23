package com.example.proyectov2.notificaciones

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.proyectov2.MainActivity
import com.example.proyectov2.R

/** Recurso nativo: notificaciones del sistema para avisar los cambios de los levantamientos. */
object Notificador {

    private const val ID_CANAL = "levantamientos"

    /** El canal es obligatorio desde Android 8 y se crea una sola vez al iniciar la app. */
    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val canal = NotificationChannel(
            ID_CANAL,
            "Levantamientos",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos cuando se crea o se aprueba un levantamiento"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    fun levantamientoCreado(context: Context, levantamientoId: Int, codigoRack: String) {
        mostrar(
            context = context,
            idNotificacion = levantamientoId,
            titulo = "Inspección registrada",
            mensaje = "El Rack $codigoRack quedó pendiente de revisión del supervisor."
        )
    }

    fun levantamientoAprobado(context: Context, levantamientoId: Int, codigoRack: String) {
        mostrar(
            context = context,
            idNotificacion = levantamientoId,
            titulo = "Levantamiento aprobado",
            mensaje = "El Rack $codigoRack fue aprobado y quedó bloqueado para edición."
        )
    }

    private fun mostrar(context: Context, idNotificacion: Int, titulo: String, mensaje: String) {
        if (!tienePermiso(context)) return

        // Al tocar la notificación se abre la app.
        val abrirApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificacion = NotificationCompat.Builder(context, ID_CANAL)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setColor(ContextCompat.getColor(context, R.color.azul_petroleo))
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(idNotificacion, notificacion)
        } catch (error: SecurityException) {
            // El usuario quitó el permiso justo antes de mostrarla: no se hace nada.
        }
    }

    private fun tienePermiso(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}

/** Pide el permiso de notificaciones (Android 13 o superior) la primera vez que se muestra la pantalla. */
@Composable
fun SolicitarPermisoNotificaciones() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val solicitarPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        val permisoConcedido = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!permisoConcedido) {
            solicitarPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
