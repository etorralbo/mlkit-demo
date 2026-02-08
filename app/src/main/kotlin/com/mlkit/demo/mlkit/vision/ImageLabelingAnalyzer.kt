package com.mlkit.demo.mlkit.vision

import android.graphics.Bitmap
import android.util.Log
import com.mlkit.demo.mlkit.model.LabelResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import kotlinx.coroutines.tasks.await

private const val CONFIDENCE_THRESHOLD = 0.70f

internal class ImageLabelingAnalyzer(
    private val labeler: ImageLabeler,
) {

    suspend fun analyzeImage(croppedBitmap: Bitmap, rotationDegrees: Int): LabelResult? {
        return try {
            val inputImage = InputImage.fromBitmap(croppedBitmap, rotationDegrees)
            val labels = labeler.process(inputImage).await()

            val bestLabel = labels
                .filter { it.confidence >= CONFIDENCE_THRESHOLD }
                .maxByOrNull { it.confidence }

            if (bestLabel != null) {
                LabelResult(
                    label = bestLabel.text,
                    confidence = bestLabel.confidence,
                )
            } else {
                null
            }
        } catch (exception: Exception) {
            Log.e("MLKitDemo", "Image labeling failed", exception)
            throw exception
        }
    }
}
