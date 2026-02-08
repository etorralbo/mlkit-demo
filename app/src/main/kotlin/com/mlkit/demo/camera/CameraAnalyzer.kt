package com.mlkit.demo.camera

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.mlkit.demo.mlkit.ObjectDetectionAnalyzer
import com.mlkit.demo.utils.BoundingBoxTransformer
import com.mlkit.demo.viewmodel.DemoViewModel
import kotlinx.coroutines.runBlocking

internal class CameraAnalyzer(
    private val objectDetectionAnalyzer: ObjectDetectionAnalyzer,
    private val viewModel: DemoViewModel,
    private val viewWidth: Int,
    private val viewHeight: Int
) : ImageAnalysis.Analyzer {
    override fun analyze(imageProxy: ImageProxy) {
        runBlocking {
            try {
                val result = objectDetectionAnalyzer.analyze(imageProxy)
                if (result != null) {
                    Log.d("MLKitDemo", "Detected: ${result.label} (${(result.confidence * 100).toInt()}%)")

                    // Transform bounding box from image space to view space
                    val transformedBox = BoundingBoxTransformer.transform(
                        imageBoundingBox = result.boundingBox,
                        imageWidth = imageProxy.width,
                        imageHeight = imageProxy.height,
                        viewWidth = viewWidth,
                        viewHeight = viewHeight
                    )

                    // Emit to ViewModel
                    viewModel.onObjectDetected(
                        label = result.label,
                        confidence = result.confidence,
                        boundingBox = transformedBox
                    )
                } else {
                    viewModel.clearDetection()
                }
            } catch (e: Exception) {
                Log.e("MLKitDemo", "Detection error", e)
                viewModel.clearDetection()
            } finally {
                imageProxy.close()
            }
        }
    }
}
