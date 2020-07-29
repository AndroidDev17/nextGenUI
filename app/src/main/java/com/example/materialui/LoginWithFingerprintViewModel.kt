package com.example.materialui

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.materialui.biometric.*

class LoginWithFingerprintViewModel(
    private val appContext: Context
) : ViewModel() {
    private val TAG = "LoginWithIdViewModel"

    private val _biometricSuccess = MutableLiveData<Boolean>()

    fun biometricSuccessLiveData() : LiveData<Boolean> = _biometricSuccess

    private val cipherTextWrapper
        get() = CryptographyManagerImpl.instance.getCipherTextWrapperFromSharedPrefs(
            appContext,
            SHARED_PREFS_FILENAME,
            Context.MODE_PRIVATE,
            CIPHER_TEXT_WRAPPER
        )

    fun showBiometricPromptForDecryption(fragment: Fragment): BiometricPrompt? {
        cipherTextWrapper?.let { textWrapper ->
            val cipher = CryptographyManagerImpl.instance.getInitializedCipherForDecryption(
                SECRET_KEY_NAME, textWrapper.initializationVector
            )
            val biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(
                    fragment,
                    ::decryptServerTokenFromStorage
                )
            val promptInfo = BiometricPromptUtils.createPromptInfo(
                appContext.getString(R.string.use_fp),
                appContext.getString(R.string.use_fp_subtitle),
                appContext.getString(R.string.use_fp_description),
                true,
                appContext.getString(R.string.use_fp_cancel)
            )
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            return biometricPrompt
        }
        return null
    }

    private fun decryptServerTokenFromStorage(authResult: BiometricPrompt.AuthenticationResult) {
        cipherTextWrapper?.let { textWrapper ->
            authResult.cryptoObject?.cipher?.let {
                val plaintext =
                    CryptographyManagerImpl.instance.decryptData(textWrapper.ciphertext, it)
                User.token = plaintext
                // Now that you have the token, you can query server for everything else
                // the only reason we call this fakeToken is because we didn't really get it from
                // the server. In your case, you will have gotten it from the server the first time
                // and therefore, it's a real token.
            }
        }
        _biometricSuccess.postValue(true)
    }
}