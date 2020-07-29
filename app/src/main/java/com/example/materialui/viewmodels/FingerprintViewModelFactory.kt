package com.example.materialui.viewmodels

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.materialui.FingerprintViewModel

@Suppress("UNCHECKED_CAST")
class FingerprintViewModelFactory (private val biometricManager: BiometricManager, private val appContext: Context) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FingerprintViewModel::class.java)) {
            return FingerprintViewModel(biometricManager,appContext) as T
        } else {
            throw RuntimeException("Cannot create an instance of $modelClass")
        }

    }
}