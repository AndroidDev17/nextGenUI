package com.example.materialui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainViewModel(private val appContext: Context) : ViewModel(){
    fun getAuthenticationMode(): Int {
        val sharedPreferences =
            appContext.getSharedPreferences(PLAIN_FILE_NAME_PREFERENCE, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(AUTHENTICATION_MODE,AUTHENTICATION_MODE_NOT_SET)
    }


}