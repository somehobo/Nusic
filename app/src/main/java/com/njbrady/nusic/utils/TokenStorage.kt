package com.njbrady.nusic.utils

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Singleton

@Singleton
class TokenStorage(private val context: Context) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
    }
    private val _containsToken = MutableStateFlow(false)
    val containsToken: StateFlow<Boolean> = _containsToken

    init {
        _containsToken.value = checkLoggedIn()
    }


    fun storeToken(token: String) {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        val secretKey = keyGenerator.generateKey()

        val cipher = Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encryptedToken = cipher.doFinal(token.toByteArray())

        val editor = prefs.edit()
        editor.putString(PREF_TOKEN, Base64.encodeToString(encryptedToken, Base64.DEFAULT))
        editor.putString(PREF_IV, Base64.encodeToString(iv, Base64.DEFAULT))
        editor.apply()
        _containsToken.value = true
    }

    fun retrieveToken(): String {
        val encryptedTokenString = prefs.getString(PREF_TOKEN, null)
        val ivString = prefs.getString(PREF_IV, null)
        if (encryptedTokenString == null || ivString == null) {
            return ""
        }
        val encryptedToken = Base64.decode(encryptedTokenString, Base64.DEFAULT)
        val iv = Base64.decode(ivString, Base64.DEFAULT)
        val cipher = Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}")
        val spec = GCMParameterSpec(128, iv)
        val keystore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keystore.load(null)
        val secretKey = keystore.getKey(KEY_ALIAS, null) as SecretKey
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val decryptedToken = cipher.doFinal(encryptedToken)
        return String(decryptedToken)
    }

    fun deleteToken() {
        val keystore = KeyStore.getInstance(ANDROID_KEY_STORE)
        val editor = prefs.edit()

        keystore.load(null)
        keystore.deleteEntry(KEY_ALIAS)

        editor.remove(PREF_TOKEN)
        editor.remove(PREF_IV)
        editor.apply()
        _containsToken.value = false
    }

    private fun checkLoggedIn(): Boolean {
        val keystore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keystore.load(null)
        return keystore.containsAlias(KEY_ALIAS)
    }

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "authentication_token_alias"
        private const val PREFS_FILENAME = "token_prefs"
        private const val PREF_TOKEN = "token"
        private const val PREF_IV = "iv"
        private const val TOKEN_PREFACE = "TOKEN "
    }
}
