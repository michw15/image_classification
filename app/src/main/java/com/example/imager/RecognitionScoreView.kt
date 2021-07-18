/* Copyright 2015 The TensorFlow Authors. All Rights Reserved.

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

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat

class RecognitionScoreView(context: Context?, set: AttributeSet?) : View(context, set),
    ResultsView {
    private var results: List<Classifier.Recognition>? = null
    private val textSizePx: Float
    private val fgPaint: Paint
    private val bgPaint: Paint
    override fun setResults(results: List<Classifier.Recognition?>?) {
        this.results = results as List<Classifier.Recognition>?
        postInvalidate()
    }

    public override fun onDraw(canvas: Canvas) {
        val x = 10
        var y = (fgPaint.textSize * 1.5f).toInt()
        canvas.drawPaint(bgPaint)
        if (results != null) {
            for (recog in results!!) {
                canvas.drawText(
                    recog.title.toString() + ": " + (recog.confidence?.times(100) ?: 0) + "%",
                    x.toFloat(),
                    y.toFloat(),
                    fgPaint
                )
                y += (fgPaint.textSize * 1.5f).toInt()
            }
        }
    }

    companion object {
        private const val TEXT_SIZE_DIP = 24f
    }

    init {
        textSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, resources.displayMetrics
        )
        fgPaint = Paint()
        fgPaint.textSize = textSizePx
        val color: Int = ContextCompat.getColor(context!!, R.color.database_text)
        fgPaint.color = color
        bgPaint = Paint()
        bgPaint.color = ContextCompat.getColor(context, R.color.database_background)
    }
}