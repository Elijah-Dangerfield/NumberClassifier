package com.dangerfield.numberclassifier

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.Interpreter;
import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

var modelFile = "model.tflite"
var tflite: Interpreter? = null


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        paintView.init(metrics)
        //try to load the model into tflite
        try {
            tflite = Interpreter(loadModelFile(this@MainActivity, modelFile))
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun clearPaintView(view: View){
        paintView.clear()
    }

     fun onDetectClicked(view: View) {
        //use the model to make prediction
    }


}

@Throws(IOException::class)
private fun loadModelFile(activity: Activity, MODEL_FILE: String): MappedByteBuffer {
    val fileDescriptor = activity.assets.openFd(MODEL_FILE)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.getChannel()
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
}
