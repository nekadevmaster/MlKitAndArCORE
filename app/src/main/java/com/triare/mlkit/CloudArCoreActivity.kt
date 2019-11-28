package com.triare.mlkit

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.TransformableNode
import com.triare.mlkit.components.MyArFragment
import kotlinx.android.synthetic.main.activity_cloud_ar_core.*

class CloudArCoreActivity : AppCompatActivity() {

    private val cloudar_arscene: MyArFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.cloudar_arscene) as MyArFragment
    }

    private var anchor: Anchor? = null

    private val prefs: SharedPreferences by lazy {
        getSharedPreferences("anchorCloud", Context.MODE_PRIVATE)
    }

    private val editPrefs: SharedPreferences.Editor by lazy {
        prefs.edit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_ar_core)

        cloudar_arscene.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            if (!onModelFoxExis) {
                anchor =
                    cloudar_arscene.arSceneView.session?.hostCloudAnchor(hitResult.createAnchor())
                anchor?.let {
                    createModel(it)
                }
            }
        }

        cloudar_arscene.arSceneView.scene.addOnUpdateListener {
            if (!onModelFoxExis) {
                return@addOnUpdateListener
            }

            val cloudAnchorState = anchor!!.cloudAnchorState

            if (cloudAnchorState.isError) {
                Toast.makeText(this, cloudAnchorState.toString(), Toast.LENGTH_SHORT).show()
            }
            else if (cloudAnchorState == Anchor.CloudAnchorState.SUCCESS) {
                Toast.makeText(this, "anchor id created", Toast.LENGTH_SHORT).show()
                editPrefs.putString("anchorID", anchor!!.cloudAnchorId)
                editPrefs.apply()
            }

        }

        cloudar_button.setOnClickListener {
            prefs.getString("anchorID", null).let {
                if (it.isNotEmptyReal()) {
                    cloudar_arscene.arSceneView.session?.resolveCloudAnchor(it)?.let { anchor ->
                        this.anchor = anchor
                        createModel(anchor)
                    }
                }
            }
        }

    }

    private var onModelFoxExis = false

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

    private fun addModelToScene(anchor: Anchor, modelRenderable: ModelRenderable) {
        Log.d("Nek", "onAdd model")

        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(cloudar_arscene.arSceneView.scene)

        val transformableNode = TransformableNode(cloudar_arscene.transformationSystem)
        transformableNode.setParent(anchorNode)
        transformableNode.renderable = modelRenderable
        transformableNode.scaleController.maxScale = 0.2f
        transformableNode.scaleController.minScale = 0.1f
        transformableNode.select()

        // anchorNode.renderable = modelRenderable

        //arScence.arSceneView.scene.addChild(transformableNode)
    }

    private fun String?.isNotEmptyReal(): Boolean {
        return when {
            isNullOrEmpty() -> false
            this == "null" -> false
            else -> true
        }
    }


}
