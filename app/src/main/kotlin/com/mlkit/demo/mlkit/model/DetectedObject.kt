package com.mlkit.demo.mlkit.model

import android.graphics.Rect

internal data class DetectedObject(
    val boundingBox: Rect,
    val trackingId: Int?,
)
