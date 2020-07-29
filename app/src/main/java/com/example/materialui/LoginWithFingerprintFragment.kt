package com.example.materialui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.materialui.viewmodels.LoginWithFingerprintViewModelFactory
import kotlinx.android.synthetic.main.login_with_id_fragment.*

class LoginWithFingerprintFragment : Fragment() {

    private lateinit var factory : LoginWithFingerprintViewModelFactory

    private val viewModel: LoginWithFingerprintViewModel by viewModels {
        factory
    }

    private var biometricPrompt: BiometricPrompt? =null

    private val args :LoginWithFingerprintFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        factory = LoginWithFingerprintViewModelFactory(requireContext().applicationContext)
        return inflater.inflate(R.layout.login_with_id_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenResumed {
            biometricPrompt = viewModel.showBiometricPromptForDecryption(this@LoginWithFingerprintFragment)
        }
        observeLiveData()
    }

    private fun observeLiveData() {
        viewModel.biometricSuccessLiveData().observe(viewLifecycleOwner, Observer { success->
            if (success) {
                biometricPrompt = null
                welcome_label.text = "Welcome back user"
        }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        biometricPrompt?.cancelAuthentication()
    }
}