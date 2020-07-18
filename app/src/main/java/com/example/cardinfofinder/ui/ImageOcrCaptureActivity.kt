package com.example.cardinfofinder.ui

/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.content.ContentValues
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.cardinfofinder.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImage.fromFilePath
import kotlinx.android.synthetic.main.activity_capture_ocr_image.*

class ImageOcrCaptureActivity : AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback {

    companion object {
        const val OCR_PERMISSIONS_REQUEST: Int = 102
        const val OCR_REQUEST_IMAGE_CAPTURE = 1
    }

    private lateinit var outputFileUri: Uri

    private lateinit var fab: FloatingActionButton

    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture_ocr_image)

        fab = findViewById(R.id.capture_image_fab)

        imageView = findViewById(R.id.image_view)

        fab.setOnClickListener {
            val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePhotoIntent.resolveActivity(packageManager) != null) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.TITLE, "card_info_finder")
                outputFileUri = contentResolver
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!

                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
                startActivityForResult(takePhotoIntent, OCR_REQUEST_IMAGE_CAPTURE)
            }
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED) {

            fab.isEnabled = false
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                OCR_PERMISSIONS_REQUEST
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == OCR_REQUEST_IMAGE_CAPTURE &&
            resultCode == Activity.RESULT_OK) {

            val image = getCapturedImage()

            // display capture image
            imageView.setImageBitmap(image)

            // run through OCR and display result
            runObjectDetection(image)
        }
    }

    /**
     * MLKit Object Detection Function
     */
    private fun runObjectDetection(bitmap: Bitmap) {
        FirebaseApp.initializeApp(this)
        // Step 1: create MLKit's VisionImage object
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        // Step 2: acquire detector object
        val TextRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer

        // Step 3: feed given image to detector and setup callback
        TextRecognizer.processImage(image)
            .addOnSuccessListener {
                // Task completed successfully
                // Post-detection processing : draw result
//                val drawingView = DrawingView(applicationContext, it)
//                drawingView.draw(Canvas(bitmap))
                bottom.visibility = View.VISIBLE
                fab.visibility = View.GONE

                Log.e("TAG", it.text)
                val words = it.text.split("\n")
                for (word in words) {
                    Log.e("TAG", word)
                    //REGEX for detecting a credit card
                    if (word.replace(" ", "").matches(Regex("^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})\$")))
                        tvCardNumber.text = word
                    //Find a better way to do this
                    if (word.contains("/")) {
                        for (year in word.split(" ")) {
                            if (year.contains("/"))
                                tvCardExpiry.text = year
                        }
                    }
                }
//                runOnUiThread {
//                }

            }
            .addOnFailureListener {
                // Task failed with an exception
                Toast.makeText(
                    baseContext, "Oops, something went wrong!",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * getCapturedImage():
     *     Decodes and center crops the captured image from camera.
     */
    private fun getCapturedImage(): Bitmap {

        var srcImage = FirebaseVisionImage.fromFilePath(baseContext, outputFileUri).bitmap

        // crop image to match imageView's aspect ratio
        val scaleFactor = Math.min(
            srcImage.width / imageView.width.toFloat(),
            srcImage.height / imageView.height.toFloat()
        )

        val deltaWidth = (srcImage.width - imageView.width * scaleFactor).toInt()
        val deltaHeight = (srcImage.height - imageView.height * scaleFactor).toInt()

        val scaledImage = Bitmap.createBitmap(
            srcImage, deltaWidth / 2, deltaHeight / 2,
            srcImage.width - deltaWidth, srcImage.height - deltaHeight
        )
        srcImage.recycle()
        return scaledImage

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            OCR_PERMISSIONS_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    fab.isEnabled = true
                }
            }
        }
    }
}

///**
// * DrawingView class:
// *    onDraw() function implements drawing
// *     - boundingBox
// *     - Category
// *     - Confidence ( if Category is not CATEGORY_UNKNOWN )
// */
//class DrawingView(context: Context, var visionObjects: List<FirebaseVisionObject>) : View(context) {
//
//    companion object {
//        // mapping table for category to strings: drawing strings
//        val categoryNames: Map<Int, String> = mapOf(
//            FirebaseVisionObject.CATEGORY_UNKNOWN to "Unknown",
//            FirebaseVisionObject.CATEGORY_HOME_GOOD to "Home Goods",
//            FirebaseVisionObject.CATEGORY_FASHION_GOOD to "Fashion Goods",
//            FirebaseVisionObject.CATEGORY_FOOD to "Food",
//            FirebaseVisionObject.CATEGORY_PLACE to "Place",
//            FirebaseVisionObject.CATEGORY_PLANT to "Plant"
//        )
//    }
//
//    val MAX_FONT_SIZE = 96F
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//        val pen = Paint()
//        pen.textAlign = Paint.Align.LEFT
//
//        for (item in visionObjects) {
//            // draw bounding box
//            pen.color = Color.RED
//            pen.strokeWidth = 8F
//            pen.style = Paint.Style.STROKE
//            val box = item.getBoundingBox()
//            canvas.drawRect(box, pen)
//
//            // Draw result category, and confidence
//            val tags: MutableList<String> = mutableListOf()
//            tags.add("Category: ${categoryNames[item.classificationCategory]}")
//            if (item.classificationCategory != FirebaseVisionObject.CATEGORY_UNKNOWN) {
//                tags.add("Confidence: ${item.classificationConfidence!!.times(100).toInt()}%")
//            }
//
//            var tagSize = Rect(0, 0, 0, 0)
//            var maxLen = 0
//            var index: Int = -1
//
//            for ((idx, tag) in tags.withIndex()) {
//                if (maxLen < tag.length) {
//                    maxLen = tag.length
//                    index = idx
//                }
//            }
//
//            // calculate the right font size
//            pen.style = Paint.Style.FILL_AND_STROKE
//            pen.color = Color.YELLOW
//            pen.strokeWidth = 2F
//
//            pen.textSize = MAX_FONT_SIZE
//            pen.getTextBounds(tags[index], 0, tags[index].length, tagSize)
//            val fontSize: Float = pen.textSize * box.width() / tagSize.width()
//
//            // adjust the font size so texts are inside the bounding box
//            if (fontSize < pen.textSize) pen.textSize = fontSize
//
//            var margin = (box.width() - tagSize.width()) / 2.0F
//            if (margin < 0F) margin = 0F
//
//            // draw tags onto bitmap (bmp is in upside down format)
//            for ((idx, txt) in tags.withIndex()) {
//                canvas.drawText(
//                    txt, box.left + margin,
//                    box.top + tagSize.height().times(idx + 1.0F), pen
//                )
//            }
//        }
//    }
