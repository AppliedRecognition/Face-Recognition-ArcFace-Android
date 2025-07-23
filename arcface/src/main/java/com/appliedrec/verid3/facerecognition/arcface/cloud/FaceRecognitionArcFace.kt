package com.appliedrec.verid3.facerecognition.arcface.cloud

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import com.appliedrec.verid3.common.Face
import com.appliedrec.verid3.common.IImage
import com.appliedrec.verid3.common.serialization.toBitmap
import com.appliedrec.verid3.facerecognition.arcface.core.FaceAlignment
import com.appliedrec.verid3.facerecognition.arcface.core.FaceRecognitionArcFaceCore
import com.appliedrec.verid3.facerecognition.arcface.core.FaceTemplateArcFace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException

class FaceRecognitionArcFace(val apiToken: String, val serverUrl: HttpUrl) : FaceRecognitionArcFaceCore() {

    constructor(apiToken: String, serverUrl: String) : this(apiToken, serverUrl.toHttpUrl())

    constructor(context: Context) : this(
        resolveMetaData(
            context,
            "com.appliedrec.facerecognitionarcface.apikey"
        ),
        resolveMetaData(
            context,
            "com.appliedrec.facerecognitionarcface.serverurl"
        )
    )

    companion object {
        private fun resolveMetaData(context: Context, key: String): String {
            val appInfo = context.packageManager
                .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val value = appInfo.metaData?.getString(key)
            require(!value.isNullOrEmpty()) {
                "Missing or empty meta-data for key: $key"
            }
            return value
        }
    }

    private val faceAlignment = FaceAlignment()
    private val httpClient = OkHttpClient()

    override suspend fun createFaceRecognitionTemplates(
        faces: Array<Face>,
        image: IImage
    ): Array<FaceTemplateArcFace> = withContext(Dispatchers.IO) {
        val bitmap = image.toBitmap()
        val imageList = faces.map { face ->
            require(face.noseTip != null && face.mouthLeftCorner != null && face.mouthRightCorner != null)
            val aligned = faceAlignment.alignFace(bitmap, face)
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
            response.body.string()
        }
        val wrapper = Json.decodeFromString<Array<FaceTemplateWrapper>>(body)
        return@withContext wrapper.map { it.faceTemplate }.toTypedArray()
    }

    private fun bitmapToJpeg(bitmap: Bitmap): ByteArray {
        return ByteArrayOutputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.toByteArray()
        }
    }
}