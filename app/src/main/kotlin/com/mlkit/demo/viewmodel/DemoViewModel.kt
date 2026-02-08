package com.mlkit.demo.viewmodel

import androidx.lifecycle.ViewModel
import com.mlkit.demo.model.BoundingBox
import com.mlkit.demo.model.DetectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DemoViewModel : ViewModel() {
    private val _detectionState = MutableStateFlow<DetectionState?>(null)
    val detectionState: StateFlow<DetectionState?> = _detectionState.asStateFlow()

    fun onObjectDetected(
        label: String,
        confidence: Float,
        boundingBox: BoundingBox
    ) {
        _detectionState.value = DetectionState(label, confidence, boundingBox)
    }

    fun clearDetection() {
        _detectionState.value = null
    }
}
