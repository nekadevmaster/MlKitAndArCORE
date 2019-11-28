package com.triare.mlkit.components

import android.graphics.*
import android.util.Log
import android.view.TextureView
import android.widget.ImageView
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.triare.mlkit.components.GraphicOverlay.Graphic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async


abstract class MyImageAnalyzer : ImageAnalysis.Analyzer {

    private val TAG = "MyImageAnalyzer"

    abstract suspend fun onImageGenerated(image: FirebaseVisionImage)

    private fun degreesToFirebaseRotation(degrees: Int): Int = when(degrees) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
    }

    override fun analyze(imageProxy: ImageProxy?, degrees: Int) {
        val mediaImage = imageProxy?.image
        val imageRotation = degreesToFirebaseRotation(degrees)
        if (mediaImage != null) {
            val image: FirebaseVisionImage = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)
            GlobalScope.async(Dispatchers.IO) { onImageGenerated(image) }
        }
    }

}