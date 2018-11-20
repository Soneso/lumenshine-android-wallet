package com.soneso.lumenshine.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.soneso.lumenshine.persistence.room.DbNames
import java.util.*

@Entity(tableName = DbNames.TABLE_USER_DATA)
data class UserSecurity(

        @PrimaryKey
        @ColumnInfo(name = DbNames.COLUMN_USERNAME)
        val username: String,

        @ColumnInfo(name = DbNames.COLUMN_PK_0)
        val publicKeyIndex0: String,

        @ColumnInfo(name = DbNames.COLUMN_SEP_10_CHALLENGE)
        var sep10Challenge: String,

        @ColumnInfo(name = DbNames.COLUMN_PASS_KDF_SALT)
        val passwordKdfSalt: ByteArray,

        @ColumnInfo(name = DbNames.COLUMN_ENCRYPTED_MNEMONIC_MASTER_KEY)
        val encryptedMnemonicMasterKey: ByteArray,

        @ColumnInfo(name = DbNames.COLUMN_MMK_ENCRYPTION_IV)
        val mnemonicMasterKeyEncryptionIv: ByteArray,

        @ColumnInfo(name = DbNames.COLUMN_ENCRYPTED_MNEMONIC)
        val encryptedMnemonic: ByteArray,

        @ColumnInfo(name = DbNames.COLUMN_MNEMONIC_ENCRYPTION_IV)
        val mnemonicEncryptionIv: ByteArray,

        @ColumnInfo(name = DbNames.COLUMN_ENCRYPTED_WORD_LIST_MASTER_KEY)
        val encryptedWordListMasterKey: ByteArray,

        @ColumnInfo(name = DbNames.COLUMN_WLMK_ENCRYPTION_IV)
        val wordListMasterKeyEncryptionIv: ByteArray,

        @ColumnInfo(name = DbNames.COLUMN_ENCRYPTED_WORD_LIST)
        val encryptedWordList: ByteArray,

        @ColumnInfo(name = DbNames.COLUMN_WORD_LIST_ENCRYPTION_IV)
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