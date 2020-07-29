package com.example.materialui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.materialui.viewmodels.PinViewModelFactory
import kotlinx.android.synthetic.main.pin_fragment.*


class PinFragment : DismissibleFragment (){

    private val TAG = "PinFragment"

    private val args: PinFragmentArgs by navArgs()

    private val factory : PinViewModelFactory by lazy {
        PinViewModelFactory(requireContext().applicationContext)
    }

    private val viewModel: PinViewModel by viewModels {
        factory
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.pin_fragment, container, false)
    }

    override fun getAnimationSetting(): AnimationSetting? = args.ANIMATIONSETTING

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listener = object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                log(TAG,"onAnimationStart")
            }

            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                super.onAnimationEnd(animation, isReverse)
                log(TAG,"onAnimationEnd")
                createAnimatorFor(edt_pin)
                createAnimatorFor(edt_pin_confirm)
            }
        }
        AnimationUtils.registerCircularRevealAnimation(
            requireContext(),
            listener,
            requireView(),
            args.ANIMATIONSETTING,
            ContextCompat.getColor(requireContext(), R.color.colorGreen),
            ContextCompat.getColor(requireContext(), R.color.off_white)
        )

        setupTextWatcher()
        setObserver()
    }

    private fun setObserver() {
        viewModel.result.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.savePin(requireContext())
                viewModel.saveNavigationType()
                navigateToLogin()
            }
//            edt_pin.backgroundTintList =
//                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))
        })
    }

    private fun navigateToLogin() {
        findNavController().navigate(
            PinFragmentDirections.actionPinFragmentToLoginWithPinFragment(
                AUTHENTICATION_MODE_PIN
            )
        )
    }

    private fun setupTextWatcher() {
        edt_pin.addTextChangedListener {text->
            viewModel.setPin(text.toString())
        }
        edt_pin_confirm.addTextChangedListener { text->
            viewModel.setConfirmPin(text.toString())
        }
    }

    private fun createAnimatorFor(view:View): AnimatorSet {

        val animationSet = AnimatorSet()
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", .5f, 1f)
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", .5f, 1f)
        animationSet.playTogether(scaleX, scaleY)
        animationSet.start()
        return animationSet
    }

}