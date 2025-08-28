package com.appliedrec.verid3.facerecognition.arcface.core

import com.appliedrec.verid3.common.FaceRecognition
import com.appliedrec.verid3.common.FaceTemplate
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.sqrt

abstract class FaceRecognitionArcFaceCore : FaceRecognition<FaceTemplateVersionV24, FloatArray> {

    override val version: FaceTemplateVersionV24 = FaceTemplateVersionV24

    override val defaultThreshold: Float = 0.8f

    override suspend fun compareFaceRecognitionTemplates(
        faceRecognitionTemplates: List<FaceTemplate<FaceTemplateVersionV24, FloatArray>>,
        template: FaceTemplate<FaceTemplateVersionV24, FloatArray>
    ): FloatArray = coroutineScope {
        require(faceRecognitionTemplates.all { it.data.size == template.data.size }) {
            "Face recognition templates must have the same length"
        }
        val a = template.data
        faceRecognitionTemplates.map { it.data }
            .chunked(100)
            .map { chunk ->
                async {
                    FloatArray(chunk.size) { idx ->
                        val b = chunk[idx]
                        val cos = innerProduct(a, b)
                        ((cos + 1f) * 0.5f).coerceIn(0f, 1f)
                    }
                }
            }
            .awaitAll()
            .reduce { acc, arr -> acc + arr }
    }

    protected fun innerProduct(v1: FloatArray, v2: FloatArray): Float {
        return v1.zip(v2) { a, b -> a * b }.sum()
    }
}