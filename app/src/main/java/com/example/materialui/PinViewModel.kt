package com.example.materialui

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PinViewModel(private val appContext:Context) : ViewModel() {

    private var _pin = MutableLiveData<String>()
    private var _confirmPin = MutableLiveData<String>()
    val result = zip(_pin, _confirmPin) { pin, confirmPin -> checkValidPin(pin, confirmPin) }

    fun setPin(pin: String) {
        if (pin.isNotBlank()) {
            _pin.postValue(pin)
        }
    }

    fun setConfirmPin(pin: String) {
        if (pin.isNotBlank()) {
            _confirmPin.postValue(pin)
        }
    }

    private fun checkValidPin(pin: String?, confirmPin: String?): Boolean {
        if (pin?.length == 4
            && confirmPin?.length == 4
            && pin == confirmPin
        ) {
            return true
        }
        return false
    }

    fun savePin(context: Context) {
        context.getSharedPreferences(PLAIN_FILE_NAME_PREFERENCE, Context.MODE_PRIVATE).edit()
            .putString(PIN,_pin.value).apply()
    }

    fun saveNavigationType() {
        val sharedPreferences =
            appContext.getSharedPreferences(PLAIN_FILE_NAME_PREFERENCE, Context.MODE_PRIVATE).edit()
        sharedPreferences.putInt(AUTHENTICATION_MODE, AUTHENTICATION_MODE_PIN)
        sharedPreferences.apply()
    }
}