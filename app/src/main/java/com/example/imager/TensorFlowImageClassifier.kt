/* Copyright 2016 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/
package com.example.imager

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.Trace
import android.util.Log
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import org.tensorflow.Operation
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

/** A classifier specialized to label images using TensorFlow.  */
class TensorFlowImageClassifier private constructor() : Classifier {
    // Config values.
    private var inputName: String? = null
    private var outputName: String? = null
    private var inputSize = 0L
    private var imageMean = 0L
    private var imageStd = 0f

    // Pre-allocated buffers.
    private val labels = Vector<String>()
    private lateinit var intValues: IntArray
    private lateinit var floatValues: FloatArray
    private lateinit var outputs: FloatArray
    private lateinit var outputNames: Array<String?>
    private var logStats = false
    private var inferenceInterface: TensorFlowInferenceInterface? = null
    override fun recognizeImage(bitmap: Bitmap?): List<Classifier.Recognition?>? {
        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage")
        Trace.beginSection("preprocessBitmap")
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap!!.getPixels(
            intValues,
            0,
            bitmap.getWidth(),
            0,
            0,
            bitmap.getWidth(),
            bitmap.getHeight()
        )
        for (i in intValues.indices) {
            val `val` = intValues[i]
            floatValues[i * 3 + 0] = ((`val` shr 16 and 0xFF) - imageMean) / imageStd
            floatValues[i * 3 + 1] = ((`val` shr 8 and 0xFF) - imageMean) / imageStd
            floatValues[i * 3 + 2] = ((`val` and 0xFF) - imageMean) / imageStd
        }
        Trace.endSection()

        // Copy the input data into TensorFlow.
        Trace.beginSection("feed")
        inferenceInterface?.feed(inputName, floatValues, 1, inputSize, inputSize, 3)
        Trace.endSection()

        // Run the inference call.
        Trace.beginSection("run")
        inferenceInterface!!.run(outputNames, logStats)
        Trace.endSection()

        // Copy the output Tensor back into the output array.
        Trace.beginSection("fetch")
        inferenceInterface!!.fetch(outputName, outputs)
        Trace.endSection()

        // Find the best classifications.
        val pq: PriorityQueue<Classifier.Recognition> = PriorityQueue(
            3
        ) { lhs, rhs -> // Intentionally reversed to put high confidence at the head of the queue.
            java.lang.Float.compare(rhs.confidence!!, lhs.confidence!!)
        }
        for (i in outputs.indices) {
            if (outputs[i] > THRESHOLD) {
                pq.add(
                    Classifier.Recognition(
                        "" + i, if (labels.size > i) labels[i] else "unknown", outputs[i], null
                    )
                )
            }
        }
        val recognitions: ArrayList<Classifier.Recognition?> = ArrayList<Classifier.Recognition?>()
        val recognitionsSize = Math.min(pq.size, MAX_RESULTS)
        for (i in 0 until recognitionsSize) {
            recognitions.add(pq.poll())
        }
        Trace.endSection() // "recognizeImage"
        return recognitions
    }

    override fun enableStatLogging(logStats: Boolean) {
        this.logStats = logStats
    }

    override val statString: String
        get() = inferenceInterface!!.statString

    override fun close() {
        inferenceInterface!!.close()
    }

    companion object {
        private const val TAG = "TensorFlowImageClassifier"

        // Only return this many results with at least this confidence.
        private const val MAX_RESULTS = 3
        private const val THRESHOLD = 0.1f

        /**
         * Initializes a native TensorFlow session for classifying images.
         *
         * @param assetManager The asset manager to be used to load assets.
         * @param modelFilename The filepath of the model GraphDef protocol buffer.
         * @param labelFilename The filepath of label file for classes.
         * @param inputSize The input size. A square image of inputSize x inputSize is assumed.
         * @param imageMean The assumed mean of the image values.
         * @param imageStd The assumed std of the image values.
         * @param inputName The label of the image input node.
         * @param outputName The label of the output node.
         * @throws IOException
         */
        fun create(
            assetManager: AssetManager,
            modelFilename: String?,
            labelFilename: String,
            inputSize: Int,
            imageMean: Int,
            imageStd: Float,
            inputName: String?,
            outputName: String?
        ): Classifier {
        val c = TensorFlowImageClassifier()
            c.inputName = inputName
            c.outputName = outputName

            // Read the label names into memory.
            val actualFilename = labelFilename.split("file:///android_asset/").toTypedArray()[1]
            Log.i(TAG, "Reading labels from: $actualFilename")
            var br: BufferedReader? = null
            try {
                br = BufferedReader(InputStreamReader(assetManager.open(actualFilename)))
                var line: String
                while (br.readLine().also { line = it } != null) {
                    c.labels.add(line)
                }
                br.close()
            } catch (e: IOException) {
                throw RuntimeException("Problem reading label file!", e)
            }
            c.inferenceInterface = TensorFlowInferenceInterface(assetManager, modelFilename)

            // The shape of the output is [N, NUM_CLASSES], where N is the batch size.
            val operation: Operation = c.inferenceInterface!!.graphOperation(outputName)
            val numClasses : Long = operation.output<Int>(0).shape().size(1)
            Log.i(TAG, "Read " + c.labels.size + " labels, output layer size is " + numClasses)

            // Ideally, inputSize could have been retrieved from the shape of the input operation.  Alas,
            // the placeholder node for input in the graphdef typically used does not specify a shape, so it
            // must be passed in as a parameter.
            c.inputSize = inputSize.toLong()
            c.imageMean = imageMean.toLong()
            c.imageStd = imageStd

            // Pre-allocate buffers.
            c.outputNames = arrayOf(outputName)
            c.intValues = IntArray(inputSize * inputSize)
            c.floatValues = FloatArray(inputSize * inputSize * 3)
            c.outputs = FloatArray(numClasses.toInt())
            return c
        }
    }
}