package com.hse.coursework.nutrik.ui.theme.components.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.hse.coursework.nutrik.ui.theme.screen.camera.imageProxyToBitmap


@Composable
fun CameraCaptureView(
    onImageCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                bitmap?.let { onImageCaptured(it) }
            }
        }
    )

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

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f)
                    .border(2.dp, Color.White, RoundedCornerShape(8.dp))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            IconButton(
                onClick = {
                    imageCapture.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                val bitmap = imageProxyToBitmap(imageProxy)
                                imageProxy.close()
                                bitmap?.let { onImageCaptured(it) }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("CameraScreen", "Capture failed: ${exception.message}")
                            }
                        }
                    )
                },
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White, shape = CircleShape)
                )
            }

            IconButton(
                onClick = { pickImageLauncher.launch("image/*") },
                modifier = Modifier
                    .size(82.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp)
            ) {
                Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Выбрать из галереи",
                        tint = Color.White,
                        modifier = Modifier.size(96.dp)
                    )
                }
            }
        }
    }
}
