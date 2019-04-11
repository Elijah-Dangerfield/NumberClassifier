package com.dangerfield.numberclassifier

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.Interpreter;
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.util.Log
import com.dangerfield.numberclassifier.ml.DigitsDetector
import kotlinx.android.synthetic.main.activity_main.view.*
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

var modelFile = "model.tflite"
var tflite: Interpreter? = null


class MainActivity : AppCompatActivity() {

    private val PIXEL_WIDTH = 28
    private val TAG = "Main Activity"
    private var mnistClassifier: DigitsDetector? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val metrics = DisplayMetrics()
        mnistClassifier = DigitsDetector(this)
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
         val scaledBitmap = Bitmap.createScaledBitmap(paintView.bitmap, PIXEL_WIDTH, PIXEL_WIDTH, false)

         val digit = mnistClassifier?.classify(scaledBitmap)
         if (digit != null) {
             if (digit >= 0) {
                 Log.d(TAG, "Found Digit = $digit")
                 tv_result.text = digit.toString()
             } else {
                 tv_result.text = "Digit not found"
             }
         }else {
             tv_result.text = "Digit not even detected bitch"
         }
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
