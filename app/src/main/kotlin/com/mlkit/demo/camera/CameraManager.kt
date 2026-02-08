package com.mlkit.demo.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CameraManager(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null

    suspend fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val provider = getCameraProvider()

        // Unbind all use cases before rebinding
        provider.unbindAll()

        // Build preview use case
        val preview = Preview.Builder()
            .build()
            .also {
                it.surfaceProvider = previewView.surfaceProvider
            }

        // Select back camera
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            // Bind use cases to camera
            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview
            )
        } catch (e: Exception) {
            android.util.Log.e("CameraManager", "Camera binding failed", e)
        }
    }

    private suspend fun getCameraProvider(): ProcessCameraProvider {
        return cameraProvider ?: suspendCoroutine { continuation ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val provider = cameraProviderFuture.get()
                cameraProvider = provider
                continuation.resume(provider)
            }, ContextCompat.getMainExecutor(context))
        }
    }
}
