package com.triare.mlkit.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import kotlinx.android.synthetic.main.activity_face_detection.*
import com.triare.mlkit.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class FaceDetectionActivity : AppCompatActivity(), LifecycleOwner {

    // This is an arbitrary number we are using to keep track of the permission
    // request. Where an app has multiple context for requesting permission,
    // this can help differentiate the different contexts.
    private val REQUEST_CODE_PERMISSIONS = 10

    // This is an array of all the permission specified in the manifest.
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detection)

        view_camera.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }

        view_camera.post { startCamera() }

    }

    private fun processFaceContourDetectionResult(faces: List<FirebaseVisionFace>) {
        // Task completed successfully
        if (faces.size == 0) {
            Log.e("Nek", "face not found")
            //showToast("No face found")
            return
        }

        graphicOverlay.clear()
        val imageGraphic = CameraImageGraphic(graphicOverlay, null)
        graphicOverlay.add(imageGraphic)
        for (i in faces.indices) {
            val face = faces[i]
            val cameraFacing = 0
            val faceGraphic = FaceGraphic(graphicOverlay, face, cameraFacing, null)
            graphicOverlay.add(faceGraphic)
        }
        graphicOverlay.postInvalidate()

    }

    private fun startCamera() {
        val size = Size(640, 480)
        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder().apply {
        }
            .setTargetResolution(size)
            .setLensFacing(CameraX.LensFacing.BACK)
            .build()


        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {

            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = view_camera.parent as ViewGroup
            parent.removeView(view_camera)
            parent.addView(view_camera, 0)

            view_camera.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        val min = Math.min(size.width, size.height)
        val max = Math.max(size.width, size.height)

        graphicOverlay.setCameraInfo(min, max, 0)

        // Add this before CameraX.bindToLifecycle

        // Setup image analysis pipeline that computes average pixel luminance
        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            // In our analysis, we care more about the latest image than
            // analyzing *every* image
            setImageReaderMode(
                ImageAnalysis.ImageReaderMode.ACQUIRE_NEXT_IMAGE)
        }.build()

        // Build the image analysis use case and instantiate our analyzer
        val analyzerUseCase = ImageAnalysis(analyzerConfig)

        analyzerUseCase.setAnalyzer(executor, FaceImageAnalyzer {
            GlobalScope.launch(Dispatchers.IO) {processFaceContourDetectionResult(it)}
        })

        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to
        // version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, preview, analyzerUseCase)
    }

    private val executor = Executors.newSingleThreadExecutor()

    private fun updateTransform() {
        val matrix = Matrix()

        /*val bitmap = Bitmap.createBitmap(viewFinder.width, viewFinder.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        //canvas.drawRGB(255, 128, 128)
        val paint = Paint().let {

        }

        txtrecognizer_imageview_real.setImageBitmap(bitmap)*/

        // Compute the center of the view finder
        val centerX = view_camera.width / 2f
        val centerY = view_camera.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when(view_camera.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        //view_camera.setTransform(matrix)
    }

    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                view_camera.post { startCamera() }
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

}
