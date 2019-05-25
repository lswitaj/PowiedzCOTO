package com.example.a02cameraapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    val CAMERA_REQUEST_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tts = TextToSpeech(this, this)

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
                    recognizeObject(photo)
                    photoImageView.setImageBitmap(photo)
                }
            }
            else -> {
                displayLongToast("Unrecognized request code")
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language specified is not supported!")
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    private fun recognizeObject(photo: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(photo)

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
                    processImage(labeler, image)
                }
                // else use on device processing and suggest turning on the internet
                else{
                    displayShortToast("To improve recognition turn on internet connection")
                    Thread.sleep(2500)
                    val options = FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                        .setConfidenceThreshold(0.7f)
                        .build()
                    labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler(options)
                    processImage(labeler, image)
                }
            }
        })
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
        displayLongToast(labels)
    }

    private fun displayLongToast(label: String) {
        Toast.makeText(this, label, Toast.LENGTH_LONG).show()
        speakOut(label)
    }

    private fun displayShortToast(label: String) {
        Toast.makeText(this, label, Toast.LENGTH_SHORT).show()
        speakOut(label)
    }

    private fun speakOut(text: String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
    }
}