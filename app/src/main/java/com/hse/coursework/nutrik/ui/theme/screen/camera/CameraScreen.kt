package com.hse.coursework.nutrik.ui.theme.screen.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.navigation.Screen
import com.hse.coursework.nutrik.ui.theme.components.BottomNavigationBar
import com.hse.coursework.nutrik.ui.theme.components.camera.CameraCaptureView
import com.hse.coursework.nutrik.ui.theme.components.camera.CapturedPhotoView


@Composable
fun CameraScreen(
    navController: NavController,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state = viewModel.uiState
    var cameraPermissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> cameraPermissionGranted = granted }
    )
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                bitmap?.let { viewModel.onImageCaptured(it) }
            }
        }
    )

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            cameraPermissionGranted = true
        }
    }

    if (!cameraPermissionGranted) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Для сканирования продукта необходимо разрешение камеры")
        }
        return
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val previewUseCase = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                previewUseCase,
                imageCapture
            )
        } catch (exc: Exception) {
            Log.e("CameraScreen", "Ошибка привязки камеры", exc)
        }
    }

    if (!state.hasPhoto) {
        CameraCaptureView(
            onImageCaptured = viewModel::onImageCaptured
        )
    } else {
        CapturedPhotoView(
            navController = navController,
            photoBitmap = state.photoBitmap,
            foundProduct = state.foundProduct,
            scanning = state.scanning,
            message = state.message,
            onNavigateToProduct = { productId ->
                navController.navigate("product_detail_screen/$productId") {
                    popUpTo(Screen.Camera.route) { inclusive = true }
                }
            },
            onRetake = viewModel::onRetake
        )
    }
}


fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
    val rotationDegrees = image.imageInfo.rotationDegrees
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
    return Bitmap.createBitmap(
        originalBitmap,
        0, 0,
        originalBitmap.width,
        originalBitmap.height,
        matrix,
        true
    )
}
