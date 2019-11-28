package com.triare.mlkit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.triare.mlkit.components.VisionImageProcessor
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_textmlkit.setOnClickListener {
            startActivity(Intent(this, TatRecognizerActivity::class.java))
        }
        main_facemlkit.setOnClickListener {
            startActivity(Intent(this, FaceDetection2Activity::class.java))
        }
        main_arcore.setOnClickListener {
            startActivity(Intent(this, ArCoreActivity::class.java))
        }
        main_arcorecloud.setOnClickListener {
            startActivity(Intent(this, CloudArCoreActivity::class.java))
        }
        main_arcore_customshape.setOnClickListener {
            startActivity(Intent(this, ArCustomShapeActivity::class.java))
        }
        main_arcore_w_view.setOnClickListener {
            startActivity(Intent(this, ArWViewActivity::class.java))
        }
        main_arcore_w_animation.setOnClickListener {

        }

    }
}
