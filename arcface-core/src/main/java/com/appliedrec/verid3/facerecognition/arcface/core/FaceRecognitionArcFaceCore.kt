package com.appliedrec.verid3.facerecognition.arcface.core

import com.appliedrec.verid3.common.FaceRecognition
import com.appliedrec.verid3.common.FaceTemplate
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.sqrt

abstract class FaceRecognitionArcFaceCore : FaceRecognition<FaceTemplateVersionV24, FloatArray> {

    override val version: FaceTemplateVersionV24 = FaceTemplateVersionV24

    override val defaultThreshold: Float = 6.8f

    override suspend fun compareFaceRecognitionTemplates(
        faceRecognitionTemplates: List<FaceTemplate<FaceTemplateVersionV24, FloatArray>>,
        template: FaceTemplate<FaceTemplateVersionV24, FloatArray>
    ): FloatArray = coroutineScope {
        require(faceRecognitionTemplates.all { it.data.size == template.data.size }) { "Face recognition templates must have the same length" }
        val data1 = template.data
        val norm1 = norm(data1)
        val templateData = faceRecognitionTemplates.map { it.data }
        val chunks = templateData.chunked(100)
        val scores = chunks.map { chunk ->
            async {
                chunk.map { data2 ->
                    val norm2 = norm(data2)
                    innerProduct(data1, data2) / (norm1 * norm2)
                }
            }
        }.awaitAll().flatten().toFloatArray()
        return@coroutineScope scores
    }

    private fun innerProduct(v1: FloatArray, v2: FloatArray): Float {
        return v1.zip(v2) { a, b -> a * b }.sum()
    }

    private fun norm(v: FloatArray): Float {
        return sqrt(innerProduct(v, v))
    }
}