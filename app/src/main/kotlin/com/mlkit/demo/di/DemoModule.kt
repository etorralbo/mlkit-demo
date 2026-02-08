package com.mlkit.demo.di

import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.mlkit.demo.camera.CameraManager
import com.mlkit.demo.mlkit.ObjectDetectionAnalyzer
import com.mlkit.demo.mlkit.vision.ImageLabelingAnalyzer
import com.mlkit.demo.mlkit.vision.ObjectDetectionDetector
import com.mlkit.demo.viewmodel.DemoViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val demoModule = module {
    single {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            // .enableClassification() - We use ImageLabeling for classification instead
            .build()
        ObjectDetection.getClient(options)
    }

    single {
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.70f)
            .build()
        ImageLabeling.getClient(options)
    }

    single { ObjectDetectionDetector(get()) }
    single { ImageLabelingAnalyzer(get()) }
    single { ObjectDetectionAnalyzer(get(), get()) }
    single { CameraManager(androidContext()) }
    viewModel { DemoViewModel() }
}
