package com.mlkit.demo.mlkit.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

/**
 * Converts ImageProxy to Bitmap using an optimized pipeline:
 * 1. YUV_420_888 → NV21 conversion
 * 2. NV21 → JPEG compression (quality 80)
 * 3. JPEG → Bitmap with RGB_565 format (50% memory reduction)
 *
 * @return Bitmap in RGB_565 format, or null if conversion fails
 */
internal fun ImageProxy.toCustomBitmap(): Bitmap? {
    return try {
        // CameraX provides YUV_420_888 format, convert to RGB Bitmap
        val yuvBytes = yuv420ToByteArray()
        val yuvImage = YuvImage(
            yuvBytes,
            ImageFormat.NV21,
            width,
            height,
            null,
        )

        // Compress YUV to JPEG in memory (reduced quality for better performance)
        val outputStream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            Rect(0, 0, width, height),
            80, // Reduced from 100 to 80 for faster compression and lower memory usage
            outputStream,
        )
        val jpegBytes = outputStream.toByteArray()

        // Decode JPEG to Bitmap with RGB_565 format (50% memory reduction)
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.RGB_565 // 16-bit instead of 32-bit ARGB_8888
        }
        BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size, options)
    } catch (e: Exception) {
        Log.v("ImageProxyExtension", "Failed to convert ImageProxy to Bitmap: ${e.message}")
        null
    }
}

/**
 * Converts YUV_420_888 ImageProxy planes to NV21 byte array format.
 * NV21 format: [Y Y Y ... | V U V U V U ...]
 */
private fun ImageProxy.yuv420ToByteArray(): ByteArray {
    val ySize = width * height
    val uvSize = ySize / 2
    val nv21 = ByteArray(ySize + uvSize)

    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    yBuffer.rewind()
    yBuffer.get(nv21, 0, ySize)

    // Interleave U and V planes into NV21 format (VUVUVU...)
    val uvPixelStride = planes[1].pixelStride
    val uvRowStride = planes[1].rowStride

    if (uvPixelStride == 1) {
        // Tightly packed U and V planes, can copy directly
        vBuffer.rewind()
        vBuffer.get(nv21, ySize, uvSize)
    } else {
        // Need to interleave U and V manually
        var pos = ySize
        val uvWidth = width / 2
        val uvHeight = height / 2

        for (row in 0 until uvHeight) {
            for (col in 0 until uvWidth) {
                val uvIndex = row * uvRowStride + col * uvPixelStride
                nv21[pos++] = vBuffer.get(uvIndex)
                nv21[pos++] = uBuffer.get(uvIndex)
            }
        }
    }

    return nv21
}
