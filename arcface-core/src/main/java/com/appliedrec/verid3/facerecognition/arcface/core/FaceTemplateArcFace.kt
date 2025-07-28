package com.appliedrec.verid3.facerecognition.arcface.core

import com.appliedrec.verid3.common.FaceTemplate
import kotlinx.serialization.Serializable

@Serializable(with = FaceTemplateSerializer::class)
class FaceTemplateArcFace(
    data: FloatArray
) : FaceTemplate<FaceTemplateVersionV24, FloatArray>(FaceTemplateVersionV24, data) {

    override fun equals(other: Any?): Boolean {
        return other is FaceTemplateArcFace && other.data.contentEquals(data)
    }

    override fun hashCode(): Int {
        return 31 * version.hashCode() + data.contentHashCode()
    }
}