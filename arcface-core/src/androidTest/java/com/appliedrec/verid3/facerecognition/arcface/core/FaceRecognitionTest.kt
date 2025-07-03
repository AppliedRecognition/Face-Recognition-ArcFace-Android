package com.appliedrec.verid3.facerecognition.arcface.core

import android.graphics.BitmapFactory
import android.graphics.PointF
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class FaceRecognitionTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals(
            "com.appliedrec.verid3.facerecognition.arcface.core.test",
            appContext.packageName
        )
    }

    @Test
    fun testAlignFace() {
        val faceAlignment = FaceAlignment()
        val leftEye = PointF(231.97186f, 402.37427f)
        val rightEye = PointF(371.93054f, 400.8451f)
        val mouthCentre = PointF(301.36154f, 568.8225f)
        val noseTip = PointF(296.47003f, 477.80737f)
        InstrumentationRegistry.getInstrumentation().context.assets.open("Photo 04-05-2016, 18 57 50.png").use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val aligned = faceAlignment.alignFace(bitmap, leftEye, rightEye, noseTip, mouthCentre)
            assertEquals(112, aligned.width)
            assertEquals(112, aligned.height)
        }
    }
}