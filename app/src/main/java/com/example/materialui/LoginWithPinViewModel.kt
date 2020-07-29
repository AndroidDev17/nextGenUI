package com.example.materialui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginWithPinViewModel(private val context: Context) : ViewModel() {

    private val _pinValidatorData = MutableLiveData<Boolean>()

    private val savedPin by lazy {
        val sharedPreferences =
            context.getSharedPreferences(PLAIN_FILE_NAME_PREFERENCE, Context.MODE_PRIVATE)
             sharedPreferences.getString(PIN,null)
    }

    fun validatePin(pin:String) {
        _pinValidatorData.value = savedPin == pin
    }

    fun getValidationStatus():LiveData<Boolean> = _pinValidatorData

}