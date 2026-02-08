package com.mlkit.demo.mlkit

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageProxy
import com.mlkit.demo.mlkit.model.DetectedObject
import com.mlkit.demo.mlkit.utils.toCustomBitmap
import com.mlkit.demo.mlkit.vision.ImageLabelingAnalyzer
import com.mlkit.demo.mlkit.vision.ObjectDetectionDetector
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.pow
import kotlin.math.sqrt

data class ObjectDetectionResult(
    val label: String,
    val confidence: Float,
    val boundingBox: Rect,
)

internal class ObjectDetectionAnalyzer(
    private val detector: ObjectDetectionDetector,
    private val imageLabelingAnalyzer: ImageLabelingAnalyzer,
) {

    private var lastTrackedId: Int? = null
    private var framesSinceSelection = 0

    suspend fun analyze(imageProxy: ImageProxy): ObjectDetectionResult? {
        var bitmap: Bitmap? = null
        var croppedBitmap: Bitmap? = null
        try {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            bitmap = imageProxy.toCustomBitmap() ?: return null

            val detectedObjects = detector.detectObjects(bitmap, rotationDegrees) ?: return null

            val selectedObject = selectBestObject(
                detectedObjects = detectedObjects,
                imageWidth = imageProxy.width,
                imageHeight = imageProxy.height,
            ) ?: return null

            // Crop bitmap to only include the detected object's bounding box
            val boundingBox = selectedObject.boundingBox
            croppedBitmap = Bitmap.createBitmap(
                bitmap,
                boundingBox.left.coerceAtLeast(0),
                boundingBox.top.coerceAtLeast(0),
                boundingBox.width().coerceAtMost(bitmap.width - boundingBox.left),
                boundingBox.height().coerceAtMost(bitmap.height - boundingBox.top),
            )

            val labelResult = imageLabelingAnalyzer.analyzeImage(
                croppedBitmap = croppedBitmap,
                rotationDegrees = rotationDegrees,
            ) ?: return null

            return ObjectDetectionResult(
                label = labelResult.label,
                confidence = labelResult.confidence,
                boundingBox = selectedObject.boundingBox,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("MLKitDemo", "ML Kit analysis failed", e)
            return null
        } finally {
            bitmap?.recycle()
            croppedBitmap?.recycle()
        }
    }

    private fun selectBestObject(
        detectedObjects: List<DetectedObject>,
        imageWidth: Int,
        imageHeight: Int,
    ): DetectedObject? {
        if (detectedObjects.isEmpty()) {
            lastTrackedId = null
            return null
        }

        framesSinceSelection++

        // Find currently tracked object
        val trackedObject = lastTrackedId?.let { id ->
            detectedObjects.find { it.trackingId == id }
        }

        // Determine if we should reconsider selection
        val shouldReconsiderSelection = trackedObject == null || framesSinceSelection >= SELECTION_COOLDOWN_FRAMES

        val selectedObject = when {
            !shouldReconsiderSelection -> trackedObject
            trackedObject == null -> selectInitialObject(
                detectedObjects = detectedObjects,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
            )
            else -> considerSwitchingToNewObject(
                trackedObject = trackedObject,
                detectedObjects = detectedObjects,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
            )
        }

        lastTrackedId = selectedObject.trackingId
        return selectedObject
    }

    private fun selectInitialObject(
        detectedObjects: List<DetectedObject>,
        imageWidth: Int,
        imageHeight: Int,
    ): DetectedObject {
        framesSinceSelection = 0
        return selectBestByScore(detectedObjects, imageWidth, imageHeight).obj
    }

    private fun considerSwitchingToNewObject(
        trackedObject: DetectedObject,
        detectedObjects: List<DetectedObject>,
        imageWidth: Int,
        imageHeight: Int,
    ): DetectedObject {
        val bestScored = selectBestByScore(detectedObjects, imageWidth, imageHeight)
        val currentScore = calculateScore(trackedObject, imageWidth, imageHeight)
        val shouldSwitch = bestScored.score > currentScore * SELECTION_HYSTERESIS_MULTIPLIER

        return if (shouldSwitch) {
            framesSinceSelection = 0
            bestScored.obj
        } else {
            trackedObject
        }
    }

    private fun selectBestByScore(
        detectedObjects: List<DetectedObject>,
        imageWidth: Int,
        imageHeight: Int,
    ): ScoredObject {
        return detectedObjects
            .asSequence()
            .map { ScoredObject(it, calculateScore(it, imageWidth, imageHeight)) }
            .maxBy { it.score }
    }

    private fun calculateScore(
        obj: DetectedObject,
        imageWidth: Int,
        imageHeight: Int,
    ): Float {
        val rect = obj.boundingBox
        val centerX = rect.exactCenterX()
        val centerY = rect.exactCenterY()

        val imageCenterX = imageWidth / 2f
        val imageCenterY = imageHeight / 2f

        // Normalized distance to center [0, ~0.707]
        val distanceToCenter = sqrt(
            ((centerX - imageCenterX) / imageWidth).pow(2) +
                ((centerY - imageCenterY) / imageHeight).pow(2),
        )

        // Normalized area [0, 1]
        val area = rect.width() * rect.height()
        val normalizedArea = area / (imageWidth * imageHeight).toFloat()

        // Combined score: 70% center proximity, 30% size
        // Clamped to [0, 1] for robustness
        val centerScore = (1f - distanceToCenter).coerceIn(0f, 1f)

        return (centerScore * CENTER_WEIGHT) + (normalizedArea * AREA_WEIGHT)
    }

    private data class ScoredObject(
        val obj: DetectedObject,
        val score: Float,
    )

    companion object {
        private const val SELECTION_COOLDOWN_FRAMES = 15
        private const val SELECTION_HYSTERESIS_MULTIPLIER = 1.3f
        private const val CENTER_WEIGHT = 0.7f
        private const val AREA_WEIGHT = 0.3f
    }
}
