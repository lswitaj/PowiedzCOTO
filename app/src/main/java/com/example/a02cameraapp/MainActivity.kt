package com.example.a02cameraapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.support.annotation.RequiresApi
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
import com.example.a02cameraapp.Labeler

open class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var labeler: Labeler? = null
    private var tts: TextToSpeech? = null
    val CAMERA_REQUEST_CODE = 0
    //val mainContext: Context = this.applicationContext

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tts = TextToSpeech(this, this)
        labeler = Labeler()

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
                    val label: String = labeler!!.recognizeObject(photo)
                    photoImageView.setImageBitmap(photo)
                    displayLongToast(label)
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

    fun displayLongToast(label: String) {
        Toast.makeText(this, label, Toast.LENGTH_LONG).show()
        speakOut(label)
    }

//    fun displayLongToast(label: Labeler) {
//        Toast.makeText(this, labeler!!.getLabel(), Toast.LENGTH_LONG).show()
//        speakOut(labeler!!.getLabel())
//    }

    fun displayShortToast(label: String) {
        Toast.makeText(this, label, Toast.LENGTH_SHORT).show()
        speakOut(label)
    }

//    fun displayShortToast(label: Labeler) {
//        Toast.makeText(this, labeler!!.getLabel(), Toast.LENGTH_SHORT).show()
//        speakOut(labeler!!.getLabel())
//    }

    private fun speakOut(text: String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    public override fun onDestroy() {
        // Shutdown TTS
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }
}