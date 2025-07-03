package com.appliedrec.verid3.facerecognition.arcface.core

import kotlinx.serialization.Serializable

@Serializable
data class FaceRecognitionTemplate(
    @Serializable(with = FloatArrayAsListSerializer::class)
    val data: FloatArray,
    val version: Int=24
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FaceRecognitionTemplate) return false

        if (!data.contentEquals(other.data)) return false
        if (version != other.version) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + version
        return result
    }
}