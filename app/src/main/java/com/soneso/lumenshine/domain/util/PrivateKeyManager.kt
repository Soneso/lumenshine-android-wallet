package com.soneso.lumenshine.domain.util

import com.soneso.stellarmnemonics.Wallet
import com.soneso.stellarmnemonics.derivation.Ed25519Derivation
import com.soneso.stellarmnemonics.mnemonic.Mnemonic
import org.stellar.sdk.KeyPair

object PrivateKeyManager {

    fun getKeyPair(accountId: String, mnemonic: String? = null): KeyPair? {
        mnemonic?.let { mnem ->
            val index = getIndex(mnem, accountId)
            index?.let { ind ->
                return Wallet.createKeyPair(mnem.toCharArray(), null, ind)
            }
        }
        return null
    }

    private fun getIndex(mnemonic: String, accountId: String): Int? {
        val pairs = getWalletKeyParis(mnemonic)
        return pairs[accountId]
    }

    private fun getWalletKeyParis(mnemonic: String?): Map<String, Int> {
        val walletIndexesMap = mutableMapOf<String, Int>()
        mnemonic?.let {
            val bip39Seed = Mnemonic.createSeed(it.toCharArray(), null)
            val masterPrivateKey = Ed25519Derivation.fromSecretSeed(bip39Seed)
            val purpose = masterPrivateKey.derived(44)
            val coinType = purpose.derived(148)
            for (index in 0 until 256) {
                val account = coinType.derived(index)
                val keyPair = KeyPair.fromSecretSeed(account.privateKey)
                walletIndexesMap[keyPair.accountId] = index
            }
        }
        return walletIndexesMap
    }
}
