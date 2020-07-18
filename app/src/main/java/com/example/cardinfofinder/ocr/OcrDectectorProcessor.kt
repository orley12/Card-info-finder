package com.example.cardinfofinder.ocr

/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.SparseArray

import com.example.cardinfofinder.camera.GraphicOverlay
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import java.util.regex.Pattern.matches

/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
class OcrDetectorProcessor internal constructor(

    private val graphicOverlay: GraphicOverlay<OcrGraphic>
)
    : Detector.Processor<TextBlock> {

    var ocrText: String? = null

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    override fun receiveDetections(detections: Detector.Detections<TextBlock>?) {
        graphicOverlay.clear()
        val items = detections!!.detectedItems
        for (i in 0 until items.size()) {
            val item: TextBlock = items.valueAt(i)
            if (item != null && item.value != null) {
                if(item.value.toIntOrNull() is Int) {
                    ocrText = item.value
                    Log.d("OcrDetectorProcessor", "Text detected! " + item.value)
                    val graphic = OcrGraphic(graphicOverlay, item)
                    graphicOverlay.add(graphic)
                    if(item.value.replace(" ", "").matches(Regex("^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})\$"))){
                        val accurateCarNumber: String = item.value
                        ocrText = accurateCarNumber
                    }
                }
            }
        }
    }

    private fun gotoMainActivity(activity: Activity, cardNumber: String) {
//        val cardNumber = ocrDetectorProcessor?.ocrText
        //                Log.d("onOptionsItemSelected", cardNumber)
        var ocrTextResultIntent = Intent()
        ocrTextResultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ocrTextResultIntent.putExtra("cardNumber", cardNumber)

        activity.setResult(Activity.RESULT_OK, ocrTextResultIntent)
        activity.finish()
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    override fun release() {
        graphicOverlay.clear()
    }
}
