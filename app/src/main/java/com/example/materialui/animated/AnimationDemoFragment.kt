package com.example.materialui.animated

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import androidx.core.graphics.ColorUtils
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.example.materialui.R
import kotlinx.android.synthetic.main.fragment_animation_demo.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AnimationDemoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AnimationDemoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val topLeftAnimationForward : AnimatedVectorDrawableCompat? by lazy {
        AnimatedVectorDrawableCompat.create(requireActivity(),
            R.drawable.top_left_liquid_forward
        )
    }

    val topLeftAnimationReverse : AnimatedVectorDrawableCompat? by lazy {
        AnimatedVectorDrawableCompat.create(requireActivity(),
            R.drawable.top_left_liquid_reverse
        )
    }

    val bottomRightAnimationForward : AnimatedVectorDrawableCompat? by lazy {
        AnimatedVectorDrawableCompat.create(requireActivity(),
            R.drawable.bottom_right_liquid_forward
        )
    }

    val bottomRightAnimationReverse : AnimatedVectorDrawableCompat? by lazy {
        AnimatedVectorDrawableCompat.create(requireActivity(),
            R.drawable.bottom_right_liquid_reverse
        )
    }

    private lateinit var topLeftImageView : ImageView
    private lateinit var bottomRightImageView : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.theme?.applyStyle(R.style.MaterialTheme,true)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_animation_demo, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AnimationDemoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AnimationDemoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topLeftImageView = view.findViewById(R.id.TopLeft)
        topLeftImageView.apply {
            setImageDrawable(topLeftAnimationForward)
        }
        bottomRightImageView = view.findViewById(R.id.BottomRight)
        bottomRightImageView.apply {
            setImageDrawable(bottomRightAnimationForward)
        }
        setAnimationListener()
    }

    private fun setAnimationListener() {
        root.setTransitionListener(object : TransitionAdapter() {

            override fun onTransitionChange(
                motionLayout: MotionLayout,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
                val color =
                    ColorUtils.setAlphaComponent(
                        Color.WHITE,
                        calculateProgressAlpha(progress)
                    )
                bottomRightAnimationForward?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
                bottomRightAnimationReverse?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            }

        })
        topLeftAnimationForward?.registerAnimationCallback(object :
            Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                topLeftImageView.setImageDrawable(topLeftAnimationReverse)
                topLeftAnimationReverse?.start()
            }
        })
        topLeftAnimationReverse?.registerAnimationCallback(object :
            Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                topLeftImageView.setImageDrawable(topLeftAnimationForward)
                topLeftAnimationForward?.start()
            }
        })

        bottomRightAnimationForward?.registerAnimationCallback(object :
            Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                bottomRightImageView.setImageDrawable(bottomRightAnimationReverse)
                bottomRightAnimationReverse?.start()
            }
        })
        bottomRightAnimationReverse?.registerAnimationCallback(object :
            Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                bottomRightImageView.setImageDrawable(bottomRightAnimationForward)
                bottomRightAnimationForward?.start()
            }
        })

        topLeftAnimationForward?.start()
        bottomRightAnimationForward?.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        root.setTransitionListener(null)
    }

    fun calculateProgressAlpha(progress: Float) = ((progress * 100) * 2.55).toInt()
}