package com.example.materialui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.materialui.AnimationUtils.registerCircularRevealAnimation
import com.example.materialui.viewmodels.FingerprintViewModelFactory


class FingerprintFragment : DismissibleFragment() {
    private val TAG = "FingerprintFragment"

    private val args: FingerprintFragmentArgs by navArgs()

    private lateinit var factory: FingerprintViewModelFactory

    private val viewModel: FingerprintViewModel by viewModels {
        factory
    }

    private var biometricPrompt: BiometricPrompt? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        factory = FingerprintViewModelFactory(
            BiometricManager.from(requireContext()),
            requireContext().applicationContext
        )
        return inflater.inflate(R.layout.fingerprint_fragment, container, false)
    }

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
                biometricPrompt = viewModel.showBiometricPromptForEncryption(this@FingerprintFragment)
            }
        }
        registerCircularRevealAnimation(
            requireContext(),
            listener,
            requireView(),
            args.ANIMATIONSETTING,
            ContextCompat.getColor(requireContext(), R.color.colorGreen),
            ContextCompat.getColor(requireContext(), R.color.off_white)
        )

        observerLiveData()
    }

    private fun observerLiveData() {
        viewModel.biometricSuccessLiveData().observe(viewLifecycleOwner, Observer {success ->
            if (success) {
                biometricPrompt = null
                viewModel.saveNavigationType()
                popToLoginWithId()
            }
        })
    }

    private fun popToLoginWithId() {
        log(TAG, "Move to Login Screen")
        findNavController().navigate(
            FingerprintFragmentDirections.actionFingerprintFragmentToLoginWithFingerprintFragment(
                AUTHENTICATION_MODE_FINGERPRINT)
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        biometricPrompt?.cancelAuthentication()
    }

    override fun getAnimationSetting(): AnimationSetting? = args.ANIMATIONSETTING

}