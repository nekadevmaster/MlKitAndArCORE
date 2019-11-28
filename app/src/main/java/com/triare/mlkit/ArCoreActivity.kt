package com.triare.mlkit

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import kotlinx.coroutines.*
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.TransformableNode
import com.triare.mlkit.components.MyArFragment
import java.util.*

class ArCoreActivity : AppCompatActivity(), Scene.OnUpdateListener {

    private var onModelFoxExis = false

    override fun onUpdate(faceTime: FrameTime?) {
        if (onModelFoxExis)
            return
        val frame = arScence.arSceneView.arFrame
        frame?.let {
            val trackables = it.getUpdatedTrackables(AugmentedImage::class.java)
            for (image in trackables) {
                if (image.trackingState == TrackingState.TRACKING) {
                    if (image.name == "fox") {
                        val anchor = image.createAnchor(image.centerPose)
                        createModel(anchor)
                        break
                    }
                }
            }
        }
    }

    private val arScence: MyArFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.ar_scence_fragment) as MyArFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_core)

        GlobalScope.async(Dispatchers.IO) {
            load()
        }

        //setupScence()

    }

    private fun setUpCamera() {
        // Create an ARCore session.
        val session = Session(applicationContext)

// Create a camera config filter for the session.
        val filter = CameraConfigFilter(session)

// Return only camera configs that target 30 fps camera capture frame rate.
        filter.setTargetFps(EnumSet.of(CameraConfig.TargetFps.TARGET_FPS_30))

// Return only camera configs that will not use the depth sensor.
        filter.setDepthSensorUsage(EnumSet.of(CameraConfig.DepthSensorUsage.DO_NOT_USE))

// Get list of configs that match filter settings.
// In this case, this list is guaranteed to contain at least one element,
// because both TargetFps.TARGET_FPS_30 and DepthSensorUsage.DO_NOT_USE
// are supported on all ARCore supported devices.
        val cameraConfigList = session.getSupportedCameraConfigs(filter)

// Use element 0 from the list of returned camera configs. This is because
// it contains the camera config that best matches the specified filter
// settings.
        session.cameraConfig = cameraConfigList[0]
    }

    private fun createModel(anchor: Anchor) {
        Log.d("Nek", "onCreateModel")
        onModelFoxExis = true
        ModelRenderable.builder()
            .setSource(this, Uri.parse("Fox.sfb"))
            .build()
            .thenAccept {
                addModelToScene(anchor, it)
            }
            .exceptionally {
                Log.e("Nek", "on model create error", it)
                return@exceptionally null
            }
    }

    private fun setupScence() {
        /*arScence.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            val anchor = hitResult.createAnchor()
        }*/
        arScence.arSceneView.scene.addOnUpdateListener(this)
    }

    private fun addModelToScene(anchor: Anchor, modelRenderable: ModelRenderable) {
        Log.d("Nek", "onAdd model")

        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arScence.arSceneView.scene)

        val transformableNode = TransformableNode(arScence.transformationSystem)
        transformableNode.setParent(anchorNode)
        transformableNode.renderable = modelRenderable
        transformableNode.scaleController.maxScale = 0.2f
        transformableNode.scaleController.minScale = 0.1f
        transformableNode.select()

        // anchorNode.renderable = modelRenderable

        //arScence.arSceneView.scene.addChild(transformableNode)
    }

    private suspend fun load() {
        if (checkArCore()) {
            GlobalScope.launch(Dispatchers.Main) {
                Toast.makeText(this@ArCoreActivity, "ar is supported", Toast.LENGTH_SHORT).show()
            }
            setupScence()
        } else {
            GlobalScope.launch(Dispatchers.Main) { Toast.makeText(this@ArCoreActivity, "ar is not supported", Toast.LENGTH_SHORT).show()}
        }
    }

    private suspend fun checkArCore(): Boolean {
        val arCoreApk = ArCoreApk.getInstance().checkAvailability(this)
        return if (arCoreApk.isTransient) {
            delay(200)
            checkArCore()
        } else arCoreApk.isSupported
    }

}
