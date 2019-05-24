package com.example.a02cameraapp

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val CAMERA_REQUEST_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraButton.setOnClickListener {
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if(callCameraIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            CAMERA_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data!= null){
                    val photo = data.extras.get("data") as Bitmap
                    displayToastWithLabel(photo)
                    photoImageView.setImageBitmap(photo)
                }
            }
            else -> {
                Toast.makeText(this, "Unrecognized request code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayToastWithLabel(photo: Bitmap) {
        val label = "Lukasz Switaj w okularach"
        recognizeObject(photo)

        Toast.makeText(this@MainActivity, label, Toast.LENGTH_LONG).show()

        // https://developer.android.com/guide/topics/ui/notifiers/toasts
    }

    private fun recognizeObject(photo: Bitmap): String {
        // https://firebase.google.com/docs/ml-kit/android/label-images
        val image = FirebaseVisionImage.fromBitmap(photo)
//        val options = FirebaseVisionOnDeviceImageLabelerOptions.Builder()
//            .setConfidenceThreshold(0.8f)
//            .build()
//        val labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler(options)
//        val labeler = FirebaseVision.getInstance().onDeviceImageLabeler
//        val labeler = FirebaseVision.getInstance().onDeviceImageLabeler
//        val labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler()
            val labeler = FirebaseVision.getInstance().cloudTextRecognizer


        // TODO if connected with the internet use .getCloudImageLabeler() instead of getOnDeviceImageLabeler()
        // TODO - add options https://medium.com/androidiots/firebase-ml-kit-101-image-labeling-8078784205cb

        var result:String = "ok"

//        labeler.processImage(image)
//            .addOnSuccessListener { labels ->
//                for (label in labels) {
//                    val text = label.text
//                    val confidence = label.confidence
//                    result = result + text + confidence.toString()
//                }
//            }
//            .addOnFailureListener {
//                result = "No label found, try one more time"
//            }

        return result
    }
}
