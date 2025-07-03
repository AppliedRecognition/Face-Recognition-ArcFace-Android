package com.appliedrec.verid3.facerecognition.arcface.cloud

import kotlinx.serialization.Serializable

@Serializable
data class RequestBody(
    @Serializable(with = ByteArrayListSerializer::class)
    val images: List<ByteArray>
)
