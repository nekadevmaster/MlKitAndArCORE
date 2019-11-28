package com.triare.mlkit.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.TextureView
import android.widget.ImageView
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import kotlinx.android.synthetic.main.activity_txt_recognozer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FaceImageAnalyzer(private val onDraw: (List<FirebaseVisionFace>) -> Unit) : MyImageAnalyzer() {

    private val TAG = "FaceImageAnalyzer"

    override suspend fun onImageGenerated(image: FirebaseVisionImage) {
        val list = GlobalScope.async(Dispatchers.IO) { detect(image) }.await()
        GlobalScope.launch(Dispatchers.Main) {
            onDraw.invoke(list)
        }
    }

    private suspend fun detect(image: FirebaseVisionImage): List<FirebaseVisionFace> = suspendCoroutine { cont ->
        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .build()

        val detector = FirebaseVision.getInstance()
            .getVisionFaceDetector(options)

        detector.detectInImage(image)
            .addOnSuccessListener {
                cont.resume(it)
            }
            .addOnFailureListener {
                Log.d(TAG, "on detect error", it)
            }

    }

}