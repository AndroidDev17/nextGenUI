package com.example.materialui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.materialui.FacePhotoViewModel

@Suppress("UNCHECKED_CAST")
class FacePhotoViewModelFactory(private val imageUri: Uri, private val context: Context) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FacePhotoViewModel::class.java)) {
            return FacePhotoViewModel(imageUri, context) as T
        } else {
            throw RuntimeException("Cannot create an instance of $modelClass")
        }

    }
}