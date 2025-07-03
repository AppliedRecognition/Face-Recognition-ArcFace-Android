package com.appliedrec.verid3.facerecognition.arcface.cloud

import android.graphics.Bitmap
import com.appliedrec.verid3.common.Face
import com.appliedrec.verid3.common.IImage
import com.appliedrec.verid3.common.serialization.toBitmap
import com.appliedrec.verid3.facerecognition.arcface.core.FaceAlignment
import com.appliedrec.verid3.facerecognition.arcface.core.FaceRecognitionCore
import com.appliedrec.verid3.facerecognition.arcface.core.FaceRecognitionTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException

class FaceRecognition(val serverUrl: HttpUrl, val apiToken: String) : FaceRecognitionCore() {

    private val faceAlignment = FaceAlignment()
    private val httpClient = OkHttpClient()

    override suspend fun createFaceRecognitionTemplates(
        faces: Array<Face>,
        image: IImage
    ): Array<FaceRecognitionTemplate> = withContext(Dispatchers.IO) {
        val bitmap = image.toBitmap()
        val imageList = faces.map { face ->
            require(face.noseTip != null && face.mouthCentre != null)
            val aligned = faceAlignment.alignFace(bitmap, face.leftEye, face.rightEye, face.noseTip!!, face.mouthCentre!!)
            bitmapToJpeg(aligned)
        }
        val requestBody = Json.encodeToString(RequestBody(imageList))
        val request = Request.Builder()
            .url(serverUrl)
            .header("x-api-key", apiToken)
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
        val body = httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected HTTP code ${response.code}")
            }
            response.body?.string() ?: throw IllegalStateException("Response body is null")
        }
        Json.decodeFromString<Array<FaceRecognitionTemplate>>(body)
    }

    private fun bitmapToJpeg(bitmap: Bitmap): ByteArray {
        return ByteArrayOutputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.toByteArray()
        }
    }
}