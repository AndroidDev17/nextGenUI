package com.example.materialui

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.materialui.biometric.*

class FingerprintViewModel(
    private val biometricManager: BiometricManager,
    private val appContext: Context
) : ViewModel() {

    private val TAG = "FingerprintViewModel"

    private val _biometricSuccess = MutableLiveData<Boolean>()

    fun biometricSuccessLiveData(): LiveData<Boolean> = _biometricSuccess

    fun showBiometricPromptForEncryption(fragment: Fragment): BiometricPrompt? {
        val canAuthenticate = biometricManager.canAuthenticate()
        log(TAG, "showBiometricPromptForEncryption  $canAuthenticate")
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            log(TAG, "showBiometricPrompt  BIOMETRIC_SUCCESS")
            val cryptographyManager = CryptographyManagerImpl.instance
            val cipher = cryptographyManager.getInitializedCipherForEncryption(SECRET_KEY_NAME)
            val biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(fragment, ::encryptAndStoreServerToken)
            val promptInfo = BiometricPromptUtils.createPromptInfo(
                appContext.getString(R.string.register_fp),
                appContext.getString(R.string.register_subtitle),
                appContext.getString(R.string.register_description),
                true,
                appContext.getString(R.string.register_cancel)
            )
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            return biometricPrompt
        }
        return null
    }

    private fun encryptAndStoreServerToken(authResult: BiometricPrompt.AuthenticationResult) {
        authResult.cryptoObject?.cipher?.apply {
            FAKE_SERVER_TOKEN.let { token ->
                log(TAG, "The token from server is $token")
                val cryptographyManager = CryptographyManagerImpl.instance
                val encryptedServerTokenWrapper = cryptographyManager.encryptData(token, this)
                cryptographyManager.persistCipherTextWrapperToSharedPrefs(
                    encryptedServerTokenWrapper,
                    appContext,
                    SHARED_PREFS_FILENAME,
                    Context.MODE_PRIVATE,
                    CIPHER_TEXT_WRAPPER
                )
            }
        }
        _biometricSuccess.postValue(true)
    }

    fun saveNavigationType() {
        val sharedPreferences =
            appContext.getSharedPreferences(PLAIN_FILE_NAME_PREFERENCE, Context.MODE_PRIVATE).edit()
        sharedPreferences.putInt(AUTHENTICATION_MODE, AUTHENTICATION_MODE_FINGERPRINT)
        sharedPreferences.apply()
    }

}