package com.example.materialui.faceCongnito

import android.content.ContentResolver
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import com.microsoft.projectoxford.face.contract.Face
import com.microsoft.projectoxford.face.contract.FaceRectangle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Defined several functions to load, draw, save, resize, and rotate images.
 */
object ImageHelper {
    // The maximum side length of the image to detect, to keep the size of image less than 4MB.
    // Resize the image if its side length is larger than the maximum.
    private const val IMAGE_MAX_SIDE_LENGTH = 1280

    // Ratio to scale a detected face rectangle, the face rectangle scaled up looks more natural.
    private const val FACE_RECT_SCALE_RATIO = 1.3

    // Decode image from imageUri, and resize according to the expectedMaxImageSideLength
    // If expectedMaxImageSideLength is
    //     (1) less than or equal to 0,
    //     (2) more than the actual max size length of the bitmap
    //     then return the original bitmap
    // Else, return the scaled bitmap
    suspend fun loadSizeLimitedBitmapFromUri(
        imageUri: Uri,
        contentResolver: ContentResolver
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // Load the image into InputStream.
            var imageInputStream = contentResolver.openInputStream(imageUri)

            // For saving memory, only decode the image meta and get the side length.
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            val outPadding = Rect()
            BitmapFactory.decodeStream(imageInputStream, outPadding, options)

            // Calculate shrink rate when loading the image into memory.
            var maxSideLength =
                if (options.outWidth > options.outHeight) options.outWidth else options.outHeight
            options.inSampleSize = 1
            options.inSampleSize =
                calculateSampleSize(maxSideLength, IMAGE_MAX_SIDE_LENGTH)
            options.inJustDecodeBounds = false
            imageInputStream?.close()

            // Load the bitmap and resize it to the expected size length
            imageInputStream = contentResolver.openInputStream(imageUri)
            var bitmap = BitmapFactory.decodeStream(imageInputStream, outPadding, options)
            maxSideLength =
                if (bitmap!!.width > bitmap.height) bitmap.width else bitmap.height
            val ratio =
                IMAGE_MAX_SIDE_LENGTH / maxSideLength.toDouble()
            if (ratio < 1) {
                bitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * ratio).toInt(),
                    (bitmap.height * ratio).toInt(),
                    false
                )
            }
            rotateBitmap(
                bitmap,
                getImageRotationAngle(imageUri, contentResolver)
            )
        } catch (e: Exception) {
            null
        }
    }


    // Draw detected face rectangles in the original image. And return the image drawn.
    // If drawLandmarks is set to be true, draw the five main landmarks of each face.
    fun drawFaceRectanglesOnBitmap(
        originalBitmap: Bitmap,
        faces: Array<Face>?,
        drawLandmarks: Boolean
    ): Bitmap {
        val bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.color = Color.GREEN
        var stokeWidth =
            Math.max(originalBitmap.width, originalBitmap.height) / 100
        if (stokeWidth == 0) {
            stokeWidth = 1
        }
        paint.strokeWidth = stokeWidth.toFloat()
        if (faces != null) {
            for (face in faces) {
                val faceRectangle = calculateFaceRectangle(
                    bitmap,
                    face.faceRectangle,
                    FACE_RECT_SCALE_RATIO
                )
                canvas.drawRect(
                    faceRectangle.left.toFloat(),
                    faceRectangle.top.toFloat(),
                    faceRectangle.left + faceRectangle.width.toFloat(),
                    faceRectangle.top + faceRectangle.height.toFloat(),
                    paint
                )
                if (drawLandmarks) {
                    var radius = face.faceRectangle.width / 30
                    if (radius == 0) {
                        radius = 1
                    }
                    paint.style = Paint.Style.FILL
                    paint.strokeWidth = radius.toFloat()
                    canvas.drawCircle(
                        face.faceLandmarks.pupilLeft.x.toFloat(),
                        face.faceLandmarks.pupilLeft.y.toFloat(),
                        radius.toFloat(),
                        paint
                    )
                    canvas.drawCircle(
                        face.faceLandmarks.pupilRight.x.toFloat(),
                        face.faceLandmarks.pupilRight.y.toFloat(),
                        radius.toFloat(),
                        paint
                    )
                    canvas.drawCircle(
                        face.faceLandmarks.noseTip.x.toFloat(),
                        face.faceLandmarks.noseTip.y.toFloat(),
                        radius.toFloat(),
                        paint
                    )
                    canvas.drawCircle(
                        face.faceLandmarks.mouthLeft.x.toFloat(),
                        face.faceLandmarks.mouthLeft.y.toFloat(),
                        radius.toFloat(),
                        paint
                    )
                    canvas.drawCircle(
                        face.faceLandmarks.mouthRight.x.toFloat(),
                        face.faceLandmarks.mouthRight.y.toFloat(),
                        radius.toFloat(),
                        paint
                    )
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = stokeWidth.toFloat()
                }
            }
        }
        return bitmap
    }

    // Highlight the selected face thumbnail in face list.
    fun highlightSelectedFaceThumbnail(originalBitmap: Bitmap): Bitmap {
        val bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#3399FF")
        var stokeWidth =
            Math.max(originalBitmap.width, originalBitmap.height) / 10
        if (stokeWidth == 0) {
            stokeWidth = 1
        }
        bitmap.width
        paint.strokeWidth = stokeWidth.toFloat()
        canvas.drawRect(
            0f, 0f,
            bitmap.width.toFloat(),
            bitmap.height.toFloat(),
            paint
        )
        return bitmap
    }

    // Crop the face thumbnail out from the original image.
    // For better view for human, face rectangles are resized to the rate faceRectEnlargeRatio.
    @Throws(IOException::class)
    fun generateFaceThumbnail(
        originalBitmap: Bitmap,
        faceRectangle: FaceRectangle
    ): Bitmap {
        val faceRect = calculateFaceRectangle(
            originalBitmap,
            faceRectangle,
            FACE_RECT_SCALE_RATIO
        )
        return Bitmap.createBitmap(
            originalBitmap, faceRect.left, faceRect.top, faceRect.width, faceRect.height
        )
    }

    // Return the number of times for the image to shrink when loading it into memory.
    // The SampleSize can only be a final value based on powers of 2.
    private fun calculateSampleSize(maxSideLength: Int, expectedMaxImageSideLength: Int): Int {
        var maxSideLength = maxSideLength
        var inSampleSize = 1
        while (maxSideLength > 2 * expectedMaxImageSideLength) {
            maxSideLength /= 2
            inSampleSize *= 2
        }
        return inSampleSize
    }

    // Get the rotation angle of the image taken.
    @Throws(IOException::class)
    private fun getImageRotationAngle(
        imageUri: Uri, contentResolver: ContentResolver
    ): Int {
        var angle = 0
        val cursor = contentResolver.query(
            imageUri,
            arrayOf(MediaStore.Images.ImageColumns.ORIENTATION),
            null,
            null,
            null
        )
        if (cursor != null) {
            if (cursor.count == 1) {
                cursor.moveToFirst()
                angle = cursor.getInt(0)
            }
            cursor.close()
        } else {
            val exif = ExifInterface(imageUri.path!!)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> angle = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> angle = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> angle = 90
                else -> {
                }
            }
        }
        return angle
    }

    // Rotate the original bitmap according to the given orientation angle
    private fun rotateBitmap(bitmap: Bitmap?, angle: Int): Bitmap? {
        // If the rotate angle is 0, then return the original image, else return the rotated image
        return if (angle != 0) {
            val matrix = Matrix()
            matrix.postRotate(angle.toFloat())
            Bitmap.createBitmap(
                bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true
            )
        } else {
            bitmap
        }
    }

    // Resize face rectangle, for better view for human
    // To make the rectangle larger, faceRectEnlargeRatio should be larger than 1, recommend 1.3
    private fun calculateFaceRectangle(
        bitmap: Bitmap, faceRectangle: FaceRectangle, faceRectEnlargeRatio: Double
    ): FaceRectangle {
        // Get the resized side length of the face rectangle
        var sideLength = faceRectangle.width * faceRectEnlargeRatio
        sideLength = Math.min(sideLength, bitmap.width.toDouble())
        sideLength = Math.min(sideLength, bitmap.height.toDouble())

        // Make the left edge to left more.
        var left = (faceRectangle.left
                - faceRectangle.width * (faceRectEnlargeRatio - 1.0) * 0.5)
        left = Math.max(left, 0.0)
        left = Math.min(left, bitmap.width - sideLength)

        // Make the top edge to top more.
        var top = (faceRectangle.top
                - faceRectangle.height * (faceRectEnlargeRatio - 1.0) * 0.5)
        top = Math.max(top, 0.0)
        top = Math.min(top, bitmap.height - sideLength)

        // Shift the top edge to top more, for better view for human
        var shiftTop = faceRectEnlargeRatio - 1.0
        shiftTop = Math.max(shiftTop, 0.0)
        shiftTop = Math.min(shiftTop, 1.0)
        top -= 0.15 * shiftTop * faceRectangle.height
        top = Math.max(top, 0.0)

        // Set the result.
        val result = FaceRectangle()
        result.left = left.toInt()
        result.top = top.toInt()
        result.width = sideLength.toInt()
        result.height = sideLength.toInt()
        return result
    }
}