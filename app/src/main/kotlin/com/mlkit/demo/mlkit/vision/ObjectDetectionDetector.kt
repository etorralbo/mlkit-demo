package com.mlkit.demo.mlkit.vision

import android.graphics.Bitmap
import android.util.Log
import com.mlkit.demo.mlkit.model.DetectedObject
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetector
import kotlinx.coroutines.tasks.await

internal class ObjectDetectionDetector(
    private val detector: ObjectDetector,
) {

    suspend fun detectObjects(bitmap: Bitmap, rotationDegrees: Int): List<DetectedObject>? {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, rotationDegrees)
            val detectedObjects = detector.process(inputImage).await()

            detectedObjects.map { obj ->
                DetectedObject(
                    boundingBox = obj.boundingBox,
                    trackingId = obj.trackingId,
                )
            }
        } catch (exception: Exception) {
            Log.e("MLKitDemo", "ML Kit object detection failed", exception)
            throw exception
        }
    }
}
