package com.triare.mlkit.components

import android.util.Log
import android.view.TextureView
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText

class TxtImageAnalyzer(val proccessTxtCanvas: (FirebaseVisionText) -> Unit) : MyImageAnalyzer() {

    private val TAG = "TxtImageAnalyzer"

    override suspend fun onImageGenerated(image: FirebaseVisionImage) {
        val detector = FirebaseVision.getInstance()
            .onDeviceTextRecognizer
        //.cloudTextRecognizer
        detector.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                Log.d(TAG, "ON TEXT detected " + firebaseVisionText.text)
                proccessTxtCanvas.invoke(firebaseVisionText)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "on detector error ", e)
            }
    }

    /*private fun processGraphicOverlayTextBlocks(texts: FirebaseVisionText) {
        val blocks = texts.textBlocks
        if (blocks.size == 0) {
            return
        }
        mGraphicOverlay.clear()
        for (block: FirebaseVisionText.TextBlock in blocks) {
            val textGraphic = TextGraphic(mGraphicOverlay, block)
            mGraphicOverlay.add(textGraphic)
        }
        *//*for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines.indices) {
                val elements = lines[j].elements
                for (k in elements.indices) {
                    val textGraphic = TextGraphic(mGraphicOverlay, elements[k])
                    mGraphicOverlay.add(textGraphic)
                }
            }
        }*//*
    }*/

    /*@Synchronized
    private fun drawTextBlock(textBlock : List<FirebaseVisionText.TextBlock>) {
        if (textBlock.isEmpty())
            return

        for (block in textBlock) {
            val rectPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                color = Color.RED
                strokeWidth = 10f
            }

            // Draws the bounding box around the TextBlock.
            //val rect = RectF(element.boundingBox)

            val bitmap = viewFinder.bitmap
            val overlay = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas: Canvas? = Canvas(overlay)

            Log.d("$TAG-DATA", "on draw")

            //val box = block.boundingBox

            canvas?.drawRect(RectF(20f, 20f, 300f, 300f), rectPaint)

            overlay?.let { Canvas(it) }?.apply {
                canvas
            }

            mGraphicOverlay.setImageBitmap(bitmap)

        }
    }*/

}