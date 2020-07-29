package com.example.materialui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.materialui.faceCongnito.ImageHelper
import com.microsoft.projectoxford.face.FaceServiceClient.FaceAttributeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

class FacePhotoViewModel(private val imageUri: Uri,private val context: Context) :ViewModel() {

    private val _bitmap = MutableLiveData<Bitmap?>()

    private val _faceResult = MutableLiveData<FaceResponseData>()

    private val _progressEnable = MutableLiveData<Boolean>()

    fun loadBitmap() {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                _progressEnable.value = true
            }
            _bitmap.value = ImageHelper.loadSizeLimitedBitmapFromUri(
                imageUri, context.contentResolver
            )
            withContext(Dispatchers.Main) {
                _progressEnable.value = false
            }
        }
    }

    fun getBitmap() : LiveData<Bitmap?> = _bitmap

    fun getProgressBarStatus() : LiveData<Boolean> = _progressEnable

    fun getLiveFaceDetectData():LiveData<FaceResponseData> = _faceResult

    fun detectFace(bitmap: Bitmap) {
        viewModelScope.launch (Dispatchers.IO){
            try {
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
                        FaceAttributeType.Age,
                        FaceAttributeType.Gender,
                        FaceAttributeType.Smile,
                        FaceAttributeType.Glasses,
                        FaceAttributeType.FacialHair,
                        FaceAttributeType.Emotion,
                        FaceAttributeType.HeadPose,
                        FaceAttributeType.Accessories,
                        FaceAttributeType.Blur,
                        FaceAttributeType.Exposure,
                        FaceAttributeType.Hair,
                        FaceAttributeType.Makeup,
                        FaceAttributeType.Noise,
                        FaceAttributeType.Occlusion
                    )
                )
                withContext(Dispatchers.Main) {
                    _faceResult.value = FaceResponseData(detect,true, null)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _faceResult.value = FaceResponseData(null,false,e)
                }
                e.printStackTrace()
            }
            finally {
                withContext(Dispatchers.Main) {
                    _progressEnable.value = false
                }
            }

        }
    }

    fun saveFaceId(faceId: UUID?) {
        context.getSharedPreferences(PLAIN_FILE_NAME_PREFERENCE,Context.MODE_PRIVATE).edit()
            .putString(FACE_ID,faceId.toString())
            .apply()
    }

    fun saveLoginTypeFaceId() {
        val sharedPreferences =
            context.getSharedPreferences(PLAIN_FILE_NAME_PREFERENCE, Context.MODE_PRIVATE).edit()
        sharedPreferences.putInt(AUTHENTICATION_MODE, AUTHENTICATION_MODE_FACE_ID)
        sharedPreferences.apply()
    }

}