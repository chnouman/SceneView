package com.nouman.sceneview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.ar.sceneform.SceneView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    fun onLocalModelClick(view: View) {
        startActivity(
            Intent(
                this,
                SceneViewActivity::class.java
            ).putExtra(SceneViewActivity.Statics.EXTRA_MODEL_TYPE, "local")
        )

    }

    fun onRemoteModelClick(view: View) {
        startActivity(
            Intent(
                this,
                SceneViewActivity::class.java
            ).putExtra(SceneViewActivity.Statics.EXTRA_MODEL_TYPE, "remote")
        )
    }
}
