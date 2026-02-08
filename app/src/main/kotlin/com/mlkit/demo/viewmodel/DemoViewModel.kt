package com.mlkit.demo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlkit.demo.model.BoundingBox
import com.mlkit.demo.model.DetectionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DemoViewModel : ViewModel() {
    private val _detectionState = MutableStateFlow<DetectionState?>(null)
    val detectionState: StateFlow<DetectionState?> = _detectionState.asStateFlow()

    private var detectionJob: Job? = null
    private var isInDetectionCycle = false

    companion object {
        private const val DISPLAY_DURATION_MS = 4000L  // Show label for 4 seconds
        private const val COOLDOWN_DURATION_MS = 1000L  // Wait 1 second before next detection
    }

    fun onObjectDetected(
        label: String,
        confidence: Float,
        boundingBox: BoundingBox
    ) {
        // Ignore new detections if we're currently in a detection cycle
        if (isInDetectionCycle) {
            return
        }

        // Cancel any pending job and start new detection cycle
        detectionJob?.cancel()
        detectionJob = viewModelScope.launch {
            isInDetectionCycle = true

            // Show the detection
            _detectionState.value = DetectionState(label, confidence, boundingBox)

            // Display for 4 seconds
            delay(DISPLAY_DURATION_MS)

            // Clear the detection
            _detectionState.value = null

            // Cooldown for 1 second
            delay(COOLDOWN_DURATION_MS)

            // Ready for next detection
            isInDetectionCycle = false
        }
    }

    fun clearDetection() {
        detectionJob?.cancel()
        _detectionState.value = null
        isInDetectionCycle = false
    }

    override fun onCleared() {
        super.onCleared()
        detectionJob?.cancel()
    }
}
