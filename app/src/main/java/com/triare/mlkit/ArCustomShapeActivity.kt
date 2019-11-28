package com.triare.mlkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.triare.mlkit.components.MyArFragment

class ArCustomShapeActivity : AppCompatActivity() {

    private val arScene: MyArFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.arcustomshape_arscene) as MyArFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_custom_shape)

        arScene.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            val anchor =
                arScene.arSceneView.session?.hostCloudAnchor(hitResult.createAnchor())
            anchor?.let {
                arScene.createCubeModel(it)
            }
        }

    }
}
