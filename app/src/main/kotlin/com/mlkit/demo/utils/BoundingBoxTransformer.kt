package com.mlkit.demo.utils

import android.graphics.Rect
import com.mlkit.demo.model.BoundingBox

object BoundingBoxTransformer {

    /**
     * Transforms bounding box coordinates from image space to view space.
     *
     * @param imageBoundingBox Bounding box in image coordinates (e.g., 1280x720)
     * @param imageWidth Width of the image from camera
     * @param imageHeight Height of the image from camera
     * @param viewWidth Width of the preview view on screen
     * @param viewHeight Height of the preview view on screen
     * @return BoundingBox in view coordinates (screen pixels)
     */
    fun transform(
        imageBoundingBox: Rect,
        imageWidth: Int,
        imageHeight: Int,
        viewWidth: Int,
        viewHeight: Int
    ): BoundingBox {
        // Calculate scaling factor for CENTER_CROP (fill the view, crop if needed)
        val imageAspectRatio = imageWidth.toFloat() / imageHeight.toFloat()
        val viewAspectRatio = viewWidth.toFloat() / viewHeight.toFloat()

        val (scaleFactor, offsetX, offsetY) = if (imageAspectRatio > viewAspectRatio) {
            // Image is wider - scale based on height, crop width
            val scale = viewHeight.toFloat() / imageHeight.toFloat()
            val scaledImageWidth = imageWidth * scale
            val xOffset = (scaledImageWidth - viewWidth) / 2f
            Triple(scale, -xOffset, 0f)
        } else {
            // Image is taller - scale based on width, crop height
            val scale = viewWidth.toFloat() / imageWidth.toFloat()
            val scaledImageHeight = imageHeight * scale
            val yOffset = (scaledImageHeight - viewHeight) / 2f
            Triple(scale, 0f, -yOffset)
        }

        // Transform coordinates
        val left = (imageBoundingBox.left * scaleFactor + offsetX).coerceIn(0f, viewWidth.toFloat())
        val top = (imageBoundingBox.top * scaleFactor + offsetY).coerceIn(0f, viewHeight.toFloat())
        val right = (imageBoundingBox.right * scaleFactor + offsetX).coerceIn(0f, viewWidth.toFloat())
        val bottom = (imageBoundingBox.bottom * scaleFactor + offsetY).coerceIn(0f, viewHeight.toFloat())

        return BoundingBox(left, top, right, bottom)
    }
}
