package com.example.proyectov2.camara

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.proyectov2.ui.componentes.AvatarUsuario
import com.example.proyectov2.ui.theme.VerdeAzulado
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CamaraFotos(nombreUsuario: String, fotoUri: String?, onFotoTomada: (Uri) -> Unit) {
    val tomarFoto = rememberLanzadorCamara(onFotoTomada)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AvatarUsuario(
            nombre = nombreUsuario,
            fotoUri = fotoUri,
            tamano = 96.dp,
            modifier = Modifier.clickable { tomarFoto() }
        )
        Spacer(modifier = Modifier.height(4.dp))
        TextButton(onClick = tomarFoto) {
            Text("Tomar foto de perfil", color = VerdeAzulado, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun rememberLanzadorCamara(onFotoTomada: (Uri) -> Unit): () -> Unit {
    val context = LocalContext.current
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uriDeLaFoto = imageUri
        if (success && uriDeLaFoto != null) {
            onFotoTomada(uriDeLaFoto)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = crearUriParaFoto(context, "foto_camara")
            imageUri = uri
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Debes permitir el uso de la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    return { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
}

@Composable
fun rememberLanzadorGaleria(onFotoElegida: (Uri) -> Unit): () -> Unit {
    val context = LocalContext.current

    val galeriaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uriElegida ->
        if (uriElegida != null) {
            val archivoCopia = File.createTempFile("foto_galeria", ".jpg", context.filesDir)
            context.contentResolver.openInputStream(uriElegida)?.use { entrada ->
                archivoCopia.outputStream().use { salida -> entrada.copyTo(salida) }
            }
            onFotoElegida(uriDeArchivo(context, archivoCopia))
        }
    }

    return { galeriaLauncher.launch("image/*") }
}

@Composable
fun FotoGuardada(fotoUri: String, descripcion: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, fotoUri) {
        value = withContext(Dispatchers.IO) { cargarFoto(context, Uri.parse(fotoUri)) }
    }

    bitmap?.let { foto ->
        Image(
            bitmap = foto.asImageBitmap(),
            contentDescription = descripcion,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}

private fun crearUriParaFoto(context: Context, prefijo: String): Uri {
    val file = File.createTempFile(prefijo, ".jpg", context.filesDir)
    return uriDeArchivo(context, file)
}

private fun uriDeArchivo(context: Context, file: File): Uri {
    return FileProvider.getUriForFile(
        context,
        context.packageName + ".provider",
        file
    )
}

@Suppress("DEPRECATION")
private fun cargarFoto(context: Context, uri: Uri): Bitmap? {
    return try {
        val bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        val gradosDeGiro = context.contentResolver.openInputStream(uri)?.use { entrada ->
            when (ExifInterface(entrada).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
        val escala = minOf(1f, 1280f / maxOf(bmp.width, bmp.height))
        val ajustes = Matrix()
        ajustes.postScale(escala, escala)
        ajustes.postRotate(gradosDeGiro)
        Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, ajustes, true)
    } catch (error: Exception) {
        null
    }
}
