package com.example.materialui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.materialui.PinViewModel

@Suppress("UNCHECKED_CAST")
class PinViewModelFactory(private val appContext: Context) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PinViewModel::class.java)) {
            return PinViewModel(appContext) as T
        } else {
            throw RuntimeException("Cannot create an instance of $modelClass")
        }

    }
}