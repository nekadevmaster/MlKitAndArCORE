package com.triare.mlkit

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.triare.mlkit.components.TxtImageAnalyzer
import kotlinx.android.synthetic.main.activity_txt_recognozer.*
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import android.graphics.Bitmap
import com.triare.mlkit.components.TextGraphic

class TatRecognizerActivity : AppCompatActivity(), LifecycleOwner {

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

    // Create configuration object for the image capture use case
    private val imageCaptureConfig: ImageCaptureConfig by lazy {
        ImageCaptureConfig.Builder()
            .apply {
                // We don't set a resolution for image capture; instead, we
                // select a capture mode which will infer the appropriate
                // resolution based on aspect ration and requested mode
                setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            }.build()
    }

    // Build the image capture use case and attach button click listener
    private val imageCapture : ImageCapture by lazy {  ImageCapture(imageCaptureConfig) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_txt_recognozer)

        // Request camera permissions
        if (allPermissionsGranted()) {
            viewFinder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Every time the provided texture view changes, recompute layout
        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }

        txtrecognizer_takeimage.setOnClickListener {

            /*imageCapture.takePicture(object : ImageCapture.OnImageCapturedListener() {
                override fun onCaptureSuccess(image: ImageProxy?, rotationDegrees: Int) {
                    image.use { image ->
                        var bitmap: Bitmap? = image?.let {
                            imageProxyToBitmap(it)
                        } ?: return
                        if (bitmap != null) {

                            val matrix = Matrix().apply {
                                postRotate(90f)
                            }

                            bitmap = Bitmap.createScaledBitmap(bitmap, viewFinder.width, viewFinder.height, true)

                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                            val detector = FirebaseVision.getInstance()
                                .onDeviceTextRecognizer
                            val imageN: FirebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap)
                            detector.processImage(imageN)
                                .addOnSuccessListener {
                                    processTextRecognitionResult(it)
                                }
                            //txtrecognizer_imageview_real.setImageBitmap(bitmap)

                        }
                    }
                }
                private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
                    val buffer: ByteBuffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
            })*/

            /*val file = File(externalMediaDirs.first(),
                "${System.currentTimeMillis()}.jpg")*/

           /* imageCapture.takePicture(file, object : ImageCapture.OnImageSavedListener {

                override fun onImageSaved(file: File) {
                    val msg = "Photo capture succeeded: ${file.absolutePath}"
                    Log.d("CameraXApp", msg)
                    viewFinder.post {
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(
                    imageCaptureError: ImageCapture.ImageCaptureError,
                    message: String,
                    exc: Throwable?
                ) {
                    val msg = "Photo capture failed: $message"
                    Log.e("CameraXApp", msg, exc)
                    viewFinder.post {
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    }
                }

            })*/

        }

    }

    private val executor = Executors.newSingleThreadExecutor()
    private val viewFinder: TextureView by lazy { findViewById<TextureView>(R.id.txtrecognizer_txtview) }

    private fun startCamera() {
        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(Size(640, 480))
        }.build()


        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {

            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        // Add this before CameraX.bindToLifecycle

        // Setup image analysis pipeline that computes average pixel luminance
        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            // In our analysis, we care more about the latest image than
            // analyzing *every* image
            setImageReaderMode(
                ImageAnalysis.ImageReaderMode.ACQUIRE_NEXT_IMAGE)
        }.build()

        // Build the image analysis use case and instantiate our analyzer
        /*val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
            //this.analyzer = TxtImageAnalyzer(viewFinder, txtrecognizer_imageview)
            this.analyzer = TxtImageAnalyzer {
                processTextRecognitionResult(it)
            }
        }*/

        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to
        // version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, preview, imageCapture)
    }

    suspend fun startDrawLoop(data: FirebaseVisionText, bitmapN: Bitmap) {

        var bitMaperNek : Bitmap? = null

        val blocks = data.textBlocks

        for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines.indices) {
                val elements = lines[j].elements
                for (k in elements.indices) {
                    val element: FirebaseVisionText.Element = elements[k]
                    bitMaperNek = getBitmap(
                        element, when (bitMaperNek != null) {
                            true -> bitMaperNek
                            false -> bitmapN
                        }
                    )
                }
            }
        }

        if (bitMaperNek != null) {
            withContext(Dispatchers.Main) {
                txtrecognizer_imageview_real.setImageBitmap(bitMaperNek)
            }
        }
    }

    private fun processTextRecognitionResult(texts: FirebaseVisionText) {
        val blocks = texts.textBlocks
        if (blocks.size == 0) {
            ///showToast("No text found")
            return
        }
        mGraphicOverlay.clear()
        for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines.indices) {
                val elements = lines[j].elements
                for (k in elements.indices) {
                    val textGraphic = TextGraphic(mGraphicOverlay, elements[k])
                    mGraphicOverlay.add(textGraphic)

                }
            }
        }
    }

    suspend fun startDraw(textBlock: FirebaseVisionText.Element, bitmapN: Bitmap): Bitmap? = runBlocking(Dispatchers.Main) {
        val bitmap = withContext(Dispatchers.IO) {
            getBitmap(textBlock, bitmapN)
        }
        return@runBlocking bitmap
    }

    fun getBitmap(element: FirebaseVisionText.Element, bitmap: Bitmap) : Bitmap {
        val workingBitmap = Bitmap.createBitmap(bitmap)
        val matrix = Matrix().apply {
            postRotate(90f)
        }
        var mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true)
        mutableBitmap = Bitmap.createBitmap(mutableBitmap, 0, 0, mutableBitmap.width, mutableBitmap.height, matrix, true)
        val canvas = Canvas(mutableBitmap)
        val rectPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = Color.RED
            strokeWidth = 1f
        }
        val rect = RectF(element.boundingBox)
        canvas.drawRect(rect, rectPaint)
        // Renders the text at the bottom of the box.
        //canvas.drawText(element.text, rect.left, rect.bottom, textPaint)
        return mutableBitmap
    }

    private var onDrawing = false
    fun draw(rect: Rect) {
        if (onDrawing)
            return
        onDrawing = true
        val bitmap: Bitmap? = viewFinder.bitmap
        if (bitmap != null) {
            val canvas = Canvas(bitmap)
            val rectPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                color = Color.RED
                strokeWidth = 1f
            }
            canvas.drawRect(rect, rectPaint)
            txtrecognizer_imageview_real.setImageBitmap(bitmap)
        }
    }

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
        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when(viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix)
    }

    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { startCamera() }
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
