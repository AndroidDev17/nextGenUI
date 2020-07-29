package com.example.materialui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.view.ViewAnimationUtils
import androidx.interpolator.view.animation.FastOutSlowInInterpolator


object AnimationUtils {
    private const val DURATION = 2000L
    fun registerCircularRevealAnimation(
        context: Context,
        listener: AnimatorListenerAdapter,
        view: View,
        revealSettings: AnimationSetting,
        startColor: Int,
        endColor: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                override fun onLayoutChange(
                    v: View,
                    left: Int,
                    top: Int,
                    right: Int,
                    bottom: Int,
                    oldLeft: Int,
                    oldTop: Int,
                    oldRight: Int,
                    oldBottom: Int
                ) {
                    v.removeOnLayoutChangeListener(this)
                    val cx = revealSettings.centerX
                    val cy = revealSettings.centerY
                    val width = revealSettings.width
                    val height = revealSettings.height

                    //Simply use the diagonal of the view
                    val finalRadius =
                        Math.sqrt(width * width + height * height.toDouble()).toFloat()
                    val anim =
                        ViewAnimationUtils.createCircularReveal(v, cx, cy, 0f, finalRadius)
                            .setDuration(DURATION)
                    anim.interpolator = FastOutSlowInInterpolator()
                    anim.start()
                    startColorAnimation(view, startColor, endColor, DURATION.toInt(),listener)
                }
            })
        }
    }

    fun startColorAnimation(
        view: View,
        startColor: Int,
        endColor: Int,
        duration: Int,
        listener: AnimatorListenerAdapter?
    ) {
        val anim = ValueAnimator()
        anim.setIntValues(startColor, endColor)
        anim.setEvaluator(ArgbEvaluator())
        anim.addUpdateListener { valueAnimator -> view.setBackgroundColor(valueAnimator.animatedValue as Int) }
        anim.duration = duration.toLong()
        anim.start()
        listener?.let {
            anim.addListener(listener)
        }
    }


    fun startCircularExitAnimation(context: Context, view: View, animationSetting: AnimationSetting, startColor: Int, endColor: Int,listener:CircularRevealFinishListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val cx = animationSetting.centerX
            val cy = animationSetting.centerY
            val width = animationSetting.width
            val height = animationSetting.height

            val squareSum = width * width + height * height
            val initRadius = Math.sqrt(squareSum.toDouble()).toFloat()

            val anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initRadius, 0f)
            anim.apply {
                duration = DURATION
                interpolator = FastOutSlowInInterpolator()
                addListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        listener.onAnimationFinish()
                    }
                })
            }.start()
            startColorAnimation(view, startColor, endColor, DURATION.toInt(),null)
        } else {
            listener.onAnimationFinish()
        }
    }

}

/**
 * save animation setting as Parcelable
 */
data class AnimationSetting(
    val centerX: Int,
    val centerY: Int,
    val width: Int,
    val height: Int
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(centerX)
        parcel.writeInt(centerY)
        parcel.writeInt(width)
        parcel.writeInt(height)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AnimationSetting> {
        override fun createFromParcel(parcel: Parcel): AnimationSetting {
            return AnimationSetting(parcel)
        }

        override fun newArray(size: Int): Array<AnimationSetting?> {
            return arrayOfNulls(size)
        }
    }
}

