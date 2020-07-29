package com.example.materialui

import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

abstract class DismissibleFragment : Fragment(), CircularRevealFinishListener, BackPressAware {

    private val TAG = "DismissibleFragment"
    override fun onAnimationFinish() {
        findNavController().popBackStack()
    }


    override fun onBackPressed() {
        log(TAG,"onBackPressed")
        getAnimationSetting()?.let {animationSetting ->
            AnimationUtils.startCircularExitAnimation(
                requireContext(),
                requireView(),
                animationSetting,
                ContextCompat.getColor(requireContext(), R.color.colorGray),
                ContextCompat.getColor(requireContext(), R.color.colorGreen),
                this)
        }
    }

    abstract fun getAnimationSetting() : AnimationSetting?
}