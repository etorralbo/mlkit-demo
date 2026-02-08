package com.mlkit.demo.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.mlkit.demo.model.BoundingBox

@Composable
fun BoundingBoxOverlay(
    boundingBox: BoundingBox,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            color = Color.Green,
            topLeft = Offset(boundingBox.left, boundingBox.top),
            size = Size(
                boundingBox.right - boundingBox.left,
                boundingBox.bottom - boundingBox.top
            ),
            style = Stroke(width = 8f)
        )
    }
}
