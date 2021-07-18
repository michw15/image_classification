package com.example.imager
import android.graphics.*
import android.media.ImageReader
import android.os.SystemClock
import android.util.Size
import android.util.TypedValue
import android.view.Display
import android.view.View
import com.example.imager.env.BorderedText
import com.example.imager.env.ImageUtils
import com.example.imager.env.Logger
import java.util.*

open class ClassifierActivity(override val desiredPreviewFrameSize: Size?) : CameraActivity(), ImageReader.OnImageAvailableListener {
    private var resultsView: ResultsView? = null
    private var rgbFrameBitmap: Bitmap? = null
    private var croppedBitmap: Bitmap? = null
    private var cropCopyBitmap: Bitmap? = null
    private var lastProcessingTimeMs: Long = 0
    private var sensorOrientation: Int? = null
    private var classifier: Classifier? = null
    private var frameToCropTransform: Matrix? = null
    private var cropToFrameTransform: Matrix? = null
    private var borderedText: BorderedText? = null
    override val layoutId: Int
        get() = R.layout.camera_connection_fragment

    override fun onPreviewSizeChosen(size: Size?, rotation: Int) {
        val textSizePx: Float = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, resources.displayMetrics
        )
        borderedText = BorderedText(textSizePx)
        borderedText!!.setTypeface(Typeface.MONOSPACE)
        classifier = TensorFlowImageClassifier.create(
            assets,
            MODEL_FILE,
            LABEL_FILE,
            INPUT_SIZE,
            IMAGE_MEAN,
            IMAGE_STD,
            INPUT_NAME,
            OUTPUT_NAME
        )
        previewWidth = size!!.width
        previewHeight = size.height
        val display: Display = windowManager.defaultDisplay
        val screenOrientation: Int = display.getRotation()
        LOGGER.i("Sensor orientation: %d, Screen orientation: %d", rotation, screenOrientation)
        sensorOrientation = rotation + screenOrientation
        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight)
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
        croppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888)
        frameToCropTransform = ImageUtils.getTransformationMatrix(
            previewWidth, previewHeight,
            INPUT_SIZE, INPUT_SIZE,
            sensorOrientation!!, MAINTAIN_ASPECT
        )
        cropToFrameTransform = Matrix()
        frameToCropTransform!!.invert(cropToFrameTransform)

    }

    override fun processImage() {
        rgbFrameBitmap!!.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight)
        val canvas = Canvas(croppedBitmap!!)
        canvas.drawBitmap(rgbFrameBitmap!!, frameToCropTransform!!, null)

        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap!!)
        }
        runInBackground(
            Runnable {
                val startTime = SystemClock.uptimeMillis()
                val results: List<Classifier.Recognition?>? = classifier!!.recognizeImage(croppedBitmap)
                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime
                LOGGER.i("Detect: %s", results)
                cropCopyBitmap = Bitmap.createBitmap(croppedBitmap!!)
                if (resultsView == null) {
                    resultsView = findViewById<View>(R.id.results) as ResultsView
                }
                resultsView!!.setResults(results)
                requestRender()
                readyForNextImage()
            })
    }

    override fun onSetDebug(debug: Boolean) {
        classifier?.enableStatLogging(debug)
    }

    private fun renderDebug(canvas: Canvas) {
        if (!isDebug) {
            return
        }
        val copy: Bitmap? = cropCopyBitmap
        if (copy != null) {
            val matrix = Matrix()
            val scaleFactor = 2f
            matrix.postScale(scaleFactor, scaleFactor)
            matrix.postTranslate(
                canvas.width - copy.getWidth() * scaleFactor,
                canvas.height - copy.getHeight() * scaleFactor
            )
            canvas.drawBitmap(copy, matrix, Paint())
            val lines = Vector<String>()
            if (classifier != null) {
                val statString: String? = classifier!!.statString
                val statLines = statString?.split("\n")?.toTypedArray()
                if (statLines != null) {
                    for (line in statLines) {
                        lines.add(line)
                    }
                }
            }
            lines.add("Frame: " + previewWidth.toString() + "x" + previewHeight)
            lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight())
            lines.add("View: " + canvas.width + "x" + canvas.height)
            lines.add("Rotation: $sensorOrientation")
            lines.add("Inference time: " + lastProcessingTimeMs + "ms")
            borderedText?.drawLines(canvas, 10F, canvas.height - 10, lines)
        }
    }

    companion object {
        private val LOGGER: Logger = Logger()
        protected const val SAVE_PREVIEW_BITMAP = false

        // These are the settings for the original v1 Inception model. If you want to
        // use a model that's been produced from the TensorFlow for Poets codelab,
        // you'll need to set IMAGE_SIZE = 299, IMAGE_MEAN = 128, IMAGE_STD = 128,
        // INPUT_NAME = "Mul", and OUTPUT_NAME = "final_result".
        // You'll also need to update the MODEL_FILE and LABEL_FILE paths to point to
        // the ones you produced.
        //
        // To use v3 Inception model, strip the DecodeJpeg Op from your retrained
        // model first:
        //
        // python strip_unused.py \
        // --input_graph=<retrained-pb-file> \
        // --output_graph=<your-stripped-pb-file> \
        // --input_node_names="Mul" \
        // --output_node_names="final_result" \
        // --input_binary=true
        private const val INPUT_SIZE = 299
        private const val IMAGE_MEAN = 128
        private const val IMAGE_STD = 128f
        private const val INPUT_NAME = "Mul"
        private const val OUTPUT_NAME = "final_result"
        private const val MODEL_FILE = "file:///android_asset/stripped_graph.pb"
        private const val LABEL_FILE = "file:///android_asset/retrained_labels.txt"
        private const val MAINTAIN_ASPECT = true
        private const val TEXT_SIZE_DIP = 10f
    }
}