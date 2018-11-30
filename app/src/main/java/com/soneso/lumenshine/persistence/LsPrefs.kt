package com.soneso.lumenshine.persistence

import android.content.Context
import android.content.SharedPreferences
import com.soneso.lumenshine.LsApp
import com.soneso.lumenshine.domain.util.Cryptor
import com.soneso.lumenshine.domain.util.toByteArray
import org.bouncycastle.util.encoders.Base64
import timber.log.Timber
import java.util.*

/**
 * Shared Prefs.
 * Created by cristi.paval on 3/12/18.
 */
object LsPrefs {

    const val TAG = "LsPrefs"
    private const val PREF_NAME = "secured-app-prefs"
    private const val KEY_APP_PASS = "app-pass"
    private const val KEY_ENCRYPTION_IV = "encryption-iv"
    private const val KEY_PASS_SALT = "pass-salt"
    private const val KEY_USERNAME = "username"
    const val KEY_JWT_TOKEN = "api-token"
    private const val KEY_TFA_SECRET = "tfa-secret"
    private const val KEY_REGISTRATION_COMPLETE = "registration-complete"
    private const val KEY_FINGERPRINT_ENABLED = "fingerprint_enabled"

    private val listeners = mutableListOf<((String) -> Unit)>()
    private val prefs: SharedPreferences
    private val encryptionIv: ByteArray
    private val derivedPass: ByteArray

    val appPass: String

    init {
        val context = LsApp.sAppContext
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val keyHolder = AppKeyHolder(context, PREF_NAME)
        appPass = initializeAppPass(keyHolder)
        val salt = initializePassSalt(keyHolder)
        derivedPass = Cryptor.deriveKeyPbkdf2(salt, appPass.toCharArray())
        encryptionIv = initializeEncryptionIv(keyHolder)
    }

    private fun initializeAppPass(keyHolder: AppKeyHolder): String {

        return if (!prefs.contains(KEY_APP_PASS)) {

            val uuid = UUID.randomUUID().toString()
            Timber.d("Generated appPass: $uuid")
            val encryptedUuid = keyHolder.encryptPass(uuid)
            saveString(KEY_APP_PASS, encryptedUuid)
            uuid
        } else {

            val encryptedPass = getString(KEY_APP_PASS)
            keyHolder.decryptPass(encryptedPass)
        }
    }

    private fun initializeEncryptionIv(keyHolder: AppKeyHolder): ByteArray {

        return if (!prefs.contains(KEY_ENCRYPTION_IV)) {

            val iv = Cryptor.generateIv()
            Timber.d("Generated encryption iv: ${Base64.toBase64String(iv)}")
            val encryptedIv = keyHolder.encryptKey(iv)
            saveString(KEY_ENCRYPTION_IV, Base64.toBase64String(encryptedIv))
            iv

        } else {

            val encryptedIv = Base64.decode(getString(KEY_ENCRYPTION_IV))
            keyHolder.decryptKey(encryptedIv)
        }
    }

    private fun initializePassSalt(keyHolder: AppKeyHolder): ByteArray {

        return if (!prefs.contains(KEY_PASS_SALT)) {

            val salt = Cryptor.generateSalt()
            Timber.d("Generated salt: ${Base64.toBase64String(salt)}")
            val encryptedIv = keyHolder.encryptKey(salt)
            saveString(KEY_PASS_SALT, Base64.toBase64String(encryptedIv))
            salt

        } else {

            val encryptedSalt = Base64.decode(getString(KEY_PASS_SALT))
            keyHolder.decryptKey(encryptedSalt)
        }
    }

    var username: String
        get() = decryptAndGetString(KEY_USERNAME)
        set(value) {
            encryptAndSaveString(KEY_USERNAME, value)
        }

    var jwtToken: String
        get() = decryptAndGetString(KEY_JWT_TOKEN)
        set(value) = encryptAndSaveString(KEY_JWT_TOKEN, value)

    var tfaSecret: String
        get() = decryptAndGetString(KEY_TFA_SECRET)
        set(value) {
            encryptAndSaveString(KEY_TFA_SECRET, value)
        }

    var registrationCompleted: Boolean
        get() {
            val value = decryptAndGetString(KEY_REGISTRATION_COMPLETE)
            return if (value.isBlank()) false else value.toBoolean()
        }
        set(value) {
            encryptAndSaveString(KEY_REGISTRATION_COMPLETE, value.toString())
        }

    var isFingeprintEnabled: Boolean
        get() = prefs.getBoolean(KEY_FINGERPRINT_ENABLED, false)
        set(value) = prefs.edit()
                .putBoolean(KEY_FINGERPRINT_ENABLED, value)
                .apply()

    private fun encryptAndSaveString(key: String, value: String) {

        if (value.isBlank()) {
            saveString(key, value)
        } else {
            val bytes = Cryptor.applyPadding(value.toByteArray())
            val encryptedValue = Cryptor.encryptValue(bytes, derivedPass, encryptionIv)
            saveString(key, Base64.toBase64String(encryptedValue))
        }
    }

    private fun saveString(key: String, value: String) {

        prefs.edit()
                .putString(key, value)
                .apply()

        for (listener in listeners) {
            listener.invoke(key)
        }
    }

    private fun getString(key: String): String = prefs.getString(key, "") ?: ""

    private fun decryptAndGetString(key: String): String {

        val encryption = getString(key)
        if (encryption.isBlank()) {
            return encryption
        }
        val encryptedValue = Base64.decode(encryption)
        val valueBytes = Cryptor.decryptValue(encryptedValue, derivedPass, encryptionIv)
        return String(Cryptor.removePadding(valueBytes))
    }

    fun registerListener(listener: ((String) -> Unit)) {
        listeners.add(listener)
    }
}