package com.appliedrec.verid3.facerecognition.arcface.core

import android.graphics.Bitmap
import android.graphics.PointF

class FaceAlignment {

    init {
        System.loadLibrary("FaceRecognitionArcFaceCore")
    }

    external fun alignFace(image: Bitmap, leftEye: PointF, rightEye: PointF, noseTip: PointF, mouthCentre: PointF): Bitmap
}