package com.dangerfield.numberclassifier.ml

import android.app.Activity
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class DigitsDetector(){

    // The tensorflow lite file
    private var tflite: Interpreter? = null

    // Name of the file in the assets folder
    private val MODEL_PATH = "model.tflite"



    // Specify the output size
    private val NUMBER_LENGTH = 10

    // Specify the input size
    private val DIM_BATCH_SIZE = 1
    private val DIM_IMG_SIZE_X = 28
    private val DIM_IMG_SIZE_Y = 28
    private val DIM_PIXEL_SIZE = 1

    // Number of bytes to hold a float (32 bits / float) / (8 bits / byte) = 4 bytes / float
    private val BYTE_SIZE_OF_FLOAT = 4

    val mnistOutput = Array(DIM_BATCH_SIZE) { FloatArray(NUMBER_LENGTH) }



    fun classify(bitmap: Bitmap): Int {
        if (tflite == null) {
            Log.e(TAG, "Image classifier has not been initialized; Skipped.")
        }
        val inputBuffer = ByteBuffer.allocateDirect(
            BYTE_SIZE_OF_FLOAT * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE
        )
        inputBuffer.order(ByteOrder.nativeOrder())
        preprocess(bitmap,inputBuffer)

        return postprocess()
    }


    constructor(activity: Activity) : this() {
        try {
            tflite = Interpreter(loadModelFile(activity))
            val inputBuffer = ByteBuffer.allocateDirect(
                BYTE_SIZE_OF_FLOAT * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE
            )
            inputBuffer.order(ByteOrder.nativeOrder())

        } catch (e: IOException) {
            Log.e(TAG, "IOException loading the tflite file")
        }

    }

    /**
     * Load the model file from the assets folder
     */
    @Throws(IOException::class)
    private fun loadModelFile(activity: Activity): MappedByteBuffer {
        val fileDescriptor = activity.assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun preprocess(bitmap: Bitmap?, inputBuffer: ByteBuffer) {
        if (bitmap == null ) {
            return
        }

        // Reset the image data
        inputBuffer.rewind()

        val width = bitmap.width
        val height = bitmap.height

        val startTime = SystemClock.uptimeMillis()

        // The bitmap shape should be 28 x 28
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            // Set 0 for white and 255 for black pixels
            val pixel = pixels[i]
            // The color of the input is black so the blue channel will be 0xFF.
            val channel = pixel and 0xff
            inputBuffer.putFloat((0xff - channel).toFloat())
        }
        val endTime = SystemClock.uptimeMillis()
        Log.d(TAG, "Time cost to put values into ByteBuffer: " + java.lang.Long.toString(endTime - startTime))

        runInference(inputBuffer,mnistOutput)
    }

    protected fun runInference(inputBuffer: ByteBuffer,mnistOutput:Array<FloatArray>) {
        tflite?.run(inputBuffer, mnistOutput)
    }

    private fun postprocess(): Int {
        for (i in 0 until mnistOutput[0].size) {
            val value = mnistOutput[0][i]
            Log.d(TAG, "Output for " + Integer.toString(i) + ": " + java.lang.Float.toString(value))
            if (value == 1f) {
                return i
            }
        }
        return -1
    }


}