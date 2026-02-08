package com.mlkit.demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mlkit.demo.camera.CameraAnalyzer
import com.mlkit.demo.camera.CameraManager
import com.mlkit.demo.mlkit.ObjectDetectionAnalyzer
import com.mlkit.demo.ui.BoundingBoxOverlay
import com.mlkit.demo.viewmodel.DemoViewModel
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CameraScreen()
                }
            }
        }
    }
}

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        CameraPreview()
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Camera permission is required")
        }
    }
}

@Composable
fun CameraPreview() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    val cameraManager: CameraManager = koinInject()
    val objectDetectionAnalyzer: ObjectDetectionAnalyzer = koinInject()
    val viewModel: DemoViewModel = viewModel()
    val detectionState by viewModel.detectionState.collectAsState()
    val previewView = remember { PreviewView(context) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val viewWidth = with(density) { maxWidth.toPx().toInt() }
        val viewHeight = with(density) { maxHeight.toPx().toInt() }

        val cameraAnalyzer = remember(viewWidth, viewHeight) {
            CameraAnalyzer(objectDetectionAnalyzer, viewModel, viewWidth, viewHeight)
        }

        LaunchedEffect(cameraAnalyzer) {
            cameraManager.startCamera(lifecycleOwner, previewView, cameraAnalyzer)
        }

        DisposableEffect(Unit) {
            onDispose {
                cameraManager.shutdown()
            }
        }

        // Camera preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Bounding box overlay with label
        detectionState?.let { state ->
            BoundingBoxOverlay(
                label = state.label,
                confidence = state.confidence,
                boundingBox = state.boundingBox
            )
        }
    }
}
