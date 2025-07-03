package com.appliedrec.verid3.facerecognition.arcface.cloud

import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.appliedrec.verid3.common.Face
import com.appliedrec.verid3.common.Image
import com.appliedrec.verid3.common.serialization.fromBitmap
import com.appliedrec.verid3.facedetection.mp.FaceDetection
import com.appliedrec.verid3.facerecognition.arcface.core.FaceRecognitionTemplate
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class FaceRecognitionTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var faceDetection: FaceDetection

    private fun createFaceRecognition(mockWebServer: MockWebServer? = null): FaceRecognition {
        return mockWebServer?.let { server ->
            FaceRecognition(server.url("/"), "api_token")
        } ?: run {
            val context = InstrumentationRegistry.getInstrumentation().context
            val config = context.assets.open("config.json").use { inputStream ->
                Json.decodeFromString<Config>(inputStream.reader().readText())
            }
            FaceRecognition(config.url.toHttpUrl(), config.apiKey)
        }
    }

    @Before
    fun setup() {
        faceDetection = FaceDetection(InstrumentationRegistry.getInstrumentation().context)
    }

    @Test
    fun testExtractFaceTemplate(): Unit = runBlocking {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val faceRecognition = createFaceRecognition(mockWebServer)
        val fakeTemplate = FaceRecognitionTemplate(generateRandomFaceTemplate())
        val body = Json.encodeToString(arrayOf(fakeTemplate))
        mockWebServer.enqueue(
            MockResponse()
                .setBody(body)
                .addHeader("Content-Type", "application/json")
        )
        val (image, face) = createTestImageAndFace()
        val template = faceRecognition.createFaceRecognitionTemplates(arrayOf(face), image).first()
        assertEquals(128, template.data.size)
        mockWebServer.shutdown()
    }

    @Test
    fun testExtractFaceTemplateInCloud(): Unit = runBlocking {
        val faceRecognition = createFaceRecognition()
        val (image, face) = createTestImageAndFace()
        val template = faceRecognition.createFaceRecognitionTemplates(arrayOf(face), image).first()
        assertEquals(128, template.data.size)
    }

    @Test
    fun identifyUserInFace(): Unit = runBlocking {
        val challengeFace = FaceRecognitionTemplate(generateRandomFaceTemplate())
        val users: Array<Pair<String, FaceRecognitionTemplate>> = arrayOf(
            "user1" to FaceRecognitionTemplate(generateRandomFaceTemplate()),
            "user2" to FaceRecognitionTemplate(generateFaceTemplateSimilarTo(challengeFace.data, 0.8f)),
            "user3" to FaceRecognitionTemplate(generateRandomFaceTemplate()),
        )
        val threshold = 0.5f
        // 1. Create FaceRecognition instance
        val faceRecognition = createFaceRecognition()
        // 2. Compare registered user faces to the challenge face
        val scores = faceRecognition.compareFaceRecognitionTemplates(
            users.map { it.second }.toTypedArray(), challengeFace
        )
        // 3. Return users with scores matching or exceeding the threshold
        val result = scores
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
        assertEquals(1, result.size)
        assertEquals("user2", result.keys.first())
    }

    private suspend fun createTestImageAndFace(): Pair<Image, Face> {
        val bitmap = InstrumentationRegistry.getInstrumentation().context.assets
            .open("Photo 04-05-2016, 18 57 50.png")
            .use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        val image = Image.fromBitmap(bitmap)
        val face = faceDetection.detectFacesInImage(image, 1).first()
        return image to face
    }

    private fun generateRandomFaceTemplate(): FloatArray {
        val raw = FloatArray(128) { Random.nextFloat() - 0.5f }
        val norm = norm(raw)
        return raw.map { it / norm }.toFloatArray()
    }

    private fun generateFaceTemplateSimilarTo(template: FloatArray, score: Float): FloatArray {
        val v2Raw = FloatArray(template.size) { Random.nextFloat() - 0.5f }
        val dot = innerProduct(template, v2Raw)
        val v2Ortho = v2Raw.zip(template) { vi, v1i -> vi - dot * v1i }.toFloatArray()
        val v2OrthoNorm = norm(v2Ortho)
        val v2OrthoUnit = v2Ortho.map { it / v2OrthoNorm }.toFloatArray()
        val angle = acos(score)
        val v2 = FloatArray(template.size) { i ->
            (cos(angle) * template[i] + sin(angle) * v2OrthoUnit[i])
        }
        return v2
    }


    private fun innerProduct(v1: FloatArray, v2: FloatArray): Float {
        return v1.zip(v2) { a, b -> a * b }.sum()
    }

    private fun norm(v: FloatArray): Float {
        return sqrt(innerProduct(v, v))
    }
}

@Serializable
private data class Config(
    val apiKey: String,
    val url: String
)