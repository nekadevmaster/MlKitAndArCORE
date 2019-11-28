package com.triare.mlkit

import android.app.ProgressDialog
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_face_detection2.*
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import com.triare.mlkit.components.FaceGraphic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FaceDetection2Activity : AppCompatActivity() {

    private val TAG = "NEK"

    private val imageRealBitma: Bitmap by lazy {
       BitmapFactory.decodeResource(
            this.getResources(),
            R.drawable.my_face
        )
    }

    private val noseBitma: Bitmap by lazy {
        BitmapFactory.decodeResource(
            this.getResources(),
            R.drawable.nose
        )
    }

    private val mutableImageBitmap: Bitmap by lazy {
        imageRealBitma.copy(Bitmap.Config.ARGB_8888, true)
    }

    private val progressDialog: ProgressDialog by lazy {
        ProgressDialog(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detection2)

        GlobalScope.launch(Dispatchers.IO) {
            launch(Dispatchers.Main) {
                progressDialog.show()
            }
            runDetect(FirebaseVisionImage.fromBitmap(mutableImageBitmap))
            launch(Dispatchers.Main) {
                progressDialog.hide()
            }
        }

    }

    private suspend fun runDetect(image: FirebaseVisionImage) {
        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .build()

        val detector = FirebaseVision.getInstance()
            .getVisionFaceDetector(options)

        val faces = detector.detectInImage(image).await()

        drawFaces(faces)
    }

    private val faceBoxPaint: Paint by lazy {
        Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 10f
        }
    }
    private val nosePaint: Paint by lazy {
        Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
            //strokeWidth = 10f
        }
    }

    private fun drawFaces(faces: List<FirebaseVisionFace>) = runBlocking(Dispatchers.Main) {
        val canvas = Canvas(mutableImageBitmap)

        for (face in faces) {
            canvas.drawRect(face.boundingBox, faceBoxPaint)
            drawNose(canvas, face)
            drawLeftEye(canvas, face)
            drawRightEye(canvas, face)

        }

        face_imageview.setImageBitmap(mutableImageBitmap)
    }

    private fun drawNose(canvas: Canvas, face: FirebaseVisionFace) {
        val landmark = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE) ?: return

        val point = landmark.position

        val imageEdgeSizeBasedOnFaceSize = face.boundingBox.width() / 4.0f

        val left = (point.x - imageEdgeSizeBasedOnFaceSize).toInt()
        val top = (point.y - imageEdgeSizeBasedOnFaceSize).toInt()
        val right = (point.x + imageEdgeSizeBasedOnFaceSize).toInt()
        val bottom = (point.y + imageEdgeSizeBasedOnFaceSize).toInt()

        /*canvas.drawBitmap(noseBitma, null,
            Rect(left, top, right, bottom), null)*/

        canvas.drawCircle(point.x, point.y, 200f, nosePaint)

    }

    private fun drawLeftEye(canvas: Canvas, face: FirebaseVisionFace) {
        val landmark = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE) ?: return

        val point = landmark.position

        canvas.drawCircle(point.x, point.y, 100f, nosePaint)
    }

    private fun drawRightEye(canvas: Canvas, face: FirebaseVisionFace) {
        val landmark = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE) ?: return

        val point = landmark.position

        canvas.drawCircle(point.x, point.y, 100f, nosePaint)
    }

    private fun getPossition(pos1: Int, pos2: Int) : Float {
        var possitionReal = 0
        possitionReal = if (pos1 > pos2) {
            pos1 - pos2
        } else {
            pos2 - pos1
        }
        return possitionReal.toFloat()
    }

}

suspend fun Task<List<FirebaseVisionFace>>.await(): List<FirebaseVisionFace> = suspendCoroutine { const ->
    this.addOnSuccessListener { const.resume(it) }
    this.addOnFailureListener { const.resumeWithException(it) }
}
