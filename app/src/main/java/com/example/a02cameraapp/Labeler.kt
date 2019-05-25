package com.example.a02cameraapp

import android.content.Context
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions

class Labeler(tts: TextToSpeech?) {
    val tts = tts
    fun recognizeObject(context: Context, photo: Bitmap?) {
        val image = FirebaseVisionImage.fromBitmap(photo!!)

        InternetCheck(object: InternetCheck.Consumer{
            override fun accept(isConnected: Boolean?) {
                // if the device is connected to the internet use cloud processing
                var labeler: FirebaseVisionImageLabeler
                if(isConnected!!)
                {
                    val options = FirebaseVisionCloudImageLabelerOptions.Builder()
                        .setConfidenceThreshold(0.7f)
                        .build()
                    labeler = FirebaseVision.getInstance().getCloudImageLabeler(options)
                    processImage(context, labeler, image)
                }
                // else use on device processing and suggest turning on the internet
                else{
                    displayShortToast(context, "To improve recognition turn on internet connection")
                    val options = FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                        .setConfidenceThreshold(0.7f)
                        .build()
                    labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler(options)
                    processImage(context, labeler, image)
                }
            }
        })
    }

    private fun processImage(context: Context, labeler: FirebaseVisionImageLabeler, image: FirebaseVisionImage) {
        labeler.processImage(image)
            .addOnSuccessListener { result ->
                chooseLabels(context, result)
            }
            .addOnFailureListener {
                displayLongToast(context, "No label found, try one more time")
            }
    }

    private fun chooseLabels(context: Context, result: List<FirebaseVisionImageLabel>) {
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
        displayLongToast(context, labels)
    }

    public fun displayLongToast(context: Context, label: String) {
        Toast.makeText(context, label, Toast.LENGTH_LONG).show()
        speakOut(label)
    }

    public fun displayShortToast(context: Context, label: String) {
        Toast.makeText(context, label, Toast.LENGTH_SHORT).show()
        speakOut(label)
    }

    private fun speakOut(text: String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }
}