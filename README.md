# Face Recognition for Ver-ID SDK using ArcFace model

## Installation

1. Add the following to your project's **settings.gradle.kts**:

    ```kotlin
    dependencyResolutionManagement {
        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
        repositories {
            google()
            mavenCentral()
            maven {
                url = uri("https://maven.pkg.github.com/AppliedRecognition/Ver-ID-Releases-Android")
            }
        }
    }
    ```
2. Add the following dependency in your **build.gradle.kts** file:

    ```kotlin
    implementation("com.appliedrec.verid3:face-recognition-arcface:1.0.0")
    ```

## Usage

The library implements the [FaceRecognition](https://github.com/AppliedRecognition/Ver-ID-Common-Types-Android/blob/main/lib/src/main/java/com/appliedrec/verid3/common/FaceRecognition.kt) interface from the Ver-ID-Common-Types package. This makes it compatible with the Ver-ID SDK.

### Example: Create a face template from an image and face

Use a class that implements the [FaceDetection](https://github.com/AppliedRecognition/Ver-ID-Common-Types-Android/blob/main/lib/src/main/java/com/appliedrec/verid3/common/FaceDetection.kt) interface from the Ver-ID-Common-Types package. For example:

```kotlin
implementation("com.appliedrec:verid3-face-detection-retinaface:1.0.0")
```

To create an instance of [Image](https://github.com/AppliedRecognition/Ver-ID-Common-Types-Android/blob/main/lib/src/main/java/com/appliedrec/verid3/common/Image.kt) import the Ver-ID serialization library by adding the following dependency:

```kotlin
implementation("com.appliedrec:verid3-serialization:1.0.1")
```

```kotlin
suspend fun detectFacesForRecognition(
    context: Context, 
    uri: Uri, 
    faceDetection: FaceDetection
): Array<FaceTemplateArcFace> = coroutineScope {
    // 1. Read image from URL
    val bitmap = context.contentResolver.openInputStream(uri)
        .use(BitmapFactory::decodeStream)
    // 2. Convert bitmap to Ver-ID image
    val image = Image.fromBitmap(bitmap)
    // 3. Detect up to 5 faces
    val faces = faceDetection.detectFacesInImage(image, 5)
    // 4. Create face recognition instance
    val templates = FaceRecognitionArcFace(context).use {
        // 5. Extract face templates
        it.createFaceRecognitionTemplates(faces, image) 
    }
    // 6. Return face templates
    templates
}
```

### Example: Identify user in a face

In this example, we have a population with face templates registered for different users. We want to find the user that best matches a challenge face. 

```kotlin
suspend fun identifyUserInFace(
   context: Context,
   challengeFace: FaceTemplateArcFace, 
   users: Array<Pair<String,FaceTemplateArcFace>>, 
   threshold: Float = 0.5f
): LinkedHashMap<String,Float> = coroutineScope {
   // 1. Create FaceRecognitionArcFace instance
   val scores = FaceRecognitionArcFace(context).use { faceRecognition ->
       // 2. Compare registered user faces to the challenge face
       faceRecognition.compareFaceRecognitionTemplates(
          users.map { it.second }.toTypedArray(), challengeFace
       )
   }
   // 3. Return users with scores matching or exceeding the threshold
   scores
      .asList() // Convert scores array to list
      .mapIndexedNotNull { index, score ->
         if (score < threshold) {
            null // Ignore element if score doesn't match the threshold
         } else {
            users[index].first to score // Return user/score pair
         }
      }
      .groupBy { it.first } // Group by user
      .map { it -> it.value.maxBy { it.second } } // Map pairs to max score for user
      .sortedByDescending { it.second } // Sort by highest score
      .toMap(LinkedHashMap()) // Convert to LinkedHashMap
}
```