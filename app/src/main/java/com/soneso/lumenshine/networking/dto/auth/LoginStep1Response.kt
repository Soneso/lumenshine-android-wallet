package com.soneso.lumenshine.networking.dto.auth

import com.fasterxml.jackson.annotation.JsonProperty
import org.bouncycastle.util.encoders.Base64

@Suppress("MemberVisibilityCanBePrivate")
class LoginStep1Response {

    @JsonProperty("kdf_password_salt")
    var passwordKdfSalt = ""

    @JsonProperty("encrypted_mnemonic_master_key")
    var encryptedMnemonicMasterKey = ""

    @JsonProperty("mnemonic_master_key_encryption_iv")
    var mnemonicMasterKeyEncryptionIv = ""

    @JsonProperty("encrypted_mnemonic")
    var encryptedMnemonic = ""

    @JsonProperty("mnemonic_encryption_iv")
    var mnemonicEncryptionIv = ""

    @JsonProperty("encrypted_wordlist_master_key")
    var encryptedWordListMasterKey = ""

    @JsonProperty("wordlist_master_key_encryption_iv")
    var wordListMasterKeyEncryptionIv = ""

    @JsonProperty("encrypted_wordlist")
    var encryptedWordList = ""

    @JsonProperty("wordlist_encryption_iv")
    var wordListEncryptionIv = ""

    @JsonProperty("public_key_index0")
    var publicKeyIndex0 = ""

    @JsonProperty("sep10_transaction_challenge")
    var sep10TransactionChallenge = ""

    fun passwordKdfSalt(): ByteArray = Base64.decode(passwordKdfSalt)

    fun encryptedMnemonicMasterKey(): ByteArray = Base64.decode(encryptedMnemonicMasterKey)

    fun mnemonicMasterKeyEncryptionIv(): ByteArray = Base64.decode(mnemonicMasterKeyEncryptionIv)

    fun encryptedMnemonic(): ByteArray = Base64.decode(encryptedMnemonic)

    fun mnemonicEncryptionIv(): ByteArray = Base64.decode(mnemonicEncryptionIv)

    fun encryptedWordListMasterKey(): ByteArray = Base64.decode(encryptedWordListMasterKey)

    fun wordListMasterKeyEncryptionIv(): ByteArray = Base64.decode(wordListMasterKeyEncryptionIv)

    fun encryptedWordList(): ByteArray = Base64.decode(encryptedWordList)

    fun wordListEncryptionIv(): ByteArray = Base64.decode(wordListEncryptionIv)
}