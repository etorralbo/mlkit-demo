package com.mlkit.demo.camera

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.mlkit.demo.mlkit.ObjectDetectionAnalyzer
import kotlinx.coroutines.runBlocking

internal class CameraAnalyzer(
    private val objectDetectionAnalyzer: ObjectDetectionAnalyzer
) : ImageAnalysis.Analyzer {
    override fun analyze(imageProxy: ImageProxy) {
        runBlocking {
            try {
                val result = objectDetectionAnalyzer.analyze(imageProxy)
                result?.let {
                    Log.d("MLKitDemo", "Detected: ${it.label} (${(it.confidence * 100).toInt()}%)")
                }
            } catch (e: Exception) {
                Log.e("MLKitDemo", "Detection error", e)
            } finally {
                imageProxy.close()
            }
        }
    }
}
