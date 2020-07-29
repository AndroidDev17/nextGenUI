package com.example.materialui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.materialui.LoginWithFingerprintViewModel

class LoginWithFingerprintViewModelFactory(private val appContext: Context) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginWithFingerprintViewModel::class.java)) {
            return LoginWithFingerprintViewModel( appContext) as T
        } else {
            throw RuntimeException("Cannot create an instance of $modelClass")
        }

    }
}
