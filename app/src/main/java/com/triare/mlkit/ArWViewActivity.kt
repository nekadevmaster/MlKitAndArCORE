package com.triare.mlkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ViewRenderable
import com.triare.mlkit.components.MyArFragment

class ArWViewActivity : AppCompatActivity() {

    private val arScene: MyArFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.arcustomview_ar_scene) as MyArFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_wview)

        arScene.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            createViewArModel(hitResult.createAnchor())
        }

    }

    private fun createViewArModel(anchor: Anchor) {
        ViewRenderable
            .builder()
            .setView(this, R.layout.ar_cusom_view_model)
            .build()
            .thenAccept { addViewModelToScene(it, anchor) }
    }

    private fun addViewModelToScene(viewRenderable: ViewRenderable, anchor: Anchor) {
        val anchorNode = AnchorNode(anchor)
        anchorNode.renderable = viewRenderable
        arScene.arSceneView.scene.addChild(anchorNode)

        val rootView = viewRenderable.view

        val showImageBtn = rootView.findViewById<Button>(R.id.arcustomviewmodel_btn)
        val image = rootView.findViewById<ImageView>(R.id.arcustomviewmodel_image)

        showImageBtn.setOnClickListener {
            image.visibility = View.VISIBLE
        }

    }

}
