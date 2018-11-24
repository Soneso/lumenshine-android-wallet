package com.soneso.lumenshine.model.entities

import java.util.*

data class UserSecurity(
        val username: String,
        val publicKeyIndex0: String,
        var sep10Challenge: String,
        val passwordKdfSalt: ByteArray,
        val encryptedMnemonicMasterKey: ByteArray,
        val mnemonicMasterKeyEncryptionIv: ByteArray,
        val encryptedMnemonic: ByteArray,
        val mnemonicEncryptionIv: ByteArray,
        val encryptedWordListMasterKey: ByteArray,
        val wordListMasterKeyEncryptionIv: ByteArray,
        val encryptedWordList: ByteArray,
        val wordListEncryptionIv: ByteArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserSecurity

        if (username != other.username) return false
        if (publicKeyIndex0 != other.publicKeyIndex0) return false
        if (sep10Challenge != other.sep10Challenge) return false
        if (!Arrays.equals(passwordKdfSalt, other.passwordKdfSalt)) return false
        if (!Arrays.equals(encryptedMnemonicMasterKey, other.encryptedMnemonicMasterKey)) return false
        if (!Arrays.equals(mnemonicMasterKeyEncryptionIv, other.mnemonicMasterKeyEncryptionIv)) return false
        if (!Arrays.equals(encryptedMnemonic, other.encryptedMnemonic)) return false
        if (!Arrays.equals(mnemonicEncryptionIv, other.mnemonicEncryptionIv)) return false
        if (!Arrays.equals(encryptedWordListMasterKey, other.encryptedWordListMasterKey)) return false
        if (!Arrays.equals(wordListMasterKeyEncryptionIv, other.wordListMasterKeyEncryptionIv)) return false
        if (!Arrays.equals(encryptedWordList, other.encryptedWordList)) return false
        if (!Arrays.equals(wordListEncryptionIv, other.wordListEncryptionIv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + publicKeyIndex0.hashCode()
        result = 31 * result + sep10Challenge.hashCode()
        result = 31 * result + Arrays.hashCode(passwordKdfSalt)
        result = 31 * result + Arrays.hashCode(encryptedMnemonicMasterKey)
        result = 31 * result + Arrays.hashCode(mnemonicMasterKeyEncryptionIv)
        result = 31 * result + Arrays.hashCode(encryptedMnemonic)
        result = 31 * result + Arrays.hashCode(mnemonicEncryptionIv)
        result = 31 * result + Arrays.hashCode(encryptedWordListMasterKey)
        result = 31 * result + Arrays.hashCode(wordListMasterKeyEncryptionIv)
        result = 31 * result + Arrays.hashCode(encryptedWordList)
        result = 31 * result + Arrays.hashCode(wordListEncryptionIv)
        return result
    }
}