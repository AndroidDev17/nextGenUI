package com.example.materialui

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.lang.Exception
import java.nio.ByteBuffer

class FaceAnalyzer(private val faceBoundary: FaceBoundary) : ImageAnalysis.Analyzer,
    OnSuccessListener<MutableList<Face>>,
    OnFailureListener {

    private val TAG = "FaceAnalyzer"
    private val _faceInsideBoundary = MutableLiveData<Boolean>()
    // High-accuracy landmark detection and face classification
    private val highAccuracyOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    // Real-time contour detection
    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .build()


    private val detector = FaceDetection.getClient(realTimeOpts)

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val bitmap = mediaImage.toBitmap()
            imageProxy.close()
            val image = InputImage.fromBitmap(bitmap, rotationDegrees)
            detector.process(image).addOnSuccessListener(this).addOnFailureListener(this)
        }
    }

    override fun onSuccess(faceList: MutableList<Face>?) {
        log(TAG, "onSuccess...${faceList?.size}")
        faceList?.forEach {
            isFaceInside(it.boundingBox)
        }
    }

    private fun isFaceInside(boundingBox: Rect) {
        log(TAG, "isFaceInside..")
        if (boundingBox.left > faceBoundary.left &&
            boundingBox.top > faceBoundary.top &&
            boundingBox.right < faceBoundary.right &&
            boundingBox.bottom < faceBoundary.bottom
        ) {
            log(TAG, "face found..")
            _faceInsideBoundary.postValue(true)
        }
    }

    fun getFaceInBoundaryLiveData() : LiveData<Boolean> = _faceInsideBoundary

    override fun onFailure(e: Exception) {
        log(TAG, "onFailure..")
    }
}