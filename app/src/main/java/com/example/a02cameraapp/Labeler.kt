package com.example.a02cameraapp

import android.content.Context
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions

class Labeler: MainActivity() {
    private var label: String = ""
    var waiting: Boolean = true

    fun recognizeObject(photo: Bitmap?): String {
        val image = FirebaseVisionImage.fromBitmap(photo!!)
//        InternetCheck(object: InternetCheck.Consumer{
//            override fun accept(isConnected: Boolean?) {
//                // if the device is connected to the internet use cloud processing
//                val labeler: FirebaseVisionImageLabeler
//                if(isConnected!!)
//                {
//                    val options = FirebaseVisionCloudImageLabelerOptions.Builder()
//                        .setConfidenceThreshold(0.7f)
//                        .build()
//                    labeler = FirebaseVision.getInstance().getCloudImageLabeler(options)
//                    processImage(labeler, image)
//                }
//                // else use on device processing and suggest turning on the internet
//                else{
//                    displayShortToast("To improve recognition turn on internet connection")
//                    val options = FirebaseVisionOnDeviceImageLabelerOptions.Builder()
//                        .setConfidenceThreshold(0.7f)
//                        .build()
//                    labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler(options)
//                    processImage(labeler, image)
//                }
//            }
//        })
        val labeler: FirebaseVisionImageLabeler
        val options = FirebaseVisionCloudImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()
        labeler = FirebaseVision.getInstance().getCloudImageLabeler(options)
        processImage(labeler, image)
        return getLabel()
    }

    private fun processImage(labeler: FirebaseVisionImageLabeler, image: FirebaseVisionImage) {
         labeler.processImage(image)
            .addOnSuccessListener { result ->
                chooseLabels(result)
            }
            .addOnFailureListener {
                displayLongToast("No label found, try one more time")
            }
    }

    private fun chooseLabels(result: List<FirebaseVisionImageLabel>) {
        var labels:String

        if(result[0].confidence > 0.95f){
            labels = "I'm sure it's " + result[0].text
        }
        else{
            labels = "It may be "
            var i = 0
            for (label in result) {
                val text = label.text
                labels = labels + "\n" + text
                if(++i >= 3)
                    break
            }
        }
        setLabel(labels)
    }

    private fun getLabel(): String{
        while(waiting)
            Thread.sleep(1000)
        return label
    }

    private fun setLabel(text: String){
        waiting = false
        label = text
    }
}