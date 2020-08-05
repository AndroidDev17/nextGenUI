package com.example.materialui

import android.content.Context
import android.graphics.*
import android.media.Image
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import javax.security.auth.callback.Callback
import kotlin.coroutines.resumeWithException

fun log(tag:String,msg:String?) {
    msg?.let {
        Log.d(tag,msg)
    }
}

fun Context.toast(msg:String) {
    Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
}


fun <T1, T2, R> zip(src1: LiveData<T1>, src2: LiveData<T2>,
                     zipper: (T1, T2) -> R): LiveData<R> {

    return MediatorLiveData<R>().apply {
        var src1Version = 0
        var src2Version = 0

        var lastSrc1: T1? = null
        var lastSrc2: T2? = null

        fun updateValueIfNeeded() {
            if (src1Version > 0 && src2Version > 0 &&
                lastSrc1 != null && lastSrc2 != null) {
                value = zipper(lastSrc1!!, lastSrc2!!)
            }
        }

        addSource(src1) {
            lastSrc1 = it
            src1Version++
            updateValueIfNeeded()
        }

        addSource(src2) {
            lastSrc2 = it
            src2Version++
            updateValueIfNeeded()
        }
    }
}

fun Image.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer // Y
    val uBuffer = planes[1].buffer // U
    val vBuffer = planes[2].buffer // V

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    //U and V are swapped
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

fun Int.toDp(context: Context):Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,this.toFloat(),context.resources.displayMetrics
)

//fun callbackToCoroutine(): Result<String> =
//    suspendCancellableCoroutine { continuation ->
//        if (continuation.isActive) {
//            api.addOnCompleteListener { result ->
//                continuation.resume(result)
//            }.addOnFailureListsner { error ->
//                continuation.resumeWithException(error)
//            }
//        }
//    }
//
//fun flowFrom(api:CallbackBasedApi) : Flow<T> = callbackFlow {
//    val responseListener = object : Callback {
//        override fun onNext(value:T) {
//            offer(value)
//        }
//        override fun onError(e:Throwable) {
//            close(e)
//        }
//        override fun onComplete() {
//            close()
//        }
//    }
//    api.register(responseListener)
//    awaitClose { api.unregister(responseListener) }
//
//}