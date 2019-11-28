package com.triare.mlkit.components

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.triare.mlkit.R

class MyArFragment : ArFragment() {

    override fun getSessionConfiguration(session: Session?): Config {

        val config = Config(session)
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
        config.focusMode = Config.FocusMode.AUTO
        session?.configure(config)

        this.arSceneView.setupSession(session)

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.fox)
        val imageDatabase = AugmentedImageDatabase(session)
        imageDatabase.addImage("fox", bitmap)
        config.augmentedImageDatabase = imageDatabase

        return config
    }

    fun createFoxModel(anchor: Anchor) {
        Log.d("Nek", "onCreateModel")
        ModelRenderable.builder()
            .setSource(this.context, Uri.parse("Fox.sfb"))
            .build()
            .thenAccept {
                addModelToScene(anchor, it)
            }
            .exceptionally {
                Log.e("Nek", "on model create error", it)
                return@exceptionally null
            }
    }

    fun createCubeModel(anchor: Anchor) {
        MaterialFactory
            .makeOpaqueWithColor(this.context, Color(android.graphics.Color.RED))
            .thenAccept {
                val cube =
                    ShapeFactory.makeCube(Vector3(0.10f, 0.10f, 0.10f), Vector3(0f, 0.10f, 0f), it)
                addModelToScene(anchor, cube)
            }
    }

    fun addModelToScene(anchor: Anchor, modelRenderable: ModelRenderable) {
        Log.d("Nek", "onAdd model")

        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(this.arSceneView.scene)

        val transformableNode = TransformableNode(this.transformationSystem)
        transformableNode.setParent(anchorNode)
        transformableNode.renderable = modelRenderable
        transformableNode.scaleController.maxScale = 0.2f
        transformableNode.scaleController.minScale = 0.1f
        transformableNode.select()

        // anchorNode.renderable = modelRenderable

        //arScence.arSceneView.scene.addChild(transformableNode)
    }

}