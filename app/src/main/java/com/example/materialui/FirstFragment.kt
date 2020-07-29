package com.example.materialui

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.transition.TransitionListenerAdapter
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.transition.TransitionInflater
import kotlinx.android.synthetic.main.fragment_first.*
import kotlinx.coroutines.delay

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val transition = root.getTransition(R.id.transition_current_scene)
        (root as MotionLayout).setTransitionListener(@RequiresApi(Build.VERSION_CODES.O)
        object  : TransitionListenerAdapter(),
            MotionLayout.TransitionListener {
            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
            }

            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
            }

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                scaler(logo)
            }

        })
        lifecycle.coroutineScope.launchWhenResumed {
            delay(500)
            root.transitionToEnd()
            sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        }

        view.findViewById<AppCompatButton>(R.id.btn_login).setOnClickListener {

            val action = FirstFragmentDirections.actionFirstFragmentToSecondFragment()
            findNavController().navigate(
                action,
                FragmentNavigator.Extras.Builder()
                    .addSharedElement(btn_login, getString(R.string.shared_login_btn_transition))
                    .build()
            )
        }
    }

    private fun scaler(view: View) {

        // Scale the view up to 4x its default size and back

        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.2f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2f)
        val animator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY)
        animator.apply {
            repeatCount = 1
            repeatMode = ObjectAnimator.REVERSE
            duration = 500
        }.start()
    }

    fun navigate() {

    }
}