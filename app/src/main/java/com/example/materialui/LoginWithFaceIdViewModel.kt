package com.example.materialui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.materialui.faceCongnito.ImageHelper
import com.microsoft.projectoxford.face.FaceServiceClient
import com.microsoft.projectoxford.face.contract.VerifyResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

class LoginWithFaceIdViewModel(private val context: Context) : ViewModel() {

    private val TAG = "LoginWithFaceIdViewMode"

    private val _faceResult = MutableLiveData<FaceResponseData>()

    private val _progressEnable = MutableLiveData<Boolean>()

    private val _verifyResult = MutableLiveData<VerifyResult?>()

    fun loadBitmap(imageUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            log(TAG,"loadBitmap start -> $imageUri")
            withContext(Dispatchers.Main) {
                _progressEnable.value = true
            }
            val bitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                imageUri, context.contentResolver
            )
            bitmap?.let {
                detectFace(it)
            }

            withContext(Dispatchers.Main) {
                _progressEnable.value = false
            }
            log(TAG,"loadBitmap end")
        }
    }

    private suspend fun detectFace(bitmap: Bitmap) {
        try {
            log(TAG,"detectFace start")
            withContext(Dispatchers.Main) {
                _progressEnable.value = true
            }
            val output = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
            val inputStream =
                ByteArrayInputStream(output.toByteArray())
            val faceServiceClient = MyApp.getFaceServiceClient()
            val detect = faceServiceClient.detect(
                inputStream/* Input stream of image to detect */,
                true/* Whether to return face ID */,
                true/* Whether to return face landmarks */,
                /* Which face attributes to analyze, currently we support:
                   age,gender,headPose,smile,facialHair */
                arrayOf(
                    FaceServiceClient.FaceAttributeType.Age,
                    FaceServiceClient.FaceAttributeType.Gender,
                    FaceServiceClient.FaceAttributeType.Smile,
                    FaceServiceClient.FaceAttributeType.Glasses,
                    FaceServiceClient.FaceAttributeType.FacialHair,
                    FaceServiceClient.FaceAttributeType.Emotion,
                    FaceServiceClient.FaceAttributeType.HeadPose,
                    FaceServiceClient.FaceAttributeType.Accessories,
                    FaceServiceClient.FaceAttributeType.Blur,
                    FaceServiceClient.FaceAttributeType.Exposure,
                    FaceServiceClient.FaceAttributeType.Hair,
                    FaceServiceClient.FaceAttributeType.Makeup,
                    FaceServiceClient.FaceAttributeType.Noise,
                    FaceServiceClient.FaceAttributeType.Occlusion
                )
            )
            withContext(Dispatchers.Main) {
                _faceResult.value = FaceResponseData(detect, true, null)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                _faceResult.value = FaceResponseData(null, false, e)
            }
            e.printStackTrace()
        } finally {
            log(TAG,"detectFace end ...")
            withContext(Dispatchers.Main) {
                _progressEnable.value = false
            }
        }

    }

    fun getFaceId(): UUID? {
        val uuid = context.getSharedPreferences(PLAIN_FILE_NAME_PREFERENCE, Context.MODE_PRIVATE)
            .getString(FACE_ID, null)

        if (uuid != null) {
            return UUID.fromString(uuid)
        }
        return null
    }

    fun getProgressBarStatus(): LiveData<Boolean> = _progressEnable

    fun getLiveFaceDetectData(): LiveData<FaceResponseData> = _faceResult

    fun getLiveVerifyFaceData(): LiveData<VerifyResult?> = _verifyResult

    fun verifyFace(currentFaceId: UUID?, savedFaceId: UUID?) {
        viewModelScope.launch(Dispatchers.IO) {
            log(TAG,"verifyFace start")
            withContext(Dispatchers.Main) {
                _progressEnable.value = true
            }
            try {
                val verify = MyApp.getFaceServiceClient()
                    .verify(currentFaceId, savedFaceId)
                withContext(Dispatchers.Main) {
                    _verifyResult.value = verify
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _verifyResult.value = null
                }
            } finally {
                log(TAG,"verifyFace end")
                withContext(Dispatchers.Main) {
                    _progressEnable.value = false
                }

            }
        }
    }
}