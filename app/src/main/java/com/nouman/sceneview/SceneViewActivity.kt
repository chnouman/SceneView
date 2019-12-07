package com.nouman.sceneview

import android.content.DialogInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer
import com.google.ar.sceneform.ux.TransformationSystem
import com.nouman.sceneview.SceneViewActivity.Statics.EXTRA_MODEL_TYPE
import com.nouman.sceneview.nodes.DragTransformableNode
import kotlinx.android.synthetic.main.activity_scene_view.*
import java.lang.Exception
import java.util.concurrent.CompletionException

class SceneViewActivity : AppCompatActivity() {

    var remoteModelUrl =
        "https://poly.googleusercontent.com/downloads/0BnDT3T1wTE/85QOHCZOvov/Mesh_Beagle.gltf"

    var localModel = "model.sfb"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scene_view)

        val remoteModelUrl = intent.getStringExtra(EXTRA_MODEL_TYPE)
        if (remoteModelUrl.equals("remote")) {
            renderRemoteObject()
        } else {
            //load local model
            renderLocalObject()
        }
    }

    private fun renderRemoteObject() {

        skuProgressBar.setVisibility(View.VISIBLE)
        ModelRenderable.builder()
            .setSource(
                this, RenderableSource.Builder().setSource(
                    this,
                    Uri.parse(remoteModelUrl),
                    RenderableSource.SourceType.GLTF2
                ).setScale(0.01f)
                    .setRecenterMode(RenderableSource.RecenterMode.CENTER)
                    .build()
            )
            .setRegistryId(remoteModelUrl)
            .build()
            .thenAccept { modelRenderable: ModelRenderable ->
                skuProgressBar.setVisibility(View.GONE)
                addNodeToScene(modelRenderable)
            }
            .exceptionally { throwable: Throwable? ->
                var message: String?
                message = if (throwable is CompletionException) {
                    skuProgressBar.setVisibility(View.GONE)
                    "Internet is not working"
                } else {
                    skuProgressBar.setVisibility(View.GONE)
                    "Can't load Model"
                }
                val mainHandler = Handler(Looper.getMainLooper())
                val finalMessage: String = message
                val myRunnable = Runnable {
                    AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage(finalMessage + "")
                        .setPositiveButton("Retry") { dialogInterface: DialogInterface, _: Int ->
                            renderRemoteObject()
                            dialogInterface.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss() }
                        .show()
                }
                mainHandler.post(myRunnable)
                null
            }
    }

    private fun renderLocalObject() {

        skuProgressBar.setVisibility(View.VISIBLE)
        ModelRenderable.builder()
            .setSource(this, Uri.parse(localModel))
            .setRegistryId(localModel)
            .build()
            .thenAccept { modelRenderable: ModelRenderable ->
                skuProgressBar.setVisibility(View.GONE)
                addNodeToScene(modelRenderable)
            }
            .exceptionally { throwable: Throwable? ->
                var message: String?
                message = if (throwable is CompletionException) {
                    skuProgressBar.setVisibility(View.GONE)
                    "Internet is not working"
                } else {
                    skuProgressBar.setVisibility(View.GONE)
                    "Can't load Model"
                }
                val mainHandler = Handler(Looper.getMainLooper())
                val finalMessage: String = message
                val myRunnable = Runnable {
                    AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage(finalMessage + "")
                        .setPositiveButton("Retry") { dialogInterface: DialogInterface, _: Int ->
                            renderLocalObject()
                            dialogInterface.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss() }
                        .show()
                }
                mainHandler.post(myRunnable)
                null
            }
    }

    override fun onPause() {
        super.onPause()
        sceneView.pause()
    }

    private fun addNodeToScene(model: ModelRenderable) {
        if (sceneView != null) {
            val transformationSystem = makeTransformationSystem()
            var dragTransformableNode = DragTransformableNode(1f, transformationSystem)
            dragTransformableNode?.renderable = model
            sceneView.getScene().addChild(dragTransformableNode)
            dragTransformableNode?.select()
            sceneView.getScene()
                .addOnPeekTouchListener { hitTestResult: HitTestResult?, motionEvent: MotionEvent? ->
                    transformationSystem.onTouch(
                        hitTestResult,
                        motionEvent
                    )
                }
        }
    }

    private fun makeTransformationSystem(): TransformationSystem {
        val footprintSelectionVisualizer = FootprintSelectionVisualizer()
        return TransformationSystem(resources.displayMetrics, footprintSelectionVisualizer)
    }


    override fun onResume() {
        super.onResume()
        try {
            sceneView.resume()
        } catch (e: CameraNotAvailableException) {
            e.printStackTrace()
        }
    }

    object Statics {
        var EXTRA_MODEL_TYPE = "modelType"
    }

    override fun onDestroy() {
        super.onDestroy()
        try {

            sceneView.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
