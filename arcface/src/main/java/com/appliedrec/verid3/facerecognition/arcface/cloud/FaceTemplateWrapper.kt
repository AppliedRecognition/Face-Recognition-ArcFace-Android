package com.appliedrec.verid3.facerecognition.arcface.cloud

import com.appliedrec.verid3.facerecognition.arcface.core.FaceTemplateArcFace
import com.appliedrec.verid3.facerecognition.arcface.core.FaceTemplateVersionV24
import kotlinx.serialization.Serializable

@Serializable
internal data class FaceTemplateWrapper(
    val version: Int,
    val data: List<Float>
) {
    val faceTemplate: FaceTemplateArcFace
        get() {
            require(version == FaceTemplateVersionV24.id) {
                "Invalid version: $version. Expected: ${FaceTemplateVersionV24.id}."
            }
            return FaceTemplateArcFace(data.toFloatArray())
        }

    constructor(faceTemplate: FaceTemplateArcFace) : this(
        FaceTemplateVersionV24.id,
        faceTemplate.data.toList()
    )
}
