package com.soneso.lumenshine.domain.data

import com.soneso.lumenshine.model.entities.StellarWallet
import com.soneso.lumenshine.model.entities.WalletEntity

class WalletCardData(entity: WalletEntity, stellar: StellarWallet? = null) {

    val id = entity.id
    val name = entity.name
    val federationAddress = entity.federationAddress
    val shownInHomeScreen = entity.shownInHomeScreen
    val publicKey = entity.publicKey
    var balances = stellar?.balances
        private set

    fun setStellarWallet(wallet: StellarWallet) {
        balances = wallet.balances
    }

    fun hasBalances() = balances?.isNotEmpty() == true

    fun isLoaded() = balances != null
}