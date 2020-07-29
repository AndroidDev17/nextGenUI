package com.example.materialui

import android.animation.*
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_second.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {
    private var density: Float = 0f
    private var fabSize : Int = 0
    private lateinit var btnFingerprint : FloatingActionButton
    private lateinit var btnPin : FloatingActionButton
    private lateinit var btnFaceID : FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        density = resources.displayMetrics.density
        fabSize = 56 * density.toInt()
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        val guideAnimator = createAnimatorFor(card)
        guideAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                root_container.doOnLayout {
                    setUpOptions(actions as ViewGroup)
                }
            }
        })
        guideAnimator.start()

    }

    private fun createAnimatorFor(view:View): ObjectAnimator {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.2f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2f)
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            view, scaleX, scaleY)
        animator.repeatCount = 1
        animator.repeatMode = ObjectAnimator.REVERSE
        return animator
    }

    private fun setUpOptions(container: ViewGroup) {
        val containerWidth = container.width
        val containerHeight = container.height
        btnPin = getFab(R.drawable.ic_baseline_fiber_pin_24)
        btnFingerprint = getFab(R.drawable.ic_baseline_fingerprint_24)
        btnFaceID = getFab(R.drawable.ic_baseline_face_24)

        container.addView(btnPin)
        container.addView(btnFingerprint)
        container.addView(btnFaceID)

        btnPin.translationY = containerHeight - (fabSize+ 16*density)
        btnFingerprint.translationY = containerHeight - (fabSize+ 16*density)
        btnFaceID.translationY = containerHeight - (fabSize+ 16*density)

        val animationSet2 = btnFingerprint.createAnimationSet(
            fabSize.toFloat(),
            (containerWidth/2 - fabSize.toFloat())+container.paddingLeft,
            500
        )
        animationSet2.start()
        val animationSet3 = btnFaceID.createAnimationSet(
            (containerWidth/2 - fabSize.toFloat())+container.paddingLeft,
            containerWidth - (fabSize.toFloat()+container.paddingRight+container.paddingLeft),
            500
        )
        animationSet2.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                animationSet3.start()
            }
        })
        btnFaceID.setOnClickListener {
            navigateToFaceId(it)
        }

        btnPin.setOnClickListener {
            navigateToPin(it)
        }

        btnFingerprint.setOnClickListener {
            navigateToFingerprint(it)
        }
    }

    private fun navigateToFaceId(view: View) {
        findNavController().navigate(
            SecondFragmentDirections.actionSecondFragmentToFaceIdFragment(
                createAnimationSetting(view)
            )
        )
    }

    private fun navigateToPin(view: View) {
        findNavController().navigate(
            SecondFragmentDirections.actionSecondFragmentToPinFragment(
                createAnimationSetting(view)
            )
        )
    }

    private fun getFab(img:Int): FloatingActionButton =
        FloatingActionButton(this.requireContext()).apply {
            setImageResource(img)
            backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            layoutParams = FrameLayout.LayoutParams(
                fabSize,
                fabSize
            )
        }

    private fun createAnimationSetting(view:View): AnimationSetting {
        return AnimationSetting(
            (view.x + view.width / 2).toInt(),
            (view.y + view.height).toInt() + card.height,
            root_container.width,
            root_container.height
        )
    }

    private fun navigateToFingerprint(view:View) {

        findNavController().navigate(
            SecondFragmentDirections.actionSecondFragmentToFingerprintFragment(
                createAnimationSetting(view)
            )
        )
    }
}

private fun FloatingActionButton.createAnimationSet(
    start: Float,
    translateX: Float,
    duration: Long
): AnimatorSet {
    val mover = ObjectAnimator.ofFloat(
        this, View.TRANSLATION_X,
        start, translateX
    )
    mover.interpolator = AccelerateInterpolator(1f)
    val rotator = ObjectAnimator.ofFloat(
        this, View.ROTATION,
        -360f, 0f
    )
    ObjectAnimator.ofFloat(this,View.ALPHA,.5f,1f)
    rotator.interpolator = LinearInterpolator()
    val set = AnimatorSet()
    set.playTogether(mover, rotator)
    set.duration = (duration)
    return set
}

