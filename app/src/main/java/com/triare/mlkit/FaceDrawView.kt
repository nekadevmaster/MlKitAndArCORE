package com.triare.mlkit

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark

class FaceDrawView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var faces: List<FirebaseVisionFace>? = null

    private val noseBitmap: Bitmap by lazy {
        BitmapFactory.decodeResource(
            this.resources,
            R.drawable.nose
        )
    }

    private val boxPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val circulPaint = Paint().apply {
        color = Color.RED
    }

    fun update(faces: List<FirebaseVisionFace>) {
        this.faces = faces
        postInvalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

        if (faces == null || canvas == null)
            return

        for (face in faces!!) {

            Log.d("Nek", "draw box")
            canvas.drawRect(face.boundingBox, boxPaint)

            Log.d("Nek", "top ${face.boundingBox.top}, " +
                    "bottom ${face.boundingBox.bottom}, " +
                    "right ${face.boundingBox.right}, " +
                    "left ${face.boundingBox.left}")

            //Find nose
            val landmark = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE)

            landmark?.let {
                val point = landmark.position
                val imageEdgeSizeBasedOnFaceSize = face.boundingBox.width() / .0f

                val left = (point.x - imageEdgeSizeBasedOnFaceSize).toInt()
                val top = (point.y - imageEdgeSizeBasedOnFaceSize).toInt()
                val right = (point.x + imageEdgeSizeBasedOnFaceSize).toInt()
                val bottom = (point.y + imageEdgeSizeBasedOnFaceSize).toInt()

                Log.d("Nek", "draw nose")
                Log.d("Nek", "top $top, " +
                        "bottom $bottom, " +
                        "right $right, " +
                        "left $left")

                //val b = Bitmap.createScaledBitmap(noseBitmap, 10, 10, false)

                canvas.drawBitmap(noseBitmap, null,
                    Rect(left, top, right, bottom), null)
            }

        }

        super.onDraw(canvas)
    }

}