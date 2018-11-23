package com.soneso.lumenshine.domain.usecases

import com.soneso.lumenshine.BuildConfig
import com.soneso.lumenshine.domain.util.*
import com.soneso.lumenshine.model.DecryptedUserData
import com.soneso.lumenshine.model.entities.UserSecurity
import com.soneso.stellarmnemonics.Wallet
import com.soneso.stellarmnemonics.mnemonic.WordList
import org.bouncycastle.util.encoders.Base64
import org.stellar.sdk.KeyPair
import timber.log.Timber
import java.nio.ByteBuffer
import java.util.*

class UserSecurityHelper(private val pass: CharArray) {

    lateinit var mnemonicChars: CharArray
        private set

    fun generateUserSecurity(email: String): UserSecurity {

        val passwordKdfSalt = Cryptor.generateSalt()
        val derivedPassword = Cryptor.deriveKeyPbkdf2(passwordKdfSalt, pass)

        val wordListMasterKey = Cryptor.generateMasterKey()
        val wordListMasterKeyEncryptionIv = Cryptor.generateIv()
        val encryptedWordListMasterKey = Cryptor.encryptValue(wordListMasterKey, derivedPassword, wordListMasterKeyEncryptionIv)

        val mnemonicMasterKey = Cryptor.generateMasterKey()
        val mnemonicMasterKeyEncryptionIv = Cryptor.generateIv()
        val encryptedMnemonicMasterKey = Cryptor.encryptValue(mnemonicMasterKey, derivedPassword, mnemonicMasterKeyEncryptionIv)

        val wordList = mutableListOf<String>(*WordList.ENGLISH.words.toTypedArray()).shuffled()

        mnemonicChars = Wallet.generate24WordMnemonic()
        val mnemonicWords = String(mnemonicChars).split(" ")

        val mnemonicIndexes = ShortArray(mnemonicWords.size) {
            wordList.indexOf(mnemonicWords[it]).toShort()
        }
        val mnemonicByteList = mnemonicIndexes.flatMap { index: Short ->
            val bytes = ByteBuffer.allocate(2)
            bytes.putShort(index)
            bytes.array().asList()
        }
        val mnemonicBytes = ByteArray(2 * mnemonicIndexes.size) {
            mnemonicByteList[it]
        }
        val mnemonicEncryptionIv = Cryptor.generateIv()
        val encryptedMnemonic = Cryptor.encryptValue(mnemonicBytes, mnemonicMasterKey, mnemonicEncryptionIv)

        val publicKeyIndex0 = Wallet.createKeyPair(mnemonicChars, null, 0).accountId
        val publicKeyIndex188 = Wallet.createKeyPair(mnemonicChars, null, 188).accountId

        val wordListStringBuilder = StringBuilder()
        wordList.forEach {
            wordListStringBuilder.append(it).append(",")
        }
        val wordListBytes = wordListStringBuilder.dropLast(1).padToBlocks(16).toByteArray()

        val wordListEncryptionIv = Cryptor.generateIv()
        val encryptedWordList = Cryptor.encryptValue(wordListBytes, wordListMasterKey, wordListEncryptionIv)

        if (BuildConfig.DEBUG) {
            Timber.d("password kdf salt: ${Base64.toBase64String(passwordKdfSalt)}")
            Timber.d("derived password: ${Base64.toBase64String(derivedPassword)}")

            Timber.d("word list master key: ${Base64.toBase64String(wordListMasterKey)}")
            Timber.d("word list master key encryption iv: ${Base64.toBase64String(wordListMasterKeyEncryptionIv)}")
            Timber.d("encrypted word list master key: ${Base64.toBase64String(encryptedWordListMasterKey)}")

            Timber.d("mnemonic master key: ${Base64.toBase64String(mnemonicMasterKey)}")
            Timber.d("mnemonic master key encryption iv: ${Base64.toBase64String(mnemonicMasterKey)}")
            Timber.d("encrypted mnmemonic master key: ${Base64.toBase64String(encryptedMnemonicMasterKey)}")

            Timber.d("mnemonic: ${String(mnemonicChars)}")
            Timber.d("mnemonic indexes: ${Arrays.toString(mnemonicIndexes)}")

            Timber.d("mnemonic encryption iv: ${Base64.toBase64String(mnemonicEncryptionIv)}")
            Timber.d("encrypted mnemonic: ${Base64.toBase64String(encryptedMnemonic)}")

            Timber.d("public key index 0: $publicKeyIndex0")
            Timber.d("public key index 188: $publicKeyIndex188")

            logLongString("word list: ${wordListStringBuilder.dropLast(1)}")
            Timber.d("word list encryption iv: ${Base64.toBase64String(wordListEncryptionIv)}")
            logLongString("encrypted word list: ${Base64.toBase64String(encryptedWordList)}")
        }

        return UserSecurity(
                email,
                publicKeyIndex0,
                publicKeyIndex188,
                passwordKdfSalt,
                encryptedMnemonicMasterKey,
                mnemonicMasterKeyEncryptionIv,
                encryptedMnemonic,
                mnemonicEncryptionIv,
                encryptedWordListMasterKey,
                wordListMasterKeyEncryptionIv,
                encryptedWordList,
                wordListEncryptionIv
        )
    }


    fun decipherUserSecurityNew(userSecurity: UserSecurity): DecryptedUserData? {
        try {

            val derivedPassword = Cryptor.deriveKeyPbkdf2(userSecurity.passwordKdfSalt, pass)

            val wordListMasterKey = Cryptor.decryptValue(userSecurity.encryptedWordListMasterKey, derivedPassword, userSecurity.wordListMasterKeyEncryptionIv)

            val wordListCsvBytes = Cryptor.decryptValue(userSecurity.encryptedWordList, wordListMasterKey, userSecurity.wordListEncryptionIv)
            val wordList = String(wordListCsvBytes).trim().split(",")

            val mnemonicMasterKey = Cryptor.decryptValue(userSecurity.encryptedMnemonicMasterKey, derivedPassword, userSecurity.mnemonicMasterKeyEncryptionIv)

            val mnemonicBytes = Cryptor.decryptValue(userSecurity.encryptedMnemonic, mnemonicMasterKey, userSecurity.mnemonicEncryptionIv)

            if (mnemonicBytes.size != 48) {
                return null
            }

            val mnemonicIndexes = ShortArray(mnemonicBytes.size / 2) { index ->
                val bf = ByteBuffer.wrap(mnemonicBytes, index * 2, 2)
                bf.short
            }
            val mnemonicBuilder = StringBuilder()
            mnemonicIndexes.forEach { index ->
                mnemonicBuilder.append(wordList[index.toInt()]).append(" ")
            }
            mnemonicChars = mnemonicBuilder.removeSuffix(" ").toCharArray()

            val publicKeyIndex0 = Wallet.createKeyPair(mnemonicChars, null, 0).accountId

            if (publicKeyIndex0 != userSecurity.publicKeyIndex0) {
                return null
            }

            return DecryptedUserData(mnemonicChars, wordListMasterKey, mnemonicMasterKey)

        } catch (t: Throwable) {

            return null
        }
    }

    fun decipherUserSecurity_(userSecurity: UserSecurity): KeyPair? {

        try {
            val derivedPassword = Cryptor.deriveKeyPbkdf2(userSecurity.passwordKdfSalt, pass)

            val wordListMasterKey = Cryptor.decryptValue(userSecurity.encryptedWordListMasterKey, derivedPassword, userSecurity.wordListMasterKeyEncryptionIv)

            val wordListCsvBytes = Cryptor.decryptValue(userSecurity.encryptedWordList, wordListMasterKey, userSecurity.wordListEncryptionIv)
            val wordList = String(wordListCsvBytes).trim().split(",")

            val mnemonicMasterKey = Cryptor.decryptValue(userSecurity.encryptedMnemonicMasterKey, derivedPassword, userSecurity.mnemonicMasterKeyEncryptionIv)

            val mnemonicBytes = Cryptor.decryptValue(userSecurity.encryptedMnemonic, mnemonicMasterKey, userSecurity.mnemonicEncryptionIv)

            if (mnemonicBytes.size != 48) {
                return null
            }

            val mnemonicIndexes = ShortArray(mnemonicBytes.size / 2) { index ->
                val bf = ByteBuffer.wrap(mnemonicBytes, index * 2, 2)
                bf.short
            }
            val mnemonicBuilder = StringBuilder()
            mnemonicIndexes.forEach { index ->
                mnemonicBuilder.append(wordList[index.toInt()]).append(" ")
            }
            mnemonicChars = mnemonicBuilder.removeSuffix(" ").toCharArray()

            val publicKeyIndex0 = Wallet.createKeyPair(mnemonicChars, null, 0).accountId

            if (publicKeyIndex0 != userSecurity.publicKeyIndex0) {
                return null
            }

            return Wallet.createKeyPair(mnemonicChars, null, 0)

        } catch (t: Throwable) {

            return null
        }
    }

    fun decipherUserSecurity(userSecurity: UserSecurity): String? {

        try {
            val derivedPassword = Cryptor.deriveKeyPbkdf2(userSecurity.passwordKdfSalt, pass)

            val wordListMasterKey = Cryptor.decryptValue(userSecurity.encryptedWordListMasterKey, derivedPassword, userSecurity.wordListMasterKeyEncryptionIv)

            val wordListCsvBytes = Cryptor.decryptValue(userSecurity.encryptedWordList, wordListMasterKey, userSecurity.wordListEncryptionIv)
            val wordList = String(wordListCsvBytes).trim().split(",")

            val mnemonicMasterKey = Cryptor.decryptValue(userSecurity.encryptedMnemonicMasterKey, derivedPassword, userSecurity.mnemonicMasterKeyEncryptionIv)

            val mnemonicBytes = Cryptor.decryptValue(userSecurity.encryptedMnemonic, mnemonicMasterKey, userSecurity.mnemonicEncryptionIv)

            if (mnemonicBytes.size != 48) {
                return null
            }

            val mnemonicIndexes = ShortArray(mnemonicBytes.size / 2) { index ->
                val bf = ByteBuffer.wrap(mnemonicBytes, index * 2, 2)
                bf.short
            }
            val mnemonicBuilder = StringBuilder()
            mnemonicIndexes.forEach { index ->
                mnemonicBuilder.append(wordList[index.toInt()]).append(" ")
            }
            mnemonicChars = mnemonicBuilder.removeSuffix(" ").toCharArray()

            val publicKeyIndex0 = Wallet.createKeyPair(mnemonicChars, null, 0).accountId

            if (publicKeyIndex0 != userSecurity.publicKeyIndex0) {
                return null
            }

            return Wallet.createKeyPair(mnemonicChars, null, 188).accountId

        } catch (t: Throwable) {

            return null
        }
    }

    fun changePassword(userSecurity: UserSecurity, newPassword: CharArray): UserSecurity {

        val derivedPassword = Cryptor.deriveKeyPbkdf2(userSecurity.passwordKdfSalt, pass)
        val wordListMasterKey = Cryptor.decryptValue(userSecurity.encryptedWordListMasterKey, derivedPassword, userSecurity.wordListMasterKeyEncryptionIv)
        val mnemonicMasterKey = Cryptor.decryptValue(userSecurity.encryptedMnemonicMasterKey, derivedPassword, userSecurity.mnemonicMasterKeyEncryptionIv)

        val newKdfSalt = Cryptor.generateSalt()
        val derivedNewPassword = Cryptor.deriveKeyPbkdf2(newKdfSalt, newPassword)
        val wordListMasterKeyEncryptionIv = Cryptor.generateIv()
        val encryptedWordListMasterKey = Cryptor.encryptValue(wordListMasterKey, derivedNewPassword, wordListMasterKeyEncryptionIv)
        val mnemonicMasterKeyEncryptionIv = Cryptor.generateIv()
        val encryptedMnemonicMasterKey = Cryptor.encryptValue(mnemonicMasterKey, derivedNewPassword, mnemonicMasterKeyEncryptionIv)

        return UserSecurity(
                userSecurity.username,
                userSecurity.publicKeyIndex0,
                decipherUserSecurity(userSecurity) ?: "",
                newKdfSalt,
                encryptedMnemonicMasterKey,
                mnemonicMasterKeyEncryptionIv,
                userSecurity.encryptedMnemonic,
                userSecurity.mnemonicEncryptionIv,
                encryptedWordListMasterKey,
                wordListMasterKeyEncryptionIv,
                userSecurity.encryptedWordList,
                userSecurity.wordListEncryptionIv
        )
    }

    companion object {

        const val TAG = "UserSecurityHelper"
    }
}