package com.soneso.lumenshine.model.entities

data class UserSecurity(
        val publicKeyIndex0: String,
        val passwordKdfSalt: ByteArray,
        val encryptedMnemonicMasterKey: ByteArray,
        val mnemonicMasterKeyEncryptionIv: ByteArray,
        val encryptedMnemonic: ByteArray,
        val mnemonicEncryptionIv: ByteArray,
        val encryptedWordListMasterKey: ByteArray,
        val wordListMasterKeyEncryptionIv: ByteArray,
        val encryptedWordList: ByteArray,
        val wordListEncryptionIv: ByteArray,
        var sep10Challenge: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserSecurity

        if (publicKeyIndex0 != other.publicKeyIndex0) return false
        if (sep10Challenge != other.sep10Challenge) return false
        if (!passwordKdfSalt.contentEquals(other.passwordKdfSalt)) return false
        if (!encryptedMnemonicMasterKey.contentEquals(other.encryptedMnemonicMasterKey)) return false
        if (!mnemonicMasterKeyEncryptionIv.contentEquals(other.mnemonicMasterKeyEncryptionIv)) return false
        if (!encryptedMnemonic.contentEquals(other.encryptedMnemonic)) return false
        if (!mnemonicEncryptionIv.contentEquals(other.mnemonicEncryptionIv)) return false
        if (!encryptedWordListMasterKey.contentEquals(other.encryptedWordListMasterKey)) return false
        if (!wordListMasterKeyEncryptionIv.contentEquals(other.wordListMasterKeyEncryptionIv)) return false
        if (!encryptedWordList.contentEquals(other.encryptedWordList)) return false
        if (!wordListEncryptionIv.contentEquals(other.wordListEncryptionIv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKeyIndex0.hashCode()
        result = 31 * result + sep10Challenge.hashCode()
        result = 31 * result + passwordKdfSalt.contentHashCode()
        result = 31 * result + encryptedMnemonicMasterKey.contentHashCode()
        result = 31 * result + mnemonicMasterKeyEncryptionIv.contentHashCode()
        result = 31 * result + encryptedMnemonic.contentHashCode()
        result = 31 * result + mnemonicEncryptionIv.contentHashCode()
        result = 31 * result + encryptedWordListMasterKey.contentHashCode()
        result = 31 * result + wordListMasterKeyEncryptionIv.contentHashCode()
        result = 31 * result + encryptedWordList.contentHashCode()
        result = 31 * result + wordListEncryptionIv.contentHashCode()
        return result
    }

}