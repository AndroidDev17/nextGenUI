package com.example.materialui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.materialui.LoginWithFaceIdViewModel

@Suppress("UNCHECKED_CAST")
class LoginWithFaceIdViewModelFactory(private val context: Context) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginWithFaceIdViewModel::class.java)) {
            return LoginWithFaceIdViewModel(context) as T
        } else {
            throw RuntimeException("Cannot create an instance of $modelClass")
        }

    }
}