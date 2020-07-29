package com.example.materialui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.materialui.LoginWithPinViewModel

@Suppress("UNCHECKED_CAST")
class LoginWithPinViewModelFactory(private val appContext: Context) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginWithPinViewModel::class.java)) {
            return LoginWithPinViewModel(appContext) as T
        } else {
            throw RuntimeException("Cannot create an instance of $modelClass")
        }

    }
}