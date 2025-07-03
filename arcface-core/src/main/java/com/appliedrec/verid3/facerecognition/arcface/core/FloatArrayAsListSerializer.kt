package com.appliedrec.verid3.facerecognition.arcface.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

object FloatArrayAsListSerializer : KSerializer<FloatArray> {
    private val delegate = ListSerializer(Float.serializer())
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: FloatArray) {
        delegate.serialize(encoder, value.toList())
    }

    override fun deserialize(decoder: Decoder): FloatArray {
        return delegate.deserialize(decoder).toFloatArray()
    }
}