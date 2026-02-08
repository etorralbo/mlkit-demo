# MLKit Object Detection Demo

A real-time object detection Android app demonstrating MLKit's capabilities with a polished UI and smart object tracking.

![Android](https://img.shields.io/badge/Android-34A853?style=flat&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![MLKit](https://img.shields.io/badge/MLKit-4285F4?style=flat&logo=google&logoColor=white)

## ✨ Features

- **Real-time Object Detection** - Detect and classify objects using MLKit's on-device models
- **Smart Object Selection** - Intelligent tracking with scoring algorithm (70% center proximity + 30% size)
- **Visual Bounding Boxes** - Green rounded rectangles around detected objects
- **Object Labels** - Display object name and confidence percentage
- **Timed Detection Cycle** - 4-second display + 1-second cooldown for smooth UX
- **Coordinate Transformation** - Accurate mapping from camera space to screen space
- **Lifecycle Management** - Proper camera and coroutine handling

## 🎬 Demo

Point your camera at everyday objects like:
- Phones
- Laptops
- Cups
- Books
- Furniture

The app will detect, classify, and display a label like **"Phone 87%"** with a bounding box around it.

## 🏗️ Architecture

Built with modern Android development practices:

```
app/
├── camera/              # CameraX integration
│   ├── CameraManager    # Camera lifecycle management
│   └── CameraAnalyzer   # Bridge between CameraX and MLKit
├── mlkit/               # MLKit integration
│   ├── ObjectDetectionAnalyzer  # Main detection logic
│   ├── vision/          # Detectors and analyzers
│   ├── model/           # Data models
│   └── utils/           # Image processing utilities
├── ui/                  # Compose UI components
│   └── BoundingBoxOverlay
├── viewmodel/           # State management
│   └── DemoViewModel
├── model/               # UI state models
└── di/                  # Dependency injection
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- JDK 17+
- Android device or emulator (API 28+)
- Camera permission

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd mlkit-demo
```

2. Open in Android Studio

3. Build and run:
```bash
./gradlew installDebug
```

Or use Android Studio's Run button (▶️)

## 🛠️ Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.1.0 | Programming language |
| Jetpack Compose | 1.7.6 | UI framework |
| CameraX | 1.4.1 | Camera integration |
| MLKit Object Detection | 17.0.1 | On-device object detection |
| MLKit Image Labeling | 17.0.8 | Object classification |
| Koin | 4.0.1 | Dependency injection |
| Coroutines | 1.10.1 | Async operations |
| ViewModel + StateFlow | 2.8.7 | State management |
| JUnit 4 | 4.13.2 | Unit testing framework |
| Coroutines Test | 1.10.1 | Testing coroutines |
| Turbine | 1.1.0 | Testing Flow emissions |

## 📱 How It Works

1. **Camera Capture** - CameraX captures frames at 1280x720 resolution
2. **Image Processing** - Frames converted to RGB_565 bitmap format
3. **Object Detection** - MLKit detects objects with bounding boxes
4. **Smart Selection** - Algorithm selects the most relevant object:
   - 70% weight on center proximity
   - 30% weight on object size
   - 15-frame cooldown
   - 1.3x hysteresis multiplier to prevent flickering
5. **Image Labeling** - MLKit classifies the selected object (70% confidence threshold)
6. **Coordinate Transformation** - Bounding box mapped from image space to screen space
7. **UI Display** - Shows for 4 seconds, then 1-second cooldown before next detection

## ⚙️ Configuration

Key constants in `DemoViewModel.kt`:
```kotlin
DISPLAY_DURATION_MS = 4000L  // Label display time
COOLDOWN_DURATION_MS = 1000L  // Pause before next detection
```

Key constants in `ObjectDetectionAnalyzer.kt`:
```kotlin
SELECTION_COOLDOWN_FRAMES = 15        // Frames before reconsidering selection
SELECTION_HYSTERESIS_MULTIPLIER = 1.3 // Prevent flickering
CENTER_WEIGHT = 0.7                   // Center proximity weight
AREA_WEIGHT = 0.3                     // Object size weight
```

MLKit configuration in `DemoModule.kt`:
```kotlin
ObjectDetectorOptions.STREAM_MODE      // Optimized for video
ImageLabelerOptions.confidenceThreshold(0.70f)  // 70% confidence
```

## 📂 Project Structure

```
mlkit-demo/
├── app/
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   └── kotlin/com/mlkit/demo/
│   │       ├── DemoApplication.kt
│   │       ├── MainActivity.kt
│   │       ├── camera/
│   │       ├── mlkit/
│   │       ├── ui/
│   │       ├── viewmodel/
│   │       ├── model/
│   │       ├── utils/
│   │       └── di/
│   └── src/test/
│       └── kotlin/com/mlkit/demo/
│           └── viewmodel/
│               └── DemoViewModelTest.kt
├── gradle/
│   └── libs.versions.toml
└── build.gradle.kts
```

## 🎨 UI Design

- **Bounding Box**: Bright green (#00FF00) with 6px stroke and 16px rounded corners
- **Label**: Green background with bold black text showing "Object NN%"
- **Positioning**: Label floats above bounding box with 8dp minimum top margin

## 🧪 Testing

### Unit Tests

The project includes comprehensive unit tests for the ViewModel logic:

```bash
./gradlew testDebugUnitTest
```

**Test Coverage:**
- ✅ Initial state validation
- ✅ Detection state emission
- ✅ 4-second display duration
- ✅ 1-second cooldown period
- ✅ Detection cycle enforcement (ignores detections during active cycle)
- ✅ Manual state clearing
- ✅ Multiple sequential detections
- ✅ Coroutine cancellation handling

**Test File:** `app/src/test/kotlin/com/mlkit/demo/viewmodel/DemoViewModelTest.kt`

**Technologies Used:**
- JUnit 4 for test framework
- Kotlinx Coroutines Test for testing coroutines with virtual time
- Turbine for testing Flow emissions

### Manual Testing

Run the app and point your camera at various objects:
- ✅ Objects should be detected within 1-2 seconds
- ✅ Bounding box should accurately surround the object
- ✅ Label should display for 4 seconds
- ✅ 1-second pause before next detection
- ✅ Smooth tracking as camera moves

## 📄 Build Info

- **Min SDK**: 28 (Android 9.0)
- **Target SDK**: 36 (Android 16)
- **Compile SDK**: 36
- **APK Size**: ~69 MB (includes MLKit models)

## 🤝 Contributing

This is a demo project showcasing MLKit integration. Feel free to fork and experiment!

## 📝 License

This project is available for educational and demonstration purposes.

## 🔗 Resources

- [MLKit Documentation](https://developers.google.com/ml-kit)
- [CameraX Guide](https://developer.android.com/training/camerax)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Koin Documentation](https://insert-koin.io/)

---

Built with ❤️ using Android, Kotlin, and MLKit
